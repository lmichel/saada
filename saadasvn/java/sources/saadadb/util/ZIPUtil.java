package saadadb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.SaadaException;

/**
 * @author laurent
 * @version 07/2011
 */
public class ZIPUtil {
	public static final int MAX_SIZE = 300; //Mb	
	

	public static final String buildZipBall(Map<String, Set<String>> data_tree, String out_filename) throws IOException, SaadaException {
		/*
		 * Build now the zip file
		 */
		String outFilename =  out_filename;
		if( !outFilename.endsWith(".zip") ) {
			outFilename += ".zip";
		}
		Messenger.printMsg(Messenger.TRACE, "Build ZIP file <" + out_filename + ">");
		ZipOutputStream out;
		if( new File(out_filename).isAbsolute() ) {
			out = new ZipOutputStream(new FileOutputStream(outFilename));			
		}
		else {
			out = new ZipOutputStream(new FileOutputStream(Repository.getVoreportsPath()
					+ Database.getSepar() 
					+ outFilename));
		}
		long file_size=0;
		boolean full = false;
		for(String dir: data_tree.keySet()) {
			Set<String> files = data_tree.get(dir);

			for( String fileitem : files) {
				byte[] buf = new byte[1024];
				/*
				 * We suppose that files exist but can be compressed
				 */
				FileInputStream in;
				File f;
				if( (f = new File(fileitem)).exists() ) {
					in = new FileInputStream(fileitem);
					file_size += f.length()/1000000;
				}
				else if( (f = new File(fileitem+ ".gz")).exists()) {
					in = new FileInputStream(fileitem + ".gz");				
					file_size += f.length()/1000000;
				}
				else {
					Messenger.printMsg(Messenger.ERROR, "File <" + fileitem + "> or <" + fileitem + ".gz> not found");
					continue;
				}
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Compress " + fileitem);
				ZipEntry ze = new ZipEntry(dir + "/" + (new File(fileitem)).getName());
				out.putNextEntry(ze);
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.closeEntry();	
				if( file_size > MAX_SIZE) {
					Messenger.printMsg(Messenger.WARNING, "The size of zipped file exceed " + MAX_SIZE + "Mb: zipball truncated");
					full = true;
					break;
				}
			}
			if( full ) {
				break;
			}
		}		

		out.closeEntry();	
		out.close();	
		return Repository.getVoreportsPath()
		+ Database.getSepar() 
		+ outFilename	;
	}

	/**
	 * Gunzip input file if its name ends with .gz
	 * @param input_file
	 * @return : the path of the uncompressed file
	 * @throws Exception
	 */
	public static  String gunzip(String input_file) throws Exception {

		if( input_file.toLowerCase().endsWith(".gz") ) {
			Messenger.printMsg(Messenger.DEBUG, "Gunzip " + input_file);
			InputStream in = new FileInputStream(input_file);
			String outputfile = input_file.substring(0, input_file.length() - 3);
			OutputStream out = new FileOutputStream(outputfile);


			byte[] buffer = new byte[8192];
			in = new GZIPInputStream(in, buffer.length);
			int count = in.read(buffer);
			while (count > 0) {
				out.write(buffer, 0, count);
				count = in.read(buffer);
			}
			in.close();
			out.close();
			return outputfile;
		}
		else {
			return input_file;
		}
	}
}
