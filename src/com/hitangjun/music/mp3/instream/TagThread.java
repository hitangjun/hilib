/*
* TagThread.java -- 解码远程MP3文件ID3 tag
*/
package com.hitangjun.music.mp3.instream;

import com.hitangjun.music.mp3.tag.ID3Tag;

import java.io.IOException;
import java.net.URL;

public class TagThread implements Runnable {
	private HttpReader objHR;
	private long longFileSize;

	public TagThread(URL u,long flen) throws IOException {
		objHR = new HttpReader(u);
		longFileSize = flen;
		new Thread(this).start();
	}

	public void run() {
		byte[] tmp_buf, b;
		int retry = 0;
		ID3Tag objID3tag = new ID3Tag();
		while (true) {
			try {
				tmp_buf = new byte[128];

				// id3 v1
				objHR.seek(longFileSize - 128);
				if (objHR.getData(tmp_buf, 0, 128) == 128)
					if (objID3tag.checkID3V1(tmp_buf))
						objID3tag.parseID3V1(tmp_buf);

				// ID3 v2
				int v2_size = 0;
				objHR.seek(0);
				if (objHR.getData(tmp_buf, 0, 10) == 10) {
					v2_size = objID3tag.checkID3V2(tmp_buf, 0);
					// System.out.println("v2_size = " + v2_size);
					if (v2_size > 0) {
						v2_size -= 10; // id3 v2 header
						b = new byte[v2_size];
						if (objHR.getData(b, 0, v2_size) == v2_size)
							objID3tag.parseID3V2(b, 0);
					}
				}

				objID3tag.printTag();
				break;
			} catch (Exception e) {
				if (++retry == HttpReader.MAX_RETRY)
					break;
			}
		}
		tmp_buf = null;
		b = null;
		objID3tag.destroy();
		objID3tag = null;
	}
}
