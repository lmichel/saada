package adqlParser;

import adqlParser.parser.ParseException;
import adqlParser.parser.QueryBuilderTools;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLQuery;
import adqlParser.query.ADQLTable;
import adqlParser.query.function.UserFunction;

public class SaadaQueryBuilderTools extends QueryBuilderTools {
	
	protected SaadaDBConsistency contextManager;

	public SaadaQueryBuilderTools(SaadaDBConsistency ucdCollector) {
		contextManager = ucdCollector;
	}

	@Override
	public ADQLQuery createQuery() throws ParseException {
		return new SaadaADQLQuery();
	}

	@Override
	public ADQLColumn createColumn(String colName, String prefix, String alias) throws ParseException {
		ADQLColumnAndMeta col = new ADQLColumnAndMeta(colName, prefix);
		col.setAlias(alias);
		return col;
	}

	@Override
	public ADQLTable createTable(String table, String alias) throws ParseException {
		return new SaadaADQLTable(table, alias);
	}

	@Override
	public UserFunction createUserFunction(String name, ADQLOperand[] params) throws ParseException {
		if (name.equalsIgnoreCase("UCD")){
			UCDFunction ucd = new UCDFunction(params);
			ucd.setPrintUcdAsAlias(contextManager.getNbAliasedTables() <= 0);
			return ucd;
		}else
			return super.createUserFunction(name, params);
	}

}
