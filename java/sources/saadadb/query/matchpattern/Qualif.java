package saadadb.query.matchpattern;

import java.io.Serializable;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.parser.Operator;


/**
 * @author laurentmichel
 *v * @version $Id: Qualif.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public final class Qualif implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String name;
	private final int    op  ;
	private final double val1;
	private final double val2;

	public Qualif(String name,String op,String val1,String val2) throws SaadaException{
		this(name,Operator.getCode(op),Double.parseDouble(val1),Double.parseDouble(val2));
	} 
	private Qualif(String name,int op,double val1,double val2) throws SaadaException{
		this.name = name;
		this.op   = op  ;
		this.val1 = val1;
		this.val2 = val2;
		if(op==Operator.IN || op==Operator.OUT){
			if(val1>val2)  QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Error! The first qualifier value \""+val1+"\" is greater than the second value \""+val2+"\"! Change the order!");
			if(val1==val2) QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Error! The two qualif values (\""+val1+"\" and \""+val2+"\") are the same! In this case, use \"=\" or \"!=\"");
		}
	}
	
	public final String getName(){return this.name;}
	public final int    getOp  (){return this.op  ;}
	public final double getVal1(){return this.val1;}
	public final double getVal2(){return this.val2;}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		try {
			return "[" + name + " " + Operator.getString(this.op) + " (" + this.val1 + "," + this.val2 + ")]";
		} catch (SaadaException e) {
			e.printStackTrace();
			return null;
		}
	}
	public final String getOpString()throws SaadaException{return Operator.getString(this.op);}
} 
