package saadadb.query.region.test;



import saadadb.query.region.request.Cone;
import saadadb.query.region.request.Zone;
import saadadb.query.region.test.gridcreator.SaadaQLRequest;
import saadadb.util.Messenger;
import cds.astro.ICRS;

public class Test {

	public static void main(String[] args) throws Exception{
		Messenger.debug_mode=true;
		/*
		Set<Entry<String,Polygone>> array =(Set<Entry<String, Polygone>>) listPolygone.getNomPoly();
		
		for (Entry<String,Polygone> e: array) {
			
			String database = "clabase";
			String collection = "petite";
			String nomPolygone = e.getKey();
			System.out.println("Polygone "+nomPolygone+" traité");

			Polygone p = e.getValue();
			Region r = new Region(p,new ICRS());
			String where = r.getSQL();


		//Test.getFich(where, database, collection, nomPolygone);
		}
		*/
		
		String database = "clabase";
		String collection = "petite";
		String nomPolygone = "minicara";

		//Region r = new Region(listPolygone.getPoly(nomPolygone),new ICRS());
		Zone c= new Cone(0,0,0.0000001,new ICRS());
		String where = c.getSQL();

		//Test.getFich(where, database, collection, nomPolygone);
		
		
	}

	public static void getFich (String where, String database, String collection, String nomPolygone) throws Exception{
		String query = "query_Select ENTRY From * In "+collection+" WhereAttributeSaada {"+where+"}";
		String file_name="Region_"+nomPolygone;
		String [] args_exec= {query,"-debug_on",file_name,database};
		SaadaQLRequest.execute(args_exec);
	}
}



