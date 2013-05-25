package saadadb.query.merger;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.UTypeHandler;
import saadadb.query.constbuilders.SaadaQLConstraint;
import saadadb.query.parser.UnitHandler;
import saadadb.util.RegExp;

public class ClassQNode extends QNode{
	private  MetaClass metaclass = null;
	protected SetOfSelectedColumns set_of_selected_columns = new SetOfSelectedColumns();
	private CollectionQNode root;

	
	/**
	 * @param classe
	 * @throws FatalException
	 */
	ClassQNode(String classe, CollectionQNode root, Merger merger) throws FatalException {
		super(classe, merger);
		metaclass = Database.getCachemeta().getClass(classe);
		this.root = root;
	}

	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#getBusniessAttributesHandlers()
	 */
	public Map<String, AttributeHandler> getBusinessAttributesHandlers() {
		return metaclass.getAttributes_handlers();
	}

	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#getFields()
	 */
	public Field[] getFields() throws FatalException, ClassNotFoundException {
	 return Class.forName("generated." + Database.getDbname() + "." + this.name).getFields();
	}

	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#hasUCD(java.lang.String)
	 */
	public boolean hasUCD(String ucd) {
		if( this.metaclass.getUCDField(ucd, true) != null ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#updateSQL(java.util.LinkedHashMap)
	 */
	public void readSaadaQLConstraints(LinkedHashMap<String, SaadaQLConstraint> builders) throws Exception {
		for( Entry<String, SaadaQLConstraint>eb: builders.entrySet()) {
			SaadaQLConstraint scb = eb.getValue();
			if( scb.isNative() ||  scb.isGlobal()) {
				this.readSaadaQLConstraint(eb.getKey(), scb);	
				scb.takeByClass();
			}
			else if( scb.isDMMapped()) {
				this.readDMMappedSaadaQLConstraint(scb);				
			}
			else if( scb.isColMapped()) {
				this.readColMappedSaadaQLConstraint(scb);				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#updateDMSQL(saadadb.query.constbuilders.SQLConstraint)
	 */
	public void readDMMappedSaadaQLConstraint(SaadaQLConstraint scb) throws Exception {
		/*
		 * Ici rechercher le mapping
		 */
		SaadaInstance si = (SaadaInstance) Class.forName("generated." + Database.getDbname() + "." + this.metaclass.getName()).newInstance();		
		si.activateDataModel(this.merger.getVor().getJavaName() );
		String computed_column = si.getSQLField(scb.getMetacolname());
		UTypeHandler uth = scb.getDM().getUTypeHandler(scb.getMetacolname() );
		if( uth == null ) {
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "Can't find DM field " + scb.getMetacolname());
		}
		String mapping_expression = scb.computeWhereStatement(computed_column);
		Pattern p = Pattern.compile("@@@(" + RegExp.UTYPE + ")@@@", Pattern.DOTALL);
		Matcher m = p.matcher(mapping_expression);
		String new_where = mapping_expression;
		while( m.find()  ) {
			new_where = new_where.replaceAll(m.group(1), "(" + mapping_expression + ")");
		}
		if( this.where.length() != 0 && new_where.length() != 0) {
			this.where += " AND "; 
		}
		this.where += new_where.replaceAll("@@@", "");
		for( String sql_colname: scb.getSqlcolnames()) {
			/*
			 * Makes the unit conversion
			 */
			String exp = UnitHandler.getConvFunction(scb.getUnit(),uth.getUnit(),computed_column);
			/*
			 * Insert coll and class alias in the expression
			 */
			exp = insertAlias(exp, this.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.name)	;	
			exp = insertAlias(exp, this.root.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.root.name)	;	
			this.addSelectedColumn(sql_colname, new ColumunSelectDef(exp
							, sql_colname, ColumunSelectDef.EXPRESSION));
		}
		scb.takeByClass();
	}
	
	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#updateColSQL(saadadb.query.constbuilders.SQLConstraint)
	 */
	public void readColMappedSaadaQLConstraint(SaadaQLConstraint scb) throws SaadaException {
		/*
		 * Ici rechercher le mapping
		 */
		String ucd = scb.getMetacolname();
		AttributeHandler ah = this.metaclass.getUCDField(ucd, true);
		if( ah != null ) {
			String mapping_expression = scb.computeWhereStatement(ah);
			Pattern p = Pattern.compile("@@@(" + RegExp.UTYPE + ")@@@", Pattern.DOTALL);
			Matcher m = p.matcher(mapping_expression);
			String new_where = mapping_expression;
			while( m.find()  ) {
				new_where = new_where.replaceAll(m.group(1), ah.getNameattr());
			}
			if( this.where.length() != 0 && new_where.length() != 0) {
				this.where += " AND "; 
			}
			this.where += new_where.replaceAll("@@@", "");
			for( String sql_colname: scb.getSqlcolnames()) {
				this.addSelectedClassColumn(sql_colname, new ColumunSelectDef(
						insertAlias(UnitHandler.getConvFunction(scb.getUnit(),ah.getUnit(),ah.getNameattr())
								, new String[]{ ah.getNameattr()}, this.name)
								, sql_colname, ColumunSelectDef.EXPRESSION));
			}
			scb.takeByClass();
		}
		else {
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "Can't find  field " + scb.getMetacolname());
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#setAllcolumnsSelect()
	 */
	public void setAllcolumnsSelect() throws Exception {
		/*
		 * Take the native columns
		 */
		if( this.merger.getVor() == null ) {
			super.setAllcolumnsSelect();
			for( AttributeHandler ah: this.root.getBusinessAttributesHandlers().values()) {
				String computed_column = ah.getNameattr();

				String exp = computed_column;
				/*
				 * Insert coll and class alias in the expression
				 */
				exp = insertAlias(exp, this.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.name)	;	
				exp = insertAlias(exp, this.root.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.root.name)	;	

				this.addSelectedClassColumn(ah.getNameattr(), new ColumunSelectDef(exp
								, ah.getNameattr(), ColumunSelectDef.EXPRESSION));
			}
			for( AttributeHandler ah: this.metaclass.getAttributes_handlers().values()) {
				String computed_column = ah.getNameattr();

				String exp = computed_column;
				/*
				 * Insert coll and class alias in the expression
				 */
				exp = insertAlias(exp, this.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.name)	;	
				exp = insertAlias(exp, this.root.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.root.name)	;	

				this.addSelectedClassColumn(ah.getNameattr(), new ColumunSelectDef(exp
								, ah.getNameattr(), ColumunSelectDef.EXPRESSION));
			}
		}
		/*
		 * Take the DM columns
		 */
		else {
			SaadaInstance si = (SaadaInstance) Class.forName("generated." + Database.getDbname() + "." + this.metaclass.getName()).newInstance();		
			si.activateDataModel(this.merger.getVor().getJavaName() );
			for( UTypeHandler uth: this.merger.getVor().getUTypeHandlers()) {
				si.activateDataModel(this.merger.getVor().getJavaName() );
				String computed_column = si.getSQLField(uth.getUtypeOrNickname());
				if( uth == null ) {
					QueryException.throwNewException(SaadaException.METADATA_ERROR, "Can't find DM field " + uth.getUtypeOrNickname());
				}

				String exp = computed_column;
				/*
				 * Insert coll and class alias in the expression
				 */
				exp = insertAlias(exp, this.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.name)	;	
				exp = insertAlias(exp, this.root.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.root.name)	;	
				this.set_of_selected_columns.forceJoin();
				this.addSelectedColumn(uth.getNickname(), new ColumunSelectDef(exp
								, uth.getNickname(), ColumunSelectDef.EXPRESSION));
			}
		}
	}	
	
	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#addSelectedColumn(java.lang.String, saadadb.query.merger.ColumunSelectDef)
	 */
	protected void addSelectedColumn(String result_column_name, ColumunSelectDef csd ){
		this.set_of_selected_columns.addSelectedColumn(result_column_name, csd);
	};
	protected void addSelectedClassColumn(String result_column_name, ColumunSelectDef csd ){
		this.set_of_selected_columns.addSelectedClassColumn(result_column_name, csd);
	};

}
