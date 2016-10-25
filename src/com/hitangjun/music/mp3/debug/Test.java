package com.hitangjun.music.mp3.debug;


import com.hitangjun.music.mp3.Mp3Util;

public class Test {
	public static void main(String[] args) throws Exception {
//		IRandomAccess ira = new BuffRandAcceURL("http://192.168.1.2:8010/music/data/music/a581e38019fd469d8bed05c92f6dab5a.mp3");
//		IRandomAccess ira = new BuffRandAcceURL("http://mp3.baidu.com/j?j=2&url=http%3A%2F%2Fzhangmenshiting.baidu.com%2Fdata%2Fmusic%2F935374%2F%25E8%25A7%25A3%25E8%2584%25B1.mp3%3Fxcode%3D1003e6c4475a7c010ca177dffe5246f3");
//		long start = System.currentTimeMillis();
////		IRandomAccess ira = new RandAccessURL("http://192.168.1.88:8080/music/data.msp?fileName=4e7011d45e0e79a1f9dcb204.mp3");
//		System.out.println(System.currentTimeMillis()-start);
//		Header h = new Header(ira);
//		h.syncFrame();
//		System.out.println("歌曲时长 "+h.getDurationStr());
//		System.out.println(System.currentTimeMillis()-start);
//		h.printHeaderInfo();
//		//h = null;
//		
//		IRandomAccess irb = new BuffRandAcceFile("F:/KwDownload/song/刘德华-17岁.mp3",2048);
//		Header hb = new Header(irb);
//		hb.syncFrame();
//		System.out.println("歌曲时长 "+hb.getDurationStr());
//		hb.printHeaderInfo();
//		Thread.currentThread().getThreadGroup().list();
//		System.out.println(Thread.activeCount()); 
		
		long start = System.currentTimeMillis();
//		Mp3Util.getNetMp3Tag("http://192.168.1.2:8010/music/msp/data.msp?fileName=4e7011d45e0e79a1f9dcb204.mp3").printTag();
		Mp3Util.getLocalMp3Tag("F:/KwDownload/song/刘德华-17岁.mp3").printTag();
		Mp3Util.getLocalMp3Tag("F:/KwDownload/song/王菲-传奇.mp3").printTag();
		System.out.println(Mp3Util.getLocalMp3Tag("F:/KwDownload/song/梅兰芳-贵妃醉酒.mp3").getStrArtist());
		System.out.println(System.currentTimeMillis()-start);
		
	}
}
