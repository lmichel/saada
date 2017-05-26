package saadadb.kdtree;

import java.util.Comparator;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public final class KCooComparatorManager {
	private final KCooComparator[] cTab;
	
	public KCooComparatorManager(int k){
		cTab = new KCooComparator[k];
		for(int i=0;i<k;i++) cTab[i] = new KCooComparator(i);
	}
	public final Comparator<HasKCoo> getComparator(int i){ return cTab[i]; }
	
	private final class KCooComparator implements Comparator<HasKCoo> {
		private final int ind;
		private KCooComparator(int i){
			this.ind = i;
		}
		public final int compare(HasKCoo hasKCoo1, HasKCoo hasKCoo2) {
			final double coo1 = hasKCoo1.coo(this.ind);
			final double coo2 = hasKCoo2.coo(this.ind);
			return coo1<coo2 ? -1 : coo1>coo2 ? 1 : 0 ;
		}
	}
}
