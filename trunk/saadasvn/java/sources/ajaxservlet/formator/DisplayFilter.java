package ajaxservlet.formator;

import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.query.result.OidsaadaResultSet;

/*
 * Presentation interface for SaadaInstances
 * This interface could be enriched with an external configuration allowing users
 * to define themselves their display format
 * It returns meta data without regard on the JSON formatting
 */
public interface DisplayFilter {
	
	/**
	 * Set the oid of the object to process
	 * @param oid
	 * @throws FatalException 
	 */
	public void setOId(long oidsaada) throws FatalException;
	
	/**
	 * Set the metaclass it has to work on. Used when
	 * there is no specific oid
	 * @param mc
	 * @throws FatalException 
	 */
	public void setMetaClass(MetaClass mc) throws FatalException;

	/**
	 * Can be used to build data from a resultset instead of SaadaInstances
	 * @param resultSet
	 */
	public void setResultSet(OidsaadaResultSet resultSet);

	/**
	 * Add a column named bu an UCD
	 * @param ah
	 */
	public void addUCDColumn(AttributeHandler ah) ;

	/**
	 * Return the names of the columns to be displayed in the result table
	 * Does not need to match any attribute. 
	 * A column can be a merge of attributes or a URL (DL link e.g.)
	 * @return
	 */
	public Set<String> getDisplayedColumns() ;
	
	/**
	 * Return the list of attribute handlers proposed by the query form
	 * @param mc
	 * @return (in json format)
	 * @throws FatalException 
	 */
	public Set<AttributeHandler> getQueriableColumns() throws FatalException ;
	
	/**
	 * Return yes if ah can be queried
	 * @param ah
	 * @return
	 */
	public  boolean valid(AttributeHandler ah) ;


	/**
	 * Return one data row for a  result table having the columns returned 
	 * by @see getDisplayedColumns
	 * @param obj for any purpose
	 * @param rank 
	 * @return
	 * @throws SaadaException 
	 */
	public  List<String> getRow(Object obj, int rank) throws Exception;
	
	/**
	 * Returns a Json array populating a data table with class level attributes 
	 * Table Columns are definitely set as KW/value/unit/comment
	 * @param oidsaada
	 * @return
	 * @throws Exception
	 */
	public JSONArray getClassKWTable() throws Exception;
	
	/**
	 * Returns a Json array populating a data table with collection level attributes .
	 * This consist only in atomic values. Composite values as returned by @see getDisplayedColumns
	 * are ignored here.
	 * Table Columns are definitely set as KW/value/unit/comment	 
	 * @param oidsaada
	 * @return
	 * @throws Exception
	 */
	public JSONArray getCollectionKWTable() throws Exception;
	
	/**
	 * Returns the page title
	 * @return
	 */
	public String getTitle() ;
	
	/**
	 * return a list of things to be displayed under the tile e.g.
	 * @return
	 */
	public List<String> getLinks() ;
}
