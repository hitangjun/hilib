
package com.hitangjun.music.mp3.instream;

/*
 * 读/写通信的接口.类似于C++的回调函数
 * 
 * 例:
 * class BuffRandAcceURL 内实现本接口的方法tryWriting()
 * class BuffRandAcceURL 内new Writer(this, ...)传值到Writer.objIWCB
 * class Writer 内调用objIWCB.tryWriting()
 */
public interface IWriterCallBack {
	public int tryWriting() throws InterruptedException;
	public void updateBuffer(int i, int len);
	public void updateWriterCount();
	public int getWriterCount();
	public void terminateWriters();
}
