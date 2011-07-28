package saadadb.database;

/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @version SAADA 1.0
 * @author: NGUYEN Ngoc Hoan
 * E-Mail: nguyen@saadadb.u-strasbg.fr</p>
 */
import java.lang.reflect.Method;

import saadadb.configuration.SaadaDBConf;
import saadadb.util.Messenger;

public class SaadaDB {

	public static SaadaDBConf getSaadaDBConfig() {
		try {
			Class cls = Class.forName("SaadaDBSystem");
			Method method = cls.getMethod("getSaadaDBConf", (Class[]) null);
			return (SaadaDBConf) method.invoke(cls.newInstance(),
					(Object[]) null);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			/*
			 * New DB gui must run without SaadaDBSystem class.
			 */
			//System.exit(1);
		}
		return null;
	}


}
