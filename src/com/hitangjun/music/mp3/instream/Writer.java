/**
* Writer.java -- 用于创建“下载”线程
*/
package com.hitangjun.music.mp3.instream;
import java.net.URL;

public final class Writer implements Runnable {
	private static boolean boolIsAlive = true;	// 一个线程超时其它线程也能退出
	private static byte[] byteBuf;
	private static IWriterCallBack objIWCB;
	private HttpReader objHR;
	
	public Writer(IWriterCallBack cb, URL u, byte[] b, int i) {
		objHR = new HttpReader(u);
		objIWCB = cb;
		byteBuf = b;
		Thread t = new Thread(this,"Writer_dt_"+i);
		t.setPriority(Thread.NORM_PRIORITY + 1);
		t.start();
	}

	public void run() {
		int wpos = 0, rema = 0, retry = 0;
		int idxmask = BuffRandAcceURL.UNIT_COUNT - 1;
		boolean cont = true;
		int index = 0;		// byteBuf[]内"块"索引号
		int startpos = 0;	// index对应的文件位置(相对于文件首的偏移量)
		long time0 = 0;
		while (cont) {
			try {
				// 1.等待空闲块
				if (retry == 0) {
					if ((startpos = objIWCB.tryWriting()) == -1)
						break;
					index = (startpos >> BuffRandAcceURL.UNIT_LENGTH_BITS) & idxmask;
					wpos = startpos & BuffRandAcceURL.BUF_LENGTH_MASK;
					rema = BuffRandAcceURL.UNIT_LENGTH;
					time0 = System.currentTimeMillis();
				}

				// 2.定位
				objHR.seek(startpos);

				// 3.下载"一块"
				int w;
				while (rema > 0 && boolIsAlive) {
					w = (rema < 2048) ? rema : 2048; // 每次读几K合适?
					if ((w = objHR.read(byteBuf, wpos, w)) == -1) {
						cont = false;
						break;
					}
					rema -= w;
					wpos += w;
					startpos += w; // 能断点续传
				}

				// 下载速度足够快就结束本线程
				long t = System.currentTimeMillis() - time0;
				if (objIWCB.getWriterCount() > 1 && t < 500)
					cont = false;

				// 4.通知"读"线程
				objIWCB.updateBuffer(index, BuffRandAcceURL.UNIT_LENGTH - rema);
				retry = 0;
				//读取结束就停止线程
				cont = false;
			} catch (Exception e) {
//				e.printStackTrace();
				if (++retry == HttpReader.MAX_RETRY) {
					boolIsAlive = false;
					objIWCB.terminateWriters();
					break;
				}
			}
		}
		objIWCB.updateWriterCount();
		objHR.close();
		objHR = null;
	}
}
