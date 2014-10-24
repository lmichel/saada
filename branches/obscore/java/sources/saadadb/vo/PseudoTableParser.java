package saadadb.vo;

import java.util.ArrayList;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.vocabulary.RegExp;

/**
 * Tranforms a pseudotable VO in a list of collection and a list of classes
 * @author michel
 * @version 07/2011
 */
public class PseudoTableParser {
	ArrayList<String> collections = new ArrayList<String>();;
	ArrayList<String> classes = new ArrayList<String>();
	String pseudotable;

	/** * @version $Id$

	 * @param pseudotable
	 * @throws QueryException
	 */
	public PseudoTableParser(String pseudotable) throws QueryException {
		if( pseudotable == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Null pseudotable ");								
		}
		this.pseudotable = pseudotable; 
		if( "any".equalsIgnoreCase(pseudotable) || "*".equalsIgnoreCase(pseudotable)) {
			collections.add("*");
			classes.add("*");
		}
		else if( pseudotable.matches(RegExp.COLLNAME)) {
			collections.add(pseudotable);
			classes.add("*");			
		}
		else if( pseudotable.matches("\\[" + RegExp.COLLNAME + "(," + RegExp.COLLNAME + ")*\\]" ) ){
			String grp[] = pseudotable.replaceAll("[\\[\\]]", "").split(",");
			for(String s: grp) {
				collections.add(s.trim());				
			}
			classes.add("*");	
		}
		else if( pseudotable.matches( "\\[(" + RegExp.COLLNAME + ")\\((" + RegExp.CLASSNAME + ")(?:,(" + RegExp.CLASSNAME + "))*\\)\\]") ){
			String grp[] = pseudotable.replaceAll("[\\[\\]]", "").split("\\(");
			collections.add(grp[0].trim());	
			grp = grp[1].trim().replace(")", "").split(",");
			for(String s: grp) {
				classes.add(s.trim());				
			}
		}
		else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Can not interpret pseudotable " +pseudotable );					
		}

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String retour = "pseudotable " + pseudotable + ": Collection(s) ";
		for( String c: collections) {
			retour += " " + c;
		}
		retour += "  Classex(s) ";
		for( String c: classes) {
			retour += " " + c;
		}
		return retour;
	}
	
	/**
	 * @return
	 */
	public String[] getCollections() {
		return collections.toArray(new String[0]);
	}
	/**
	 * @return
	 */
	public String[] getclasses() {
		return classes.toArray(new String[0]);
	}
	public static void main(String[] args) {
		try {
			PseudoTableParser ptp = new PseudoTableParser("ASSASA");
			System.out.println(ptp);
			ptp = new PseudoTableParser("[ASSASA]");
			System.out.println(ptp);
			ptp = new PseudoTableParser("[ASSASA,dadasda]");
			System.out.println(ptp);
			ptp = new PseudoTableParser("[ASSASA(aaa)]");
			System.out.println(ptp);
			ptp = new PseudoTableParser("[ASSASA(aaa,asdasd,asdassd)]");
			System.out.println(ptp);
			ptp = new PseudoTableParser("ASSASA)");
			System.out.println(ptp);
		} catch (QueryException e) {
			e.printStackTrace();
		}
	}
}
