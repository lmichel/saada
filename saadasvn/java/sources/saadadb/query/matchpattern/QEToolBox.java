package saadadb.query.matchpattern;

import java.util.Arrays;

import saadadb.query.parser.Operator;
import saadadb.relationship.DoubleCPIndex;
import saadadb.relationship.KeyIndex;
import saadadb.relationship.LongCPIndex;
import saadadb.util.Messenger;

/**
 * Povide some usefull tools for the Query Engine of Saada
 * <p>
 * <b>WARNING:</b> for all operations involving Index and BitSet, List (of Long,Double) and/or BitSet
 *                 must have the same order! (Double 1 of a list = Long(OID) 1 of an other list = bit 1 of a BitSet).
 *                 See the method used to load Indexes to assure this is the case!
 *
 *
 * <a href="http://amwdb.u-strasbg.fr/saada">Saada Web Site</a>.
 *
 * @author   Pineau Francois-Xavier
 * @version  1.33, 25/10/2006
 * @see	    <tt>nothing</tt> 
 **/
public abstract class QEToolBox{

	/**
	 * Returns <tt>true</tt> if the specified table contains the specified element.
	 * If the specified table is <b>null</b>, false is returned.
	 *
	 * @param toFind   element whose presence in the specified table is to be tested.
	 * @param searchIn table where we are looking for the specifier element
	 * @return <tt>true</tt> if the specified table contains the specified element.
	 */
	public static boolean isInList(String toFind,String[] searchIn){
		if(searchIn==null) return false;
		for(int i=0;i<searchIn.length;i++){
			if(searchIn[i].equals(toFind)) return true;
		}
		return false;
	}

	/**
	 * Returns the <tt></tt> <tt>Set</tt> oid_PList without OIDs present in oid_MP. 
	 * &lt=&gt oid_PList substract oid_MP.
	 * <p>
	 * <b>WARNING:</b> oid_MP is cleared by the function! 
	 *
	 * @param oid_PList oid main list.
	 * @param oid_MP    contains oids to substract to main list if present.
	 * @return oid_PList substract oid_MP.
	 */
	public static KeyIndex fusionOIDForCard_0(KeyIndex oid_PList,KeyIndex oid_MP){
		/*
		 * select primaries not selected in secondary
		 */
		oid_PList.unselectedKeysFusion(oid_MP);
		return oid_PList;
	}

	/**
	 * Returns a <tt>Set (HashSet)</tt> containing all OIDs from the specified Index Counter Cardinality
	 * (strucutre like Map&lt;Integer,Collection&lt;Long&gt;&gt;) that meets the condition posed by the specified Card.
	 * <p>
	 * <b>WARNING:</b> The specified <tt>Map</tt> is cleared. 
	 */
	public static KeyIndex getOIDsFromIndexCardAndCard(KeyIndex indexC,Card card){
		int v1 = card.getVal1();
		int v2 = card.getVal2();
		switch(card.getOp()){
		case Operator.EQ:
			indexC.selectKey(v1);
			break;
		case Operator.GT:
			indexC.selectKeysGreaterThan(v1);
			break;
		case Operator.GE:
			indexC.selectKeysEGreaterThan(v1);
			break;
		case Operator.LT:
			indexC.selectKeysLowerThan(v1);
			break;
		case Operator.LE:
			indexC.selectKeysELowerThan(v1);
			break;
		case Operator.NE:
			indexC.selectKeysOutOfRange(v1, v1);
			break;
		case Operator.IN:
			indexC.selectKeysInERange(v1, v2);
			break;
		case Operator.IN_S:
			indexC.selectKeysInRange(v1, v2);
			break;
		case Operator.OUT:
			indexC.selectKeysOutOfERange(v1, v2);
			break;
		case Operator.OUT_S:
			indexC.selectKeysOutOfRange(v1, v2);
			break;
		default: Messenger.printMsg(Messenger.ERROR, "Operator number "+card.getOp()+" unknown in function \"getOIDsFromIndexCPAndCard()\"");
		break;
		}
		long[] selected_cp = indexC.getLongCPOfSelectedKeys();
		Arrays.sort(selected_cp);
		return new KeyIndex(selected_cp);
	}


	/**
	 * Returns a <tt>Set</tt> contaning all <tt>Long</tt> of the specified <tt>Map</tt> entry
	 * which associed Long Collection "INTERSECT" specified Long Set meets the condition put on cardinality.
	 *
	 *
	 */
	public static KeyIndex getOIDFromIndexAndCP(KeyIndex index_correlation, KeyIndex oid_CP,Card card){
		index_correlation.rejectAllKeys();
		index_correlation.selectCPMatchingList(oid_CP.getKeys(),false);
		switch(card.getOp()){
		case Operator.EQ:
			index_correlation.selectKeysOnCPCardEqualsTo(card.getVal1());
			break;
		case Operator.NE:
			index_correlation.selectKeysOnCPCardOutOfRange(card.getVal1(), card.getVal1());
			break;
		case Operator.GT:
			index_correlation.selectKeysOnCPCardGreaterThan(card.getVal1());
			break;
		case Operator.GE:
			index_correlation.selectKeysOnCPCardGreaterThan(card.getVal1()-1);
			break;
		case Operator.LT:
			index_correlation.selectKeysOnCPCardLowerThan(card.getVal1());
			break;
		case Operator.LE:
			index_correlation.selectKeysOnCPCardLowerThan(card.getVal1()+1);
			break;
		case Operator.IN:
			index_correlation.selectKeysOnCPCardInERange(card.getVal1(), card.getVal2());
			break;
		case Operator.IN_S:
			index_correlation.selectKeysOnCPCardInRange(card.getVal1(), card.getVal2());
			break;
		case Operator.OUT:
			index_correlation.selectKeysOnCPCardOutOfERange(card.getVal1(), card.getVal2());
			break;
		case Operator.OUT_S:
			index_correlation.selectKeysOnCPCardOutOfRange(card.getVal1(), card.getVal2());
			break;
		default: Messenger.printMsg(Messenger.ERROR, "Operator number "+card.getOp()+" unknown in function \"getOIDsFromIndexCPAndCard()\"");
		}
		return index_correlation;
	}



	/**
	 * Returns a <tt>Set (HashSet)</tt> containing all OIDs from the specified Index BitSet that meets condition posed by
	 * the specified Card.
	 * <p>
	 * <b>WARNING:</b> The specified <tt>Map</tt> is cleared. 
	 *
	 */
	public static KeyIndex getOIDsFromIndexBitSetAndCard(KeyIndex indexBS,Card card){
		int v1 = card.getVal1();
		int v2 = card.getVal2();
		switch(card.getOp()){
		case Operator.EQ:
			indexBS.selectKeysOnCPCardEqualsTo(v1);
			break;
		case Operator.GT:
			indexBS.selectKeysOnCPCardGreaterThan(v1);
			break;
		case Operator.GE:
			indexBS.selectKeysOnCPCardEGreaterThan(v1);
			break;
		case Operator.LT:
			indexBS.selectKeysOnCPCardLowerThan(v1);
			break;
		case Operator.LE:
			indexBS.selectKeysOnCPCardELowerThan(v1);
			break;
		case Operator.NE:
			indexBS.selectKeysOnCPCardOutOfRange(v1, v1);
			break;
		case Operator.IN:
			indexBS.selectKeysOnCPCardInERange(v1, v2);
			break;
		case Operator.OUT:
			indexBS.selectKeysOnCPCardOutOfERange(v1, v2);
			break;
		case Operator.IN_S:
			indexBS.selectKeysOnCPCardInRange(v1, v2);
			break;
		case Operator.OUT_S:
			indexBS.selectKeysOnCPCardOutOfRange(v1, v2);
			break;
		default: Messenger.printMsg(Messenger.ERROR, "Operator number "+card.getOp()+" unknowed in function \"getOIDsFromIndexBitSetAndCardCondition()\"");
		}
		return indexBS;
	}



	/**
	 * Returns a <tt>BitSet</tt> Index (structure like <tt>Map&lt;Long,BitSet&gt;</tt>) resulting from
	 * the fusion of two other <tt>BitSet</tt> Index.<p>
	 * 
	 * <b>Exemple</b>:<br>
	 *   &nbsp;&nbsp;&nbsp;&nbsp;<i>bs1:</i>             L1->{1,1,0,1} ; L2->{0,0,0,1}<br>
	 *   &nbsp;&nbsp;&nbsp;&nbsp;<i>bs2:</i>             L1->{0,1,1,1} ; L2->{1,0,1,0}<br>
	 *   &nbsp;&nbsp;&nbsp;&nbsp;<i><b>returns:</b></i>  L1->{0,1,0,1} ; L2->{0,0,0,0}
	 * <p> 
	 * <b>WARNING:</b><br>
	 *   &nbsp;&nbsp; 1) The two specified <tt>BitSte</tt> Indexes are modified by the method (one is returned,
	 *                   the other is clear)!<br> 
	 *   &nbsp;&nbsp; 2) Two <tt>BitSte</tt> associed with the same Key must have the same size!
	 *
	 * @return  A <tt>BitSet</tt> Index (same structure as bs1) where for each Key the BitSet
	 *          is the result of a logical "AND" between <tt>BitSet</tt>
	 *          associed with that Key in specified <tt>BitSet</tt> Indexes   
	 */
	public static KeyIndex fusionIndexBitSet(KeyIndex ref_index, KeyIndex append_index) {//Methode testee independament
		//append_index.selectAllCP(false);
		for( int i=0 ; i<append_index.getSize() ; i++ ) {
			long key = append_index.getKey(i);
			ref_index.cpTagFusion(ref_index.hasDichotoKey(key, false), append_index.getCPTag(i));
		}
		return ref_index;
	}

	/**
	 * Returns a <tt>BitSet</tt> Index from a specified Qualifier (strucutre like <tt>Map&lt;Long,List&lt;Double&gt;&gt;</tt>)
	 * Index and a condition posed by the specified <tt>Qualif</tt>.<p>
	 * 
	 * <b>NOTE</b> this function is an iteration ousing the function <i>getBitSetFromListAndQual()</i>. See it
	 *             for more information.
	 *
	 * @return a <tt>BitSet</tt> Index (structure like <tt>Map\<Long,BitSet\></tt>)
	 */
	public static KeyIndex getIndexBitSet(KeyIndex index ,Qualif qual){
		double q1 = qual.getVal1(), q2 = qual.getVal2();
		switch(qual.getOp()){
		case  Operator.EQ:
			index.selectCPEqualsTo(q1, false);
			break;
		case  Operator.NE:
			index.selectCPOutOfRange(q1, q1, false);
			break;
		case  Operator.GE:
			index.selectCPEGreaterThan(q1, false);
			break;
		case  Operator.GT:
			index.selectCPGreaterThan(q1,false);
			break;
		case  Operator.LT:
			index.selectCPLowerThan(q1, false);
			break;
		case  Operator.LE:
			index.selectCPELowerThan(q1,false);
			break;
		case  Operator.IN:
			index.selectCPInERange(q1, q2, false);
			break;
		case  Operator.OUT:
			index.selectCPOutOfERange(q1, q2, false);
			break;
		case  Operator.IN_S:
			index.selectCPInRange(q1, q2, false);
			break;
		case  Operator.OUT_S:
			index.selectCPOutOfRange(q1, q2, false);
			break;
		}
		return index;
	}

	/**
	 * Returns a <tt>BitSet</tt> Index from a specified Qualifier (strucutre like <tt>Map&lt;Long,List&lt;Double&gt;&gt;</tt>)
	 * Index and a condition posed by the specified <tt>Qualif</tt>.<p>
	 * 
	 * <b>NOTE</b> this function is an iteration ousing the function <i>getBitSetFromListAndQual()</i>. See it
	 *             for more information.
	 *
	 * @return a <tt>BitSet</tt> Index (structure like <tt>Map\<Long,BitSet\></tt>)
	 */
	public static KeyIndex getIndexBitSet(KeyIndex index ,Card card){
		int q1 = card.getVal1(), q2 = card.getVal2();
		switch(card.getOp()){
		case  Operator.EQ:
			index.selectCPEqualsTo(q1, false);
			break;
		case  Operator.NE:
			index.selectCPOutOfRange(q1, q1, false);
			break;
		case  Operator.GE:
			index.selectCPEGreaterThan(q1, false);
			break;
		case  Operator.GT:
			index.selectCPGreaterThan(q1,false);
			break;
		case  Operator.LT:
			index.selectCPLowerThan(q1, false);
			break;
		case  Operator.LE:
			index.selectCPELowerThan(q1,false);
			break;
		case  Operator.IN_S:
			index.selectCPInRange(q1, q2, false);
			break;
		case  Operator.OUT_S:
			index.selectCPOutOfRange(q1, q2, false);
			break;
		case  Operator.IN:
			index.selectCPInERange(q1, q2, false);
			break;
		case  Operator.OUT:
			index.selectCPOutOfERange(q1, q2, false);
			break;
		}
		return index;
	}

	/**
	 * Returns a <tt>Map</tt> containing for each Key of the two specified <tt>Map</tt> a <tt>Collection</tt> view
	 * of all <tt>Object</tt> present in <tt>Collection</tt> values associated with the same Key.
	 * If a Key of one of the <tt>Map</tt> is not present in the second one, the corresponding <tt>Entry</tt>
	 * of the returned <tt>Map</tt> is the same as the one of the <tt>Map</tt> containing the Key.<p>
	 *
	 * <b>WARNING:</b> the two specified <tt>Map</tt> are modified by the method (one is returned, the other is clear)! 
	 * 
	 * @param   paramA a <tt>Map</tt> which structure is like <tt>Map&lt;Object,Collection&lt;Object&gt;&gt;</tt>
	 * @param   paramB a <tt>Map</tt> which structure is the same as <tt>paramA</tt> (same type of <tt>Object</tt>)
	 * @return  A <tt>Map</tt> where each Key is associed with a <tt>Collection</tt> containing all Objects present in <Collection> value(s) associed with that Key in M1 and/or M2 
	 */
	public static KeyIndex unionWithDifferentKey(KeyIndex ref_index, LongCPIndex append_index){ //Methode testee independament
		for( int i=0 ; i<append_index.getSize() ; i++ ) {
			ref_index.addCounterparts(append_index.getKey(i), append_index.getLongCP(i));
		}
		return ref_index;
	}
	public static KeyIndex unionWithDifferentKey(DoubleCPIndex ref_index, DoubleCPIndex append_index){ //Methode testee independament
		for( int i=0 ; i<append_index.getSize() ; i++ ) {
			ref_index.addCounterparts(append_index.getKey(i), append_index.getDoubleCP(i));
		}
		return ref_index;
	}

	/**
	 * Returns a <tt>Set</tt> containing elements present in both the two specified <tt>Set</tt>.
	 * We use here the method <i>retainAll</i>.<p> 
	 *
	 * <b>NOTE:</b><br>
	 *  &nbsp;&nbsp;&nbsp;&nbsp; 1) If specified <tt>Set</tt> <i>paramA</i> is a <tt>LinkedHashSet</tt> its order is conserved.<br>
	 *  &nbsp;&nbsp;&nbsp;&nbsp; 2) If you have two <tt>LinkedHashSet<tt>, put the smaller as <i>paramA</i>
	 *                (if you don't care the order of <i>paramB</i>).<p>
	 *
	 * <b>WARNING:</b> the two specified <tt>Set</tt> are modified by the method
	 *                 (one is returned with modifications, the other is clear)! 
	 *
	 * @param   paramA a <tt>Set</tt>
	 * @param   paramB a <tt>Set</tt> which <tt>Object</tt>s are the same as <i>paramA</i> ones
	 * @return  A <tt>Map</tt> where each Key is associed with a <tt>Collection</tt> containing all Objects
	 *          present in <Collection> value(s) associed with that Key in M1 and/or M2 
	 */
	public static KeyIndex fusionOID(KeyIndex paramA, KeyIndex paramB){//isInstance(new LinkedHashSet()))  
		paramA.selectedKeysFusion(paramB);
		return paramA;
	}
}
