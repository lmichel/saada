/**
 * 
 */
package saadadb.products;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.Loader;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

/**
 * @author michel
 * @version $Id$
 *
 */
public class DataResourcePointer {
	public final boolean isURL;
	public final String nameFile;
	public final String nameOrg;
	public final File file;


	/**
	 * @param nameFile
	 */
	public DataResourcePointer(String urlOrFileName) throws Exception{
		this.isURL = (urlOrFileName.matches(RegExp.URL))?true: false;
		this.nameOrg = urlOrFileName;
		if( this.isURL ){
			File f;
			do {
				f = new File( Repository.getTmpPath() + File.separator + "datapointer" + (int)(Math.random() * 1000) + ".data");
			} while( f.exists());
			this.nameFile = f.getAbsolutePath();
			Messenger.printMsg(Messenger.TRACE, "Download URL " + urlOrFileName + " to " + f.getName());
			URL url = new URL(urlOrFileName);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			InputStream inputStream = httpConn.getInputStream();
			FileOutputStream outputStream = new FileOutputStream(this.nameFile);
			int bytesRead = -1;
			byte[] buffer = new byte[4096];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}	 
			outputStream.close();
			inputStream.close();

		} else {
			this.nameFile = urlOrFileName;
		}
		this.file = new File(this.nameFile);
	}

	/**
	 * 
	 */
	public void cleanUp() {
		if( this.isURL ){
			Messenger.printMsg(Messenger.TRACE, "delete tempo file " + this.file.getAbsolutePath());
			this.file.delete();
		}
	}

	public String getObjectName(String suffix) {

		String name ;
		if( this.isURL )  {
			String[] ele = this.nameOrg.split("[/\\?]");
			name = ele[ele.length - 1];
		} else {
			name = this.file.getName();
		}

		if( suffix == null ) {
			name = name.trim().replaceAll("'", "");
		} else {
			name = name.trim().replaceAll("'", "") + "_" + suffix;			
		}
		return name;
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] argsZ) throws Exception {
		Database.init("Sqlite9");
		DataResourcePointer dp = new DataResourcePointer("http://xcatdb.unistra.fr/3xmm/download?oid=1442559281411943215");
		System.out.println(dp.getObjectName("eee"));
		dp.cleanUp();
		String[] args = {"-classfusion=http"
				, "-category=misc"
				, "-collection=XMM"
				, "-filename=http://xcatdb.unistra.fr/3xmm/download?oid=1442559281411943215"
				, "Sqlite9"};
		Loader.main(args);
	}

}
