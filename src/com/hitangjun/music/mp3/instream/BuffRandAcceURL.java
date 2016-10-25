/*
* BuffRandAcceURL.java -- 用环形缓冲方式读取HTTP协议远程文件
*/
package com.hitangjun.music.mp3.instream;

import com.hitangjun.music.mp3.tag.ID3Tag;

import java.net.URL;
import java.net.URLDecoder;

/*
 * FIFO方式共享环形缓冲区buf[]
 * byteBuf[]逻辑上分成16块, 每一块的长度UNIT_LENGTH=32K不小于最大帧长1732
 * 
 * 同步: 写/写 -- 互斥等待空闲块
 *       读/写 -- 并发访问buf[]
 * 
 * 简单有效解决死锁?
 */
public final class BuffRandAcceURL implements IRandomAccess, IWriterCallBack {
	public static final int UNIT_LENGTH_BITS = 15;
	public static final int UNIT_LENGTH = 1 << UNIT_LENGTH_BITS; //2^16=32K
	public static final int BUF_LENGTH = UNIT_LENGTH << 4;
	public static final int UNIT_COUNT = BUF_LENGTH >> UNIT_LENGTH_BITS; //16块
	public static final int BUF_LENGTH_MASK = (BUF_LENGTH - 1);
	private static final int MAX_WRITER = 5;
	private static long longFilePointer;
	private static int longReadCursor;
	private static int longReaded;
	private static byte[] byteBuf;		//同时作写线程同步锁
	private static int[] intUnitSize;	//同时作读线程互斥锁
	private static int intAllocPos;
	private static URL objURL;
	private static boolean boolAlive = true;
	private static int intWriterCounter;
	private static long longFileSize;
	private static long longAllFrameSize;
	private static int intFreeUnits = UNIT_COUNT;	// "信号量"计数器

	public BuffRandAcceURL(String sURL) throws Exception {
		this(sURL,MAX_WRITER);
	}

	public BuffRandAcceURL(String sURL, int download_threads) throws Exception {
		byteBuf = new byte[BUF_LENGTH];
		intUnitSize = new int[UNIT_COUNT];
		objURL = new URL(sURL);

		// 打印文件名
		try {
			String s = URLDecoder.decode(sURL, "GBK");
			d("解析>> " + s.substring(s.lastIndexOf("/") + 1));
		} catch (Exception e) {
			d(sURL);
		}

		// 获取文件长度
		// 为何同一URL(如http://mat1.qq.com/news/wmv/tang/03.mp3)文件长度有时对有时不对?
		longAllFrameSize = longFileSize = objURL.openConnection().getContentLength();
		if (longFileSize == -1)
			throw new Exception("ContentLength=-1");

		// 以异步方式解析tag
		new TagThread(objURL, longFileSize);

		// 创建"写"线程
		// 线程被创建后立即连接URL开始下载,由于服务器限制了同一IP每秒最大连接次数,频繁连接
		// 会被服务器拒绝,因此延时.
		intWriterCounter = download_threads;
		for (int i = 0; i < download_threads; i++) {
			new Writer(this, objURL, byteBuf, i + 1);
			Thread.sleep(200);
		}

		// 缓冲
		tryCaching();

		// 跳过 ID3 v2
		ID3Tag tag = new ID3Tag();
		int v2_size = tag.checkID3V2(byteBuf, 0);
		if (v2_size > 0) {
			longAllFrameSize -= v2_size;
			seek(v2_size);
		}
		tag = null;
	}

	private void tryCaching() throws InterruptedException {
		int cache_size = BUF_LENGTH;
		int bi = intUnitSize[longReadCursor >> UNIT_LENGTH_BITS];
		if(bi != 0)
			cache_size -= UNIT_LENGTH - bi;
		while (longReaded < cache_size) {
			if (intWriterCounter == 0 || boolAlive == false)
				return;
//			System.out.printf("\r[缓冲%1$6.2f%%] ",(float)longReaded / cache_size * 100);
			synchronized (intUnitSize) {
				intUnitSize.wait(200);	//wait(200)错过通知也可结束循环?
			}
		}
//		System.out.printf("\r");
	}

	private int tryReading(int i, int len) throws Exception {
		int n = (i + 1) & (UNIT_COUNT - 1);
		int r = (intUnitSize[i] > 0) ? (intUnitSize[i] + intUnitSize[n]) : intUnitSize[i];
		if (r < len) {
			if (intWriterCounter == 0 || boolAlive == false)
				return r;
			tryCaching();
		}
		
		return len;
	}

	/*
	 * 各个"写"线程互斥等待空闲块
	 * 空闲块按序号由小到大顺序分配;管理空闲块采用类似于C++的信号量机制.
	 */
	public int tryWriting() throws InterruptedException {
		int ret = -1;
		synchronized (byteBuf) {
			while (intFreeUnits == 0 && boolAlive)
				byteBuf.wait();
			
			if(intAllocPos >= longFileSize || boolAlive == false)
				return -1;
			ret = intAllocPos;
			intAllocPos += UNIT_LENGTH;
			intFreeUnits--;
		}
		return ret;
	}

	/*
	 * "写"线程向buf[]写完数据后调用,通知"读"线程
	 */
	public void updateBuffer(int i, int len) {
		synchronized (intUnitSize) {
			intUnitSize[i] = len;
			longReaded += len;
			intUnitSize.notify();
		}
	}

	/*
	 * "写"线程准备退出时调用
	 */
	public void updateWriterCount() {
		synchronized (intUnitSize) {
			intWriterCounter--;
			intUnitSize.notify();
		}
	}

	public int getWriterCount() {
		return intWriterCounter;
	}

	/*
	 * read方法内调用
	 */
	public void notifyWriter() {
		synchronized (byteBuf) {
			byteBuf.notifyAll();
		}
	}

	/*
	 * 被某个"写"线程调用,用于终止其它"写"线程;isalive也影响"读"线程流程
	 */
	public void terminateWriters() {
		synchronized (intUnitSize) {	
			if (boolAlive) {
				boolAlive = false;
				d("\n读取文件超时。重试 " + HttpReader.MAX_RETRY
						+ " 次后放弃，请您稍后再试。");
			}
			intUnitSize.notify();
		}
		notifyWriter();
	}

	public int read() throws Exception {
		int iret = -1;
		int i = longReadCursor >> UNIT_LENGTH_BITS;

		if (intUnitSize[i] == 0) {
			if(intWriterCounter == 0)
				return -1;
			tryCaching();
		}
		if(boolAlive == false)
			return -1;

		iret = byteBuf[longReadCursor] & 0xff;
		longReaded--;
		longFilePointer++;
		longReadCursor++;
		longReadCursor &= BUF_LENGTH_MASK;
		if (--intUnitSize[i] == 0) {
			intFreeUnits++;
			notifyWriter();
		}

		return iret;
	}

	public int read(byte b[]) throws Exception {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws Exception {
		int i = longReadCursor >> UNIT_LENGTH_BITS;

		// 1.等待有足够内容可读
		if(tryReading(i, len) < len || boolAlive == false)
			return -1;

		// 2.读取
		int tail = BUF_LENGTH - longReadCursor; // write_pos != BUF_LENGTH
		if (tail < len) {
			System.arraycopy(byteBuf, longReadCursor, b, off, tail);
			System.arraycopy(byteBuf, 0, b, off + tail, len - tail);
		} else
			System.arraycopy(byteBuf, longReadCursor, b, off, len);

		longReaded -= len;
		longFilePointer += len;
		longReadCursor += len;
		longReadCursor &= BUF_LENGTH_MASK;
		intUnitSize[i] -= len;
		if (intUnitSize[i] < 0) {
			int ni = longReadCursor >> UNIT_LENGTH_BITS;
			intUnitSize[ni] += intUnitSize[i];
			intUnitSize[i] = 0;
			intFreeUnits++;
			notifyWriter();
		} else if (intUnitSize[i] == 0) {
			intFreeUnits++;		// 空闲块信号量计数加1
			notifyWriter(); 	// 3.通知
		}
		// 如果邻接的下一块未填满表示文件读完,第1步已处理一次读空两块的情况.
		
		return len;
	}

	/*
	 * 从buf[]偏移src_off位置复制.不移动文件"指针",不发信号.
	 */
	public int dump(int src_off, byte b[], int dst_off, int len) throws Exception {
		int rpos = longReadCursor + src_off;
		if(tryReading(rpos >> UNIT_LENGTH_BITS, len) < len || boolAlive == false)
			return -1;
		int tail = BUF_LENGTH - rpos;
		if (tail < len) {
			System.arraycopy(byteBuf, rpos, b, dst_off, tail);
			System.arraycopy(byteBuf, 0, b, dst_off + tail, len - tail);
		} else
			System.arraycopy(byteBuf, rpos, b, dst_off, len);

		return len;
	}

	public long length() {
		return longFileSize;
	}

	public long getFilePointer() {
		return longFilePointer;
	}

	public void close() {
		boolAlive = false;
		notifyWriter();
	}

	/*
	 * 随机读取定位
	 */
	public void seek(long pos) throws Exception {
		longReaded -= pos;
		longFilePointer = pos;
		longReadCursor = (int)pos;
		longReadCursor &= BUF_LENGTH_MASK;
		int units = longReadCursor >> UNIT_LENGTH_BITS;
		for (int i = 0; i < units; i++) {
			intUnitSize[i] = 0;
			notifyWriter();
		}
		intUnitSize[units] -= pos;
	}

	private static void d(Object o){
		System.out.println(o);
	}
}