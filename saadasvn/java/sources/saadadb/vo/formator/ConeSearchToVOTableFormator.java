package saadadb.vo.formator;

import saadadb.collection.EntrySaada;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import cds.savot.model.OptionSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotOption;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotValues;

/**
 * Get the result of a Cone Search query on Saada as a list of OIDs, and 
 * produces a VOTable XML file containing the results.
 */
public class ConeSearchToVOTableFormator extends VOTableFormator {

	/**
	 * Constructor.
	 * @throws SaadaException 
	 */
	public ConeSearchToVOTableFormator(String voresource_name) throws SaadaException {
		super(voresource_name, "Cone Search default", "Saada Cone Search service", "dal:SimpleQueryResponse", "Cone Search search result on SaadaDB");
	}

	/**
	 * @param voresource_name
	 * @param result_filename
	 * @throws SaadaException
	 */
	public ConeSearchToVOTableFormator(String voresource_name, String result_filename) throws SaadaException {
		super(voresource_name, "Cone Search default", "Saada Cone Search service", "dal:SimpleQueryResponse", "Cone Search search result on SaadaDB", result_filename);
	}

	@Override
	protected void writeProtocolParamDescription() {
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setName("INPUT:RA");
		param.setDescription("Search position right ascension in the ICRS coordinate system");
		paramSet.addItem(param);
		param = new SavotParam();
		param.setName("INPUT:DEC");
		param.setDescription("Search position right ascension in the ICRS coordinate system");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:SR");
		param.setDescription("Size of search regions in the Ra and Dec directions");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:FORMAT");
		param.setValue("ALL");
		param.setDescription("Requested format of results");
		SavotValues formatValues = new SavotValues();
		OptionSet optionSet = new OptionSet();
		SavotOption option = new SavotOption();
		option.setValue("fits");
		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("votable");
		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("METADATA");
		optionSet.addItem(option);
		formatValues.setOptions(optionSet);
		param.setValues(formatValues);
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:collection");
		param.setDescription("Gives the collections and possibly the classes in which we want to search. The format is as follows : collection=coll1,coll2 OR collection=coll1[class1, class2]");
		param.setValue("any");
		SavotValues svs = new SavotValues();
		OptionSet os = new OptionSet();
		option = new SavotOption(); option.setValue("any");
		os.addItem(option);
		for( String coll : Database.getCachemeta().getCollection_names() ) {
			option = new SavotOption(); 
			option.setValue(coll);
			os.addItem(option);
		}
		svs.setOptions(os);
		param.setValues(svs);
		paramSet.addItem(param);

		writer.writeParam(paramSet);
		}


	/* (non-Javadoc)
	 * @see saadadb.vo.VOTableFormator#writeDMData(long)
	 */
	protected void writeDMData(SaadaInstance si) throws Exception {
		EntrySaada obj = (EntrySaada)( si) ;
		/*
		 * In native mode we just take attribute values (see superclass)
		 */
		if( this.vo_resource.isNative_mode() ) {
			writeNativeValues(obj);
		}
		/*
		 * If using a DM, only UTYPE and then UCDS ar considered
		 */
		else {
			for( Object f: fieldSet_dm.getItems()) {
				SavotField sf = (SavotField)f;
				String ucd   = sf.getUcd();
				String utype = sf.getUtype();
				String name  = sf.getName();
				String id = sf.getId();
				if( ucd.equals("Target.Pos")) {
					addTD(obj.getPos_ra_csa() + " " + obj.getPos_dec_csa());
				}
				else if( utype.equals("Char.SpatialAxis.Coverage.Location.Value")) {
					addTD(obj.getPos_ra_csa() + " " + obj.getPos_dec_csa());
				}
				else if( ucd.equals("VOX:Image_AccessReference")) {
					addCDataTD(Database.getUrl_root() + "/getinstance?oid=" + obj.getOid());
				}
				else if( utype.equals("Access.Format")) {
					addCDataTD("catalog");
				}
				else if( utype.equals("DataID.Title") || ucd.equalsIgnoreCase("VOX:Image_Title") ) {
					addCDataTD(obj.getNameSaada().replaceAll("#", ""));
				}
				else if( id.equals("LinktoPixels")) {
					addCDataTD(obj.getURL(true));
				}
				else if( ucd.equalsIgnoreCase("POS_EQ_RA_MAIN") ){
					addTD(Double.toString(obj.getPos_ra_csa()));
				}
				else if( ucd.equalsIgnoreCase("POS_EQ_DEC_MAIN") ){
					addTD(Double.toString(obj.getPos_dec_csa()));
				}
				else if( ucd.equalsIgnoreCase("ID_MAIN") ){
					addTD(Long.toString(obj.getOid()));
				}
				else if( ucd.equalsIgnoreCase("meta.title") ){
					addTD(obj.getNameSaada());
				}
				/*
				 * Utypes have an higher priority than UCDs: there are checked first
				 */
				else if( utype != null && utype.length() > 0 ){
					AttributeHandler ah  = obj.getFieldByUtype(sf.getUtype(), false);
					if( ah == null ) {
						addTD("");					
					}
					else {
						Object val = obj.getFieldValue(ah.getNameattr());
						if( ah.getType().equals("String")) {
							addCDataTD(val.toString());
						}
						else {
							addTD(val.toString());						
						}
					}	
				}
				else if( ucd != null && ucd.length() > 0 ){
					AttributeHandler ah  = obj.getFieldByUCD(sf.getUcd(), false);
					if( ah == null ) {
						addTD("");					
					}
					else {
						Object val = obj.getFieldValue(ah.getNameattr());
						if( ah.getType().equals("String")) {
							addCDataTD(val.toString());
						}
						else {
							addTD(val.toString());						
						}
					}
				}
			}
		}
	}

	public  static void main(String[] args ) throws SaadaException, Exception {
		Database.init("BENCH2_0_PSQL");				
		(new ConeSearchToVOTableFormator("native entry")).processVOQuery(new long[]{577586729519677441L}, System.out);

		
	}
}

