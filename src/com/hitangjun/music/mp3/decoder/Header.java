/**
* Header.java -- MPEG 1.0/2.0/2.5 Audio Layer I/II/III 帧头信息解码
*/

package com.hitangjun.music.mp3.decoder;

import com.hitangjun.music.mp3.instream.IRandomAccess;

public final class Header {
	public static final int MPEG1 = 3;
	public static final int MPEG2 = 2;
	public static final int MPEG25 = 0;
	public static final int MAX_FRAMESIZE = 1732;	//MPEG 1.0/2.0/2.5, Lay 1/2/3

	/*
	 * intBitrateTable[intLSF][intLayer-1][intBitrateIndex]
	 */
	private static final int[][][] intBitrateTable = {
		{
			//MPEG 1
			//Layer I
			{0,32,64,96,128,160,192,224,256,288,320,352,384,416,448,},
			//Layer II
			{0,32,48,56, 64, 80, 96,112,128,160,192,224,256,320,384,},
			//Layer III
			{0,32,40,48, 56, 64, 80, 96,112,128,160,192,224,256,320,}
		},
		{
			//MPEG 2.0/2.5
			//Layer I
			{0,32,48,56,64,80,96,112,128,144,160,176,192,224,256,},
			//Layer II
			{0,8,16,24,32,40,48,56,64,80,96,112,128,144,160,},
			//Layer III
			{0,8,16,24,32,40,48,56,64,80,96,112,128,144,160,}
		}
	};
	/*
	 * intSamplingRateTable[intVersionID][intSamplingFrequency]
	 */
	private static final int[][] intSamplingRateTable = {
		{11025 , 12000 , 8000,0},	//MPEG Version 2.5
		{0,0,0,0,},				//reserved
		{22050, 24000, 16000 ,0,},	//MPEG Version 2 (ISO/IEC 13818-3)
		{44100, 48000, 32000,0}		//MPEG Version 1 (ISO/IEC 11172-3)
	};

	/*
	 * intVersionID: 2 bits
	 * "00"  MPEG Version 2.5 (unofficial extension of MPEG 2);
	 * "01"  reserved;
	 * "10"  MPEG Version 2 (ISO/IEC 13818-3);
	 * "11"  MPEG Version 1 (ISO/IEC 11172-3).
	 */
	private static int intVersionID;
	
	/*
	 * intLayer: 2 bits
	 * "11"	 Layer I
	 * "10"	 Layer II
	 * "01"	 Layer III
	 * "00"	 reserved
	 * 已换算intLayer=4-intLayer: 1-Layer I; 2-Layer II; 3-Layer III; 4-reserved
	 */
	private static int intLayer;
	
	/*
	 * intProtectionBit: 1 bit
	 * "1"  no CRC;
	 * "0"  protected by 16 bit CRC following header.
	 */
	private static int intProtectionBit;
	
	/* 
	 * intBitrateIndex: 4 bits
	 */
	private static int intBitrateIndex;
	
	/*
	 * intSamplingFrequency: 2 bits
	 * '00'	 44.1kHz
	 * '01'	 48kHz
	 * '10'	 32kHz
	 * '11'  reserved
	 */
	private static int intSamplingFrequency;
	
	private static int intPaddingBit;
	
	/*
	 * intMode: 2 bits
	 * '00'  Stereo;
	 * '01'  Joint Stereo (Stereo);
	 * '10'  Dual channel (Two mono channels);
	 * '11'  Single channel (Mono).
	 */
	private static int intMode;
	
	/*
	 * intModeExtension: 2 bits
	 * 		 intensity_stereo	boolMS_Stereo
	 * '00'	 off				off
	 * '01'	 on					off
	 * '10'	 off				on
	 * '11'	 on					on
	 */
	private static int intModeExtension;

	private static boolean boolSync;
	private static int intFrameSize;
	private static int intMainDataSlots;	//main_data length
	private static int intSideInfoSize;		//side_information length
	private static int intLSF;
	private static int intStandardMask = 0xffe00000;
	private static boolean boolMS_Stereo, boolIntensityStereo;
	private static IRandomAccess iraInput;
	
	public Header(IRandomAccess in_rai) {
		iraInput = in_rai;
	}
	
	public boolean isMSStereo() {
		return boolMS_Stereo;
	}
	
	public boolean isIStereo() {
		return boolIntensityStereo;
	}
	
	public int getBitrate() {
		return intBitrateTable[intLSF][intLayer-1][intBitrateIndex];
	}
	
	public int getBitrateIndex() {
		return intBitrateIndex;
	}
	
	public int getChannels() {
		if(intMode == 3)
			return 1;
		return 2;
	}
	
	public int getMode() {
		return intMode;
	}
	
	public int getModeExtension() {
		return intModeExtension;
	}

	public int getVersion() {
		return intVersionID;
	}

	public int getLayer() {
		return intLayer;
	}

	public int getSampleFrequency() {
		return intSamplingFrequency;
	}

	public int getFrequency() {
		return intSamplingRateTable[intVersionID][intSamplingFrequency];
	}
	
	public int getMainDataSlots() {
		return intMainDataSlots;
	}
	
	public int getSideInfoSize() {
		return intSideInfoSize;
	}
	
	public int getFrameSize() {
		return intFrameSize;
	}

	private void parseHeader(int h) {
		intVersionID = (h >> 19) & 3;
		intLayer = 4 - (h >> 17) & 3;
		intProtectionBit = (h >> 16) & 0x1;
		intBitrateIndex = (h >> 12) & 0xF;
		intSamplingFrequency = (h >> 10) & 3;
		intPaddingBit = (h >> 9) & 0x1;
		intMode = (h >> 6) & 3;
		intModeExtension = (h >> 4) & 3;
		
		boolMS_Stereo = intMode == 1 && (intModeExtension & 2) != 0;
		boolIntensityStereo = intMode == 1 && (intModeExtension & 0x1) != 0;
		intLSF = (intVersionID == MPEG1) ? 0 : 1;
		
		switch (intLayer) {
		case 1:	
			intFrameSize  = intBitrateTable[intLSF][0][intBitrateIndex] * 12000;
			intFrameSize /= intSamplingRateTable[intVersionID][intSamplingFrequency];
			intFrameSize  = ((intFrameSize+intPaddingBit)<<2);
			break;
		case 2:
			intFrameSize  = intBitrateTable[intLSF][1][intBitrateIndex] * 144000;
			intFrameSize /= intSamplingRateTable[intVersionID][intSamplingFrequency];
			intFrameSize += intPaddingBit;
			break;
		case 3:
			intFrameSize  = intBitrateTable[intLSF][2][intBitrateIndex] * 144000;
			intFrameSize /= intSamplingRateTable[intVersionID][intSamplingFrequency]<<(intLSF);
			intFrameSize += intPaddingBit;
			
			//计算帧边信息长度
			if(intVersionID == MPEG1)
				intSideInfoSize = (intMode == 3) ? 17 : 32;
			else
				intSideInfoSize = (intMode == 3) ? 9 : 17;
			
			break;
		default:
			break;
		}
		
		//计算主数据长度
		intMainDataSlots = intFrameSize - 4 - intSideInfoSize;
		if(intProtectionBit == 0)
			intMainDataSlots -= 2;
	}

	// -------------------------------------------------------------------
	// syncXXX: 帧同步
	private static int intFrameCounter;	//当前帧序号
	
	public boolean syncFrame()  throws Exception{
		if(syncSearch() == false)
			return false;
		if (intProtectionBit == 0)
			headerCRC();
		intFrameCounter++;
		return true;
	}
	
	/*
	 * 帧同步: 查找到帧同步字后与下一帧的intVersionID等比较,确定是否找到有效的同步字.
	 * 
	 * 怎样更简单、有效帧同步?
	 */
	private boolean syncSearch() throws Exception {
		int h, cur_mask = 0;
		boolean bfind = false;
		long start_pos = iraInput.getFilePointer();
		while(!bfind) {
			h = syncWord();
			parseHeader(h);
			
			//若intVersionID等帧的特征未改变,不用与下一帧的同步头比较.
			
			if(boolSync) {
				bfind = true;
				break;
			}
			
			//与下一帧的同步头比较
			
			cur_mask = 0xffe00000;		//syncword
			cur_mask |= h & 0x180000;	//intVersionID
			cur_mask |= h & 0x60000;	//intLayer
			cur_mask |= h & 0x60000;	//intSamplingFrequency
			//cur_mask |= h & 0xC0;		//intMode
			//intModeExtension 不是始终不变.
			
			byte[] b4 = new byte[4];
			if(iraInput.dump(intFrameSize-4, b4, 0, 4) < 4)
				break;
			bfind = (makeInt32(b4, 0) & cur_mask) == cur_mask;
			b4 = null;
			if(iraInput.getFilePointer() - start_pos > 0xffff) {
				System.out.println("\n搜索 64K 未发现MP3帧后放弃。");
				break;
			}
		}
		
		if(!boolSync) {
			boolSync = true;
			if(bfind && intStandardMask == 0xffe00000) {	//是第一帧:
				intStandardMask = cur_mask;
				longAllFrameSize = iraInput.length();
				longFrameOffset = iraInput.getFilePointer()-4;
				longAllFrameSize -= longFrameOffset;
				parseVBR();
				getTrackFrames();
				getDuration();
				printHeaderInfo();
			}
			/*System.out.println("Begining of syncword: bytes " +
					(iraInput.getFilePointer()-4) +
					", frame_number = " + frame_number);*/
		}
		return bfind;
	}

	private int syncWord() throws Exception {
		int ioff = -4;
		int h = 0, read_byte = 0;
		do {
			if((read_byte = iraInput.read()) == -1)
				return 0;
			ioff++;
			h = (h << 8) | read_byte;
		} while (syncCheck(h) == false);

		if (ioff > 0)
			boolSync = false;

		return h;
	}

	private static boolean syncCheck(int h) {
		if ((h & intStandardMask) != intStandardMask
				|| (((h >> 19) & 3) == 1)		// version ID:  01 - reserved
				|| (((h >> 17) & 3) == 0)		// Layer index: 00 - reserved
				|| (((h >> 12) & 0xf) == 0xf)
				|| (((h >> 12) & 0xf) == 0)
				|| (((h >> 10) & 3) == 3)) {
			return false;
		}
		return true;
	}
	
	private static int makeInt32(byte[] b, int off) {
		int h = b[off] & 0xff;
		h <<= 8;
		h |= b[off + 1] & 0xff;
		h <<= 8;
		h |= b[off + 2] & 0xff;
		h <<= 8;
		h |= b[off + 3] & 0xff;
		return h;
	}
	
	private static void headerCRC() throws Exception {
		if(iraInput.read() == -1 || iraInput.read() == -1)
			throw new Exception("crc() 文件读完");
	}
	
	// -------------------------------------------------------------------
	// 以下是辅助功能。删除掉源码及相关调用不影响正常播放。
	// -------------------------------------------------------------------
	// MP3 文件帧数等信息
	private static long longAllFrameSize;	//帧长度总和(文件长度减去ID3 tag, APE tag 等长度)
	private static long longFrameOffset;	//第一帧的偏移量
	private static long longAllTrackFrames;	//帧数
	private static float floatFrameDuration;	//一帧时长(秒)
	private static String strDuration;
	
	public long getTrackFrames() {
		if(longAllTrackFrames == 0)
			longAllTrackFrames = longAllFrameSize / intFrameSize;
		return longAllTrackFrames;
	}
	
	/*
	 * 返回MP3文件时长(秒)
	 */
	public float getDuration() {
		floatFrameDuration = (float)1152 / (intSamplingRateTable[intVersionID][intSamplingFrequency] << intLSF);
		float duration = floatFrameDuration * longAllTrackFrames;
		int m = (int)(duration / 60);
		strDuration = String.format("%1$02d:%2$02d", m, (int)(duration - m * 60 + 0.5));
		progress = new StringBuffer(">----------------------------------------");
		
		return duration;
	}
	
	public String getDurationStr(){
		float duration = floatFrameDuration * longAllTrackFrames;
		int m = (int)(duration / 60);
		return String.format("%1$02d:%2$02d", m, (int)(duration - m * 60 + 0.5));
	}
	
	// -------------------------------------------------------------------
	// 解码存储在第一帧的VBR信息
	private static boolean boolVBR;
	private byte[] byteVBRToc;
	private String strVBREncoder;
	private String strBitRate;
	
	public boolean parseVBR() throws Exception {
		byte[] b = new byte[intFrameSize];
		iraInput.dump(0, b, 0, intFrameSize);
		if (intFrameSize < 124 + intSideInfoSize) {
			b = null;
			return false;
		}
		for (int i = 2; i < intSideInfoSize; ++i)
			if (b[i] != 0) {
				b = null;
				return false;
			}

		// Xing header means always VBR
		if (((b[intSideInfoSize] == 'X') && (b[intSideInfoSize + 1] == 'i')
				&& (b[intSideInfoSize + 2] == 'n') && (b[intSideInfoSize + 3] == 'g'))
				|| ((b[intSideInfoSize] == 'I') && (b[intSideInfoSize + 1] == 'n')
				&& (b[intSideInfoSize + 2] == 'f') && (b[intSideInfoSize + 3] == 'o'))) {
			boolVBR = true;
			longAllFrameSize -= intFrameSize;	//VBR的第一帧无主数据，存储的是VBR信息。
			longFrameOffset += intFrameSize;
		} else
			return false;

		int xing_flags = makeInt32(b, intSideInfoSize + 4);
		if ((xing_flags & 1) == 1) { // track frames
			longAllTrackFrames = makeInt32(b, intSideInfoSize + 8);
			if (longAllTrackFrames < 0)
				longAllTrackFrames = 0;
			System.out.println("track frames: " + longAllTrackFrames);
		}
		if ((xing_flags & 0x2) != 0) { // track bytes
			longAllFrameSize = makeInt32(b, intSideInfoSize + 12);
			System.out.println(" track bytes: " + longAllFrameSize);
		}
		if ((xing_flags & 0x4) != 0) { // TOC: intSideInfoSize+16, 100 bytes.
			byteVBRToc = new byte[100];
			System.arraycopy(b, intSideInfoSize + 16, byteVBRToc, 0, 100);
			//System.out.println("         TOC: true");
		}
		if ((xing_flags & 0x8) != 0) { // VBR quality
			int xing_quality = makeInt32(b, intSideInfoSize + 116);
			System.out.println("     quality: " + xing_quality);
		}
		
		if (b[intSideInfoSize + 120] == 0) {
			b = null;
			return true;
		}
		strVBREncoder = new String(b, intSideInfoSize + 120, 8);
		System.out.println("     encoder: " + strVBREncoder);

		int lame_vbr = b[intSideInfoSize + 129] & 0xf;
		switch (lame_vbr) {
		// from rev1 proposal... not sure if all good in practice
		case 1:
		case 8: // CBR
			//strBitRate = "CBR";
			break;
		case 2:
		case 9: // ABR
			strBitRate = "ABR";
			break;
		default: // 00==unknown is taken as VBR
			strBitRate = "VBR";
		}

		b = null;
		return true;
	}
	
	// -------------------------------------------------------------------
	// 打印信息
	public void printHeaderInfo() {
		String[] sver = {"MPEG 2.5", "reserved", "MPEG 2.0", "MPEG 1.0"};
		String[] mode_str = {", Stereo",", Joint Stereo",", Dual channel",", Single channel(Mono)"};
		String[] exmode_str = {"","(I/S)","(M/S)","(I/S & M/S)"};
		if(!boolVBR)
			strBitRate = String.format("%1$dK", intBitrateTable[intLSF][intLayer-1][intBitrateIndex]);
		System.out.println("\r" + sver[intVersionID] + ", Layer " + intLayer + 
			", " + getFrequency()+"Hz, " + 
			strBitRate +
			mode_str[intMode] +
			exmode_str[intModeExtension] + ", " +
			strDuration);
	}
	
	private static StringBuffer progress;
	private static int progress_index = 1;
	
	public void printState() {
		float t = intFrameCounter * floatFrameDuration;
		int m = (int)(t / 60);
		float s = t - 60 * m;
		float percent;
		if(boolVBR)
			percent = (float)intFrameCounter / longAllTrackFrames * 100;
		else
			percent = (float)iraInput.getFilePointer() / iraInput.length() * 100;
		int i = ((int)(percent + 0.5) << 2) / 10;
		if(i == progress_index) {
			progress.replace(i-1, i+1, "=>");
			progress_index++;
		}
		System.out.printf("\r%1$02d:%2$04.1f [%3$-41s] %4$.1f%%", m, s, progress, percent);
	}
	
	// -------------------------------------------------------------------
	// 帧定位
	//seekFrame() ...
}
