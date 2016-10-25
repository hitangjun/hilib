/**
* Layer1.java -- MPEG 1.0 Audio Layer I Decoder
*
*/
package com.hitangjun.music.mp3.decoder;

public final class Layer1 implements ILayer123 {
	Header header;
	BitStream bs;
	Synthesis filter;
	float[] factor;
	byte[][] allocation;	//[2][32]
	byte[][] scalefactor;	//[2][32]
	float[][] syin;			//[2][32]

	public Layer1(BitStream bitstream,Header h, Synthesis filter, int wch) {
		header = h;
		bs = bitstream;
		this.filter = filter;
		allocation = new byte[2][32];
		scalefactor = new byte[2][32];
		syin = new float[2][32];
		factor = Layer2.factor;		// ISO/IEC 11172-3 Table 3-B.1.
	}

	/*
	 * 逆量化公式:
	 * s'' = (2^nb / (2^nb - 1)) * (s''' + 2^(-nb + 1))
	 * s' = factor * s''
	 */
	float requantization(int ch, int sb, int nb) {
		int samplecode = bs.getBits17(nb);
		int nlevels = (1 << nb);
		float requ = 2.0f * samplecode / nlevels - 1.0f;	//s'''
		requ += (float)Math.pow(2, 1-nb);
		requ *= nlevels / (nlevels - 1);		//s''
		requ *= factor[scalefactor[ch][sb]];	//s'
		return requ;
	}

	public void decodeFrame() throws Exception {
		int sb, gr, ch, nb;
		int nch = header.getChannels();
		int bound = (header.getMode() == 1) ? ((header.getModeExtension() + 1) * 4) : 32;
		int slots = header.getMainDataSlots();
		bs.append(slots);
		int maindata_begin = bs.getBytePos();

		//1. Bit allocation decoding
		for (sb = 0; sb < bound; sb++)
			for (ch = 0; ch < nch; ++ch) {
				nb = bs.getBits9(4);
				if (nb == 15)
					throw new Exception("decodeFrame()->nb=15");
				allocation[ch][sb] = (byte)((nb != 0) ? (nb + 1) : 0);
			}
		for (sb = bound; sb < 32; sb++) {
			nb = bs.getBits9(4);
			if (nb == 15)
				throw new Exception("decodeFrame()->nb=15");
			allocation[0][sb] = (byte)((nb != 0) ? (nb + 1) : 0);
		}

		//2. Scalefactor decoding
		for (sb = 0; sb < 32; sb++)
			for (ch = 0; ch < nch; ch++)
				if (allocation[ch][sb] != 0)
					scalefactor[ch][sb] = (byte)bs.getBits9(6);

		for (gr = 0; gr < 12; gr++) {
			//3. Requantization of subband samples
			for (sb = 0; sb < bound; sb++)
				for (ch = 0; ch < nch; ch++){
					nb = allocation[ch][sb];
					if(nb == 0)
						syin[ch][sb] = 0;
					else
						syin[ch][sb] = requantization(ch, sb, nb);
				}
			//mode=1(Joint Stereo)
			for (sb = bound; sb < 32; ++sb)
				if ((nb = allocation[0][sb]) != 0)
					for (ch = 0; ch < nch; ++ch)
						syin[ch][sb] = requantization(ch, sb, nb);
				else
					for (ch = 0; ch < nch; ++ch)
						syin[ch][sb] = 0;
			
			//4. Synthesis subband filter
			for (ch = 0; ch < nch; ch++)
				filter.synthesisSubBand(syin[ch], ch);
		}

		//5. Ancillary bits
		int discard = slots + maindata_begin - bs.getBytePos();
		bs.skipBytes(discard);
	}
}
