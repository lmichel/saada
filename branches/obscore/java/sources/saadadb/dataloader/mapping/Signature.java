package saadadb.dataloader.mapping;

import saadadb.util.MD5Key;

/**
 * @author michel
 * @version $Id$
 *
 */
public class Signature {
	private StringBuffer stringSignature = new StringBuffer();
	private StringBuffer stringSignatureWithoutColl = new StringBuffer();
	private String md5Key = null, md5KeyWithoutColl = null;

	/**
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
