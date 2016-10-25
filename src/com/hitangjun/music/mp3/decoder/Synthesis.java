/**
* Synthesis.java -- Layer I/II/III 多相合成滤波(快速算法)
*/
package com.hitangjun.music.mp3.decoder;

public final class Synthesis {
	public static int PCM_LENGTH = 4608;
	private static int intStep;
	private static final int[] intIdxFIFO = new int[2];
	private static final float[][] floatFIFO = new float[2][1024];	
	private static final int[] intBufPtr = new int[2];
	public static final byte[] bytePCMBuf = new byte[PCM_LENGTH];

	public Synthesis(int channels) {
		intIdxFIFO[0] = intIdxFIFO[1] = 64;
		intStep = (channels == 1) ? 2: 4;
	}

	public void reset() {
		intBufPtr[0] = 0;
		intBufPtr[1] = 2;
	}

	/*
	 * 解码完一帧intBufPtr[0]暂存一帧的PCM数据字节数
	 */
	public int getSize() {
		return intBufPtr[0];
	}

	/*
	 * synthesisSubBand -- 多相合成滤波
	 * ISO/IEC 11172-3 ANNEX_B Figure 3-A.2
	 */
	public void synthesisSubBand (float[] floatPolyphaseIn, int ch) {
		int i, PCMi;
		int intCurOff = intIdxFIFO[ch];
		int idx = intBufPtr[ch];
		float sum, dew16[], curfifo[] = floatFIFO[ch];

		//if(idx >= 4608)	//正确的调用顺序可防止这种情况发生
		//	reset();

		//1. Shift
		intCurOff = (intCurOff - 64) & 0x3FF;

		//2. Matrixing
		dct32to64(floatPolyphaseIn, curfifo, intCurOff);

		//3. Build the U vector
		//4. Dewindowing
		//5. Calculate and output 32 samples
		switch(intCurOff) {
		case 0:
			//u_vector={0,96,128,224,256,352,384,480,512,608,640,736,768,864,896,992}=u_base
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i];
				sum += dew16[1]  * curfifo[i+96];
				sum += dew16[2]  * curfifo[i+128];
				sum += dew16[3]  * curfifo[i+224];
				sum += dew16[4]  * curfifo[i+256];
				sum += dew16[5]  * curfifo[i+352];
				sum += dew16[6]  * curfifo[i+384];
				sum += dew16[7]  * curfifo[i+480];
				sum += dew16[8]  * curfifo[i+512];
				sum += dew16[9]  * curfifo[i+608];
				sum += dew16[10] * curfifo[i+640];
				sum += dew16[11] * curfifo[i+736];
				sum += dew16[12] * curfifo[i+768];
				sum += dew16[13] * curfifo[i+864];
				sum += dew16[14] * curfifo[i+896];
				sum += dew16[15] * curfifo[i+992];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 64:
			//u_vector={64,160,192,288,320,416,448,544,576,672,704,800,832,928,960,32}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+64];
				sum += dew16[1]  * curfifo[i+160];
				sum += dew16[2]  * curfifo[i+192];
				sum += dew16[3]  * curfifo[i+288];
				sum += dew16[4]  * curfifo[i+320];
				sum += dew16[5]  * curfifo[i+416];
				sum += dew16[6]  * curfifo[i+448];
				sum += dew16[7]  * curfifo[i+544];
				sum += dew16[8]  * curfifo[i+576];
				sum += dew16[9]  * curfifo[i+672];
				sum += dew16[10] * curfifo[i+704];
				sum += dew16[11] * curfifo[i+800];
				sum += dew16[12] * curfifo[i+832];
				sum += dew16[13] * curfifo[i+928];
				sum += dew16[14] * curfifo[i+960];
				sum += dew16[15] * curfifo[i+32];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 128:
			//u_vector={128,224,256,352,384,480,512,608,640,736,768,864,896,992,0,96}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+128];
				sum += dew16[1]  * curfifo[i+224];
				sum += dew16[2]  * curfifo[i+256];
				sum += dew16[3]  * curfifo[i+352];
				sum += dew16[4]  * curfifo[i+384];
				sum += dew16[5]  * curfifo[i+480];
				sum += dew16[6]  * curfifo[i+512];
				sum += dew16[7]  * curfifo[i+608];
				sum += dew16[8]  * curfifo[i+640];
				sum += dew16[9]  * curfifo[i+736];
				sum += dew16[10] * curfifo[i+768];
				sum += dew16[11] * curfifo[i+864];
				sum += dew16[12] * curfifo[i+896];
				sum += dew16[13] * curfifo[i+992];
				sum += dew16[14] * curfifo[i];
				sum += dew16[15] * curfifo[i+96];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 192:
			//u_vector={192,288,320,416,448,544,576,672,704,800,832,928,960,32,64,160}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+192];
				sum += dew16[1]  * curfifo[i+288];
				sum += dew16[2]  * curfifo[i+320];
				sum += dew16[3]  * curfifo[i+416];
				sum += dew16[4]  * curfifo[i+448];
				sum += dew16[5]  * curfifo[i+544];
				sum += dew16[6]  * curfifo[i+576];
				sum += dew16[7]  * curfifo[i+672];
				sum += dew16[8]  * curfifo[i+704];
				sum += dew16[9]  * curfifo[i+800];
				sum += dew16[10] * curfifo[i+832];
				sum += dew16[11] * curfifo[i+928];
				sum += dew16[12] * curfifo[i+960];
				sum += dew16[13] * curfifo[i+32];
				sum += dew16[14] * curfifo[i+64];
				sum += dew16[15] * curfifo[i+160];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 256:
			//u_vector={256,352,384,480,512,608,640,736,768,864,896,992,0,96,128,224}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+256];
				sum += dew16[1]  * curfifo[i+352];
				sum += dew16[2]  * curfifo[i+384];
				sum += dew16[3]  * curfifo[i+480];
				sum += dew16[4]  * curfifo[i+512];
				sum += dew16[5]  * curfifo[i+608];
				sum += dew16[6]  * curfifo[i+640];
				sum += dew16[7]  * curfifo[i+736];
				sum += dew16[8]  * curfifo[i+768];
				sum += dew16[9]  * curfifo[i+864];
				sum += dew16[10] * curfifo[i+896];
				sum += dew16[11] * curfifo[i+992];
				sum += dew16[12] * curfifo[i];
				sum += dew16[13] * curfifo[i+96];
				sum += dew16[14] * curfifo[i+128];
				sum += dew16[15] * curfifo[i+224];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 320:
			//u_vector={320,416,448,544,576,672,704,800,832,928,960,32,64,160,192,288}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+320];
				sum += dew16[1]  * curfifo[i+416];
				sum += dew16[2]  * curfifo[i+448];
				sum += dew16[3]  * curfifo[i+544];
				sum += dew16[4]  * curfifo[i+576];
				sum += dew16[5]  * curfifo[i+672];
				sum += dew16[6]  * curfifo[i+704];
				sum += dew16[7]  * curfifo[i+800];
				sum += dew16[8]  * curfifo[i+832];
				sum += dew16[9]  * curfifo[i+928];
				sum += dew16[10] * curfifo[i+960];
				sum += dew16[11] * curfifo[i+32];
				sum += dew16[12] * curfifo[i+64];
				sum += dew16[13] * curfifo[i+160];
				sum += dew16[14] * curfifo[i+192];
				sum += dew16[15] * curfifo[i+288];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 384:
			//u_vector={384,480,512,608,640,736,768,864,896,992,0,96,128,224,256,352}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+384];
				sum += dew16[1]  * curfifo[i+480];
				sum += dew16[2]  * curfifo[i+512];
				sum += dew16[3]  * curfifo[i+608];
				sum += dew16[4]  * curfifo[i+640];
				sum += dew16[5]  * curfifo[i+736];
				sum += dew16[6]  * curfifo[i+768];
				sum += dew16[7]  * curfifo[i+864];
				sum += dew16[8]  * curfifo[i+896];
				sum += dew16[9]  * curfifo[i+992];
				sum += dew16[10] * curfifo[i];
				sum += dew16[11] * curfifo[i+96];
				sum += dew16[12] * curfifo[i+128];
				sum += dew16[13] * curfifo[i+224];
				sum += dew16[14] * curfifo[i+256];
				sum += dew16[15] * curfifo[i+352];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 448:
			//u_vector={448,544,576,672,704,800,832,928,960,32,64,160,192,288,320,416}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+448];
				sum += dew16[1]  * curfifo[i+544];
				sum += dew16[2]  * curfifo[i+576];
				sum += dew16[3]  * curfifo[i+672];
				sum += dew16[4]  * curfifo[i+704];
				sum += dew16[5]  * curfifo[i+800];
				sum += dew16[6]  * curfifo[i+832];
				sum += dew16[7]  * curfifo[i+928];
				sum += dew16[8]  * curfifo[i+960];
				sum += dew16[9]  * curfifo[i+32];
				sum += dew16[10] * curfifo[i+64];
				sum += dew16[11] * curfifo[i+160];
				sum += dew16[12] * curfifo[i+192];
				sum += dew16[13] * curfifo[i+288];
				sum += dew16[14] * curfifo[i+320];
				sum += dew16[15] * curfifo[i+416];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 512:
			//u_vector={512,608,640,736,768,864,896,992,0,96,128,224,256,352,384,480}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+512];
				sum += dew16[1]  * curfifo[i+608];
				sum += dew16[2]  * curfifo[i+640];
				sum += dew16[3]  * curfifo[i+736];
				sum += dew16[4]  * curfifo[i+768];
				sum += dew16[5]  * curfifo[i+864];
				sum += dew16[6]  * curfifo[i+896];
				sum += dew16[7]  * curfifo[i+992];
				sum += dew16[8]  * curfifo[i];
				sum += dew16[9]  * curfifo[i+96];
				sum += dew16[10] * curfifo[i+128];
				sum += dew16[11] * curfifo[i+224];
				sum += dew16[12] * curfifo[i+256];
				sum += dew16[13] * curfifo[i+352];
				sum += dew16[14] * curfifo[i+384];
				sum += dew16[15] * curfifo[i+480];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 576:
			//u_vector={576,672,704,800,832,928,960,32,64,160,192,288,320,416,448,544}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+576];
				sum += dew16[1]  * curfifo[i+672];
				sum += dew16[2]  * curfifo[i+704];
				sum += dew16[3]  * curfifo[i+800];
				sum += dew16[4]  * curfifo[i+832];
				sum += dew16[5]  * curfifo[i+928];
				sum += dew16[6]  * curfifo[i+960];
				sum += dew16[7]  * curfifo[i+32];
				sum += dew16[8]  * curfifo[i+64];
				sum += dew16[9]  * curfifo[i+160];
				sum += dew16[10] * curfifo[i+192];
				sum += dew16[11] * curfifo[i+288];
				sum += dew16[12] * curfifo[i+320];
				sum += dew16[13] * curfifo[i+416];
				sum += dew16[14] * curfifo[i+448];
				sum += dew16[15] * curfifo[i+544];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 640:
			//u_vector={640,736,768,864,896,992,0,96,128,224,256,352,384,480,512,608}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+640];
				sum += dew16[1]  * curfifo[i+736];
				sum += dew16[2]  * curfifo[i+768];
				sum += dew16[3]  * curfifo[i+864];
				sum += dew16[4]  * curfifo[i+896];
				sum += dew16[5]  * curfifo[i+992];
				sum += dew16[6]  * curfifo[i];
				sum += dew16[7]  * curfifo[i+96];
				sum += dew16[8]  * curfifo[i+128];
				sum += dew16[9]  * curfifo[i+224];
				sum += dew16[10] * curfifo[i+256];
				sum += dew16[11] * curfifo[i+352];
				sum += dew16[12] * curfifo[i+384];
				sum += dew16[13] * curfifo[i+480];
				sum += dew16[14] * curfifo[i+512];
				sum += dew16[15] * curfifo[i+608];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 704:
			//u_vector={704,800,832,928,960,32,64,160,192,288,320,416,448,544,576,672}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+704];
				sum += dew16[1]  * curfifo[i+800];
				sum += dew16[2]  * curfifo[i+832];
				sum += dew16[3]  * curfifo[i+928];
				sum += dew16[4]  * curfifo[i+960];
				sum += dew16[5]  * curfifo[i+32];
				sum += dew16[6]  * curfifo[i+64];
				sum += dew16[7]  * curfifo[i+160];
				sum += dew16[8]  * curfifo[i+192];
				sum += dew16[9]  * curfifo[i+288];
				sum += dew16[10] * curfifo[i+320];
				sum += dew16[11] * curfifo[i+416];
				sum += dew16[12] * curfifo[i+448];
				sum += dew16[13] * curfifo[i+544];
				sum += dew16[14] * curfifo[i+576];
				sum += dew16[15] * curfifo[i+672];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 768:
			//u_vector={768,864,896,992,0,96,128,224,256,352,384,480,512,608,640,736}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+768];
				sum += dew16[1]  * curfifo[i+864];
				sum += dew16[2]  * curfifo[i+896];
				sum += dew16[3]  * curfifo[i+992];
				sum += dew16[4]  * curfifo[i];
				sum += dew16[5]  * curfifo[i+96];
				sum += dew16[6]  * curfifo[i+128];
				sum += dew16[7]  * curfifo[i+224];
				sum += dew16[8]  * curfifo[i+256];
				sum += dew16[9]  * curfifo[i+352];
				sum += dew16[10] * curfifo[i+384];
				sum += dew16[11] * curfifo[i+480];
				sum += dew16[12] * curfifo[i+512];
				sum += dew16[13] * curfifo[i+608];
				sum += dew16[14] * curfifo[i+640];
				sum += dew16[15] * curfifo[i+736];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 832:
			//u_vector={832,928,960,32,64,160,192,288,320,416,448,544,576,672,704,800}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+832];
				sum += dew16[1]  * curfifo[i+928];
				sum += dew16[2]  * curfifo[i+960];
				sum += dew16[3]  * curfifo[i+32];
				sum += dew16[4]  * curfifo[i+64];
				sum += dew16[5]  * curfifo[i+160];
				sum += dew16[6]  * curfifo[i+192];
				sum += dew16[7]  * curfifo[i+288];
				sum += dew16[8]  * curfifo[i+320];
				sum += dew16[9]  * curfifo[i+416];
				sum += dew16[10] * curfifo[i+448];
				sum += dew16[11] * curfifo[i+544];
				sum += dew16[12] * curfifo[i+576];
				sum += dew16[13] * curfifo[i+672];
				sum += dew16[14] * curfifo[i+704];
				sum += dew16[15] * curfifo[i+800];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 896:
			//u_vector={896,992,0,96,128,224,256,352,384,480,512,608,640,736,768,864}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+896];
				sum += dew16[1]  * curfifo[i+992];
				sum += dew16[2]  * curfifo[i];
				sum += dew16[3]  * curfifo[i+96];
				sum += dew16[4]  * curfifo[i+128];
				sum += dew16[5]  * curfifo[i+224];
				sum += dew16[6]  * curfifo[i+256];
				sum += dew16[7]  * curfifo[i+352];
				sum += dew16[8]  * curfifo[i+384];
				sum += dew16[9]  * curfifo[i+480];
				sum += dew16[10] * curfifo[i+512];
				sum += dew16[11] * curfifo[i+608];
				sum += dew16[12] * curfifo[i+640];
				sum += dew16[13] * curfifo[i+736];
				sum += dew16[14] * curfifo[i+768];
				sum += dew16[15] * curfifo[i+864];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		case 960:
			//u_vector={960,32,64,160,192,288,320,416,448,544,576,672,704,800,832,928}
			for(i = 0; i < 32; i++) {
				dew16 = dewin[i];
				sum  = dew16[0]  * curfifo[i+960];
				sum += dew16[1]  * curfifo[i+32];
				sum += dew16[2]  * curfifo[i+64];
				sum += dew16[3]  * curfifo[i+160];
				sum += dew16[4]  * curfifo[i+192];
				sum += dew16[5]  * curfifo[i+288];
				sum += dew16[6]  * curfifo[i+320];
				sum += dew16[7]  * curfifo[i+416];
				sum += dew16[8]  * curfifo[i+448];
				sum += dew16[9]  * curfifo[i+544];
				sum += dew16[10] * curfifo[i+576];
				sum += dew16[11] * curfifo[i+672];
				sum += dew16[12] * curfifo[i+704];
				sum += dew16[13] * curfifo[i+800];
				sum += dew16[14] * curfifo[i+832];
				sum += dew16[15] * curfifo[i+928];
				PCMi = sum > 32767 ? 32767 : (sum < -32768 ? -32768 : (int)sum);
				bytePCMBuf[idx] = (byte)PCMi;
				bytePCMBuf[idx + 1] = (byte)(PCMi >>> 8);
				idx += intStep;
			}
			break;
		}
	
		intIdxFIFO[ch] = intCurOff;
		intBufPtr[ch] = idx;
	}

	private void dct32to64(float[] fIn, float[] fOut, int idx) {
		float in0,in1,in2,in3,in4,in5,in6,in7,in8,in9,in10,in11,in12,in13,in14,in15;
		float out0,out1,out2,out3,out4,out5,out6,out7,out8,out9,out10,out11,out12,out13,out14,out15;
		float d8_0,d8_1,d8_2,d8_3,d8_4,d8_5,d8_6,d8_7;
		float ein0, ein1, oin0, oin1;

		//>>>>>>>>>>>>>>>>
		// 用DCT16计算DCT32输出[0..31]的偶数下标元素
		in0 =  fIn[0]  + fIn[31];
		in1 =  fIn[1]  + fIn[30];
		in2 =  fIn[2]  + fIn[29];
		in3 =  fIn[3]  + fIn[28];
		in4 =  fIn[4]  + fIn[27];
		in5 =  fIn[5]  + fIn[26];
		in6 =  fIn[6]  + fIn[25];
		in7 =  fIn[7]  + fIn[24];
		in8 =  fIn[8]  + fIn[23];
		in9 =  fIn[9]  + fIn[22];
		in10 = fIn[10] + fIn[21];
		in11 = fIn[11] + fIn[20];
		in12 = fIn[12] + fIn[19];
		in13 = fIn[13] + fIn[18];
		in14 = fIn[14] + fIn[17];
		in15 = fIn[15] + fIn[16];

		//DCT16
		{
			//>>>>>>>> 用DCT8计算DCT16输出[0..15]的偶数下标元素
			d8_0 = in0 + in15;
			d8_1 = in1 + in14;
			d8_2 = in2 + in13;
			d8_3 = in3 + in12;
			d8_4 = in4 + in11;
			d8_5 = in5 + in10;
			d8_6 = in6 + in9;
			d8_7 = in7 + in8;

			//DCT8. 加(减)法29,乘法12次
			{
				//>>>>e 用DCT4计算DCT8的输出[0..7]的偶数下标元素
				out1 = d8_0 + d8_7;
				out3 = d8_1 + d8_6;
				out5 = d8_2 + d8_5;
				out7 = d8_3 + d8_4;

				//>>e DCT2
				ein0 = out1 + out7;
				ein1 = out3 + out5;
				fOut[idx + 48] =  -ein0 - ein1;
				fOut[idx] = (ein0 - ein1) * 0.7071068f;	// 0.5/cos(PI/4)

				//>>o DCT2
				oin0 = (out1 - out7) * 0.5411961f;	// 0.5/cos( PI/8)
				oin1 = (out3 - out5) * 1.3065630f;	// 0.5/cos(3PI/8)

				out2 =  oin0 + oin1;
				out12 = (oin0 - oin1) * 0.7071068f;// cos(PI/4)

				fOut[idx + 40] = fOut[idx + 56] = -out2 - out12;
				fOut[idx + 8] = out12;
				//<<<<e 完成计算DCT8的输出[0..7]的偶数下标元素

				//>>>>o 用DCT4计算DCT8的输出[0..7]的奇数下标元素
				//o DCT4 part1
				out1 = (d8_0 - d8_7) * 0.5097956f;	// 0.5/cos( PI/16)
				out3 = (d8_1 - d8_6) * 0.6013449f;	// 0.5/cos(3PI/16)
				out5 = (d8_2 - d8_5) * 0.8999762f;	// 0.5/cos(5PI/16)
				out7 = (d8_3 - d8_4) * 2.5629154f;	// 0.5/cos(7PI/16)

				//o DCT4 part2

				//e DCT2 part1
				ein0 = out1 + out7;
				ein1 = out3 + out5;

				//o DCT2 part1
				oin0 = (out1 - out7) * 0.5411961f;	// 0.5/cos(PI/8)
				oin1 = (out3 - out5) * 1.3065630f;	// 0.5/cos(3PI/8)

				//e DCT2 part2
				out1 =  ein0 + ein1;
				out5 = (ein0 - ein1) * 0.7071068f;	// cos(PI/4)

				//o DCT2 part2
				out3 = oin0 + oin1;
				out7 = (oin0 - oin1) * 0.7071068f;	// cos(PI/4)
				out3 += out7;

				//o DCT4 part3
				fOut[idx + 44] = fOut[idx + 52] = -out1 - out3;	//out1+=out3
				fOut[idx + 36] = fOut[idx + 60] = -out3 - out5;	//out3+=out5
				fOut[idx + 4] = out5 + out7;						//out5+=out7
				fOut[idx + 12] = out7;
				//<<<<o 完成计算DCT8的输出[0..7]的奇数下标元素
			}
			//<<<<<<<< 完成计算DCT16输出[0..15]的偶数下标元素

			//-----------------------------------------------------------------

			//>>>>>>>> 用DCT8计算DCT16输出[0..15]的奇数下标元素
			d8_0 = (in0 - in15) * 0.5024193f;	// 0.5/cos( 1 * PI/32)
			d8_1 = (in1 - in14) * 0.5224986f;	// 0.5/cos( 3 * PI/32)
			d8_2 = (in2 - in13) * 0.5669440f;	// 0.5/cos( 5 * PI/32)
			d8_3 = (in3 - in12) * 0.6468218f;	// 0.5/cos( 7 * PI/32)
			d8_4 = (in4 - in11) * 0.7881546f;	// 0.5/cos( 9 * PI/32)
			d8_5 = (in5 - in10) * 1.0606777f;	// 0.5/cos(11 * PI/32)
			d8_6 = (in6 - in9) * 1.7224471f;	// 0.5/cos(13 * PI/32)
			d8_7 = (in7 - in8) * 5.1011486f;	// 0.5/cos(15 * PI/32)

			//DCT8
			{
				//>>>>e 用DCT4计算DCT8的输出[0..7]的偶数下标元素.
				out3  = d8_0 + d8_7;
				out7  = d8_1 + d8_6;
				out11 = d8_2 + d8_5;
				out15 = d8_3 + d8_4;

				//>>e DCT2
				ein0 = out3 + out15;
				ein1 = out7 + out11;
				out1 = ein0 + ein1;
				out9 = (ein0 - ein1) * 0.7071068f;		// 0.5/cos(PI/4)

				//>>o DCT2
				oin0 = (out3 - out15) * 0.5411961f;	// 0.5/cos( PI/8)
				oin1 = (out7 - out11) * 1.3065630f;	// 0.5/cos(3PI/8)

				out5 =  oin0 + oin1;
				out13 = (oin0 - oin1) * 0.7071068f;	// cos(PI/4)

				out5 += out13;
				//<<<<e 完成计算DCT8的输出[0..7]的偶数下标元素

				//>>>>o 用DCT4计算DCT8的输出[0..7]的奇数下标元素
				//o DCT4 part1
				out3  = (d8_0 - d8_7) * 0.5097956f;	// 0.5/cos( PI/16)
				out7  = (d8_1 - d8_6) * 0.6013449f;	// 0.5/cos(3PI/16)
				out11 = (d8_2 - d8_5) * 0.8999762f;	// 0.5/cos(5PI/16)
				out15 = (d8_3 - d8_4) * 2.5629154f;	// 0.5/cos(7PI/16)

				//o DCT4 part2

				//e DCT2 part1
				ein0 = out3 + out15;
				ein1 = out7 + out11;

				//o DCT2 part1
				oin0 = (out3 - out15) * 0.5411961f;	// 0.5/cos(PI/8)
				oin1 = (out7 - out11) * 1.3065630f;	// 0.5/cos(3PI/8)

				//e DCT2 part2
				out3 =  ein0 + ein1;
				out11 = (ein0 - ein1) * 0.7071068f;	// cos(PI/4)

				//o DCT2 part2
				out7 = oin0 + oin1;
				out15 = (oin0 - oin1) * 0.7071068f;	// cos(PI/4)
				out7 += out15;

				//o DCT4 part3
				out3  += out7;
				out7  += out11;
				out11 += out15;
				//<<<<o 完成计算DCT8的输出[0..7]的奇数下标元素
			}

			fOut[idx + 46] = fOut[idx + 50] = -out1 - out3;	//out1 += out3
			fOut[idx + 42] = fOut[idx + 54] = -out3 - out5;	//out3 += out5
			fOut[idx + 38] = fOut[idx + 58] = -out5 - out7;	//out5 += out7
			fOut[idx + 34] = fOut[idx + 62] = -out7 - out9;	//out7 += out9
			fOut[idx + 2]  = out9 + out11;						//out9 += out11
			fOut[idx + 6]  = out11 + out13;					//out11 += out13
			fOut[idx + 10] = out13 + out15;					//out13 += out15
			//<<<<<<<< 完成计算DCT16输出[0..15]的奇数下标元素
		}
		fOut[idx + 14] = out15;	//fOut[idx + 14]=out32[30]
		//<<<<<<<<<<<<<<<<
		// 完成计算DCT32输出[0..31]的偶数下标元素

		//=====================================================================

		//>>>>>>>>>>>>>>>>
		// 用DCT16计算计算DCT32输出[0..31]的奇数下标元素
		in0  = (fIn[0]  - fIn[31]) * 0.5006030f;	// 0.5/cos( 1 * PI/64)
		in1  = (fIn[1]  - fIn[30]) * 0.5054710f;	// 0.5/cos( 3 * PI/64)
		in2  = (fIn[2]  - fIn[29]) * 0.5154473f;	// 0.5/cos( 5 * PI/64)
		in3  = (fIn[3]  - fIn[28]) * 0.5310426f;	// 0.5/cos( 7 * PI/64)
		in4  = (fIn[4]  - fIn[27]) * 0.5531039f;	// 0.5/cos( 9 * PI/64)
		in5  = (fIn[5]  - fIn[26]) * 0.5829350f;	// 0.5/cos(11 * PI/64)
		in6  = (fIn[6]  - fIn[25]) * 0.6225041f;	// 0.5/cos(13 * PI/64)
		in7  = (fIn[7]  - fIn[24]) * 0.6748083f;	// 0.5/cos(15 * PI/64)
		in8  = (fIn[8]  - fIn[23]) * 0.7445362f;	// 0.5/cos(17 * PI/64)
		in9  = (fIn[9]  - fIn[22]) * 0.8393496f;	// 0.5/cos(19 * PI/64)
		in10 = (fIn[10] - fIn[21]) * 0.9725682f;	// 0.5/cos(21 * PI/64)
		in11 = (fIn[11] - fIn[20]) * 1.1694399f;	// 0.5/cos(23 * PI/64)
		in12 = (fIn[12] - fIn[19]) * 1.4841646f;	// 0.5/cos(25 * PI/64)
		in13 = (fIn[13] - fIn[18]) * 2.0577810f;	// 0.5/cos(27 * PI/64)
		in14 = (fIn[14] - fIn[17]) * 3.4076084f;	// 0.5/cos(29 * PI/64)
		in15 = (fIn[15] - fIn[16]) * 10.190008f;	// 0.5/cos(31 * PI/64)

		//DCT16
		{
			//>>>>>>>> 用DCT8计算DCT16输出[0..15]的偶数下标元素:
			d8_0 = in0 + in15;
			d8_1 = in1 + in14;
			d8_2 = in2 + in13;
			d8_3 = in3 + in12;
			d8_4 = in4 + in11;
			d8_5 = in5 + in10;
			d8_6 = in6 + in9;
			d8_7 = in7 + in8;

			//DCT8
			{
				//>>>>e 用DCT4计算DCT8的输出[0..7]的偶数下标元素
				out1 = d8_0 + d8_7;
				out3 = d8_1 + d8_6;
				out5 = d8_2 + d8_5;
				out7 = d8_3 + d8_4;

				//>>e DCT2
				ein0 = out1 + out7;
				ein1 = out3 + out5;
				out0 = ein0 + ein1;
				out8 = (ein0 - ein1) * 0.7071068f;	// 0.5/cos(PI/4)

				//>>o DCT2
				oin0 = (out1 - out7) * 0.5411961f;	// 0.5/cos( PI/8)
				oin1 = (out3 - out5) * 1.3065630f;	// 0.5/cos(3PI/8)

				out4 =  oin0 + oin1;
				out12 = (oin0 - oin1) * 0.7071068f;// cos(PI/4)

				out4 += out12;
				//<<<<e 完成计算DCT8的输出[0..7]的偶数下标元素

				//>>>>o 用DCT4计算DCT8的输出[0..7]的奇数下标元素
				//o DCT4 part1
				out1 = (d8_0 - d8_7) * 0.5097956f;	// 0.5/cos( PI/16)
				out3 = (d8_1 - d8_6) * 0.6013449f;	// 0.5/cos(3PI/16)
				out5 = (d8_2 - d8_5) * 0.8999762f;	// 0.5/cos(5PI/16)
				out7 = (d8_3 - d8_4) * 2.5629154f;	// 0.5/cos(7PI/16)

				//o DCT4 part2

				//e DCT2 part1
				ein0 = out1 + out7;
				ein1 = out3 + out5;

				//o DCT2 part1
				oin0 = (out1 - out7) * 0.5411961f;	// 0.5/cos(PI/8)
				oin1 = (out3 - out5) * 1.3065630f;	// 0.5/cos(3PI/8)

				//e DCT2 part2
				out2 = ein0 + ein1;
				out10 = (ein0 - ein1) * 0.7071068f;// cos(PI/4)

				//o DCT2 part2
				out6 = oin0 + oin1;
				out14 = (oin0 - oin1) * 0.7071068f;
				out6 += out14;

				//o DCT4 part3
				out2  += out6;
				out6  += out10;
				out10 += out14;
				//<<<<o 完成计算DCT8的输出[0..7]的奇数下标元素
			}
			//<<<<<<<< 完成计算DCT16输出[0..15]的偶数下标元素

			//-----------------------------------------------------------------

			//>>>>>>>> 用DCT8计算DCT16输出[0..15]的奇数下标元素
			d8_0 = (in0 - in15) * 0.5024193f;	// 0.5/cos( 1 * PI/32)
			d8_1 = (in1 - in14) * 0.5224986f;	// 0.5/cos( 3 * PI/32)
			d8_2 = (in2 - in13) * 0.5669440f;	// 0.5/cos( 5 * PI/32)
			d8_3 = (in3 - in12) * 0.6468218f;	// 0.5/cos( 7 * PI/32)
			d8_4 = (in4 - in11) * 0.7881546f;	// 0.5/cos( 9 * PI/32)
			d8_5 = (in5 - in10) * 1.0606777f;	// 0.5/cos(11 * PI/32)
			d8_6 = (in6 - in9) * 1.7224471f;	// 0.5/cos(13 * PI/32)
			d8_7 = (in7 - in8) * 5.1011486f;	// 0.5/cos(15 * PI/32)

			//DCT8
			{
				//>>>>e 用DCT4计算DCT8的输出[0..7]的偶数下标元素.
				out1 = d8_0 + d8_7;
				out3 = d8_1 + d8_6;
				out5 = d8_2 + d8_5;
				out7 = d8_3 + d8_4;

				//>>e DCT2
				ein0 = out1 + out7;
				ein1 = out3 + out5;
				in0 =  ein0 + ein1;	//out0->in0,out4->in4
				in4 = (ein0 - ein1) * 0.7071068f;	// 0.5/cos(PI/4)

				//>>o DCT2
				oin0 = (out1 - out7) * 0.5411961f;	// 0.5/cos( PI/8)
				oin1 = (out3 - out5) * 1.3065630f;	// 0.5/cos(3PI/8)

				in2 =  oin0 + oin1;					//out2->in2,out6->in6
				in6 = (oin0 - oin1) * 0.7071068f;	// cos(PI/4)

				in2 += in6;
				//<<<<e 完成计算DCT8的输出[0..7]的偶数下标元素

				//>>>>o 用DCT4计算DCT8的输出[0..7]的奇数下标元素
				//o DCT4 part1
				out1 = (d8_0 - d8_7) * 0.5097956f;	// 0.5/cos( PI/16)
				out3 = (d8_1 - d8_6) * 0.6013449f;	// 0.5/cos(3PI/16)
				out5 = (d8_2 - d8_5) * 0.8999762f;	// 0.5/cos(5PI/16)
				out7 = (d8_3 - d8_4) * 2.5629154f;	// 0.5/cos(7PI/16)

				//o DCT4 part2

				//e DCT2 part1
				ein0 = out1 + out7;
				ein1 = out3 + out5;

				//o DCT2 part1
				oin0 = (out1 - out7) * 0.5411961f;	// 0.5/cos(PI/8)
				oin1 = (out3 - out5) * 1.3065630f;	// 0.5/cos(3PI/8)

				//e DCT2 part2
				out1 =  ein0 + ein1;
				out5 = (ein0 - ein1) * 0.7071068f;	// cos(PI/4)

				//o DCT2 part2
				out3 = oin0 + oin1;
				out15 = (oin0 - oin1) * 0.7071068f;
				out3 += out15;

				//o DCT4 part3
				out1 += out3;
				out3 += out5;
				out5 += out15;
				//<<<<o 完成计算DCT8的输出[0..7]的奇数下标元素
			}
									//out15=out7
			out13 = in6 + out15;	//out13=out6+ou7
			out11 = out5 + in6;		//out11=out5+ou6
			out9 = in4 + out5;		//out9 =out4+ou5
			out7 = out3 + in4;		//out7 =out3+ou4
			out5 = in2 + out3;		//out5 =out2+ou3
			out3 = out1 + in2;		//out3 =out1+ou2
			out1 += in0;			//out1 =out0+ou1
			//<<<<<<<< 完成计算DCT16输出[0..15]的奇数下标元素
		}

		//out32[i]=out[i]+out[i+1]; out32[31]=out[15]
		fOut[idx + 47] = fOut[idx + 49] = -out0 - out1;
		fOut[idx + 45] = fOut[idx + 51] = -out1 - out2;
		fOut[idx + 43] = fOut[idx + 53] = -out2 - out3;
		fOut[idx + 41] = fOut[idx + 55] = -out3 - out4;
		fOut[idx + 39] = fOut[idx + 57] = -out4 - out5;
		fOut[idx + 37] = fOut[idx + 59] = -out5 - out6;
		fOut[idx + 35] = fOut[idx + 61] = -out6 - out7;
		fOut[idx + 33] = fOut[idx + 63] = -out7 - out8;
		fOut[idx + 1] = out8 + out9;
		fOut[idx + 3] = out9 + out10;
		fOut[idx + 5] = out10 + out11;
		fOut[idx + 7] = out11 + out12;
		fOut[idx + 9] = out12 + out13;
		fOut[idx + 11] = out13 + out14;
		fOut[idx + 13] = out14 + out15;
		fOut[idx + 15] = out15;
		//<<<<<<<<<<<<<<<<

		fOut[idx + 16] = 0;

		fOut[idx + 17] = -out15;	//fOut[idx + 17] = -fOut[idx + 15]
		fOut[idx + 18] = -fOut[idx + 14];
		fOut[idx + 19] = -fOut[idx + 13];
		fOut[idx + 20] = -fOut[idx + 12];
		fOut[idx + 21] = -fOut[idx + 11];
		fOut[idx + 22] = -fOut[idx + 10];
		fOut[idx + 23] = -fOut[idx + 9];
		fOut[idx + 24] = -fOut[idx + 8];
		fOut[idx + 25] = -fOut[idx + 7];
		fOut[idx + 26] = -fOut[idx + 6];
		fOut[idx + 27] = -fOut[idx + 5];
		fOut[idx + 28] = -fOut[idx + 4];
		fOut[idx + 29] = -fOut[idx + 3];
		fOut[idx + 30] = -fOut[idx + 2];
		fOut[idx + 31] = -fOut[idx + 1];
		fOut[idx + 32] = -fOut[idx];
	}

	/*
	 * dewin[32][16]: D[i] * 32767 (i=0..511), 然后重新排序
	 * D[]: Coefficients Di of the synthesis window. ISO/IEC 11172-3 ANNEX_B Table 3-B.3
	 */
	private static final float[][] dewin = {
		{0f,-14.5f,106.5f,-229.5f,1018.5f,-2576.5f,3287f,-18744.5f,
		37519f,18744.5f,3287f,2576.5f,1018.5f,229.5f,106.5f,14.5f},
		{-0.5f,-15.5f,109f,-259.5f,1000f,-2758.5f,2979.5f,-19668f,
		37496f,17820f,3567f,2394f,1031.5f,200.5f,104f,13f},
		{-0.5f,-17.5f,111f,-290.5f,976f,-2939.5f,2644f,-20588f,
		37428f,16895.5f,3820f,2212.5f,1040f,173.5f,101f,12f},
		{-0.5f,-19f,112.5f,-322.5f,946.5f,-3118.5f,2280.5f,-21503f,
		37315f,15973.5f,4046f,2031.5f,1043.5f,147f,98f,10.5f},
		{-0.5f,-20.5f,113.5f,-355.5f,911f,-3294.5f,1888f,-22410.5f,
		37156.5f,15056f,4246f,1852.5f,1042.5f,122f,95f,9.5f},
		{-0.5f,-22.5f,114f,-389.5f,869.5f,-3467.5f,1467.5f,-23308.5f,
		36954f,14144.5f,4420f,1675.5f,1037.5f,98.5f,91.5f,8.5f},
		{-0.5f,-24.5f,114f,-424f,822f,-3635.5f,1018.5f,-24195f,
		36707.5f,13241f,4569.5f,1502f,1028.5f,76.5f,88f,8f},
		{-1f,-26.5f,113.5f,-459.5f,767.5f,-3798.5f,541f,-25068.5f,
		36417.5f,12347f,4694.5f,1331.5f,1016f,55.5f,84.5f,7f},
		{-1f,-29f,112f,-495.5f,707f,-3955f,35f,-25926.5f,
		36084.5f,11464.5f,4796f,1165f,1000.5f,36f,80.5f,6.5f},
		{-1f,-31.5f,110.5f,-532f,640f,-4104.5f,-499f,-26767f,
		35710f,10594.5f,4875f,1003f,981f,18f,77f,5.5f},
		{-1f,-34f,107.5f,-568.5f,565.5f,-4245.5f,-1061f,-27589f,
		35295f,9739f,4931.5f,846f,959.5f,1f,73.5f,5f},
		{-1.5f,-36.5f,104f,-605f,485f,-4377.5f,-1650f,-28389f,
		34839.5f,8899.5f,4967.5f,694f,935f,-14.5f,69.5f,4.5f},
		{-1.5f,-39.5f,100f,-641.5f,397f,-4499f,-2266.5f,-29166.5f,
		34346f,8077.5f,4983f,547.5f,908.5f,-28.5f,66f,4f},
		{-2f,-42.5f,94.5f,-678f,302.5f,-4609.5f,-2909f,-29919f,
		33814.5f,7274f,4979.5f,407f,879.5f,-41.5f,62.5f,3.5f},
		{-2f,-45.5f,88.5f,-714f,201f,-4708f,-3577f,-30644.5f,
		33247f,6490f,4958f,272.5f,849f,-53f,58.5f,3.5f},
		{-2.5f,-48.5f,81.5f,-749f,92.5f,-4792.5f,-4270f,-31342f,
		32645f,5727.5f,4919f,144f,817f,-63.5f,55.5f,3f},
		{-2.5f,-52f,73f,-783.5f,-22.5f,-4863.5f,-4987.5f,-32009.5f,
		32009.5f,4987.5f,4863.5f,22.5f,783.5f,-73f,52f,2.5f},
		{-3f,-55.5f,63.5f,-817f,-144f,-4919f,-5727.5f,-32645f,
		31342f,4270f,4792.5f,-92.5f,749f,-81.5f,48.5f,2.5f},
		{-3.5f,-58.5f,53f,-849f,-272.5f,-4958f,-6490f,-33247f,
		30644.5f,3577f,4708f,-201f,714f,-88.5f,45.5f,2f},
		{-3.5f,-62.5f,41.5f,-879.5f,-407f,-4979.5f,-7274f,-33814.5f,
		29919f,2909f,4609.5f,-302.5f,678f,-94.5f,42.5f,2f},
		{-4f,-66f,28.5f,-908.5f,-547.5f,-4983f,-8077.5f,-34346f,
		29166.5f,2266.5f,4499f,-397f,641.5f,-100f,39.5f,1.5f},
		{-4.5f,-69.5f,14.5f,-935f,-694f,-4967.5f,-8899.5f,-34839.5f,
		28389f,1650f,4377.5f,-485f,605f,-104f,36.5f,1.5f},
		{-5f,-73.5f,-1f,-959.5f,-846f,-4931.5f,-9739f,-35295f,
		27589f,1061f,4245.5f,-565.5f,568.5f,-107.5f,34f,1f},
		{-5.5f,-77f,-18f,-981f,-1003f,-4875f,-10594.5f,-35710f,
		26767f,499f,4104.5f,-640f,532f,-110.5f,31.5f,1f},
		{-6.5f,-80.5f,-36f,-1000.5f,-1165f,-4796f,-11464.5f,-36084.5f,
		25926.5f,-35f,3955f,-707f,495.5f,-112f,29f,1f},
		{-7f,-84.5f,-55.5f,-1016f,-1331.5f,-4694.5f,-12347f,-36417.5f,
		25068.5f,-541f,3798.5f,-767.5f,459.5f,-113.5f,26.5f,1f},
		{-8f,-88f,-76.5f,-1028.5f,-1502f,-4569.5f,-13241f,-36707.5f,
		24195f,-1018.5f,3635.5f,-822f,424f,-114f,24.5f,0.5f},
		{-8.5f,-91.5f,-98.5f,-1037.5f,-1675.5f,-4420f,-14144.5f,-36954f,
		23308.5f,-1467.5f,3467.5f,-869.5f,389.5f,-114f,22.5f,0.5f},
		{-9.5f,-95f,-122f,-1042.5f,-1852.5f,-4246f,-15056f,-37156.5f,
		22410.5f,-1888f,3294.5f,-911f,355.5f,-113.5f,20.5f,0.5f},
		{-10.5f,-98f,-147f,-1043.5f,-2031.5f,-4046f,-15973.5f,-37315f,
		21503f,-2280.5f,3118.5f,-946.5f,322.5f,-112.5f,19f,0.5f},
		{-12f,-101f,-173.5f,-1040f,-2212.5f,-3820f,-16895.5f,-37428f,
		20588f,-2644f,2939.5f,-976f,290.5f,-111f,17.5f,0.5f},
		{-13f,-104f,-200.5f,-1031.5f,-2394f,-3567f,-17820f,-37496f,
		19668f,-2979.5f,2758.5f,-1000f,259.5f,-109f,15.5f,0.5f}
	};
}
