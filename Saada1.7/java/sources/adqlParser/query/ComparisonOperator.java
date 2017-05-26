package adqlParser.query;

import adqlParser.parser.ParseException;

public enum ComparisonOperator {
	EQUAL,
	NOT_EQUAL,
	LESS_THAN,
	LESS_OR_EQUAL,
	GREATER_THAN,
	GREATER_OR_EQUAL,
	LIKE,
	NOTLIKE,
	IS,
	ISNOT;
	
	public static ComparisonOperator getOperator(String str) throws ParseException {
		if (str.equalsIgnoreCase("="))
			return EQUAL;
		else if (str.equalsIgnoreCase("!=") || str.equalsIgnoreCase("<>"))
			return NOT_EQUAL;
		else if (str.equalsIgnoreCase("<"))
			return LESS_THAN;
		else if (str.equalsIgnoreCase("<="))
			return LESS_OR_EQUAL;
		else if (str.equalsIgnoreCase(">"))
			return GREATER_THAN;
		else if (str.equalsIgnoreCase(">="))
			return GREATER_OR_EQUAL;
		else if (str.equalsIgnoreCase("LIKE"))
			return LIKE;
		else if (str.equalsIgnoreCase("NOT LIKE"))
			return NOTLIKE;
		else if (str.equalsIgnoreCase("IS"))
			return IS;
		else if (str.equalsIgnoreCase("IS NOT"))
			return ISNOT;
		else
			throw new ParseException("Comparison operator unknown: \""+str+"\" !");
	}
	
	public String toSQL(){
		switch(this){
		case EQUAL:
			return "=";
		case NOT_EQUAL:
			return "<>";
		case LESS_THAN:
			return "<";
		case LESS_OR_EQUAL:
			return "<=";
		case GREATER_THAN:
			return ">";
		case GREATER_OR_EQUAL:
			return ">=";
		case LIKE:
			return "LIKE";
		case NOTLIKE:
			return "NOT LIKE";
		case IS:
			return "IS";
		case ISNOT:
			return "IS NOT";
		default:
			return "???";
		}
	}
	
	public String toString(){
		return toSQL();
	}
}
