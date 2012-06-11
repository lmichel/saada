/**
 * 
 */
package saadadb.meta;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * Handle the range of a column.
 * The range can either be defined by a min/max value or by a lost of possible values
 * @author michel
 *
 */
public class RangeValue {
	private Object min;
	private Object max;
	private ArrayList<Object> valList;
	private boolean isEnum;
	
	public void setMin(Object obj ){
		this.min = obj;
		this.isEnum = false;
	}
	public void setMax(Object obj ){
		this.max = obj;
		this.isEnum = false;
	}
	public void addToList(Object obj) {
		if( this.valList == null ) {
			this.valList = new ArrayList<Object>();
		}
		this.valList.add(obj);
		this.isEnum = true;
	}
	public Object getMin() {
		return min;
	}
	public Object getMax() {
		return max;
	}
	public ArrayList<Object> getValList() {
		return valList;
	}
	public boolean isEnum() {
		return isEnum;
	}
	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject retour = new JSONObject();
		if( this.isEnum ) {
			JSONArray jsa = new JSONArray();
			for( Object o: this.valList ) {
				jsa.add("(" + o + ") possible value");
			}
			retour.put("values", jsa);
		}
		else {
			retour.put("min", "(" + this.min + ") Min value in the DB");
			retour.put("max", "(" + this.max + ") Max value in the DB");
		}
		return retour;
	}
}
