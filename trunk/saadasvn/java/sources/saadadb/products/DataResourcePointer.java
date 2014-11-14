/**
 * 
 */
package saadadb.products;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.Loader;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

/**
 * Class handling a data node which can either be an URL or a file.
 * If it is an URL, is is downloaded in the repository to be processed later as a simple file.
 * In this case, the repository name used by the download facility remains the original url.  
 * @author michel
 * @version $Id$
 *
 */
public class DataResourcePointer {
	/**
	 * Flagging the url mode
	 */
	public final boolean isURL;
	/**
	 * Name of the file really processed: either the original file name 
	 * or the name of the file downloaded from the URL
	 */
	public final String localFileName;
	/**
	 * Full name given to the constructor. Can be an URL or a full path name
	 */
	public final String inputFileName;
	/**
	 * Name used by default for the instance. can be the last element of the URL, 
	 * the name give by the "content-disposition", or the name of the file 
	 */
	public final String instanceName;
	/**
	 * File instance handled by the data loader 
	 */
	public final File file;


	/**
	 * @param localFileName
	 */
	public DataResourcePointer(String urlOrFileName) throws Exception{
		this.isURL = (urlOrFileName.matches(RegExp.URL))?true: false;
		/*
		 * Keep the input name, mainly used for logging purpose
		 */
		this.inputFileName = urlOrFileName;
		/*
		 * URL mode: the file is downloaded in the repository
		 */
		if( this.isURL ){
			File f;
			do {
				f = new File( Repository.getTmpPath() + File.separator + "datapointer" + (int)(Math.random() * 1000) + ".data");
			} while( f.exists());
			/*
			 * Get the reference of the file which will be given to the data loader
			 */
			this.localFileName = f.getAbsolutePath();
			this.file = new File(this.localFileName);
			Messenger.printMsg(Messenger.TRACE, "Download URL " + urlOrFileName + " to " + f.getName());
			/*
			 * Avoid https issues
			 */
			System.setProperty("jsse.enableSNIExtension", "false");
			/*
			 * Open the http connection
			 */
			URL url = new URL(urlOrFileName);
			URLConnection httpConn =  url.openConnection();
			InputStream inputStream = null;
			/*
			 * Try to get the file name. Get the last uRL path ele. otherwise
			 */
			String in = null;
			try {
				String raw = httpConn.getHeaderField("Content-Disposition");
				if(raw != null && raw.indexOf("=") != -1) {
					in = raw.split("=")[1].replaceAll("['\\\"]", "");
				} else {
					String[] ele = this.inputFileName.split("[/\\?]");
					in = ele[ele.length - 1];
				}			
				inputStream = httpConn.getInputStream();
			} catch( Exception e){
				String[] ele = this.inputFileName.split("[/\\?]");
				in = ele[ele.length - 1];
				IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, e);
			}
			this.instanceName = in;
			/*
			 * Copy the file content
			 */
			FileOutputStream outputStream = new FileOutputStream(this.localFileName);
			int bytesRead = -1;
			byte[] buffer = new byte[4096];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}	 
			outputStream.close();
			inputStream.close();

		} else {
			/*
			 * Just keep references of the data files
			 */
			this.localFileName = urlOrFileName.replaceAll("['\\\"]", "").trim();
			this.file = new File(this.localFileName);
			this.instanceName = this.file.getName();
		}
	}

	/**
	 * Remove the file resulting from the URL download
	 */
	public void cleanUp() {
		if( this.isURL ){
			Messenger.printMsg(Messenger.TRACE, "delete tempo file " + this.file.getAbsolutePath());
			this.file.delete();
		}
	}

	/**
	 * @param suffix to be appended to the name
	 * @return return the default Saada instance name
	 */
	public String getObjectName(String suffix) {
		String name ;
		if( suffix == null ) {
			name = this.instanceName;
		} else {
			name = this.instanceName + "_" + suffix;			
		}
		return name;
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] argsZ) throws Exception {
		Database.init("Sqlite9");
		DataResourcePointer dp;
		dp = new DataResourcePointer("http://xcatdb.unistra.fr/3xmm/download?oid=1442559281411943215");
		System.out.println(dp.getObjectName("eee"));
		dp.cleanUp();

//		dp = new DataResourcePointer("https://cadac-demo.sdsc.edu/vospace-2.0/data/f53b6edb-5e1a-4836-bd59-54bedaf85d2b");
//		System.out.println(dp.getObjectName("eee"));
//		dp.cleanUp();
//
//		System.exit(1);
		String[] args = {"-classfusion=http"
				, "-category=misc"
				, "-collection=XMM"
				, "-filename=http://xcatdb.unistra.fr/3xmm/download?oid=1442559281411943215"
				, "Sqlite9"};
		Loader.main(args);
	}

}
