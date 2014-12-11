package saadadb.products.reporting;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.meta.AttributeHandler;
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
		this.entryBuilder.elements();
		this.entryBuilder.dataFile.hasMoreElements();
		return new MappingReport(this.entryBuilder).getReport();
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#getReport()
	 */
	@Override
	public Map<String, ColumnSetter> getReport() throws Exception {
		System.out.println( entryBuilder.hashCode() + " " + entryBuilder.productAttributeHandler.hashCode());
		for( Entry<String, AttributeHandler> eah: entryBuilder.productAttributeHandler.entrySet()){
			System.out.println("getReport1 " + eah);
		}
		Map<String, ColumnSetter> retour = super.getReport();
		for( Entry<String, AttributeHandler> eah: entryBuilder.productAttributeHandler.entrySet()){
			System.out.println("getReport2 " + eah);
		}
		Map<String, ColumnSetter> em = this.getEntryReport();
		for( Entry<String, ColumnSetter>e: em.entrySet()){
			retour.put("entry." + e.getKey(), e.getValue());
		}
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
	
	protected void wrriteColumnReport(FileWriter fw) throws IOException {
		int cpt=1;
		for( Entry<String, AttributeHandler> eah: entryBuilder.productAttributeHandler.entrySet()){
			fw.write("COLUMN " + cpt+ " " + eah + "\n");
			cpt++;
		}
		
	}

}
