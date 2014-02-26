package saadadb.query.matchpattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.parser.Operator;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id: Card.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public final class Card{
	private static final Card card0   = newCard0();
	private static final Card card1   = newCard1();
	private static final Card cardAll = newCardAll();
	private final int op  ;
	private final int val1;
	private final int val2;

	public Card(String op,String val1,String val2) throws SaadaException{
		this(Operator.getCode(op),Integer.parseInt(val1),Integer.parseInt(val2));
	}
	protected Card(int op,int val1,int val2) throws SaadaException{
		this.op = op ;
		//Operator.checkCode(op);
		this.val1 = val1;
		this.val2 = val2;
		if(op==Operator.IN || op==Operator.OUT){
			if(val1>val2) QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Error! The first card value \""+val1+"\" is greater than the second value \""+val2+"\"! Change the order!");	
			if(val1==val2)QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Error! The two card values (\""+val1+"\" and \""+val2+"\") are the same! In this case, use \"=\" or \"!=\"");
		}
	}
	private static final Card newCard0(){
		try {return new Card(Operator.EQ,0,0);
		} catch (SaadaException e) {Messenger.printStackTrace(e);
		}return null;
	}
	private static final Card newCard1(){
		try{return new Card(Operator.GT,0,0);
		} catch (SaadaException e) {Messenger.printStackTrace(e);
		}return null;
	}
	private static final Card newCardAll(){
		try {return new Card(Operator.GE,0,0);
		} catch (SaadaException e) {Messenger.printStackTrace(e);
		} return null;
	}
	
	public final int getOp  (){return this.op  ;}
	public final int getVal1(){return this.val1;}
	public final int getVal2(){return this.val2;}
	
	public static final Card getCard0(){return card0;}
	public static final Card getCard1(){return card1;}
	public static final Card getCardAll(){return cardAll;}
	public final boolean card_0(){
		return (this.op==Operator.EQ && this.val1==0)
			|| (this.op==Operator.LE && this.val1== 0)
			|| (this.op==Operator.LT && this.val1== 1);
	}
	public final boolean anyNotNull(){
		return (this.op==Operator.GT && this.val1==0)
			|| (this.op==Operator.GE && this.val1==1)
			|| (this.op==Operator.NE && this.val1==0);
	}
	
	public final String getOpString() throws SaadaException{ return Operator.getString(this.op); }
	
	/**
	 * Return true if the cardinality includes the "= 0" value.
	 * @return
	 */
	public final boolean includeCard0() {
		if( (this.op==Operator.NE    && this.val1 != 0) ||
			(this.op==Operator.LE    && this.val1 >= 0) || 
			(this.op==Operator.LT    && this.val1 > 0)  ||
			(this.op==Operator.GE    && this.val1 <= 0) || 
			(this.op==Operator.GT    && this.val1 < 0)  ||
			(this.op==Operator.OUT   && this.val1 >= 0) || 
			(this.op==Operator.OUT   && this.val2 <= 0) || 
			(this.op==Operator.OUT_S && this.val1 > 0 ) ||
			(this.op==Operator.OUT_S && this.val2 < 0) || 
			(this.op==Operator.IN    && this.val1 <= 0 && this.val2 >= 0)  ||
			(this.op==Operator.IN_S  && this.val1 < 0  && this.val2 > 0) 
			) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		try {
			return "[" + Operator.getString(this.op) + " (" + this.val1 + "," + this.val2 + ")]";
		} catch (SaadaException e) {
			e.printStackTrace();
			return null;
		}
	}
} 
