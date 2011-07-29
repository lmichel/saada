package preproc.classbuilder;

import java.util.LinkedHashMap;

import saadadb.classmapping.TypeMapping;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.SchemaClassifierMapper;
import saadadb.dataloader.SchemaMapper;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class ClassBuilder {

	public static void  main(String[] args)  {
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			String collection = ap.getCollection();
			Database.getCachemeta().getCollection(collection);
			Database.getConnector().setAdminMode("saadmin");

			VOResource vor = new  VOResource("/home/michel/workspace/XCATDR3/config/vodm.ObsCore.xml");
			System.out.println(vor);
			LinkedHashMap<String, AttributeHandler> ahs = new LinkedHashMap<String, AttributeHandler> ();
			for( String group: vor.getGroups().keySet() ) {
				UTypeHandler[] uths = vor.getGroupUtypeHandlers(group);
				for( UTypeHandler uth: uths) {
					AttributeHandler ah = uth.getAttributeHandlerr();
					ahs.put(ah.getNameorg(), ah);
				}
			}
			ConfigurationDefaultHandler ch = ap.getConfiguration();
			SchemaMapper mapper = new SchemaClassifierMapper(null, null, ch, false);
			mapper.createClassFromProduct(TypeMapping.MAPPING_CLASSIFIER, ap.getClassName(), ahs);
		}
		catch(Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
		}
	}
}
