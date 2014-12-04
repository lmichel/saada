package saadadb.products.mergeandcast;

/**
 * Down casting mode used by the class merger
 * The Enum DowCasting.
 */
public enum DownCasting {
	
	/** The Bool 2 string. */
	Bool2String,
	
	/** The Bool 2 int. */
	Bool2Int,
	
	/** The Bool 2 double. */
	Bool2Double,
	
	/** The Int 2 double. */
	Int2Double,
	
	/** The Int 2 string. */
	Int2String,
	/** The Double2 string. */
	
	Double2String,
	/*
	 * The following cast mode are less typed, they are used for SQL queries
	 */
	
	/** The Bool2 num. */
	Bool2Num,
	
	/** The Nul2 string. */
	Num2String,
	NoCast

}
