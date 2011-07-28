package adqlParser.query;

public enum JoinType {
	INNER,
	OUTER_LEFT,
	OUTER_RIGHT,
	OUTER_FULL;
	
	public String toSQL(){
		return toString();
	}
	
	public String toString(){
		switch(this){
		case INNER:
			return "INNER";
		case OUTER_LEFT:
			return "LEFT OUTER";
		case OUTER_RIGHT:
			return "RIGHT OUTER";
		case OUTER_FULL:
			return "FULL OUTER";
		default:
			return name();
		}
	}
}
