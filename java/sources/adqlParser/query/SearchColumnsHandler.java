package adqlParser.query;

public class SearchColumnsHandler implements SearchHandler {
	
	public boolean match(ADQLObject obj) {
		return (obj instanceof ADQLColumn);
	}

}
