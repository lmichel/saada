package saadadb.vo.formator;

import java.io.FileOutputStream;
import java.util.LinkedHashMap;

import saadadb.collection.EntrySaada;
import saadadb.collection.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.util.Messenger;
import saadadb.vo.translator.ConeSearchTranslator;

/**
 * @author michel
 *
 */
public class ConeSearchToFITSFormator extends FITSFormator {
	
	/**
	 * Constructor.
	 * @throws SaadaException 
	 */
	public ConeSearchToFITSFormator(String voresource_name) throws SaadaException {
		super(voresource_name, "Cone Search default", "Saada Cone Search service", "dal:SimpleQueryResponse", "Cone Search search result on SaadaDB");
	}

	/**
	 * @param voresource_name
	 * @param result_filename
	 * @throws SaadaException
	 */
	public ConeSearchToFITSFormator(String voresource_name, String result_filename) throws SaadaException {
		super(voresource_name, "Cone Search default", "Saada Cone Search service", "dal:SimpleQueryResponse", "Cone Search search result on SaadaDB", result_filename);
	}

	/**
	 * @param oid
	 * @throws Exception
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
		 * If using a DM, only UTYPE and then UCDS are considered
		 */
		else {
			int pos = 0;
			for( UTypeHandler sf: this.column_set) {
				Object data_column =  this.data[pos];
				String ucd   = sf.getUcd();
				String utype = sf.getUtype();
				String name  = sf.getNickname();
				if( ucd.equals("Target.Pos")) {
					((String[])data_column)[current_line] = obj.getPos_ra_csa() + " " + obj.getPos_dec_csa();
				}
				else if( utype.equals("Char.SpatialAxis.Coverage.Location.Value")) {
					((String[])data_column)[current_line] = obj.getPos_ra_csa() + " " + obj.getPos_dec_csa();
				}
				else if( ucd.equals("VOX:Image_AccessReference")) {
					((String[])data_column)[current_line] = Database.getUrl_root() + "/getinstance?oid=" + obj.getOid();
				}
				else if( utype.equals("Access.Format")) {
					((String[])data_column)[current_line] = "catalog";
				}
				else if( utype.equals("DataID.Title") || ucd.equalsIgnoreCase("VOX:Image_Title") ) {
					((String[])data_column)[current_line] = obj.getNameSaada().replaceAll("#", "");
				}
				else if( name.equals("LinktoPixels")) {
					((String[])data_column)[current_line] = obj.getURL(true);
				}
				else if( ucd.equalsIgnoreCase("POS_EQ_RA_MAIN") ){
					((double[])data_column)[current_line] = (double)obj.getPos_ra_csa();
				}
				else if( ucd.equalsIgnoreCase("POS_EQ_DEC_MAIN") ){
					((double[])data_column)[current_line] = (double)obj.getPos_dec_csa();
				}
				else if( ucd.equalsIgnoreCase("ID_MAIN") ){
					/*
					 * ID_MAIN is declared as String in the DM file
					 */
					((String[])data_column)[current_line] = String.valueOf(obj.getOid());
				}
				else if( ucd.equalsIgnoreCase("meta.title") ){
					((String[])data_column)[current_line] = obj.getNameSaada();
				}
				/*
				 * Utypes have an higher priority than UCDs: there are checked first
				 */
				else if( utype != null && utype.length() > 0 ){
					AttributeHandler ah  = obj.getFieldByUtype(sf.getUtype(), false);
					if( ah == null ) {
						((Object[])data_column)[current_line] = "";					
					}
					else {
						Object val = obj.getFieldValue(ah.getNameattr());
						if( ah.getType().equals("String")) {
							((Object[])data_column)[current_line] = val;
						}
						else {
							((Object[])data_column)[current_line] = val;						
						}
					}	
				}
				else if( ucd != null && ucd.length() > 0 ){
					AttributeHandler ah  = obj.getFieldByUCD(sf.getUcd(), false);
					if( ah == null ) {
						((Object[])data_column)[current_line] = "";					
					}
					else {
						Object val = obj.getFieldValue(ah.getNameattr());
						if( ah.getType().equals("String")) {
							((Object[])data_column)[current_line] = val;
						}
						else {
							((Object[])data_column)[current_line] = val;						
						}
					}
				}
				if( sf.getType().equals("String") && ((String[])data_column)[current_line].length() == 0 ) {
					((Object[])data_column)[current_line] = "Not Set";
				}
				pos++;
			}
		}		
	}
	

	/**
	 * @param args
	 * @throws FatalException
	 */
	public static void main(String[] args) throws FatalException {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		LinkedHashMap<String, String> hand_params = new LinkedHashMap<String, String>();
		Messenger.debug_mode = false;
		hand_params.put("SR", "0.02");
		//	 hand_params.put("withrel", "true");	 
		hand_params.put("RA", "40.5 42.74");
		hand_params.put("collection", "any");
		try {
			(new ConeSearchToFITSFormator("native entry")).processVOQuery(new ConeSearchTranslator(hand_params)
			                                                           , new FileOutputStream("/home/michel/Desktop/cs.fits"));
//			BufferedInputStream br = new BufferedInputStream(new FileInputStream(rf));
//			BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("/home/michel/Desktop/cs.fits"));
//			int lg;
//			byte[] bf = new byte[1024];
//			while( (lg = br.read(bf)) > 0  ) {
//				bw.write(bf, 0, lg);
//			}
//			bw.close();
//			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}   
  
}
