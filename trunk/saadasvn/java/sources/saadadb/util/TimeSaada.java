package saadadb.util;
/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001
 */
/** Ceci est un petit chronometre qui mesure le temps ecoule entre 2 instants t1 et t2 en ms */
public class TimeSaada {
    long timeDeb;
    long timeInter;
    long timeFin;
    boolean etat;
    /** Constructeur
     */
    public TimeSaada() {
	timeDeb = System.currentTimeMillis();
	timeInter = timeDeb;
	etat = true;
    }
    /** Fonction qui sert, comme son nom l'indique, a mettre en route le chrono
     * * @version $Id$
/
    public void start() {
	timeDeb = System.currentTimeMillis();
	timeInter = timeDeb;
	etat = true;
    }
    /** Fonction qui sert, comme son nom l'indique, a arreter le chrono
     */
    public void stop() {
	timeFin = System.currentTimeMillis();
	etat = false;
    }
    /** Fonction qui sert a recuperer le temps ecoule depuis que l'on a mis en route le chrono
     */
    public long check() {
	if(etat){
	    timeFin = System.currentTimeMillis();
	    return timeFin - timeDeb;
	}else{
	    return  timeFin - timeDeb;
	}
    }
    /** Fonction qui sert a recuperer le temps ecoule depuis le dernier
     * appel de cette fonction ou depuis la mise en marche du chrono
     */
    public long checkInter() {
	if(etat){
	    timeFin = System.currentTimeMillis();
	    long diff = timeFin - timeInter;
	    timeInter = timeFin;
	    return  diff;
	}else{
	    return 0;
	}
    }
}
  
