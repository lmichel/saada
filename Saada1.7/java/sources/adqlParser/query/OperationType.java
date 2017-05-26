package adqlParser.query;

import adqlParser.parser.ParseException;

public enum OperationType {
	SUM, SUB, MULT, DIV;
	
	public static OperationType getOperator(String str) throws ParseException {
		if (str.equalsIgnoreCase("+"))
			return SUM;
		else if (str.equalsIgnoreCase("-"))
			return SUB;
		else if (str.equalsIgnoreCase("*"))
			return MULT;
		else if (str.equalsIgnoreCase("/"))
			return DIV;
		else
			throw new ParseException("Numeric operation unknown: \""+str+"\" !");
	}
	
	public String toSQL(){
		return toString();
	}
	
	public String toString(){
		switch(this){
		case SUM:
			return "+";
		case SUB:
			return "-";
		case MULT:
			return "*";
		case DIV:
			return "/";
		default:
			return "???";
		}
	}
}
