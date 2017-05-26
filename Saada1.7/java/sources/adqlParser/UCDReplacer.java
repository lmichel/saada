package adqlParser;

import java.util.Vector;

import saadadb.meta.AttributeHandler;
import adqlParser.parser.ParseException;
import adqlParser.query.ADQLComparison;
import adqlParser.query.ADQLConstantValue;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLTable;
import adqlParser.query.ADQLType;
import adqlParser.query.ComparisonOperator;
import adqlParser.query.SearchHandler;

public class UCDReplacer implements SearchHandler {

	protected final String wantedUCD;
	protected final String wantedTable;
	
	protected final String columnPrefix;
	protected final String columnName;
	protected final AttributeHandler columnMeta;
	
	public UCDReplacer(String ucd, String table, String prefix, String column, AttributeHandler meta){
		wantedUCD = ucd;
		wantedTable = table;
		columnPrefix = prefix;
		columnName = column;
		columnMeta = meta;
	}
	
	public boolean match(ADQLObject obj) {
		if (obj instanceof UCDFunction){
			UCDFunction matchedItem = (UCDFunction)obj;
			if (matchedItem.getUCD().equalsIgnoreCase(wantedUCD) && matchedItem.getTable().equalsIgnoreCase(wantedTable)){
				try{
					ADQLColumnAndMeta column = new ADQLColumnAndMeta(columnName, columnPrefix);
					column.setMeta(columnMeta);
					matchedItem.setColumn(column);
				}catch(ParseException pe){System.out.println("WARNING: "+pe.getMessage());}
				return true;
			}
		}
		return false;
	}
	
	/*
	 * !!! TEST !!!
	 */
	public static void main(String[] args) throws Exception {
		SaadaADQLQuery query = new SaadaADQLQuery();
		
		query.addSelectColumn(new UCDFunction(new ADQLOperand[]{new ADQLConstantValue("ObsCore"), new ADQLConstantValue("em.wl")}));
		query.addSelectColumn(new UCDFunction(new ADQLOperand[]{new ADQLConstantValue("EPIC"), new ADQLConstantValue("time.duration")}));
		query.addSelectColumn(new UCDFunction(new ADQLOperand[]{new ADQLConstantValue("ObsCore"), new ADQLConstantValue("time.duration")}));
		
		query.addTable(new ADQLTable("ObsCore"));
		query.addTable(new ADQLTable("EPIC"));
		
		query.addConstraint(new ADQLComparison(new UCDFunction("EPIC", "em.wl", "um"), ComparisonOperator.GREATER_THAN, new ADQLConstantValue("123", ADQLType.INTEGER)));
		query.addConstraint(new ADQLComparison(new UCDFunction("ObsCore", "em.wl", "um"), ComparisonOperator.GREATER_THAN, new ADQLConstantValue("456", ADQLType.INTEGER)));
		query.addConstraint(new ADQLComparison(new UCDFunction("EPIC", "time.duration", "none"), ComparisonOperator.GREATER_THAN, new ADQLConstantValue("0", ADQLType.INTEGER)));
		
		System.out.println("***** QUERY (in ADQL) *****\n"+query+"\n");
		
		System.out.print("Looking for UCDs...");
		Vector<ADQLObject> vMatched = query.getAll(new UCDReplacer("em.wl", "ObsCore", "ObsCore", "wavelength", null));
		if (vMatched.size() > 0){
			System.out.println("OK !");
			for(ADQLObject item : vMatched)
				System.out.println("\t- "+item+" (in SQL: "+item.toSQL()+")");
			System.out.println("");
		}else
			System.out.println("NO MATCH !");
	}
	
}
