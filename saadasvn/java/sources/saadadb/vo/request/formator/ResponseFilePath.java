package saadadb.vo.request.formator;

import java.io.File;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * Do some system checking to make formators sure that they can write in requested response files
 * @author laurent
 * @version $Id$

 * 06/2011 creation
 */
public class ResponseFilePath {
	private String responseFilePath = null;;

	/** * @version $Id$

	 * @return
	 */
	public String getResponseFilePath() {
		return responseFilePath;
	}

	/**
	 * @param responseDir
	 * @param filename
	 * @throws Exception
	 */
	public ResponseFilePath(String responseDir, String filename) throws Exception {
		File f = new File(responseDir);
		if( !f.exists() ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Create file " + this.responseFilePath);
			f.mkdir();
		}
		this.responseFilePath = responseDir + Database.getSepar() + filename;
		if( !check()) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Cant not write in file" + this.responseFilePath);			
		}
	}

	/**
	 * @param responseFilePath can either be a filename or path
	 * @throws Exception
	 */
	public ResponseFilePath(String responseFilePath) throws Exception {
		this.responseFilePath = responseFilePath;
		if( !check()) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Cant not write in file" + this.responseFilePath);			
		}
	}

	/**
	 * @return
	 * @throws QueryException 
	 */
	private boolean checkWritableFile() throws QueryException {
		File f = new File(this.responseFilePath);
		if( f.exists() ) {
			if( f.isFile() && f.canWrite() ) {
				return true;
			}
			else {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Cant not write in file" + this.responseFilePath);
			}
		}
		else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Cant not create or access file" + this.responseFilePath);
		}
		return false;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private boolean check() throws Exception {
		File f = new File(this.responseFilePath);
		if( !f.exists() ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Create file " + this.responseFilePath);
			f.createNewFile();
		}
		return checkWritableFile();
	}

}
