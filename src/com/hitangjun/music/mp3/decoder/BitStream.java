/**
* BitStream.java -- 读取位流
*/
package com.hitangjun.music.mp3.decoder;
import com.hitangjun.music.mp3.instream.IRandomAccess;

public final class BitStream {
	private static IRandomAccess iraInput;
	private byte[] byteBitReservoir;
	private int intBitPos;
	private int intBytePos;
	private int intBuffSize;		//已填入的字节数

	public BitStream(IRandomAccess iraIn) {
		iraInput = iraIn;
		byteBitReservoir = new byte[4096];	// 长度不小于最大帧长1732
	}

	public BitStream(int len) {
		byteBitReservoir = new byte[len];	// 任意长度,用于帧边信息解码
	}

	/*
	 * 将缓冲区byte_pos及之后的字节移动到缓冲区首
	 */
	private void move() {
		System.arraycopy(byteBitReservoir, intBytePos, byteBitReservoir, 0, intBuffSize - intBytePos);
		intBuffSize -= intBytePos;
		intBitPos = 0;
		intBytePos = 0;
	}

	/*
	 * 向bit_reservoir添加len字节.
	 */
	public int append(int len) throws Exception {
		if (len + intBuffSize > byteBitReservoir.length)
			move();
		if(iraInput.read(byteBitReservoir, intBuffSize, len) < len)
			throw new Exception("BitStream.Append(" + len + ") 文件读完");

		intBuffSize += len;
		return len;
	}

	public void resetIndex() {
		intBytePos = 0;
		intBitPos = 0;
		intBuffSize = 0;
	}

	public void skipBytes(int nbytes) {
		intBytePos += nbytes;
		intBitPos = 0;
	}

	/*
	 * 从缓冲区bit_reservoir读取1 bit.调用频度极高
	 */
	public int get1Bit() {
		int bit = byteBitReservoir[intBytePos] << intBitPos;
		bit >>= 7;
		bit &= 0x1;
		intBitPos++;
		intBytePos += intBitPos >> 3;
		intBitPos &= 0x7;

		return bit;
	}

	/*
	 * 2 <= n <= 17
	 */
	public int getBits17(int n) {
		int iret = byteBitReservoir[intBytePos];
		iret <<= 8;
		iret |= byteBitReservoir[intBytePos + 1] & 0xff;
		iret <<= 8;
		iret |= byteBitReservoir[intBytePos + 2] & 0xff;
		iret <<= intBitPos;
		iret &= 0xffffff;
		iret >>= 24 - n;
		intBitPos += n;
		intBytePos += intBitPos >> 3;
		intBitPos &= 0x7;
		return iret;
	}

	/*
	 * 2<= n <= 9
	 */
	public int getBits9(int n) {
		int iret = byteBitReservoir[intBytePos];
		iret <<= 8;
		iret |= byteBitReservoir[intBytePos + 1] & 0xff;
		iret <<= intBitPos;
		iret &= 0xffff;
		iret >>= 16 - n;
		intBitPos += n;
		intBytePos += intBitPos >> 3;
		intBitPos &= 0x7;
		return iret;
	}

	/*
	 * 返回值是0-255的无符号整数
	 */
	public int get1Byte() {
		return byteBitReservoir[intBytePos++] & 0xff;
	}

	public void backBits(int n) {
		intBitPos -= n;
		intBytePos += intBitPos >> 3;
		intBitPos &= 0x7;
	}

	public int getBitPos() {
		return intBitPos;
	}

	public int getBytePos() {
		return intBytePos;
	}

	public int getBuffBytes() {
		return intBuffSize;
	}
}
