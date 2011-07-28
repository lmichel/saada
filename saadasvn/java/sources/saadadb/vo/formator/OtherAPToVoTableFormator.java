package saadadb.vo.formator;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;

/**
 * Build a VOTable for a query which is neither a SIAP, nor a SSA and nor a CS
 * @author michel
 *
 */
public class OtherAPToVoTableFormator extends VOTableFormator {
	/**
	 * Constructor.
	 * @throws Exception 
	 */
	public OtherAPToVoTableFormator(int category) throws Exception {
		super("native " + Category.explain(category), "Saada Result service", "web:SimpleQueryResponse", "Query Result on SaadaDB " + Database.getName());
	}
	/**
	 * @param result_filename
	 * @throws Exception
	 */
	public OtherAPToVoTableFormator(int category, String resource_name) throws Exception {
		super(resource_name, "native " + Category.explain(category),"Saada Result service", "web:SimpleQueryResponse", "Query Result on SaadaDB " + Database.getName());
	}

	/**
	 * @param result_filename
	 * @throws Exception
	 */
	public OtherAPToVoTableFormator(int category, String resource_name, String result_filename) throws Exception {
		super(resource_name, "native " + Category.explain(category), "Saada Result service", "web:SimpleQueryResponse", "Query Result on SaadaDB " + Database.getName(), result_filename);
	}
	@Override
	protected void writeProtocolParamDescription() {
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setDataType("char");
		param.setArraySize("*");
		param.setName("INPUT:query");
		param.setDescription("SaadaQL Query");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:limit");
		param.setDataType("int");
		param.setUnit("deg");
		param.setDescription("Max size of the result set");

		writer.writeParam(paramSet);		
	}

	@Override
	protected void writeDMData(SaadaInstance obj) throws Exception {
		for( Object f: fieldSet_dm.getItems()) {
			SavotField sf = (SavotField)f;
			String id = sf.getId();
			if( id.length() == 0 ) {
				id = sf.getName();
			}
			Object val;
			String colname = sf.getName();
			try {
				/*
				 * Columns with names starting with ucd_ denote values returned by UCD based queries
				 * The real value must then be retrieved in the business object
				 */
				if( colname.startsWith("ucd_")) {
					val = Database.getCache().getObject(obj.getOid()).getFieldValueByUCD(sf.getUcd(), false);
				}
				else {
					val = obj.getFieldValue(colname);
				}
			} catch(Exception e) {
				e.printStackTrace();
				val = "";
			}
			if( val == null ) val = "";
			if( sf.getDataType().equals("char")) {
				addCDataTD(val.toString());
			}
			else {
				addTD(val.toString());						
			}
		}

	}
	
	public static void main(String[] args) {
		Database.init("DEVBENCH1_5");
		try {
			OtherAPToVoTableFormator o = new OtherAPToVoTableFormator(Category.IMAGE);
			o.setLimit(2);
			o.processVOQueryInStreaming("Select IMAGE From Aldebaran010 In Collection0 Order By namesaada", System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}