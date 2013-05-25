/**
 * 
 */
package saadadb.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.parser.PositionParser;
import cds.astro.Astroframe;

/**
 * Manage a list of position for multi-position queries
 * @author michel
 * 
 */
public class PositionList {
	private ArrayList<Double> ras = new ArrayList<Double>();
	private ArrayList<Double> decs= new ArrayList<Double>();
	public static final int MAX_POSTIONS = 100;
	
	/**
	 * Constructor used by single-position queries
	 * @param position
	 * @param astroFrame
	 * @param single  : dummy paramters used to make a difference between the 2 constructors
	 * @throws QueryException
	 */
	public PositionList(String position, Astroframe astroFrame, boolean single) throws QueryException{
		PositionParser pp = new PositionParser(position, astroFrame);
		this.addPos(pp.getRa(), pp.getDec());	
	}
	
	/**
	 * Constructor used to read a position list
	 * @param filename
	 * @param astroFrame
	 * @throws QueryException
	 */
	public PositionList(String filename, Astroframe astroFrame) throws QueryException{
		int line_num = 0;
		BufferedReader bf = null;
		try {
			if( filename.indexOf(Database.getSepar()) == -1  ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "File " + filename + " searched in voreport dir");
				filename = Database.getVOreportDir() + Database.getSepar() + filename;
			}
			Messenger.printMsg(Messenger.TRACE, "Build position list from file " + filename);

			bf = new BufferedReader(new FileReader(filename));
			String line;
			while( (line = bf.readLine()) != null ) {
				line = line.trim();
				line_num++;
				if( line.startsWith("#") || line.length() == 0 ) {
					continue;
				}
				PositionParser pp = new PositionParser(line, astroFrame);
				this.addPos(pp.getRa(), pp.getDec());
				if( this.size() >= MAX_POSTIONS ) {
					Messenger.printMsg(Messenger.TRACE, "Build position truncated to  " + MAX_POSTIONS);
					break;
				}
			}
			bf.close();
		} catch(SaadaException e){
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.FILE_FORMAT, "Error at line " + line_num);
		} catch(Exception e){
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.FILE_FORMAT, "Error at line " + line_num);
		} finally {
			if( bf != null )
				try {
					bf.close();
				} catch (IOException e) {}
			
		}
	}
	private void addPos(double ra, double dec){
		ras.add(ra);
		decs.add(dec);
	}
	public int size() {
		return ras.size();
	}		
	public double getRa(int pos) {
		return ras.get(pos);
	}
	public double getDec(int pos) {
		return decs.get(pos);
	}
}


