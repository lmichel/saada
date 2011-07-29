package saadadb.resourcetest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import saadadb.api.SaadaLink;
import saadadb.collection.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaRelation;
import saadadb.query.executor.Query;
import saadadb.query.matchpattern.CounterpartSelector;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;

public class QueryPatternTester {
	HashMap<String, QuerySet>  query_set = new HashMap<String, QuerySet>();
	public QueryPatternTester() {
			QuerySet qs = new QuerySet("Select ENTRY From * In PatternPrimaire\n"
		            + " WhereRelation {\n"
		            + "  matchPattern {\n"
		            + "   PatternTesteur,\n"
		            + "   Cardinality @1 \n"
		            + "  }\n"
		            + " }\n");
			qs.addParam("@1", new String[]{"= 0", "!= 0", "< 2", "<= 4", "[] (0,4)", "[=] (1,3)", "> 8", ">= 9", "][ (2,8)", "]=[ (1,9)", "< 0", "> 9"});
			this.query_set.put("CARD", qs);
			
			qs = new QuerySet("Select ENTRY From * In PatternPrimaire\n"
		            + " WhereRelation {\n"
		            + "  matchPattern {\n"
		            + "   PatternTesteur,\n"
		            + "   Cardinality @1 \n"
		            + "   AssObjAttSaada{ namesaada like 'classe_s1%' }\n"
		            + "  }\n"
		            + " }\n");
			qs.addParam("@1", new String[]{"= 0", "= 1", "!= 0", "< 2", "<= 4", "[] (0,4)", "[=] (1,3)", "> 15", ">= 16", "][ (2,8)", "]=[ (1,9)", "< 0", "> 9"/**/});
			this.query_set.put("CARD_CP", qs);

			qs = new QuerySet("Select ENTRY From * In PatternPrimaire\n"
		            + " WhereRelation {\n"
		            + "  matchPattern {\n"
		            + "   PatternTesteur,\n"
		            + "   Cardinality @1 \n"
		            + "   Qualifier{ card > 1 }\n"
		            + "  }\n"
		            + " }\n");
			qs.addParam("@1", new String[]{"= 0", "= 1", "= 2", "!= 0", "< 2", "<= 4", "[] (0,4)", "[=] (1,3)", "> 15", ">= 16", "][ (2,14)", "]=[ (1,15)", "< 0", "> 18"/**/});
			this.query_set.put("CARD_QUAL", qs);
			

			qs = new QuerySet("Select ENTRY From * In PatternPrimaire\n"
		            + " WhereRelation {\n"
		            + "  matchPattern {\n"
		            + "   PatternTesteur,\n"
		            + "   Cardinality @1 \n"
		            + "   AssObjClass{classe_s1Entry},\n"
		            + "   Qualifier{ card > 3 }\n"
		            + "  }\n"
		            + " }\n");
			qs.addParam("@1", new String[]{"= 0", "= 1", "= 2", "!= 0", "< 2", "<= 4", "[] (0,4)", "[=] (1,3)", "> 8", ">= 9", "][ (2,8)", "]=[ (1,9)", "< 0", "> 18"/**/});
			this.query_set.put("CARD_QUAL_CLASS", qs);

			qs = new QuerySet("Select ENTRY From * In PatternPrimaire\n"
		            + " WhereRelation {\n"
		            + "  matchPattern {\n"
		            + "   PatternTesteur,\n"
		            + "   Cardinality @1 \n"
		            + "   AssObjAttSaada{ namesaada like 'classe_s1%' }\n"
		            + "   Qualifier{ card > 0 }\n"
		            + "   @2\n"
		            + "  }\n"
		            + " }\n");
			qs.addParam("@1", new String[]{"= 0", "= 1", "= 2", "!= 0", "< 2", "<= 4", "[] (0,4)", "[=] (1,3)", "> 8", ">= 9", "][ (2,8)", "]=[ (1,9)", "< 0", "> 18"/**/});
			qs.addParam("@2", new String[]{"Qualifier{ card1 = 2 }"});
			this.query_set.put("CARD_QUAL_CP", qs);

			qs = new QuerySet("Select ENTRY From * In PatternPrimaire\n"
		            + " WhereRelation {\n"
		            + "  matchPattern {\n"
		            + "   PatternTesteur,\n"
		            + "   Cardinality @1 \n"
		            + "   AssObjClass{classe_s1Entry}\n"
		            + "  }\n"
		            + " }\n");
			qs.addParam("@1", new String[]{"= 2", "= 0", "!= 0", "< 2", "[] (0,4)", "[=] (1,3)", "> 8", ">= 9", "][ (2,8)", "]=[ (2,9)", "< 0", "> 18"/**/});
			this.query_set.put("CARD_CLASS", qs);
			
			qs = new QuerySet("Select ENTRY From * In PatternPrimaire\n"
		            + " WhereRelation {\n"
		            + "  matchPattern {\n"
		            + "   PatternTesteur,\n"
		            + "   Cardinality @1 \n"
		            + "   AssObjClass{classe_s1Entry}\n"
		            + "  }\n"
		            + " }\n");
			qs.addParam("@1", new String[]{"= 2", "= 0", "!= 0", "< 2", "[] (0,4)", "[=] (1,3)", "> 8", ">= 9", "][ (2,8)", "]=[ (2,9)", "< 0", "> 18"/**/});
			this.query_set.put("CARD_CLASS_CP", qs);
			
			qs = new QuerySet("Select ENTRY From * In PatternPrimaire\n"
		            + " WhereRelation {\n"
		            + "  matchPattern {\n"
		            + "   PatternTesteur,\n"
		            + "   Cardinality @1 \n"
		            + "   Qualifier { card > 0 }\n"
//		            + "   Qualifier { q2 < 0 }\n"
		            + "   AssObjAttClass{ _s1_card = 2 },\n"
		            + "   AssObjClass{classe_s1Entry},\n"
		            + "   AssObjAttSaada{ namesaada like 'classe_s1  2%' }\n"
		            + "  }\n"
		            + " }\n");
			qs.addParam("@1", new String[]{"= 2", "= 0", "!= 0", "< 2", "<= 4", "[] (0,4)", "[=] (1,3)", "> 8", ">= 9", "][ (2,8)", "]=[ (1,9)", "< 0", "> 18"/**/});
			this.query_set.put("CARD_QUAL_CLASS_CP", qs);
		}


	class QuerySet {
		String query;
		LinkedHashMap<String, String[]> params;

		QuerySet(String query) {
			this.query =  query;
			params = new LinkedHashMap<String, String[]>();
		}

		void addParam(String name, String[] values) {
			params.put(name, values);
		}

		Set<String> getAllQueries() {
			Set<String> retour = new TreeSet<String>();
			String current_query = this.query;
			addQueryForParam(retour, 0, current_query);
			return retour;
		}

		private void addQueryForParam(Set<String> retour, int pos, String current_query) {
			if( pos < this.params.size() ) {
				String param_name = this.params.keySet().toArray(new String[0])[pos];
				String[] values = params.get( param_name);
				for( String val: values ) {
					String cq  = current_query.replaceAll(param_name, val);
					addQueryForParam(retour, pos+1, cq);
					if( pos == (this.params.size()-1) ) {
						retour.add(new String(cq));
					}
				}
			}
		}
	}


	public Set<String> getQuerySet(String mode) {
		return this.query_set.get(mode).getAllQueries();
	}
	/** * @version $Id$

	 * @param args
	 * @throws SyntaxSaadaQLException
	 * @throws QuerySaadaQLException
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args)  {
		ArgsParser ap;
		try {
			ap = new ArgsParser(args);
			Database.init(ap.getDBName());
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		QueryPatternTester qt = new QueryPatternTester();
		for( String qset: qt.query_set.keySet()) {
			System.out.println("**************************************************");
			System.out.println("Query set: " + qset);
			System.out.println("**************************************************");
			Set<String> queries = qt.getQuerySet(qset);
			if( queries != null ) {
				for( String query: queries) {
					SaadaQLResultSet qn_res=null;
					Query qn = new Query();
					try {
						System.out.println("*** QUERY ******************************");
						System.out.println(query);
						System.out.println("--- Results -----------------------------");
						qn_res = qn.runQuery(query);
						for(AttributeHandler ah:qn.buildListAttrHandPrinc()) {
							System.out.println("const on: " + ah.getNameattr());
						}
						int cpt = 0;
						while( qn_res.next() ) {
							long oid= qn_res.getOid();
							SaadaInstance si = Database.getCache().getObject(oid);
							System.out.println("#" + cpt + " Name <" + si.getNameSaada() + ">");
							for( Entry<String, CounterpartSelector> e: qn.getMatchingCounterpartQueries().entrySet()) {
								String rel_name = e.getKey();
								MetaRelation mr = Database.getCachemeta().getRelation(rel_name);
								Set<SaadaLink> mcp = si.getCounterpartsMatchingQuery(rel_name, e.getValue());
								for( SaadaLink sl:mcp ) {
									long cpoid = sl.getEndindOID();
									System.out.print("      Name <" + Database.getCache().getObject(cpoid).getFieldValue("namesaada") + ">");
									for( String q: e.getValue().getQualif_query().keySet()) {
										System.out.print(" " + q + "=" + sl.getQualifierValue(q) );
									}
									System.out.println("");
								}
							}	
							cpt++;
						}
					} catch (Exception e) {
						System.out.println("-------- ERROR ----------\n" + qn.explain());
						Messenger.printStackTrace(e);
						System.exit(1);
					}
				}
			}
		}

	}

}
