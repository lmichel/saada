package saadadb.util;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class TileRiceDecompressor {

	static final public void setInt(byte[] t,int i,int val) {
		t[i]   = (byte)(0xFF & (val>>>24));
		t[i+1] = (byte)(0xFF & (val>>>16));
		t[i+2] = (byte)(0xFF & (val>>>8));
		t[i+3] = (byte)(0xFF &  val);
	}

	static final public int getInt(byte[] t,int i) {
		return ((t[i])<<24) | (((t[i+1])&0xFF)<<16)
		| (((t[i+2])&0xFF)<<8) | (t[i+3])&0xFF;
	}

	static final protected void setPixVal(byte[] t,int bitpix,int i,int c) {
		switch(bitpix) {
		case   8: t[i]=(byte)(0xFF & c);
		break;
		case  16: i*=2;
		t[i]  =(byte)(0xFF & (c>>>8));
		t[i+1]=(byte)(0xFF & c);
		break;
		case  32: i*=4;
		setInt(t,i,c);
		break;
		case -32: i*=4;
		c=Float.floatToIntBits(c);
		setInt(t,i,c);
		break;
		case -64: i*=8;
		long c1 = Double.doubleToLongBits(c);
		c = (int)(0xFFFFFFFFL & (c1>>>32));
		setInt(t,i,c);
		c = (int)(0xFFFFFFFFL & c1);
		setInt(t,i+4,c);
		break;
		}
	}

	public static void decomp(byte buf[],int pos,byte array[], int offset,int nx,int nblock,int bitpix) throws Exception {
		int[]  nonzero_count=null;
		int bsize, i, k, imax;
		int nbits, nzero, fs;
		int b, diff, lastpix;
		int bytevalue;
		int fsmax, fsbits, bbits;

		/*
		 * Original size of each pixel (bsize, bytes) and coding block
		 * size (nblock, pixels)
		 * Could make bsize a parameter to allow more efficient
		 * compression of short & byte images.
		 */
		 bsize = 4;
		/*    nblock = 32; */
		/*
		 * From bsize derive:
		 * FSBITS = # bits required to store FS
		 * FSMAX = maximum value for FS
		 * BBITS = bits/pixel for direct coding
		 */
		 switch (bsize) {
		 case 1:
			 fsbits = 3;
			 fsmax = 6;
			 break;
		 case 2:
			 fsbits = 4;
			 fsmax = 14;
			 break;
		 case 4:
			 fsbits = 5;
			 fsmax = 25;
			 break;
		 default: throw new Exception("Rice.decomp error: bsize must be 1, 2, or 4 bytes");
		 }
		 bbits = 1<<fsbits;

		 if (nonzero_count == null) {
			 /*
			  * nonzero_count is lookup table giving number of bits
			  * in 8-bit values not including leading zeros
			  */
			 nonzero_count = new int[256];
			 nzero = 8;
			 k = 128;
			 for (i=255; i>=0; ) {
				 for ( ; i>=k; i--) nonzero_count[i] = nzero;
				 k = k/2;
				 nzero--;
			 }
		 }

		 /*
		  * Decode in blocks of nblock pixels
		  */

		 /* first 4 bytes of input buffer contain the value of the first */
		 /* 4 byte integer value, without any encoding */

		 //			      lastpix = getInt(buf,pos+=4);
		 lastpix = 0;      
		 bytevalue = 0xFF & buf[pos++];
		 lastpix = lastpix | (bytevalue<<24);
		 bytevalue = 0xFF & buf[pos++];
		 lastpix = lastpix | (bytevalue<<16);
		 bytevalue = 0xFF & buf[pos++];
		 lastpix = lastpix | (bytevalue<<8);
		 bytevalue = 0xFF & buf[pos++];
		 lastpix = lastpix | bytevalue;
		 b = 0xFF & buf[pos++];         /* bit buffer           */
		 nbits = 8;                 /* number of bits remaining in b    */
		 for (i = 0; i<nx; ) {
			 /* get the FS value from first fsbits */
			 nbits -= fsbits;
			 while (nbits < 0) {
				 b = (b<<8) | (0xFF & buf[pos++]);
				 nbits += 8;
			 }

			 fs = (b >>> nbits) - 1;
			 b &= (1<<nbits)-1;
			 /* loop over the next block */
			 imax = i + nblock;
			 if (imax > nx) imax = nx;
			 if (fs<0) {
				 /* low-entropy case, all zero differences */
				 for ( ; i<imax; i++) setPixVal(array,bitpix,i+offset,lastpix);
			 } else if (fs==fsmax) {
				 /* high-entropy case, directly coded pixel values */
				 for ( ; i<imax; i++) {
					 k = bbits - nbits;
					 diff = b<<k;
					 for (k -= 8; k >= 0; k -= 8) {
						 b = 0xFF & buf[pos++];
						 diff |= b<<k;
					 }
					 if (nbits>0) {
						 b = 0xFF & buf[pos++];
						 diff |= b>>>(-k);
					 b &= (1<<nbits)-1;
					 } else {
						 b = 0;
					 }
					 /*
					  * undo mapping and differencing
					  * Note that some of these operations will overflow the
					  * unsigned int arithmetic -- that's OK, it all works
					  * out to give the right answers in the output file.
					  */
					 if ((diff & 1) == 0) {
						 diff = diff>>>1;
					 } else {
						 diff = ~(diff>>>1);
					 }
					 lastpix = diff+lastpix;
					 setPixVal(array,bitpix,i+offset,lastpix);
				 }
			 } else {
				 /* normal case, Rice coding */
				 for ( ; i<imax; i++) {
					 /* count number of leading zeros */
					 while (b == 0) {
						 nbits += 8;
						 b = 0xFF & buf[pos++];
					 }
					 nzero = nbits - nonzero_count[b];
					 nbits -= nzero+1;
					 /* flip the leading one-bit */
					 b ^= 1<<nbits;
					 /* get the FS trailing bits */
					 nbits -= fs;
					 while (nbits < 0) {
						 b = (b<<8) | (0xFF & buf[pos++]);
						 nbits += 8;
					 }
					 diff = (nzero<<fs) | (b>>>nbits);
					 b &= (1<<nbits)-1;
					 /* undo mapping and differencing */
					 if ((diff & 1) == 0) {
						 diff = diff>>>1;
					 } else {
						 diff = ~(diff>>>1);
					 }
					 lastpix = diff+lastpix;
					 setPixVal(array,bitpix,i+offset,lastpix);

				 }
			 }
		 }
	}

}
