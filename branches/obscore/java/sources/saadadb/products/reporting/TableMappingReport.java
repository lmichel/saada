package saadadb.products.reporting;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.products.EntryBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.setter.ColumnSetter;

/**
 * Class taking in charge the reporting about the table mapping
 * Purpose: splitting the builder code 
 * @author michel
 * @version $Id$
 */
public class TableMappingReport extends MappingReport{
	public EntryBuilder entryBuilder;
	
	/**
	 * @param builder
	 */
	public TableMappingReport(TableBuilder builder){
		super(builder);
		this.entryBuilder = builder.entryBuilder;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#getEntryReport()
	 */
	@Override
	public Map<String, ColumnSetter> getEntryReport() throws Exception {
		return new MappingReport(this.entryBuilder).getReport();
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#getReport()
	 */
	@Override
	public Map<String, ColumnSetter> getReport() throws Exception {
		Map<String, ColumnSetter> retour = new LinkedHashMap<String, ColumnSetter>();
		retour = super.getReport();
		retour.putAll(new MappingReport(this.entryBuilder).getReport());
		return retour;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#printReport()
	 */
	@Override
	public void printReport() throws Exception {
		super.printReport();
		new MappingReport(this.entryBuilder).printReport();
	}
}
