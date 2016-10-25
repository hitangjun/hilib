
package com.hitangjun.music.mp3.instream;

public interface IRandomAccess {
	public int read() throws Exception;
	public int read(byte b[]) throws Exception;
	public int read(byte b[], int off, int len) throws Exception;
	public int dump(int src_off, byte b[], int dst_off, int len) throws Exception;
	public void seek(long pos) throws Exception;
	public long length();
	public long getFilePointer();
	public void close();
}
