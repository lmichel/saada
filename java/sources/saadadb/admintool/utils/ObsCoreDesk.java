/**
 * 
 */
package saadadb.admintool.utils;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.meta.MetaClass;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.query.parser.UnitHandler;
import saadadb.vo.registry.Authority;

/**
 * @author michel
 * @version $Id$
 *
 */
public class ObsCoreDesk {

	public static String getDefaultFieldMapping(String fieldName, MetaClass metaClass) throws Exception{
		if( metaClass == null ) {
			return "";
		}
		int category =  metaClass.getCategory();
		Authority auth = Authority.getInstance();
		if( "dataproduct_type".equalsIgnoreCase(fieldName)  ) {
			return getConstant(Category.explain(metaClass.getCategory())) ;
		} else if( "s_ra".equalsIgnoreCase(fieldName)  ) {
			if( category == Category.IMAGE || category == Category.SPECTRUM  || category == Category.ENTRY) {
				return "s_ra";
			} else {
				return "";
			}
		} else if( "s_dec".equalsIgnoreCase(fieldName)  ) {
			if( category == Category.IMAGE || category == Category.SPECTRUM  || category == Category.ENTRY) {
				return "s_dec";
			} else {
				return "";
			}
		} else if( "s_region".equalsIgnoreCase(fieldName)  ) {
			if( category == Category.IMAGE  ) {				
				return Database.getWrapper().getStrcatOp(
				  "'POLYGON " + Database.getCooSys() + "' "
				, " " +  Database.getWrapper().castToString("(s_ra - size_alpha_csa/2)"), " " + Database.getWrapper().castToString("(s_dec - size_delta_csa/2)") 
				, " " +  Database.getWrapper().castToString("(s_ra - size_alpha_csa/2)"), " " + Database.getWrapper().castToString("(s_dec + size_delta_csa/2)") 
				, " " +  Database.getWrapper().castToString("(s_ra + size_alpha_csa/2)"), " " + Database.getWrapper().castToString("(s_dec + size_delta_csa/2)") 
				, " " +  Database.getWrapper().castToString("(s_ra + size_alpha_csa/2)"), " " + Database.getWrapper().castToString("(s_dec - size_delta_csa/2)")) ;
			} else if( category == Category.SPECTRUM ||  category == Category.ENTRY ) {
				return Database.getWrapper().getStrcatOp(
						  "'POSITION " + Database.getCooSys() + "' "
						, " " +  Database.getWrapper().castToString("s_ra"), " " + Database.getWrapper().castToString("s_dec"));
			} else {
				return "";
			}
		} else if( "s_ra_min".equalsIgnoreCase(fieldName)  ) {
			if(category == Category.IMAGE ) {
				return "s_ra - size_alpha_csa/2";
			} else {
				return "";
			}
		} else if( "s_ra_max".equalsIgnoreCase(fieldName)  ) {
			if(category == Category.IMAGE ) {
				return "s_ra + size_alpha_csa/2";
			} else {
				return "";
			}
		} else if( "s_dec_min".equalsIgnoreCase(fieldName)  ) {
			if(category == Category.IMAGE ) {
				return "s_dec - size_delta_csa/2";
			} else {
				return "";
			}
		} else if( "s_dec_max".equalsIgnoreCase(fieldName)  ) {
			if( category == Category.IMAGE ) {
				return "s_dec + size_delta_csa/2";
			} else {
				return "";
			}
		} else if( "s_pixel_size".equalsIgnoreCase(fieldName)  ) {
			if(category == Category.IMAGE || category == Category.SPECTRUM) {
				return "(crpix1_csa + crpix1_csa)/2";
			} else {
				return "";
			}
		} else if( "obs_collection_name".equalsIgnoreCase(fieldName)  ) {
			return getConstant(metaClass.getCollection_name() + "_" + Category.explain(metaClass.getCategory())) ;
		} else if( "obs_creator_name".equalsIgnoreCase(fieldName)  ) {
			return getConstant(auth.getAuthShortName());
		} else if( "obs_publisher_did".equalsIgnoreCase(fieldName)  ) {
			return Database.getWrapper().getStrcatOp(
					getConstant(auth.getAuthIdentifier() + "#") 
					, metaClass.getName() + ".oidsaada");
		} else if( "publisher_name".equalsIgnoreCase(fieldName)  ) {
			return getConstant(auth.getCurationName());
		} else if( "publisher_id".equalsIgnoreCase(fieldName)  ) {
			return getConstant(auth.getCurationPublisher());
		} else if( "data_rights".equalsIgnoreCase(fieldName)  ) {
			return "'public'";
		} else if( "access_url".equalsIgnoreCase(fieldName)  ) {
			if( category != Category.ENTRY ) {
				return Database.getWrapper().getStrcatOp(getConstant(Database.getUrl_root() + "/download?oid="),  metaClass.getName() + ".oidsaada" );
			} else {
				return "";
			}			
		} else if( "em_min".equalsIgnoreCase(fieldName)  ) {
			if( category == Category.SPECTRUM ) {
				if( Database.getSpect_type().equals("CHANNEL") ) {
					return "x_min_csa";					
				} else if( Database.getSpect_type().equals("WAVELENGTH") ) {
					return UnitHandler.getConvFunction("m", Database.getSpect_unit(), "x_min_csa");					
				} else {
					double coef = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", 1.);
					return coef + "/x_max_csa";					
				}
			} else {
				return "";
			}
		} else if( "em_max".equalsIgnoreCase(fieldName)  ) {
			if( category == Category.SPECTRUM ) {
				if( Database.getSpect_type().equals("CHANNEL") ) {
					return "x_min_csa";					
				} else if( Database.getSpect_type().equals("WAVELENGTH") ) {
					return UnitHandler.getConvFunction("m", Database.getSpect_unit(), "x_max_csa");					
				} else {
					double coef = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", 1.);
					return coef + "/x_min_csa";					
				}
			} else {
				return "";
			}
		} else return "";
	}

	public static String getConstant(String val) {
		return "'" + val + "'" ;

	}
}
