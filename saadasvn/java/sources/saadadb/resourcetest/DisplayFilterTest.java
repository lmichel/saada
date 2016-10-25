package saadadb.resourcetest;

import java.io.File;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import ajaxservlet.formator.DynamicClassDisplayFilter;
import ajaxservlet.formator.FilterBase;
import ajaxservlet.formator.StoredFilter;

public class DisplayFilterTest {
	public static String rawJson = "{"
	 + "\"saadaclass\": \"arch_0011AEntry\","
	 + "\"collection\": [\"ACDS\"],"
	 + "\"category\": \"ENTRY\","
	 + "\"relationship\": {"
	 + "	\"show\": [\"ArchSrcToEpicSrc\", \"ArchSrcToCatSrc\"],"
	 + "	\"query\": [\"Any-Relation\"]"
	 + "},"
	 + "\"ucd.show\": \"true\","
	 + "\"ucd.query\": \"false\","
	 + "\"specialField\": [\"Access\", \"Position\", \"Error (arcsec)\", \"Name\"],"
	 + "\"collections\": {"
	 + "	\"show\": [\"Any-Coll-Att\", \"Any-Class-Att\"],"
	 + "	\"query\": [\"\"]"
	 + "}"
	 + "}";
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		StoredFilter sf = new StoredFilter(rawJson);
		DynamicClassDisplayFilter ddf = new DynamicClassDisplayFilter(sf, "ACDS", "arch_0011AEntry");
		System.out.println(sf);
		System.out.println(ddf);
		
		sf.store(FilterBase.filterDirectory + File.separator + "df." +sf.getTreepath() + ".json");
		
		FilterBase.init(false);
		sf = FilterBase.getVisibleColumnsFilter("ACDS", "ENTRY");
		System.out.println(sf);
		sf = FilterBase.getVisibleColumnsFilter("ACDS", "ENTRY", "arch_0011AEntry");
		System.out.println(sf);
		Database.close();
	}
}
