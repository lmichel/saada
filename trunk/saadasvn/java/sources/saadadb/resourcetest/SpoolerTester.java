package saadadb.resourcetest;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import saadadb.database.Database;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.database.spooler.Spooler;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

/**
 * Test the jdbC Spooler. Tests are set to run on the XcatDB (hardcoded)
 * The idea is to launch a burst of thread up to the saturation of the spooler.
 * Note that using the same query or changing randomly the positions does n ot affect the results
 * @author michel
 * @version $Id$
 *
 */
public class SpoolerTester {
	public static final String q = "SELECT oidsaada, ACDS_ENTRY.pos_ra_csa as coord_pos_ra_csa, ACDS_ENTRY.pos_dec_csa as coord_pos_dec_csa "
		+ " FROM ACDS_ENTRY "
		+ "  WHERE ("
		+ " ( (abs( degrees((2*asin( sqrt( (@1-ACDS_ENTRY.pos_x_csa)*(@1-ACDS_ENTRY.pos_x_csa)+(@2-ACDS_ENTRY.pos_y_csa)*(@2-ACDS_ENTRY.pos_y_csa)+(0.5099412522922848-ACDS_ENTRY.pos_z_csa)*(0.5099412522922848-ACDS_ENTRY.pos_z_csa) )/2))))< 0.016666666666666666))) ";
	private boolean[] completed;
	public static final int SMALL=0;
	public static final int LARGE=1;
	public static final int MIXTE=2;

	SpoolerTester(int nbThreads) {
		this.completed = new boolean[nbThreads];
		for( int i=0 ; i<this.completed.length ; i++ ) this.completed[i]= false;
	}
	
	public String getQuery() {
		double ra  = Math.random();
		double dec = Math.random();
		String retour = q.replaceAll("@1", Double.toString(ra)).replaceAll("@2", Double.toString(dec));
		//System.out.println(retour);
		return retour;
	}
	/*
	 * Runnable emulating a connection the the database
	 */
	/**
	 * Do a test on small quries (connection are not closed
	 * @author michel
	 * @version $Id$
	 */
	class ClientEmulator implements Runnable {
		private int num;
		public ClientEmulator(int num) {
			this.num =  num;
		}
		@Override
		public void run() {
			try {
				Spooler spooler = Spooler.getSpooler();
				DatabaseConnection connectionReference = spooler.getConnection();
				if( connectionReference == null ) {
					System.out.println("No connection");
				} else {
					Statement stmt = connectionReference.getStatement();
					stmt.execute(getQuery());
					spooler.give(connectionReference);
				}	
				completed[num] = true;
			} catch (Exception e) {
				System.out.println("Thread " + num + " " + completed[num]);
				e.printStackTrace();
				System.exit(1);
			}
		}	
	}
	/**
	 * Do a test on large queries (connection are closed after use)
	 * @author michel
	 * @version $Id$
	 */
	class LargeClientEmulator implements Runnable {
		private int num;
		public LargeClientEmulator(int num) {
			this.num =  num;
		}
		@Override
		public void run() {
			try {
				Spooler spooler = Spooler.getSpooler();
				DatabaseConnection connectionReference = spooler.getConnection();
				if( connectionReference == null ) {
					System.out.println("No connection");
				} else {
					Statement stmt = connectionReference.getLargeStatement();
					stmt.execute(getQuery());
					spooler.give(connectionReference);
				}	
				completed[num] = true;
			} catch (Exception e) {
				System.out.println("Thread " + num + " " + completed[num]);
				e.printStackTrace();
				System.exit(1);
			}
		}	
	}
	/**
	 * Do a test on a mix of smal and large queries (connection are closed after use once on 10)
	 * @author michel
	 * @version $Id$
	 */
	class MixteClientEmulator implements Runnable {
		private int num;
		public MixteClientEmulator(int num) {
			this.num =  num;
		}
		@Override
		public void run() {
			try {
				Spooler spooler = Spooler.getSpooler();
				DatabaseConnection connectionReference = spooler.getConnection();
				if( connectionReference == null ) {
					System.out.println("No connection");
				} else {
					int proba = (int)(Math.random()*10);
					Statement stmt;
					if( proba == 3 ) {
						stmt = connectionReference.getLargeStatement();
					} else {
						stmt = connectionReference.getStatement();
					}
					stmt.execute(getQuery());
					spooler.give(connectionReference);
				}	
				completed[num] = true;
			} catch (Exception e) {
				System.out.println("Thread " + num + " " + completed[num]);
				e.printStackTrace();
				System.exit(1);
			}
		}	
	}

	/**
	 * Stores the result on one set of test
	 * @author michel
	 * @version $Id$
	 */
	class Result {
		int nbQueries;
		double eplased;
		long perQuery;
		public Result(int nbQueries, double eplased, long perQuery) {
			super();
			this.nbQueries = nbQueries;
			this.eplased = eplased;
			this.perQuery = perQuery;
		}
		public String toString(){
			return "[" + this.nbQueries + " " + this.eplased + " " + this.perQuery + "]";
		}

	}
	
	/**
	 * Starts a thread test on small mode
	 * @param num
	 */
	public void emulate(int num) {
		ClientEmulator clientEmulator = new ClientEmulator(num);
		(new Thread(clientEmulator)).start();
	}
	/**
	 * Starts a thread test on large mode
	 * @param num
	 */
	public void emulateLarge(int num) {
		LargeClientEmulator clientEmulator = new LargeClientEmulator(num);
		(new Thread(clientEmulator)).start();
	}
	/**
	 * Starts a thread test on mixed mode
	 * @param num
	 */
	public void emulateMixte(int num) {
		MixteClientEmulator clientEmulator = new MixteClientEmulator(num);
		(new Thread(clientEmulator)).start();
	}

	/**
	 * Run a complete test in one mode
	 * @param mode
	 * @return
	 * @throws InterruptedException
	 */
	public Result test(int mode, int nbThreads) throws InterruptedException {
		long t0 = (new Date()).getTime();
		for( int i=0 ; i<nbThreads ; i++ ){
			switch( mode ) {
			case SMALL: this.emulate(i);break;
			case LARGE: this.emulateLarge(i);break;
			case MIXTE: this.emulateMixte(i);break;
			}
		}
		while( true ) {
			boolean foundWorking = false;
			for( int j=0 ; j<this.completed.length ; j++) {
				if( !this.completed[j] ) {
					foundWorking = true;
					break;
				}
			}
			if( foundWorking ) Thread.sleep(30); 	
			else break;
		}
		long elapsed = (new Date()).getTime() - t0;
		long perQ = elapsed/nbThreads;
		return new Result(nbThreads, elapsed/1000.0, perQ);
	}

	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws Exception {
		Database.init("ThreeXMM");
		Messenger.debug_mode = true;
		LinkedHashMap<Integer, List<Result>> results = new LinkedHashMap<Integer, List<Result>>();
		int spoolerSize[] = {1,2,5,10 ,20,30};
		int nbQueries[] = {10,100, 1000, 10000};
		for(int ss: spoolerSize){
			System.out.println("======= Working with " + ss + " connections");
			List<Result> tmpr = new ArrayList<Result>();
			results.put(ss, tmpr);
			for( int nt: nbQueries) {
				/*
				 * limit the number ot thread..
				 */
				if( nt/ss < 5000 ) {
					System.out.println("    === Working with " + nt + " queries ");
					Spooler.getSpooler(ss);	
					SpoolerTester st = new SpoolerTester(nt);
					tmpr.add(st.test(LARGE, nt));
					Spooler.getSpooler().close();
					Spooler.reset();
				}
			}
		}

		for( Entry<Integer, List<Result>> e: results.entrySet()) {
			System.out.print(e.getKey()  + " connections\t");
			for( Result r: e.getValue() ){
				System.out.print(r);
			}
			System.out.println("");

		}
	}

}
