package com.hitangjun.music.mp3;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hitangjun.music.mp3.instream.BuffRandAcceFile;
import com.hitangjun.music.mp3.instream.HttpReader;
import com.hitangjun.music.mp3.tag.ID3Tag;

public class Mp3Util {
	private static final Logger log = LoggerFactory.getLogger(Mp3Util.class);
	/**
	 * 获取MP3的标题和歌手专辑信息，如果需要读取时间信息则处理速度会比较慢，需先缓存MP3
	 * 
	 * 
	 * IRandomAccess ira = new
	 * RandAccessURL("http://127.0.0.1:8090/music/data/music/a.mp3");
	 * Header h = new Header(ira);
	 * h.syncFrame();
	 * System.out.println("歌曲时长"+h.getDurationStr());
	 * 
	 * @param sUrl
	 * @return
	 * @throws Exception
	 */
	public static ID3Tag getNetMp3Tag(String sUrl) {
		long start = System.currentTimeMillis();
		HttpReader objHR;
		long longFileSize;
		long longAllFrameSize;
		URL objURL;

		try {
			objURL = new URL(sUrl);
			longAllFrameSize = longFileSize = objURL.openConnection()
					.getContentLength();
			if (longFileSize == -1){
				log.warn(sUrl+"  == >file length is -1");
				return null;
			}
			objHR = new HttpReader(objURL);

			byte[] tmp_buf, b;
			int retry = 0;
			ID3Tag objID3tag = new ID3Tag();
			while (true) {
				try {
					tmp_buf = new byte[128];

					// id3 v1
					objHR.seek(longFileSize - 128);
					if (objHR.getData(tmp_buf, 0, 128) == 128) {
						if (objID3tag.checkID3V1(tmp_buf))
							objID3tag.parseID3V1(tmp_buf);

					}
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
			
			return objID3tag;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			log.debug("getMp3Tag spent {} ms",System.currentTimeMillis()-start);
		}
		return null;
	}
	
	public static ID3Tag getLocalMp3Tag(String filePath){
		try {
			return new BuffRandAcceFile(filePath,2048,true).getID3TagInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
