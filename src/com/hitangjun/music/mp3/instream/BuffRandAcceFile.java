/**
* BuffRandAcceFile.java -- 带缓冲区的本地文件随机读取
*/
package com.hitangjun.music.mp3.instream;

import com.hitangjun.music.mp3.tag.ID3Tag;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BuffRandAcceFile implements IRandomAccess {
	private static final int DEFAULT_BUFSIZE=2048; 
	private static RandomAccessFile rafCurFile;
	private byte byteInBuf[];
	private long longStartPos;
	private long longEndPos = -1;
	private int intBufSize;
	private long longCurPos;
	private boolean boolBufDirty;
	private long longFileEndPos;
	private int intBufUsedSize;
	private long longBufSizeMask;
	private ID3Tag ID3TagInfo;
	
	private long longFileSize;

	public BuffRandAcceFile(String strName) throws Exception {
		this(strName, DEFAULT_BUFSIZE);
	}

	public BuffRandAcceFile(String strName, int intBufSize) throws Exception {
		rafCurFile = new RandomAccessFile(strName, "r");
		this.intBufSize = intBufSize;
		byteInBuf = new byte[intBufSize];
		longFileEndPos = rafCurFile.length() - 1;
		this.longBufSizeMask = ~((long) this.intBufSize - 1L);
		this.boolBufDirty = false;
		
		long lFrameOffset = 0;	//第一帧的偏移量
		long lFrameSize = longFileSize = rafCurFile.length();
		
		ID3Tag objID3Tag = new ID3Tag();
		byte[] byteTmpBuf = new byte[128];
		
		//ID3 v2
		this.seek(0);
		this.read(byteTmpBuf, 0, 10);
		int size_v2 = objID3Tag.checkID3V2(byteTmpBuf,0);
		if (size_v2 > 0) {
			lFrameOffset = size_v2;
			lFrameSize -= size_v2;
			size_v2 -= 10; // header: 10 bytes
			byte[] b = new byte[size_v2];
			this.read(b, 0, size_v2);
			objID3Tag.parseID3V2(b,0);
			b = null;
		}
		
		//ID3 v1
		this.seek(longFileSize - 128);
		this.read(byteTmpBuf);
		if(objID3Tag.checkID3V1(byteTmpBuf)) {
			lFrameSize -= 128;
			objID3Tag.parseID3V1(byteTmpBuf);
		}
		
		this.seek(lFrameOffset);
		objID3Tag.printTag();
		objID3Tag.destroy();
		objID3Tag = null;
		byteTmpBuf = null;
	}
	
	public BuffRandAcceFile(String strName, int intBufSize,boolean isReturnTag) throws Exception {
		rafCurFile = new RandomAccessFile(strName, "r");
		this.intBufSize = intBufSize;
		byteInBuf = new byte[intBufSize];
		longFileEndPos = rafCurFile.length() - 1;
		this.longBufSizeMask = ~((long) this.intBufSize - 1L);
		this.boolBufDirty = false;
		
		long lFrameOffset = 0;	//第一帧的偏移量
		long lFrameSize = longFileSize = rafCurFile.length();
		
		ID3Tag objID3Tag = new ID3Tag();
		byte[] byteTmpBuf = new byte[128];
		
		//ID3 v2
		this.seek(0);
		this.read(byteTmpBuf, 0, 10);
		int size_v2 = objID3Tag.checkID3V2(byteTmpBuf,0);
		if (size_v2 > 0) {
			lFrameOffset = size_v2;
			lFrameSize -= size_v2;
			size_v2 -= 10; // header: 10 bytes
			byte[] b = new byte[size_v2];
			this.read(b, 0, size_v2);
			objID3Tag.parseID3V2(b,0);
			b = null;
		}
		
		//ID3 v1
		this.seek(longFileSize - 128);
		this.read(byteTmpBuf);
		if(objID3Tag.checkID3V1(byteTmpBuf)) {
			lFrameSize -= 128;
			objID3Tag.parseID3V1(byteTmpBuf);
		}
		
		this.seek(lFrameOffset);
		objID3Tag.printTag();
		if(isReturnTag){
			this.ID3TagInfo = objID3Tag;
		}else{
			objID3Tag.destroy();
			objID3Tag = null;
		}
		byteTmpBuf = null;
	}

	public int read(long pos) throws IOException {
		if (pos < this.longStartPos || pos > this.longEndPos) {
			this.flushbuf();
			this.seek(pos);

			if ((pos < this.longStartPos) || (pos > this.longEndPos)) {
				return -1;
			}
		}
		this.longCurPos = pos;
		return this.byteInBuf[(int) (pos - this.longStartPos)] & 0xff;
	}
	
	public int read() throws IOException {
		int iret = byteInBuf[(int) (this.longCurPos - this.longStartPos)] & 0xff;
		this.seek(this.longCurPos + 1);
		return iret;
	}

	public int read(byte b[]) throws IOException {
		return this.read(b, 0, b.length);
	}

	public int read(byte b[], int off, int len) throws IOException {
		long readendpos = this.longCurPos + len - 1;

		if (readendpos <= this.longEndPos && readendpos <= this.longFileEndPos) {
			System.arraycopy(this.byteInBuf, (int) (this.longCurPos - this.longStartPos),
					b, off, len);
		} else {
			if (readendpos > this.longFileEndPos) {
				len = (int) (rafCurFile.length() - this.longCurPos + 1);
				if(len <= 0)
					return -1;
			}
			// if(this.longCurPos != rafCurFile.getFilePointer()) { //增加一条判断
			rafCurFile.seek(this.longCurPos);
			// System.out.println("1. rafCurFile.seek("+longCurPos+")");////
			// }
			len = rafCurFile.read(b, off, len);
			readendpos = this.longCurPos + len - 1;
		}
		this.seek(readendpos + 1);
		return len;
	}
	
	/*
	 * 从当前位置复制,不移动文件"指针"
	 */
	public int dump(int src_off, byte b[], int dst_off, int len) throws IOException {
		long rpos = this.longCurPos + src_off;
		long readendpos = rpos + len - 1;

		if (readendpos <= this.longEndPos && readendpos <= this.longFileEndPos) {
			System.arraycopy(this.byteInBuf, (int) (rpos - this.longStartPos), b, dst_off, len);
		} else {
			if (readendpos > this.longFileEndPos) {
				len = (int) (rafCurFile.length() - rpos + 1);// ???????
				if(len <= 0)
					return -1;
			}
			rafCurFile.seek(rpos);
			len = rafCurFile.read(b, dst_off, len);
			rafCurFile.seek(this.longCurPos);
		}
		return len;
	}

	public long length() {
		return longFileSize;
	}

	public void seek(long pos) throws IOException {
		if ((pos < this.longStartPos) || (pos > this.longEndPos)) {
			this.flushbuf();

			if ((pos >= 0) && (pos <= this.longFileEndPos)
					&& (this.longFileEndPos != 0)) {
				this.longStartPos = pos & this.longBufSizeMask;
				this.intBufUsedSize = this.fillbuf();
			} else if (((pos == 0) && (this.longFileEndPos == 0))
					|| (pos == this.longFileEndPos + 1)) {
				this.longStartPos = pos;
				this.intBufUsedSize = 0;
			}
			this.longEndPos = this.longStartPos + this.intBufSize - 1;
		}
		this.longCurPos = pos;
	}
	
	public long getFilePointer() {
		return this.longCurPos;
	}

	public void close() {
		try {
			rafCurFile.close();
		} catch (IOException e) {}
	}

	private int fillbuf() throws IOException {
		rafCurFile.seek(this.longStartPos);
		//System.out.println("2. rafCurFile.seek("+this.longStartPos+")");
		this.boolBufDirty = false;
		return rafCurFile.read(this.byteInBuf);
	}

	private void flushbuf() throws IOException {
		if (this.boolBufDirty == true) {
			if (rafCurFile.getFilePointer() != this.longStartPos) {
				rafCurFile.seek(this.longStartPos);
				//System.out.println("3. rafCurFile.seek(" + this.longStartPos + ")");
			}
			rafCurFile.write(this.byteInBuf, 0, this.intBufUsedSize);
			this.boolBufDirty = false;
		}
	}

	/**
	 * 返回MP3的tag信息
	 * @return
	 */
	public ID3Tag getID3TagInfo() {
		return ID3TagInfo;
	}

	public void setID3TagInfo(ID3Tag tagInfo) {
		ID3TagInfo = tagInfo;
	}
}
