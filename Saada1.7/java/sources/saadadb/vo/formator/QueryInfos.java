package saadadb.vo.formator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import saadadb.collection.Category;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/**@version $Id$
 * Contains all the information about a SaadaQL query that the QueryFormater needs to know 
 */
public class QueryInfos {
    
    private int category = Category.UNKNOWN;
    
    // Target of the query : select * from target
    public static final int N_COLL = 1, ONE_COLL_N_CLASS = 2, ONE_COLL_ONE_CLASS = 3;
    private int queryTarget;
    // This field is not empty when queryTarget = ONE_COLL_ONE_CLASS;
    private String className = null;
    private String InputSaadaTable = null;
   
    private ArrayList<String> selectAttributes; // of Strings
    private ArrayList<String> orderAttributes; // of Strings . Not implemented yet
    private int topRestriction;
    private boolean extensionAllowed = false;
    private String saadaqlQuery="";
    private int mode;
    public static final int CUTOUT=1;
    public static final int POINTED=2;
    public static final int MOSAIC=3;
    private double[] size = new double[2]; // size in degree
	/*
     * Query params a copied here to make a report
     */
    LinkedHashMap<String, String> params;
    String url="";
    /**
	 * @return the saadaqlQuery
	 */
	public String getSaadaqlQuery() {
		return saadaqlQuery;
	}

	/**
	 * @param saadaqlQuery the saadaqlQuery to set
	 * @throws FatalException 
	 */
	public void setSaadaqlQuery(String saadaqlQuery) throws SaadaException {
		this.saadaqlQuery = saadaqlQuery;
		for( String cat: Category.NAMES ) {
			Pattern p = Pattern.compile("Select\\s*" + cat + "\\s+.*", Pattern.DOTALL);
			if( p.matcher(saadaqlQuery).matches() ) {
				this.category = Category.getCategory(cat);
				return;
			}
		}
		QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Cannot extract category from query: " + saadaqlQuery);	
	}

    /**
     * 
     */
    public QueryInfos() {
    	selectAttributes = new ArrayList<String>();
    	orderAttributes = new ArrayList<String>();
    }
    
    public void setInputSaadaTable(String coll) {
        this.InputSaadaTable = coll;
        }
        
    public String getInputSaadaTable() {
        return this.InputSaadaTable;
        }
        
    public void setExtensionAllowed(boolean flag) {
        this.extensionAllowed = flag;
        }
    public boolean isExtensionAllowed() {
        return this.extensionAllowed;
        }
    public void setCategory(int type) {
	this.category = type;
    }
    
    public int getCategory() {
	return category;
    }
    
    public void setClassName(String name) {
	this.className = name;
    }
    
    public String getClassName() {
	return className;
    }
    
    public void setQueryTarget(int target) {
	this.queryTarget = target;
    }
    
    public int getQueryTarget() {
	return queryTarget;
    }
    
    public void addSelectAttribute(String selectAttribute) {
	selectAttributes.add(selectAttribute);
    }
    
    public ArrayList<String> getSelectAttributes() {
	return selectAttributes;
    }
    
    public void resetSelectAttributes() {
	selectAttributes = new ArrayList<String>();
    }
    
    public void addOrderAttribute(String orderAttribute) {
	this.orderAttributes.add(orderAttribute);
    }
    
    public ArrayList<String> getOrderAttributes() {
	return orderAttributes;
    }
    
    public void setTopRestriction(int nb) {
	topRestriction = nb;
    }
    
    public int getTopRestriction() {
	return topRestriction;
    }
    
    public String toString() {
    	String result = "";
    	//result += "\n  Order by attributes : " + orderAttributes + " ]";

    	if( !"".equals(this.url) ) {
    		result += "url=" + this.url + "\n ";
    	}
    	if( !"".equals(this.saadaqlQuery) ) {
    		result += "query=" + this.saadaqlQuery + "\n ";
    	}
    	if( params != null ) {
    		for( Entry e : params.entrySet()) {
    			result += e.getKey() + "=" + e.getValue() + "\n ";
    		}
    	}
    	return result;
    }

	/**
	 * @param params
	 */
	public void setParams(LinkedHashMap<String, String> params) {
		this.params = params;	
	}
    
	public void setUrl(String url ) {
		this.url = url;		
	}
	
	/**
	 * 
	 */
	public void setCutoutMode() {
		this.mode = CUTOUT;
	}
	/**
	 * 
	 */
	public void setMosaicMode() {
		this.mode = MOSAIC;
	}
	/**
	 * 
	 */
	public void setPointedMode() {
		this.mode = POINTED;
	}
	/**
	 * @return
	 */
	public boolean isCutoutMode() {
		if( this.mode == CUTOUT ) {
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * @return
	 */
	public boolean isPointedMode() {
		if( this.mode == POINTED ) {
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * @return
	 */
	public boolean isMosaicMode() {
		if( this.mode == MOSAIC ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * @param size
	 */
	public void setSize(double size_ra, double size_dec){
		this.size[0] = size_ra;
		this.size[1] = size_dec;
	}
	/**
	 * @param size_radec
	 */
	public void setSize(double size_radec){
		this.size[0] = size_radec;
		this.size[1] = size_radec;
	}
	/**
	 * @return
	 */
	public double getSizeRA(){
		return this.size[0];
	}
	/**
	 * @return
	 */
	public double getSizeDEC(){
		return this.size[1];
	}
}
  
