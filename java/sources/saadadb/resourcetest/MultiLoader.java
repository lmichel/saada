package saadadb.resourcetest;

import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.sqltable.SQLTable;

public class MultiLoader {

	public static void main(String[] args) throws Exception {
		Database.init("Vizier");
		Database.setAdminMode(null);
		SQLTable.beginTransaction();
		(new CollectionManager("vizier")).empty(null);
		SQLTable.commitTransaction();
		ArgsParser ap = new ArgsParser(new String[]{
				"-classfusion=_home_landais",
				"-collection=vizier",
				"-category=spectrum",
				"-ukw",
				"extension='-1'",
				"-filename=/home/michel/Desktop/datasample/vizier/III_114_75_CYG.fits",
				"-spcmapping=first",
				"-timemapping=first",
				"-polarmapping=first",
				"-posmapping=first",
				"-obsmapping=first",
				"-repository=no",
				"-noindex",
				"-target=OBJECT",
				"-tmin=55197.0",
				"-tmax=56000.0",
				"-exptime=-1.262303944E9",
				"-instrument=INSTRUME",
				"Vizier"
		});
		Loader loader = new Loader(ap);
		loader.load();
		System.out.println("===========================");
		loader = new Loader(ap);
		loader.load();
		System.out.println("===========================");
		loader = new Loader(ap);
		loader.load();
		Database.close();
	}

}
