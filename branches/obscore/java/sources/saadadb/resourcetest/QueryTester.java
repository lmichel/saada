package saadadb.resourcetest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import saadadb.query.executor.Query;
import saadadb.query.matchpattern.CounterpartSelector;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;

public class QueryTester {
	HashMap<String, QuerySet>  query_set = new HashMap<String, QuerySet>();
	public QueryTester() {
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
	            + "   AssObjAttSaada{ obs_id like 'classe_s1%' }\n"
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
	            + "   AssObjAttSaada{ obs_id like 'classe_s1%' }\n"
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
//	            + "   Qualifier { q2 < 0 }\n"
	            + "   AssObjAttClass{ _s1_card = 2 },\n"
	            + "   AssObjClass{classe_s1Entry},\n"
	            + "   AssObjAttSaada{ obs_id like 'classe_s1  2%' }\n"
	            + "  }\n"
	            + " }\n");
		qs.addParam("@1", new String[]{"= 2", "= 0", "!= 0", "< 2", "<= 4", "[] (0,4)", "[=] (1,3)", "> 8", ">= 9", "][ (2,8)", "]=[ (1,9)", "< 0", "> 18"/**/});
		this.query_set.put("CARD_QUAL_CLASS_CP", qs);
		
		qs = new QuerySet("		Select ENTRY  From CatalogueEntry In CATALOGUE\n"
				+ " WherePosition{\n"
				+ "     isInCircle(\"02:41:56.72+00:00:57.5\",0.3,J2000,ICRS)\n"
				+ "    }\n"
				+ " WhereRelation{\n"
				+ "    matchPattern{CatSrcToArchSrc,\n"
				+ "       AssObjClass{arch_2282AEntry}}\n"
				+ "    matchPattern{ObjClass,\n"
				+ "       Qualifier{proba_star >0.5 },\n"
				+ "        Qualifier{sample @1 }}"
				+ " }\n");
		qs.addParam("@1", new String[]{"= 3"});
		this.query_set.put("MULTPATTERN", qs);
		
	}

	

	/**
	 * @return
	 */
	public static String[] CP_queries() {
		String[] queries = new String[11];
		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"} }";

		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\"=\", 0, 0)} }";
		queries[2] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\"!=\", 0, 0)} }";

		queries[3] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\"=\", 2, 0)} }";
		queries[4] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\"!=\", 2, 0)} }";

		queries[5] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\">\", 2, 0)} }";
		queries[6] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\"<\", 2, 0)} }";
		queries[7] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\">=\", 2, 0)} }";
		queries[8] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\"<=\", 2, 0)} }";

		queries[9] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\"[]\", 2, 6)} }";
		queries[10] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "                Cardinality(\"][\", 2, 6)} }";
		return queries;
	}

	public static String[] CARD_CP_queries() {
		String[] queries = new String[8];

		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               Cardinality(\"=\", 1, 0)} }";

		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RX J105350.7+572515' \")\n"
			+ "               Cardinality(\"=\", 0, 0)} }";
		queries[2] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RX J105350.7+572515' \")\n"
			+ "               Cardinality(\"=\", 1, 0)} }";
		queries[3] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RX J105350.7+572515' \")\n"
			+ "               Cardinality(\"=\", 2, 0)} }";
		queries[4] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RX J105350.7+572515' \")\n"
			+ "               Cardinality(\">\", 0, 0)} }";
		queries[5] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RX J105350.7+572515' \")\n"
			+ "               Cardinality(\">=\", 2, 0)} }";
		queries[6] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RX J105350.7+572515' \")\n"
			+ "               Cardinality(\"[]\", 1, 2)} }";
		queries[7] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")} }";
		return queries;
	}

	public static String[] CARD_CLASS_queries() {
		String[] queries = new String[2];

		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjClass(\"SimbadEntry\")\n"
			+ "               Cardinality(\"=\", 0, 0)} }";
		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjClass(\"SimbadEntry\")\n"
			+ "               Cardinality(\"=\", 4, 0)} }";
		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjClass(\"SimbadEntry\",\"NedEntry\")"
			+ "               Cardinality(\"=\", 4, 0)} }";
		return queries;
	}

	public static String[] CLASS_queries() {
		String[] queries = new String[3];

		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjClass(\"SimbadEntry\") } }";
		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjClass(\"SimbadEntry\", \"NedEntry\") } }";

		queries[2] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjClass(\"Arch_9037AEntry\",\"Arch_9028AEntry\")} }";

		return queries;
	}

	public static String[] CARD_CLASS_CP_queries() {
		String[] queries = new String[5];

		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               AssObjClass(\"SimbadEntry\")\n"
			+ "               Cardinality(\"=\", 0, 0)} }";
		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               AssObjClass(\"SimbadEntry\")\n"
			+ "               Cardinality(\"=\", 1, 0)} }";
		queries[2] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               AssObjAttClass(\" _nref = 1 \")\n"
			+ "               AssObjClass(\"SimbadEntry\")\n"
			+ "               Cardinality(\"=\", 1, 0)} }";
		queries[3] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               AssObjAttClass(\" _nref > 1 \")\n"
			+ "               AssObjClass(\"SimbadEntry\")\n"
			+ "               Cardinality(\"=\", 1, 0)} }";
		queries[4] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               AssObjAttClass(\" _nref = 1 \")\n"
			+ "               AssObjClass(\"SimbadEntry\")} }";
		return queries;
	}

	public static String[] CARD_QUAL_queries() {
		String[] queries = new String[5];

		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 1000, 0)\n"
			+ "               Cardinality(\"=\", 0, 0)} }";
		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 1, 0)\n"
			+ "               Cardinality(\">\", 1, 0)} }";
		queries[2] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 1, 0)\n"
			+ "               Cardinality(\">=\", 1, 0)} }";
		queries[3] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 1, 0)\n"
			+ "               Qualifier(\"obs_id\", \"<\", 0, 0)\n"
			+ "               Cardinality(\">=\", 1, 0)} }";
		queries[4] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 1, 0)\n"
			+ "               Qualifier(\"obs_id\", \"<\", 0, 0)\n"
			+ "               Cardinality(\"=\", 0, 0)} }";
		return queries;
	}
	public static String[] CARD_QUAL_CLASS_queries() {
		String[] queries = new String[3];

		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjClass(\"SimbadEntry\")\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 1000, 0)\n"
			+ "               Cardinality(\"=\", 0, 0)} }";
		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjClass(\"SimbadEntry\", \"NedEntry\")\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 1000, 0)\n"
			+ "               Cardinality(\"=\", 0, 0)} }";
		queries[2] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjClass(\"SimbadEntry\")\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 1000, 0)\n"
			+ "               Qualifier(\"obs_id\", \"<\", 0, 0)\n"
			+ "               Cardinality(\"=\", 0, 0)} }";
		return queries;
	}
	public static String[] CARD_QUAL_CP_queries() {
		String[] queries = new String[4];

		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 10, 0)\n"
			+ "               Cardinality(\"=\", 1, 0)} }";
		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               Qualifier(\"d_epic_cat\", \"=\", 1000, 0)\n"
			+ "               Cardinality(\"=\", 1, 0)} }";
		queries[2] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               Qualifier(\"d_epic_cat\", \"=\", 1000, 0)\n"
			+ "               Cardinality(\"=\", 0, 0)} }";
		queries[3] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               Qualifier(\"d_epic_cat\", \"=\", 1000, 0)\n"
			+ "               Cardinality(\"!=\", 0, 0)} }";
		return queries;
	}
	public static String[] CARD_QUAL_CLASS_CP_queries() {
		String[] queries = new String[2];

		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id = 'RDS 128E' \")\n"
			+ "               AssObjClass(\"SimbadEntry\")\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 10, 0)\n"
			+ "               Cardinality(\"=\", 1, 0)} }";
		queries[1] = "Select ENTRY From CatalogueEntry In CATALOGUE \n"
			+ "WhereRelation{ matchPattern { \"CatSrcToArchSrc\"\n"
			+ "               AssObjAttSaada(\" obs_id != 'RDS 128E' \")\n"
			+ "               AssObjClass(\"SimbadEntry\", \"NedEntry\")\n"
			+ "               Qualifier(\"d_epic_cat\", \"<\", 10, 0)\n"
			+ "               Cardinality(\"=\", 1, 0)} }";
		return queries;
	}

	public static String[] UCD_queries() {
		String[] queries = new String[1];


		queries[0] = "Select ENTRY From * In ARCH_CAT\n"
			+ "Limit 1000\n"
			+ "WhereRelation{\n"
			+ "    matchPattern{\"ArchSrcToCatSrc\",\n"
			+ "        Cardinality(\"!=\", 0, 0),\n"
			+ "        AssObjClass(\"CatalogueEntry\"),\n"
			+ "        AssObjAttClass(\"_ep_det_ml > 100\")\n"
			+ "        }\n"
			+ "    }\n";
		return queries;
	}

	public static String[] ANY_queries() {
		String[] queries = new String[1];

		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE\n"
			+ "WhereAttributeClass{\n"
			+ "    \"(_ep_hr4 >= -0.5 and _ep_hr4 <= 0) and (_ep_hr3 >= -0.3 and _ep_hr3 <= 0.5)\"\n"
			+ "    }\n"
			+ "WhereRelation{\n"
			+ "    matchPattern{\"CatSrcToArchSrc\",\n"
			+ "       Cardinality(\"=\", 0, 0),\n"
			+ "         AssObjClass(\"Arch_3215AEntry\")}\n"
			+ "    matchPattern{\"CatSrcToArchSrc\",\n"
			+ "        AssObjClass(\"Arch_5050AEntry\")}\n"
			+ "    }\n"; 
		queries[0] = "Select ENTRY From CatalogueEntry In CATALOGUE\n"
			+ "WhereRelation{\n" 
			+ "    matchPattern{\"CatSrcToArchSrc\",\n" 
			+ "    AssUCD([pos.pm.dec] > 1 [marcsec/yrsss])}\n" 
			+ "    matchPattern{\"CatSrcToSrcTS\"}\n" 
			+ "    }\n"; 
		queries[0] = "Select ENTRY From * In ARCH_CAT\n"
			+ "WhereUCD{ [pos.pm.dec] > 1 [none] }\n"; 


		queries[0] = "                Select ENTRY From CatalogueEntry In CATALOGUE\n"
			+ "                Limit 1000\n"
			+ "                WhereAttributeSaada{\n"
			+ "                    \"(VIZIER_KW & 524288) = 0\" \n"
			+ "                    }\n"/*
                       + "                WhereAttributeClass{\n"
                       + "                    \"_EP_8_RATE > 0.05 and _SUM_FLAG = 1\"\n"
                       + "                    }\n"*/; 
		queries[0] = "                Select ENTRY From CatalogueEntry In CATALOGUE"
			+ "                                Limit 1000"
			+ "                                WhereAttributeClass{"
			+ "                                    \"_SRC_NUM = 1E-4 and _EP_CTS = E-4\""
			+ "                                    }";
//		Select ENTRY From * In ARCH_CAT
//		WhereUCD{
//		[stat.likelihood] = 0 [none] a cause du like
//		}

		return queries;
	}




	public static String[] testFX(){
		String[] queries = new String[1];
		queries[0] = "Select ENTRY From * In * WhereUType{ [ChAxis.ObsyLoc] >-100 }";
		queries[0] = "Select ENTRY From XID212Entry In Azerty  WhereAttributeSaada{ pos_ra_csa>-999} WhereAttributeClass{ _xra != 0} WhereUType{ [ChAxis.ObsyLoc] >-100 } Order By _xra limit 10";
		queries[0] = "Select ENTRY From * In * WhereUCD{ [em.IR] > 0 } ";
		queries[0] = "Select SPECTRUM From * In GalacticPlaneSurvey ";
		queries[0] += "WhereRelation {";
		queries[0] += "     matchPattern {";
		queries[0] += "          SP_FindingChart,";
		queries[0] += "          Cardinality(> 0)";
		queries[0] += "     }";
		queries[0] += "}";
		return queries;
	}

	public static final String[] fxquery(){
		/*Map<String,AttributeHandler> ah = Database.getCachemeta().getCollection("XMM_Data").getAttribute_handlers_entry();
		for(String s:ah.keySet()){
			System.out.println(s);
		}
		Map<String,AttributeHandler> ah2 = Database.getCachemeta().getCollection("Collection0").getAttribute_handlers_entry();
		for(String s:ah2.keySet()){
			System.out.println(s);
		}*/

		String[] queries = new String[1];
		queries[0] = "Select ENTRY From * In * Order By oidsaada limit 10";
		queries[0] = "Select ENTRY From SimpleVOtableEntry In Collection0 WhereAttributeClass{ _r < 100000 } Order By oidsaada limit 10";
		queries[0] = "Select ENTRY From SimpleVOtableEntry In Collection0 WhereAttributeClass{ _r < 100000 }\n"
			+"WhereAttributeSaada{pos_ra_csa > -200} Order By pos_dec_csa limit 10";
		queries[0] = "Select ENTRY From * In * Order By pos_ra_csa limit 20";

		queries[0] = "Select ENTRY From SimpleVOtableEntry In Collection0 WhereAttributeClass{ _r < 100000 } Order By pos_ra_csa\n";
		queries[0] = "Select ENTRY From * In * WhereUCD{ [pos.pm.dec] != -2000 [arcsec/s] }";

		queries[0] = "Select ENTRY From * In Azerty WhereAttributeSaada{ possss_ra_csa>0} WherePosition{isInCircle(\"4:12:01 +03:00:00\",0.5,J2000,FK5)  }  ";
		//queries[0] = "Select ENTRY From * In Azerty WherePosition{isInCircle(\"4:12:01 +03:00:00\",0.5,J2000,FK5)  }  ";
		//queries[0] = "Select ENTRY From * In * WhereUCD{ [pos.pm.dec] != -2000 [arcsec/s] } WhereUType{ [ChAxis.ObsyLoc] >-100 } ";
		queries[0] = "Select ENTRY From * In * WhereUCD { [pos.eq.dec] != 0 [deg] } "; 
		queries[0] = "Select ENTRY From * In * WhereUType { [ChAxis.AxisName] != 0 [deg] } ";
		queries[0] = "Select ENTRY From * In * WhereUType { [ChAxiis.AxisName] != 0 [deg] } "; 
		
		queries[0] = "Select ENTRY From MidasGPSEntry In GalacticPlaneSurvey Order By _ra_deg"; 
		queries[0] = "Select ENTRY From * In * WhereUCD{ [phys.veloc.transverse] > 0 [km/sec]}";
		queries[0] = "Select ENTRY From MidasSelGPSEntry In SelectedXGPS\n"
            + " WhereRelation { \n"
            + "    matchPattern{\n"
            + "         Selected_Candidate_Spectra\n"
            + "         Cardinality ]=[ (1,3)\n"
            + "         }\n"
            + "     }\n";
		queries[0] = "Select ENTRY From * In SelectedXGPS\n"
		            + " WhereRelation {\n"
		            + "  matchPattern {\n"
		            + "   Selected_Candidate_Spectra,\n"
		            + "   Cardinality = 0 \n"
		            + "   AssObjAttClass{ _filename like '%XRBC%' },\n"
		            + "   AssObjClass{MidasSpectra},\n"
		            + "   AssObjAttSaada{ obs_id is not null }\n"
		            + "  }\n"
		            + " }\n";
		queries[0] = "Select ENTRY From * In SelectedXGPS\n"
            + " WhereRelation {\n"
            + "  matchPattern {\n"
            + "   Selected_Candidate_Spectra,\n"
            + "   Cardinality = 0 \n"
            + "   AssObjAttSaada{ obs_id like 'fe%' }\n"
            + "  }\n"
            + " }\n";
		queries[0] = "Select ENTRY From * In SelectedXGPS\n"
            + " WhereRelation {\n"
            + "  matchPattern {\n"
            + "   Selected_Candidate_Spectra,\n"
            + "   Cardinality = 0 \n"
            + "   AssObjClass{MidasSpectra}\n"
            + "  }\n"
            + " }\n";
		queries[0] = "Select ENTRY From * In SelectedXGPS\n"
            + " WhereRelation {\n"
            + "  matchPattern {\n"
            + "   Qualificateur,\n"
            + "   Cardinality != 0 \n"
            + "  Qualifier { q1 < 0 }\n"
            + "  Qualifier { q2 > 0 }\n"
            + "  }\n"
            + " }\n";
		queries[0] = "Select ENTRY From * In SelectedXGPS\n"
            + " WhereRelation {\n"
            + "  matchPattern {\n"
            + "   Qualificateur,\n"
            + "   Cardinality = 0 \n"
            + "  Qualifier { q1 < 0 }\n"
            + "  Qualifier { q2 > 0 }\n"
            + "   AssObjClass{MidasSpectra}\n"
            + "  }\n"
            + " }\n";
		queries[0] = "Select ENTRY From * In SelectedXGPS\n"
            + " WhereRelation {\n"
            + "  matchPattern {\n"
            + "   Qualificateur,\n"
            + "   Cardinality != 1 \n"
            + "   Qualifier { q1 > 0 }\n"
            + "   Qualifier { q2 < 0 }\n"
            + "   AssObjAttClass{ _filename like '%XRBC%' },\n"
            + "   AssObjClass{MidasSpectra},\n"
            + "   AssObjAttSaada{ obs_id like 'fe%' }\n"
            + "  }\n"
            + " }\n";
		return queries;
	}

	public static final String[] posquery(){
		String[] queries = new String[1];
		queries[0] = "Select ENTRY  From * In * WherePosition{ isInCircle(\"M33\",1.0,J2000,Galactic) }	";
			return queries;
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
			Set<String> retour = new LinkedHashSet<String>();
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
	

	/**
	 * @param mode
	 * @return
	 */
	public Set<String> getQuerySet(String mode) {
		return this.query_set.get(mode).getAllQueries();
	}
	/**
	 * @param qn
	 */
	private static void errors(Query qn) {
		System.out.println("-------- ERROR ----------\n" + qn.explain());
		System.exit(1);
	}

	/**
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
		QueryTester qt = new QueryTester();
		Set<String> queries = qt.getQuerySet("MULTPATTERN");
		if( queries != null ) {
			for( String query: queries) {
				SaadaQLResultSet qn_res=null;
				Query qn = new Query();
				try {
					System.out.println("*** QUERY ******************************");
					System.out.println(query);
					qn_res = qn.runQuery(query);
					if( qn_res == null ) {
						errors(qn);
					}
					for(AttributeHandler ah:qn.buildListAttrHandPrinc()) {
						System.out.println("const on: " + ah.getNameattr());
					}
					System.out.println("--- Results : " + qn_res.getSize() + " Object founds");
					int cpt = 0;
					qn_res.rewind();
					while( qn_res.next() ) {
						long oid= qn_res.getOid();
						SaadaInstance si = Database.getCache().getObject(oid);
						System.out.println("#" + cpt +  " Name <" + si.obs_id+ ">");
						
						for( Entry<String, CounterpartSelector> e: qn.getMatchingCounterpartQueries().entrySet()) {
							String rel_name = e.getKey();
							System.out.print("   " + si.getCounterparts(rel_name).length + " cps");
					   		System.out.println(" of which  " + si.getCounterpartsMatchingQuery(rel_name, e.getValue()).size() + "  matching the query " + rel_name);
//							MetaRelation mr = Database.getCachemeta().getRelation(rel_name);
//							Set<SaadaLink> mcp = si.getCounterpartsMatchingQuery(rel_name, e.getValue());
//							for( SaadaLink sl:mcp ) {
//								long cpoid = sl.getEndindOID();
//								System.out.print("      <" + Database.getCache().getObject(cpoid).namesaada+ "> ");
//								for( String q: mr.getQualifier_names()) {
//									System.out.print(" " + q + "=" + sl.getQualifierValue(q) );
//								}
//								System.out.println("");
//							}
						}	
						cpt++;
					}
				} catch (Exception e) {
					Messenger.printStackTrace(e);
					errors(qn);
				}
			}
		}
		Database.close();
		
	}



}
