/**
 * 
 */
package saadadb.products.mergeandcast;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.database.Database;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.EntryBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.datafile.VOTableDataFile;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/**
 * Take in charge the operations of data format fusion and data value downcasting
 * @author michel
 * @version $Id$
 */
public class ClassMerger {
	ProductBuilder productBuilder;


	public ClassMerger(ProductBuilder productBuilder) {
		this.productBuilder = productBuilder;
	}

	/**
	 * Modify the data format of the productBuilder managed by the current instance in order to make it compliant
	 * with the data format of dataFileToMerge
	 * @param dataFileToMerge
	 * @throws Exception
	 */
	public void mergeProductFormat(DataFile dataFileToMerge) throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Merge format with file <" + dataFileToMerge.getName() + ">");

		/*
		 * Build a new set of attribute handlers from the product given as a parameter
		 */
		ProductBuilder prd_to_merge = this.productBuilder.mapping.getNewProductBuilderInstance(dataFileToMerge, this.productBuilder.metaClass);
		prd_to_merge.mapping = this.productBuilder.mapping;

		try {
			prd_to_merge.dataFile = new FitsDataFile(prd_to_merge);		
			this.productBuilder.typeFile = "FITS";
		} catch(Exception ef) {
			try {
				prd_to_merge.dataFile = new VOTableDataFile(prd_to_merge);
				this.productBuilder.typeFile = "VO";
			} catch(Exception ev) {
				IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "<" + dataFileToMerge + "> neither FITS nor VOTable");			
			}
		}
		this.mergeAttributeHandlers(prd_to_merge.getProductAttributeHandler());
		prd_to_merge.close();
		/*
		 * Beeuuurk
		 */
		if( this.productBuilder instanceof TableBuilder ){
			TableBuilder tb = (TableBuilder) this.productBuilder;
			Map<String, AttributeHandler> tableAttributeHandler_org;
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Merge ENTRY format with file <" + dataFileToMerge.getName() + ">");
			tableAttributeHandler_org = tb.entryBuilder.getProductAttributeHandler();
			EntryBuilder entry_to_merge = ((TableBuilder)(prd_to_merge)).entryBuilder;
			entry_to_merge.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
			entry_to_merge.productAttributeHandler = prd_to_merge.dataFile.getEntryAttributeHandlerCopy();

			for( AttributeHandler new_att: entry_to_merge.getProductAttributeHandler().values()) {
				AttributeHandler old_att = null;
				if( (old_att = tableAttributeHandler_org.get(new_att.getNameattr())) != null ) {
					old_att.mergeAttribute(new_att);
				} else {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Add attribute <" + new_att.getNameattr() + ">");
					tableAttributeHandler_org.put(new_att.getNameattr(), new_att);
				}
			}

		}
	}

	/**
	 * Modify the data format of the productBuilder managed by the current instance in order to make it compliant
	 * with the data format of attributeHandlerToMerge
	 * @param attributeHandlerToMerge
	 * @throws FatalException 
	 */
	public void mergeAttributeHandlers(Map<String, AttributeHandler>attributeHandlerToMerge) throws SaadaException {
		/*
		 * Merge old a new sets of attribute handlers
		 */
		Iterator<AttributeHandler> it = attributeHandlerToMerge.values().iterator();
		while( it.hasNext()) {
			AttributeHandler new_att = it.next();
			AttributeHandler old_att = null;
			if( (old_att = this.productBuilder.productAttributeHandler.get(new_att.getNameorg())) != null ||
					(old_att = this.productBuilder.productAttributeHandler.get(new_att.getNameattr())) != null	) {
				old_att.mergeAttribute(new_att);
			} else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Add attribute <" + new_att.getNameorg() + ">");
				this.productBuilder.productAttributeHandler.put(new_att.getNameorg(), new_att);
			}
		}
		this.productBuilder.setFmtsignature();
	}

	/**
	 * Return a SQL string denoting the sQL expression of value down-casted in the downCasting mode 
	 * @param value
	 * @param downCasting
	 * @return
	 * @throws Exception
	 */
	public static String getCastedSQLValue(Object value, DownCasting downCasting) throws Exception{
		String sValue  = value.toString();
		if( sValue.equals("Infinity")          || sValue.equals("NaN") || value.equals("") ||
			sValue.equals(SaadaConstant.NOTSET)|| sValue.equals(SaadaConstant.STRING) ||
				sValue.equalsIgnoreCase("NULL")|| sValue.equals("2147483647") || sValue.equals("9223372036854775807")) {
			return Database.getWrapper().getAsciiNull();
		}

		switch (downCasting) {
		case NoCast: return sValue;
		case Int2String:
		case Num2String:
		case Double2String:
			return "'" + sValue.toString() + "'";
		case Int2Double: 
			return Integer.valueOf(sValue).toString();
		case Bool2Double:
		case Bool2Int:
		case Bool2Num: 
			return (sValue.startsWith("t") || sValue.startsWith("T"))? "1": "0";
		case Bool2String: 
			return (sValue.startsWith("t") || sValue.startsWith("T"))? Database.getWrapper().getBooleanAsString(Boolean.parseBoolean(sValue))
					: Database.getWrapper().getBooleanAsString(false);
		default:
			return Database.getWrapper().getAsciiNull();
		}

	}

	/**
	 * Return a SQL string denoting the SQL expression of the ahOrg's value down-casted in order 
	 * to be compatible with the type finalType
	 * @param finalType
	 * @return
	 * @throws Exception
	 */
	public static String getCastedSQLValue(AttributeHandler ahOrg, String finalType)throws Exception{
		return getCastedSQLValue(ahOrg.getValue(), getDownCasting(ahOrg, finalType));
	}

	/**
	 * return the down casting mode to be applied to the value of ahOrg for making its SQL expression 
	 * compatible with the type finalType
	 * @param ahOrg
	 * @param finalType
	 * @return
	 * @throws Exception
	 */
	public static DownCasting getDownCasting(AttributeHandler ahOrg, String finalType)throws Exception{
		DownCasting downCasting = DownCasting.NoCast;
		String typeOrg = ahOrg.getType();
		switch (finalType) {
		case "String":
			switch (typeOrg) {
			case "String":downCasting = DownCasting.NoCast; break;
			case "double":
			case "float":downCasting = DownCasting.Double2String; break;
			case "short":
			case "int":
			case "long":downCasting = DownCasting.Int2String; break;
			case "boolean":downCasting = DownCasting.Bool2String; break;
			default: IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER
					, "Type " + typeOrg + " not recognized");return null;
			}
			break;
		case "double":
		case "float":
			switch (typeOrg) {
			case "String":IgnoreException.throwNewException(SaadaException.FILE_FORMAT
					, "A string can not be casted in double (internal merger error)");break;
			case "double":
			case "float":downCasting = DownCasting.NoCast; break;
			case "short":
			case "int":
			case "long":downCasting = DownCasting.Int2Double; break;
			case "boolean":downCasting = DownCasting.Bool2Double; break;
			default: IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER
					, "Type " + typeOrg + " not recognized");return null;
			}
			break;
		case "short":
		case "int":
		case "long":
			switch (typeOrg) {
			case "String":
			case "double":
			case "float":IgnoreException.throwNewException(SaadaException.FILE_FORMAT
					, "A " + typeOrg + "  can not be casted in integer (internal merger error)");break;
			case "short":
			case "int":
			case "long":downCasting = DownCasting.NoCast; break;
			case "boolean":downCasting = DownCasting.Bool2Num; break;
			default: IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER
					, "Type " + typeOrg + " not recognized");return null;
			}
			break;
		case "boolean":
			switch (typeOrg) {
			case "String":
			case "double":
			case "float":
			case "short":
			case "int":
			case "long":IgnoreException.throwNewException(SaadaException.FILE_FORMAT
					, "A " + typeOrg + "  can not be casted in boolean (internal merger error)");break;
			case "boolean":downCasting = DownCasting.NoCast; break;
			default: IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER
					, "Type " + typeOrg + " not recognized");return null;
			}
			break;
		default:IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER
				, "Type " + finalType + " not recognized");return null;

		}
		return downCasting;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
