package com.hitangjun.music.mp3.instream;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpReader {
	public static final int MAX_RETRY = 10;
	private URL objURL;
	private HttpURLConnection httpConn;
	private InputStream objIStream;
	private long longCurPos;			//决定seek方法中是否执行文件读取定位
	private int intConnectTimeout;
	private int intReadTimeout;

	public HttpReader(URL u) {
		this(u, 5000, 5000);
	}

	public HttpReader(URL u, int intConnectTimeout, int intReadTimeout) {
		this.intConnectTimeout = intConnectTimeout;
		this.intReadTimeout = intReadTimeout;
		objURL = u;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int r = objIStream.read(b, off, len);
		longCurPos += r;
		return r;
	}

	public int getData(byte[] b, int off, int len) throws IOException {
		int r, rema = len;
		while (rema > 0) {
			if ((r = objIStream.read(b, off, rema)) == -1)
				return -1;
			rema -= r;
			off += r;
			longCurPos += r;
		}
		return len;
	}

	public void close() {
		if (httpConn != null) {
			httpConn.disconnect();
			httpConn = null;
		}
		if (objIStream != null) {
			try {
				objIStream.close();
			} catch (IOException e) {}
			objIStream = null;
		}
		objURL = null;
	}

	/*
	 * 抛出异常通知重试.
	 * 例如响应码503可能是由某种暂时的原因引起的,同一IP频繁的连接请求会遭服务器拒绝.
	 */
	public void seek(long pos) throws IOException {
		if (pos == longCurPos && objIStream != null)
			return;
		if (httpConn != null) {
			httpConn.disconnect();
			httpConn = null;
		}
		if (objIStream != null) {
			objIStream.close();
			objIStream = null;
		}
		httpConn = (HttpURLConnection) objURL.openConnection();
		httpConn.setConnectTimeout(intConnectTimeout);
		httpConn.setReadTimeout(intReadTimeout);
		String sProperty = "bytes=" + pos + "-";
		httpConn.setRequestProperty("Range", sProperty);
		//httpConn.setRequestProperty("Connection", "Keep-Alive");
		int responseCode = httpConn.getResponseCode();
		if (responseCode < 200 || responseCode >= 300) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			throw new IOException("HTTP responseCode="+responseCode);
		}

		objIStream = httpConn.getInputStream();
		longCurPos = pos;
		//System.out.println(Thread.currentThread().getName()+ ", longCurPos="+longCurPos);
	}
}
