package preproc.classbuilder;

import java.util.LinkedHashMap;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.SchemaClassifierMapper;
import saadadb.dataloader.SchemaMapper;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;
import saadadb.vocabulary.enums.ClassifierMode;

public class ClassBuilder {

	public static void  main(String[] args)  {
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			String collection = ap.getCollection();
			Database.getCachemeta().getCollection(collection);
			Database.setAdminMode("saadmin");

			VOResource vor = new  VOResource("/home/michel/workspace/XCATDR3/config/vodm.ObsCore.xml");
			System.out.println(vor);
			LinkedHashMap<String, AttributeHandler> ahs = new LinkedHashMap<String, AttributeHandler> ();
			for( String group: vor.getGroups().keySet() ) {
				UTypeHandler[] uths = vor.getGroupUtypeHandlers(group);
				for( UTypeHandler uth: uths) {
					AttributeHandler ah = uth.getAttributeHandler();
					ahs.put(ah.getNameorg(), ah);
				}
			}
			ProductMapping ch = ap.getProductMapping();
			SchemaMapper mapper = new SchemaClassifierMapper(null, null, ch);
			mapper.createClassFromProduct(ClassifierMode.CLASSIFIER, ap.getClassName(), ahs);
		}
		catch(Exception e) {
			Messenger.printStackTrace(e);
		}
		Database.close();
	}
}
