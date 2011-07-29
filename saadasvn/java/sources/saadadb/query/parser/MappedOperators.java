package saadadb.query.parser;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

public class MappedOperators {
	//BE CAREFULL! For commodity, byte correspond with the index of the operator int the table!!
	public static final String[] OpTab = {"=",">",">=","<","<=","!=","[=]","]=[","[]","]["};
	public static final String[] RegexTab = {"=",">",">=","<","<=","\\!=","[=]","]=[","[]","]["};

	public static final byte EQ  = 0 ; // =    EQual   (like in FORTRAN)
	public static final byte GT  = 1 ; // >    Greater Than
	public static final byte GE  = 2 ; // >=   Greater or Equal
	public static final byte LT  = 3 ; // <    Less Than
	public static final byte LE  = 4 ; // <=   Less or Equal
	public static final byte NE  = 5 ; // !=   Not Equal
	public static final byte IN  = 6 ; // [=]   IN
	public static final byte OUT = 7 ; // ]=[   OUT
	public static final byte IN_S  = 8 ; // []  IN Strict
	public static final byte OUT_S = 9 ; // ][  OUT Strict

	public static final byte getCode(String op) throws SaadaException{
		for(byte i=0;i<OpTab.length;i++){
			if(op.equals(OpTab[i])) return i;
		}
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Unknow UCD Operator: \""+op+"\" !");
		return 0;
	}
	public static final String getString(byte op)throws SaadaException{
		try{
			return OpTab[op];
		}catch(ArrayIndexOutOfBoundsException e){
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "No UCD Operator associated with the code \""+op+"\" !");
			return null;
		}
	}
	public static final String getString(Integer op)throws SaadaException{
		return getString(op.byteValue());
	}
	
	public static final void checkString(String op) throws SaadaException{
		getCode(op);
	}
	public static final void checkCode(int op) throws SaadaException{
		getString(op);
	}
	/** * @version $Id$

	 * Comparator
	 * @param d
	 * @param op
	 * @param val1
	 * @param val2
	 * @return
	 */
	public static boolean matchValue(double d, int op, double val1, double val2) {
		switch(op) {
			case EQ : return( val1 == d)? true: false;
			case GT : return( d > val1)? true: false;
			case GE : return( d >= val1)? true: false;
			case LT : return( d < val1)? true: false;
			case LE : return( d <= val1)? true: false;
			case NE : return( val1 != d)? true: false;
			case IN : return( d >= val1 && d <= val2)? true: false;
			case OUT: return( d <= val1 || d >= val2)? true: false;
			case IN_S : return( d > val1 && d < val2)? true: false;
			case OUT_S: return( d < val1 || d > val2)? true: false;
			default : return false;
		}

	}
}
