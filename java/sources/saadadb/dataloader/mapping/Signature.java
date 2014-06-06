package saadadb.dataloader.mapping;

import saadadb.util.MD5Key;

/**
 * MD5 key builder.
 * Builds to signatures, one name stringSignature a,d the other stringSignatureWithoutColl.
 * They have no specific role, but usually  stringSignature is used for the signature englobing all fields and 
 * stringSignatureWithoutColl for the signature built with business attributes only
 * @author michel
 * @version $Id$
 *
 */
public class Signature {
	private StringBuffer stringSignature = new StringBuffer();
	private StringBuffer stringSignatureWithoutColl = new StringBuffer();
	private String md5Key = null, md5KeyWithoutColl = null;

	/**
	 * Add edlement to the signature
	 * @param element
	 * @param withColl
	 */
	public void addElement(String element, boolean withOutColl) {
		if( withOutColl ) {
			this.stringSignatureWithoutColl.append(element);
		}
		this.stringSignature.append(element);
	}


	/**
	 * @return
	 */
	public String getMd5Key() {
		if( this.md5Key == null ) {
			this.md5Key = MD5Key.calculMD5Key(this.stringSignature.toString());
		}
		return this.md5Key;
	}
	/**
	 * @return
	 */
	public String getMd5KeyWithoutColl() {
		if( this.md5KeyWithoutColl == null ) {
			this.md5KeyWithoutColl = MD5Key.calculMD5Key(this.stringSignatureWithoutColl.toString());
		}
		return this.md5KeyWithoutColl;
	}

}
