package saadadb.products.inference;


//
//Copyright 1999-2000 - Universite Louis Pasteur / Centre National de la
//Recherche Scientifique
//
//------
//
//ALADIN JAVA
//
//Author:  Pierre Fernique
//Address: Centre de Donnees astronomiques de Strasbourg
//11 rue de l'Universite
//67000 STRASBOURG
//FRANCE
//Email:   fernique@astro.u-strasbg.fr, question@simbad.u-strasbg.fr
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

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Astrocoo;
import cds.astro.Astroframe;
import cds.astro.Coo;
import cds.astro.Ecliptic;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.astro.ICRS;

/** * @version $Id$

 * Manipulation des coordonnees
 *
 * @author Francois Bonnarel [CDS], Pierre Fernique [CDS]
 * @version 1.0 : (5 mai 99) Toilettage du code
 * @version 0.9 : (??) creation
 */
public final class Coord {
	/** Ascension droite (J2000 en degres) */
	protected double al =SaadaConstant.DOUBLE;;
	/** Declinaison (J2000 en degres) */
	protected double del = SaadaConstant.DOUBLE;
	/** abcisse courante dans une projection (en reel) */
	protected double x = SaadaConstant.DOUBLE;
	protected double dx = SaadaConstant.DOUBLE;
	/** ordonnee courante dans une projection (en reel)*/
	protected double y = SaadaConstant.DOUBLE;
	protected double dy = SaadaConstant.DOUBLE;
	/** 1ere coordonnee standard */
	protected double xstand = SaadaConstant.DOUBLE;
	/** 2eme coordonnee standard */
	protected double ystand = SaadaConstant.DOUBLE;
	
	/** Creation */
	public Coord() {}
	public Coord(double ra,double dej) { al=ra; del=dej;}
	
	/**
	 * @param sys
	 * @param equi
	 * @return
	 * @throws FatalException 
	 */
	public static Astroframe getAstroframe(String sys, String equi) throws FatalException {
		if( sys.equalsIgnoreCase("FK4") ) {
			if( equi == null ) {
				Messenger.printMsg(Messenger.TRACE, "No equinox takes, (B1950.0,Ep=J2000.0) by default");
				return new FK4();				
			}
			else {
				return new FK4(Double.parseDouble(equi.replaceAll("J", "")));
			}
		}
		else if( sys.equalsIgnoreCase("FK5") ) {
			if( equi == null ) {
				Messenger.printMsg(Messenger.TRACE, "No equinox takes, J2000 by default");
				return new FK5();				
			}
			else {
				return new FK5(Double.parseDouble(equi.replaceAll("J", "")));
			}
		}
		else if( sys.equalsIgnoreCase("ICRS") ) {
			return  new ICRS();
		}
		else if( sys.equalsIgnoreCase("galactic") ) {
			return new Galactic();
		}
		else if( sys.equalsIgnoreCase("ecliptic") ) {
			return new Ecliptic();
		}
		else {
			FatalException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Unsupported coordinate system <" + sys + ">");
			return null;
		} 
	}
	
	/**
	 * @param astro_org
	 * @param coord_org
	 * @param astro_new
	 * @return
	 */
	public static double[] convert(Astroframe astro_org, double[] coord_org, Astroframe astro_new) {
		Astrocoo acoo= new Astrocoo(astro_org, coord_org[0], coord_org[1]) ;
		acoo.convertTo(astro_new);
		return (new double[]{acoo.getLon(), acoo.getLat()});	  
	}
	
	public double getPOS_RA()
	{
		return this.al;
	}
	
	public double getPOS_DEC()
	{
		return this.del;
	}
	
	public double getX()
	{
		return this.x;
	}
	public double getY()
	{
		return this.y;
	}
		
	public void setXY(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/** Affichage dans la bonne unite.
	 * Retourne un angle en degres sous forme de chaine dans la bonne unite
	 * @param x l'angle
	 * @return l'angle dans une unite coherente + l'unite utilisee
	 */
	protected static String getUnit(double x) {
		String s=null;
		boolean flagCeil=true;
		if( x>=1.0 ) s="deg";
		if( x<1.0 ) { s="'"; x=x*60.0; }
		if( x<1.0 ) { s="\""; x=x*60.0; flagCeil=false; }
		if( flagCeil ) x=Math.ceil(x*100.0)/100.0;
		else x=Math.ceil(x*10000.0)/10000.0;
		s=x+s;
		
		return s;
	}
	
	/** Affichage dans la bonne unite (H:M:S).
	 * Retourne un angle en degres sous forme de chaine dans la bonne unite
	 * @param x l'angle
	 * @return l'angle dans une unite coherente + l'unite utilisee
	 */
	protected static String getUnitTime(double x) {
		String s=null;
		if( x>=1.0 ) s="h";
		if( x<1.0 ) { s="min"; x=x*60.0; }
		if( x<1.0 ) { s="s"; x=x*60.0; }
		x=((int)(x*100.0))/100.0;
		s=x+" "+s;
		
		return s;
	}
	
	/** Calcul d'un distance entre deux points reperes par leurs coord
	 * @param c1 premier point
	 * @param c2 deuxieme point
	 * @return La distance angulaire en degres
	 protected static double getDist1(Coord c1, Coord c2) {
	 double dra = c2.al-c1.al;
	 double dde = Math.abs(c1.del-c2.del);
	 dra = Math.abs(dra);
	 if( dra>180 ) dra-=360;
	 double drac = dra*Astropos.cosd(c1.del);
	 return Math.sqrt(drac*drac+dde*dde);
	 }
	 */
	
	protected static double getDist(Coord c1, Coord c2) {
		return Coo.distance(c1.al,c1.del,c2.al,c2.del);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "x=" + this.x + " y=" + this.y + " ra=" + this.al + " dec=" + this.del ; 
	}
}



