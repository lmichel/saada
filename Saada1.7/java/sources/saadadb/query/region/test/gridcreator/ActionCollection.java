package saadadb.query.region.test.gridcreator;



/**
 * Class ActionCollection allows to do a lot of things on the collection depending on the arguments:
 * The FillTable don't work at the pole !!
 * 
 * Create a table
 * Delete a table
 * Empty a table
 * 
 * Populate a table with a region in parameter
 * Fill a table with pixels
 * 
 * @author jremy
 * @version $Id$
 * 
 * nomBDD=args[0];
 * nomCOL=args[1];
 * choix=args[2];
 * 
 * if your choice is to populate or to populate and fill with pixels a table, you have to add arguments
 * args[3] => number of nuages to create
 * 
 * args[4] => ra of the center of the population
 * args[5] => dec of the center of the population
 * args[6] => half size of a side of the square in degrees
 * 
 * args[7] => ra of the center of the population of the next nuage
 * args[8] => dec of the center of the population of the next nuage
 * args[9] => half size of a side of the square of the next nuage
 * etc..
 *
 */
public class ActionCollection {

	/**
	 * Method population
	 * Allows to populate a collection of the database with the argument
	 * @param tabArg : String[]
	 * @throws Exception
	 */
	public static void populate(String [] tabArg) throws Exception {
		String nomCOL=tabArg[1];
		if (tabArg.length > 3) {
			int nb = Integer.parseInt(tabArg[3]);
			Nuage[] tab =new Nuage[nb];
			int k=0;
			int i=4;
			while (k<nb) {
				int ra=Integer.parseInt(tabArg[i]);

				int dec=Integer.parseInt(tabArg[i+1]);

				int t=Integer.parseInt(tabArg[i+2]);

				String nomVOT="vot"+"_"+ra+"_"+dec+"_"+t;
				tab[k]=new Nuage(ra,dec,t, nomCOL ,nomVOT);

				k++;
				i+=3;


			}


			for(int j=1;j<=nb;j++) {
				String [] arg_exec = new String[5];
				//Recuperation des valeurs du nuage
				arg_exec[0]=tab[j-1].getPos();
				arg_exec[1]=String.valueOf(tab[j-1].getTaille());
				arg_exec[2]=tab[j-1].getNomCol();
				arg_exec[3]=tab[j-1].getNomVot();
				//Nom de la BDD
				arg_exec[4]=tabArg[0];
				GridBuilder.execute(arg_exec);
			}
		}
	}

	public static void main (String [] args) throws Exception {
		String nomBDD=args[0];
		String nomCOL=args[1];
		String choix=args[2];

		//Vide la collection et la remplit à nouveau
		if( "emptypop".equals(choix) ){
			new GridBuilder("empty",nomBDD,nomCOL);
			ActionCollection.populate(args);
		}

		//Vide la collection, la remplit, et met a jour les données Healpix via une table TMP
		else if( "emptypopfill".equals(choix) ) {
			new GridBuilder("empty",nomBDD,nomCOL);
			long a = System.currentTimeMillis();
			ActionCollection.populate(args);
			long b = System.currentTimeMillis();
			System.out.println("Remplissage table intermédiaire");
			FillTablePix.execute(nomCOL,nomBDD);
			System.out.println("Terminé");
			long d = System.currentTimeMillis();
			System.out.println("Remplissage table original");
			UpdateHPXTMPSQL.execute(nomCOL,nomBDD);
			System.out.println("Terminé");
			System.out.println("");
			long e = System.currentTimeMillis();
			System.out.println("Temps ajout données base : "+TpsExec.getTime(b-a));
			System.out.println("Temps insertion Table TMP : "+TpsExec.getTime(d-b));
			System.out.println("Temps mis a jour colonne Healpix : "+TpsExec.getTime(e-d));
			System.out.println("Temps total : "+TpsExec.getTime(e-a));
		}

		//Met uniquement a jour les colonnes Healpix de la table
		else if( "emptypopfill".equals(choix) ){
			long i = System.currentTimeMillis();
			System.out.println("Remplissage table intermédiaire");
			FillTablePix.execute(nomCOL,nomBDD);
			System.out.println("Terminé");
			long k = System.currentTimeMillis();
			System.out.println("Remplissage table original");
			UpdateHPXTMPSQL.execute(nomCOL,nomBDD);
			System.out.println("Terminé");
			System.out.println("");
			long l = System.currentTimeMillis();
			System.out.println("Temps insertion Table TMP : "+TpsExec.getTime(k-i));
			System.out.println("Temps mis a jour colonne Healpix : "+TpsExec.getTime(l-k));
			System.out.println("Temps total : "+TpsExec.getTime(l-i));
		}
		//Ajoute des données dans la collection donnée selon les paramètres données
		else if( "pop".equals(choix) ){
			long g = System.currentTimeMillis();
			ActionCollection.populate(args);
			long h = System.currentTimeMillis();
			System.out.println("Temps ajout données base : "+TpsExec.getTime(h-g));
		}
		else {
			System.out.println("Mauvais Argument");
		}
	}
}
