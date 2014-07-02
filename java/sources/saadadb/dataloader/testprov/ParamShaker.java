package saadadb.dataloader.testprov;

import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class ParamShaker {
	protected static String TEMPLATE ;
	protected static JSONObject jsonObject;
	
	ParamShaker() {
		JSONParser parser = new JSONParser();  
		try {
			jsonObject = (JSONObject)parser.parse(TEMPLATE);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}  
	}
	
	
	/**
	 * Priority first, good mapped parameters
	 */
	private void runFirstWithGoodMParams(){
		
	}
	/**
	 * Priority first, good inferred parameters
	 */
	private void runFirstWithGoodIParams(){
		
	}
	/**
	 * Priority first, wrong mapped parameters
	 */
	private void runFirstWithWrongMParams(){
		
	}
	/**
	 * Priority first, wrong inferred parameters
	 */
	private void runFirstWithWrongIParams(){
		
	}
	/**
	 * Priority first, partially wrong mapped parameters
	 */
	private void runFirstWithPWrongMParams(){
		
	}
	/**
	 * Priority first, partially wrong inferred parameters
	 */
	private void runFirstWithPWrongIParams(){
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
