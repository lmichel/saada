package saadadb.resourcetest;


//
//------
//
//SAVOT Sample
//
//Author:  Andr� Schaaff
//Address: Centre de Donnees astronomiques de Strasbourg
//11 rue de l'Universite
//67000 STRASBOURG
//FRANCE
//Email:   schaaff@astro.u-strasbg.fr, question@simbad.u-strasbg.fr
//
//-------
//
//In accordance with the international conventions about intellectual
//property rights this software and associated documentation files
//(the "Software") is protected. The rightholder authorizes :
//the reproduction and representation as a private copy or for educational
//and research purposes outside any lucrative use,
//subject to the following conditions:
//
//The above copyright notice shall be included.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
//EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
//OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON INFRINGEMENT,
//LOSS OF DATA, LOSS OF PROFIT, LOSS OF BARGAIN OR IMPOSSIBILITY
//TO USE SUCH SOFWARE. IN NO EVENT SHALL THE RIGHTHOLDER BE LIABLE
//FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
//TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
//THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//For any other exploitation contact the rightholder.
//
//-----------
//
//Conformement aux conventions internationales relatives aux droits de
//propriete intellectuelle ce logiciel et sa documentation sont proteges.
//Le titulaire des droits autorise :
//la reproduction et la representation a titre de copie privee ou des fins
//d'enseignement et de recherche et en dehors de toute utilisation lucrative.
//Cette autorisation est faite sous les conditions suivantes :
//
//La mention du copyright portee ci-dessus devra etre clairement indiquee.
//
//LE LOGICIEL EST LIVRE "EN L'ETAT", SANS GARANTIE D'AUCUNE SORTE.
//LE TITULAIRE DES DROITS NE SAURAIT, EN AUCUN CAS ETRE TENU CONTRACTUELLEMENT
//OU DELICTUELLEMENT POUR RESPONSABLE DES DOMMAGES DIRECTS OU INDIRECTS
//(Y COMPRIS ET A TITRE PUREMENT ILLUSTRATIF ET NON LIMITATIF,
//LA PRIVATION DE JOUISSANCE DU LOGICIEL, LA PERTE DE DONNEES,
//LE MANQUE A GAGNER OU AUGMENTATION DE COUTS ET DEPENSES, LES PERTES
//D'EXPLOITATION,LES PERTES DE MARCHES OU TOUTES ACTIONS EN CONTREFACON)
//POUVANT RESULTER DE L'UTILISATION, DE LA MAUVAISE UTILISATION
//OU DE L'IMPOSSIBILITE D'UTILISER LE LOGICIEL, ALORS MEME
//QU'IL AURAIT ETE AVISE DE LA POSSIBILITE DE SURVENANCE DE TELS DOMMAGES.
//
//Pour toute autre utilisation contactez le titulaire des droits.
//

import java.io.IOException;

import cds.savot.model.FieldSet;
import cds.savot.model.ResourceSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.model.TableSet;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;
import cds.savot.writer.SavotWriter;

/**
 * <p>A sample designed to read and write an xml file </p>
 * @author Andre Schaaff
 * @version 2.7 Copyright CDS 2002-2008
 */
public class PullInOut {
	
	public static boolean statistics = true;
	
	
	public void showSomeMeta(SavotVOTable sv) {
		ResourceSet rs = sv.getResources();
		TableSet ts = ( (SavotResource) rs.getItemAt(0)).getTables();
		
		
		SavotResource sr = (SavotResource)(rs.getItemAt(0));
		System.out.println("Desc " + sr.getDescription());
		System.out.println("Params " + sr.getParams().getItemCount());
		System.out.println("table set " + ts.getItemCount());
		for (int k = 0; k < ts.getItemCount(); k++) {
			SavotTable table = (SavotTable) ts.getItemAt(k);
			
			FieldSet fs = table.getFields();
	          for( int i=0 ; i<fs.getItemCount() ; i++ ) {
	              SavotField sf = (SavotField)fs.getItemAt(i);
	              System.out.println(sf.getName() + " " + sf.getId() + " " + sf.getDescription());
	          }
	          if (table.getId() != null && !table.getId().equals("")) {
	          }

	          if (table.getName() != null && !table.getName().equals("")); 	
		}
		
		// etc. on peut par exemple acc�der aux Fields
	}
	
	
	/**
	 * Constructor
	 * This example is just designed to make statistics about time and memory use to put a whole VOTable file
	 * into memory
	 * @param file
	 * @param type
	 */
	public PullInOut(String file, int type) {
		
		try {
			
			// read the VOTable
			SavotPullParser sb = new SavotPullParser(file, type);
			
			SavotTR currentTR = sb.getNextTR();       
			
			showSomeMeta(sb.getVOTable());
			int cpt=0;
			// a ce stade le parser a lu et stocke ce qui procode la TABLE, en particulier la RESOURCE qui contient cette table, il est donc possible d'accoder aux FIELDs en particulier
			while (currentTR != null) {  
				cpt++;
				if( (cpt%1) == 0 ) {
					System.out.println(cpt);
				}
				// on verifie s'il s'agit du debut d'une table, ce qui permet de detecter une nouvelle table, sachant que toutes les metadonnees complementaires sont egalement en memoire comme pour la table precedente
				if (sb.getStatistics().iTRLocalGet() == 1) {
					// first row of a (new) TABLE
					//System.out.println("premiere ligne");
				}
				else { // la suite de la table
					// row of the current TABLE 
					//System.out.println("ligne suivante");
				}
				
				currentTR = sb.getNextTR();    
			}
			
			SavotWriter wd = new SavotWriter();
			
			// au final tu te retrouves avec le fichier de depart (qui se contruit au fut et a mesure et qui te permet d'acceder aux metadonnees concernant des donnees) mais sans le contenu des tables car tu les a traites au fil de l'eau
			//wd.generateDocument(sb.getAllResources(), "");
		}
		catch(Exception e) {System.err.println("PullInOut : " + e);};
	}
	
	/**
	 * Main method
	 * @param argv
	 * @throws IOException
	 */
	public static void main  (String [] argv) throws IOException{
		//new PullInOut("/home/data/bigdata/215.data/gvot_215.xml", SavotPullEngine.ROWREAD);
		new PullInOut("/home/michel/saada/deploy/TestBench1_5/013.load_xml_src/data/usno013.xml", SavotPullEngine.ROWREAD);
	}
}
