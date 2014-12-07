/**
 * 
 */
package saadadb.products.datafile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map.Entry;

import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.Loader;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.util.RegExpMatcher;
import saadadb.vocabulary.RegExp;

/**
 * Class handling a data node which can either be an URL or a file.
 * If it is an URL, is is downloaded in the repository to be processed later as a simple file.
 * In this case, the repository name used by the download facility remains the original url.  
 * @author michel
 * @version $Id$
 *
 */
//  https://drive.google.com/uc?export=download&id=0B0sArvYXpLlAaFllSExndDBCblU
//  https://drive.google.com/file/d/0B0sArvYXpLlAaFllSExndDBCblU/view?usp=sharing
//
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
		this.inputFileName = getDirectGoogleDownloadLink(urlOrFileName);
		/*
		 * URL mode: the file is downloaded in the repository
		 */
		if( this.isURL ){
			File f=null;
			/*
			 * Get the reference of the file which will be given to the data loader
			 */
			//this.localFileName = f.getAbsolutePath();
			//this.file = new File(this.localFileName);
			/*
			 * Avoid https issues
			 */
			System.setProperty("jsse.enableSNIExtension", "false");
			/*
			 * Open the http connection
			 */
			URL url = new URL(this.inputFileName);
			URLConnection httpConn =  url.openConnection();
			InputStream inputStream = null;
			/*
			 * Try to get the file name. Get the last uRL path ele. otherwise
			 */
			String in = null;
			try {
				String raw = httpConn.getHeaderField("Content-Disposition");
				RegExpMatcher rem = new RegExpMatcher(".*filename=([^;]*).*", -1);
				List<String> sm = rem.getMatchesAndMore(raw);
				if(sm != null && sm.size() > 0) {
					raw = sm.get(0);
					in = raw.replaceAll("['\\\"]", "");
					f = new File( Repository.getTmpPath() + File.separator + in);
				} else {
					String[] ele = this.inputFileName.split("[/\\?]");
					in = ele[ele.length - 1];
					do {
						f = new File( Repository.getTmpPath() + File.separator + "datapointer" + (int)(Math.random() * 1000) + ".data");
					} while( f.exists());
				}	
				inputStream = httpConn.getInputStream();
			} catch( Exception e){
				Messenger.printStackTrace(e);
				String[] ele = this.inputFileName.split("[/\\?]");
				in = ele[ele.length - 1];
				IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, e);
			}
			this.file = f;
			this.instanceName = f.getName();
			/*
			 * Copy the file content
			 */
			this.localFileName = f.getAbsolutePath();
			FileOutputStream outputStream = new FileOutputStream(f);
			Messenger.printMsg(Messenger.TRACE, "Download URL " + urlOrFileName + " to " + this.localFileName);
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

	public String getCanonicalPath() throws IOException {
		return this.file.getCanonicalPath();
	}
	public String getAbsolutePath() {
		return this.file.getAbsolutePath();
	}
	public String getParent() {
		return  this.file.getParent();
	}
	public long length() {
		return this.file.length();
	}
	public boolean delete() {
		return this.file.delete();
	}
	public String getName() {
		return this.file.getName();
	}


	//  https://drive.google.com/uc?export=download&id=0B0sArvYXpLlAaFllSExndDBCblU
	//  https://drive.google.com/file/d/0B0sArvYXpLlAaFllSExndDBCblU/view?usp=sharing
	//
	/**
	 * Goole Drive URLS are redirected to a preview page. To skip it we have to take file id  out
	 * of the urls and to build a new url for z direct donwload
	 * Ex:	
	 *  file URL given par the GD share page: https://drive.google.com/file/d/0B0sArvYXpLlAaFllSExndDBCblU/view?usp=sharing
	 *  Direct donwload URL https://drive.google.com/uc?export=download&id=0B0sArvYXpLlAaFllSExndDBCblU
	 * THat supposed that the file is publicly available for people having that URL
	 * @param urlOrFileName
	 * @return
	 */
	public static String getDirectGoogleDownloadLink(String urlOrFileName){
		RegExpMatcher rem = new RegExpMatcher("https://drive.google.com/file/d/([^/]*)/.*", -1);
		List<String> sm = rem.getMatchesAndMore(urlOrFileName);
		if(sm != null && sm.size() > 0) {
			String retour = "https://drive.google.com/uc?export=download&id=" + sm.get(0);
			Messenger.printMsg(Messenger.TRACE, "Take " + retour + " as direct GDrive download URL");
			return retour;
		}else {
			return urlOrFileName;
		}

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
