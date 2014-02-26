package adqlParser.query;

public class ColumnSearchHandler implements SearchHandler {

	protected static ColumnSearchHandler currentInstance = new ColumnSearchHandler();
	
	public static ColumnSearchHandler getInstance(){
		return currentInstance;
	}
	
	public boolean match(ADQLObject obj) {
		return (obj instanceof ADQLColumn);
	}

}
