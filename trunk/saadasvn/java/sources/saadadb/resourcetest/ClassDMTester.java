package saadadb.resourcetest;

import java.sql.SQLException;
import java.util.LinkedHashMap;

import saadadb.collection.SaadaInstance;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.DMInterface;
import saadadb.query.result.SaadaQLResultSet;


/**
 * @author laurentmichel
 * * @version $Id$

 */
public class ClassDMTester extends SaadaInstance {

	@Override
	public String getProduct_url_csa() {
		return null;
	}

	@Override
	public void setProduct_url_csa(String name) {
		
	}

    // Class generated by Saada
    public class _ssa_default implements DMInterface {
        // Code generated by Saada
        public String getDMName()  throws FatalException{ return "_ssa_default";}
        public Object getDMFieldValue(String utype_or_nickname) {return null;}
        // Code generated by Saada
        public String getSQLField(String utype_or_nickname) {
            if( utype_or_nickname == null ) {
              return "null";
            }
            else if(utype_or_nickname.equals("Access.Format") || utype_or_nickname.equals("Access_For") ) {
              return " _c1t1c1_sau";
            }
            else if(utype_or_nickname.equals("Dataset.DataModel") || utype_or_nickname.equals("Dataset_Dat") ) {
              return " _c1t1c1_sasu";
            }
            else if(utype_or_nickname.equals("Dataset.Length") || utype_or_nickname.equals("Dataset_Len") ) {
              return " _c1t1c1_sansu";
            }
            else if(utype_or_nickname.equals("DataID.Title") || utype_or_nickname.equals("DataID_Tit") ) {
              return " _c1t1c1_nsu";
            }
            else if(utype_or_nickname.equals("Curation.Publisher") || utype_or_nickname.equals("Curation_Pub") ) {
              return "null";
            }
            else if(utype_or_nickname.equals("Char.SpatialAxis.Coverage.Location.Value") || utype_or_nickname.equals("Char_SpaCovLocVal") ) {
              return "null";
            }
            else if(utype_or_nickname.equals("Char.SpatialAxis.Coverage.Bounds.Extent") || utype_or_nickname.equals("Char_SpaCovBouExt") ) {
              return "null";
            }
            else if(utype_or_nickname.equals("Char.SpectralAxis.Coverage.Location.Value") || utype_or_nickname.equals("Char_SpeCovLocVal") ) {
              return "null";
            }
            else if(utype_or_nickname.equals("Char.SpectralAxis.Coverage.Bounds.Extent") || utype_or_nickname.equals("Char_SpeCovBouExt") ) {
              return "null";
            }
            else if(utype_or_nickname.equals("Char.TimeAxis.Coverage.Location.Value") || utype_or_nickname.equals("Char_TimCovLocVal") ) {
              return "null";
            }
            else  {
              return "null";
            }
        }
        public String getSQLAlias(String utype_or_nickname) {return null;};
        // Code generated by Saada
        public  LinkedHashMap<String, String> getSQLFields()  throws FatalException{
            LinkedHashMap<String, String> retour = new LinkedHashMap<String, String>();
            retour.put("Access_For", " _c1t1c1_sau");
            retour.put("Dataset_Dat", " _c1t1c1_sasu");
            retour.put("Dataset_Len", " _c1t1c1_sansu");
            retour.put("DataID_Tit", " _c1t1c1_nsu");
            retour.put("Curation_Pub", "null");
            retour.put("Char_SpaCovLocVal", "null");
            retour.put("Char_SpaCovBouExt", "null");
            retour.put("Char_SpeCovLocVal", "null");
            retour.put("Char_SpeCovBouExt", "null");
            retour.put("Char_TimCovLocVal", "null");
            return  retour;
        }
        // Code generated by Saada
        public Object getFieldValue(String utype_or_nickname, SaadaQLResultSet rs)  throws FatalException{
        	try {
				return rs.getObject(utype_or_nickname);
			} catch (SQLException e) {
				FatalException.throwNewException(SaadaException.DB_ERROR, e);
				return null;
			}
        }
    }

    public static void main(String[] args) {
    	ClassDMTester cdmt = new ClassDMTester();
    	try {
			cdmt.activateDataModel("_ssa_default");
			System.out.println(cdmt.getSQLField("Access.Format"));
			LinkedHashMap<String, String> fs = cdmt.getSQLFields();
			for( String k: fs.keySet()) {
				System.out.println(k + " " + fs.get(k));
			}
			cdmt.desactivateDataModel();
			System.out.println(cdmt.getSQLField("Access.Format"));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
