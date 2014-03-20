package saadadb.products.validation;


public class ObscoreKWSet extends KeywordsBuilder {

	public ObscoreKWSet(String[][] ahDef) throws Exception {
		super(ahDef);
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.validation.KeywordsBuilder#getInstance()
	 */
	public static KeywordsBuilder getInstance(Object object) throws Exception {
		 /*
		  *  0: name, 1: unit; 2: ucd, 3(optional): value
		  */
		return new ObscoreKWSet(new String[][]{
				{"RA", "double", "", "23.67"},
				{"DEC", "double", "", "-56.9"},
				{"eMin", "KeV", "em.wl;stat.min", "1."},
				{"eMax", "KeV", "em.wl;stat.max", "2."},
				{"obsStart", "", "", "2014-02-12"},
				{"obsEnd", "", "", "2014-02-13"},
				{"collection", "string", "", "3XMM"},
				{"target", "string", "", "M33"},
				{"instrume", "string", "", "MOS1"},
				{"facility", "string", "", "XMM"}
		}	);
	}

}
