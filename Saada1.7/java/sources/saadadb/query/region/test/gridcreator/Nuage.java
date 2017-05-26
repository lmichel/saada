package saadadb.query.region.test.gridcreator;

/**
 * Class Nuage representing the principal attribute to fill a collection 
 * @author jremy
 * @version $Id$
 *
 */
public class Nuage {

	private int ra_init;

	private int dec_init;

	private int taille;

	private String nom_collection;

	private String nom_vot;

	public Nuage (int ra, int dec, int t, String nom, String nomVot) {
		ra_init=ra;
		dec_init=dec;
		taille=t;
		nom_collection=nom;
		nom_vot=nomVot;
	}

	public int getRa_init() {
		return ra_init;
	}

	public int getDec_init() {
		return dec_init;
	}

	public int getTaille() {
		return taille;
	}

	public String getPos() {
		return (String.valueOf(ra_init)+" "+String.valueOf(dec_init));
	}

	public String getNomCol() {
		return nom_collection;
	}

	public String getNomVot() {
		return nom_vot;
	}



}
