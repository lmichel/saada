package saadadb.query.matchpattern;

import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import saadadb.api.SaadaLink;
import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaRelation;
import saadadb.query.executor.Query;
import saadadb.query.executor.Query_Report;
import saadadb.query.executor.Report;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.relationship.DoubleCPIndex;
import saadadb.relationship.KeyIndex;
import saadadb.relationship.LongCPIndex;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;
import saadadb.util.TimeSaada;

/** * @version $Id$

 * @author F.X. Pineau, L. Michel
 */
public class EPOnePattern extends Query_Report {
	// EXECUTION MODE CARD=0/1(+8) QUAL=0/1(+4) CLASS=0/1(+2) CP=0/1(+1)
	//Code grey
	public static final int NOTHING = 0;			// 0000
	public static final int CP = 1;					// 0001
	public static final int CLASS = 2;				// 0010
	public static final int CLASS_CP = 3;			// 0011
	public static final int QUAL = 4;				// 0100
	public static final int QUAL_CP = 5;			// 0101
	public static final int QUAL_CLASS = 6;			// 0110
	public static final int QUAL_CLASS_CP = 7;		// 0111
	public static final int CARD = 8;				// 1000
	public static final int CARD_CP = 9;			// 1001 // Implicitement das ce cas CP = seulement AssObjAttSaada()
	public static final int CARD_CLASS = 10;		// 1010
	public static final int CARD_CLASS_CP = 11;		// 1011
	public static final int CARD_QUAL = 12;			// 1100
	public static final int CARD_QUAL_CP = 13;		// 1101
	public static final int CARD_QUAL_CLASS = 14;	// 1110
	public static final int CARD_QUAL_CLASS_CP = 15;// 1111

	public  final boolean card_0;
	public  final int mode;
	private final long index_owner_key;
	private final MetaRelation metaRelation; // MetaRelation mr = Database.getCachemeta().getRelation(relationName);
	private final Card card;
	private final Qualif[] qualTab;
	private final MetaClass[] metaClassTab; // Database.getCachemeta().getClass(name).getId();


	private String counterPartsSQL;

	public EPOnePattern(String rname, Card card, Qualif[] qualT,String[] listClass,String cpSQL, long index_owner_key,Report report) throws SaadaException {
		super(report);
		this.index_owner_key = index_owner_key;
		this.metaRelation = Database.getCachemeta().getRelation(rname);
		if(this.metaRelation == null ) {
			QueryException.throwNewException(SaadaException.MISSING_RESOURCE, "Relation " + rname + " does not exist");
		}

		this.qualTab = qualT;
		if (listClass != null) {
			this.metaClassTab = new MetaClass[listClass.length];
			for (int i = 0; i < listClass.length; i++) {
				this.metaClassTab[i] = Database.getCachemeta().getClass(listClass[i]);
			}
		}else {this.metaClassTab = new MetaClass[0];}
		this.counterPartsSQL = cpSQL;
		int tmode = 0;
		if(card != null)                    {tmode += 8;}
		if(this.qualTab != null)         {tmode += 4;}
		if(this.metaClassTab.length != 0){tmode += 2;}
		if(this.counterPartsSQL != null) {tmode += 1;}
		this.mode = tmode;

		this.card_0 = (card!=null && card.card_0());
		/*
		 * If no explicit cardinality take card_1: means "with more than 0 counterparts
		 */
		this.card = (card==null)?Card.getCard1():card;
		//this.card   = (card==null || this.card_0)?Card.getCard1():card;
	}

	public final String getreport() {
		return this.report.getExecReport();
	}


	public MetaClass[] getMetaClassTab() {
		return metaClassTab;
	}
	/**
	 * @param index_correlation
	 * @param oid_CP
	 * @param card
	 * @return
	 */
	private final KeyIndex getOIDFromIndexAndCP(KeyIndex index_correlation,KeyIndex oid_CP, Card card) {
		this.report.addSentence("getOIDFromIndexAndCP: Select keys selected in index#1 ("
				+ oid_CP.getName()
				+ ") and matching the card constraint in index#2 (" 
				+ oid_CP.getName() 
				+ ")");
		KeyIndex ret = QEToolBox.getOIDFromIndexAndCP(index_correlation,
				oid_CP, card);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(ret.getSELECTEDKeys().length + " oid(s) selected ");
		}
		return ret;
	}

	/**
	 * @param oid_PList
	 * @param oid_MP
	 * @return
	 */
	private final KeyIndex fusionOID(KeyIndex oid_PList, KeyIndex oid_MP) {
		this.report.addSentence("fusionOID: Select OIDs selected in both index#1 ("
				+ oid_PList.getName() + ") and index#2 (" + oid_MP.getName()
				+ ")");
		KeyIndex ret = QEToolBox.fusionOID(oid_PList, oid_MP);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(ret.getSELECTEDKeys().length
					+ " oid(s) selected ");
		}
		return ret;
	}

	/**
	 * @param oid_PList
	 * @param oid_MP
	 * @return
	 */
	private final KeyIndex fusionOIDForCard_0(KeyIndex oid_PList, KeyIndex oid_MP) {
		this.report.addSentence("fusionOIDForCard_0: Select OIDs selected in index#1 ("
				+ oid_PList.getName() + " but not in indx#2 (" 
				+ oid_MP.getName() + ")");
		KeyIndex ret = QEToolBox.fusionOIDForCard_0(oid_PList, oid_MP);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(ret.getSELECTEDKeys().length + " oid(s) selected ");
		}
		return ret;
	}

	/**
	 * @param cardIndex
	 * @param card
	 * @return
	 * @throws QuerySaadaQLException
	 */
	private final KeyIndex getOIDsFromIndexCardAndCard(KeyIndex cardIndex, Card card) {
		this.report.addSentence("getOIDsFromIndexCardAndCard: Select OIDs in index#1 (" 
				+ cardIndex.getName() + ") in a given range");
		KeyIndex ret = QEToolBox.getOIDsFromIndexCardAndCard(cardIndex, card);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(ret.getSELECTEDKeys().length
					+ " oid(s) selected ");
		}
		return ret;
	}

	/**
	 * @param indexQ
	 * @param qual
	 * @return
	 */
	private final KeyIndex getIndexBitSet(KeyIndex indexQ, Qualif qual) {
		if (Messenger.debug_mode == true) {
			this.report.addSentence("getIndexBitSet: Select CP in index #1 ("
					+ indexQ.getName() 
					+ ") matching a qualifier constraint");
		}
		KeyIndex mp = QEToolBox.getIndexBitSet(indexQ, qual);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(mp.getSELECTEDKeys().length
					+ " oid(s) selected ");
		}
		return mp;
	}

	/**
	 * @param indexQ
	 * @param card
	 * @return
	 */
	private final KeyIndex getIndexBitSet(KeyIndex indexQ, Card card) {
		this.report.addSentence("getIndexBitSet: Select CP in index #1 ("
				+ indexQ.getName() 
				+ ") matching a cardinality constraint");
		KeyIndex mp = QEToolBox.getIndexBitSet(indexQ, card);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(mp.getSELECTEDKeys().length
					+ " oid(s) selected ");
		}
		return mp;
	}

	/**
	 * @param indexBS
	 * @param indexBS2
	 * @return
	 */
	private final KeyIndex fusionIndexBitSet(KeyIndex indexBS, KeyIndex indexBS2) {
		this.report.addSentence("fusionIndexBitSet: Fusion of selected CP in index#1 ("
				+ indexBS.getName() 
				+ ") and index#2 (" 
				+ indexBS2.getName()
				+")");
		KeyIndex mp = QEToolBox.fusionIndexBitSet(indexBS, indexBS2);
		return mp;
	}

	/**
	 * @param indexBS
	 * @param card
	 * @return
	 * @throws QuerySaadaQLException
	 */
	private final KeyIndex getOIDsFromIndexBitSetAndCard(KeyIndex indexBS, Card card) {
		this.report
		.addSentence("getOIDsFromIndexBitSetAndCard Select OIDs from cardinality on CP in index#1 ("
				+ indexBS.getName()
				+")");
		KeyIndex ret = QEToolBox.getOIDsFromIndexBitSetAndCard(indexBS, card);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(ret.getSELECTEDKeys().length
					+ " oid(s) selected ");
		}
		return ret;
	}


	/**
	 * @param m1
	 * @param m2
	 * @return
	 */
	private final KeyIndex unionWithDifferentKey(DoubleCPIndex m1, DoubleCPIndex m2) {
		this.report.addSentence("unionWithDifferentKey: Fusion of selected keys in index#1 ("+m1.getName()+ ") and idex#2 ("+ m2.getName()+")");
		TimeSaada time = new TimeSaada();
		time.start();
		KeyIndex mr = QEToolBox.unionWithDifferentKey(m1, m2);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(mr.getSELECTEDKeys().length
					+ " oid(s) selected " + time.check() + " ms");
		}
		return mr;
	}

	// SI oid_PList.size()=0 on considere que pas de requete principale, donc resultat integral
	//protected final KeyIndex execPattern(Set<Long> set_oid_PList)
	public final KeyIndex execPattern(KeyIndex previous_retour) throws Exception { // principal list
		if (this.card_0 && previous_retour!=null && !previous_retour.hasKeySELECTED()) {
			return previous_retour;
		}
		
		if (previous_retour!=null && Messenger.debug_mode) {
			Messenger.printMsg(Messenger.DEBUG, previous_retour.getSELECTEDKeySet().size() + " OIDS selected in previous pattern");
		}
		KeyIndex oid_MP = null;
		switch (this.mode) {
		case NOTHING:
			// SELECT oidprimary FROM relation;
			this.report.addSentence("case NOTHING");
			return this.NOTHING(previous_retour);
		case CARD:
			// SELECT oidsaada FROM indexcard WHERE card OPERATEUR
			this.report.addSentence("case CARD");
			return this.CARD(previous_retour);		
		case CP:
			this.report.addSentence("case CP");
		case CARD_CP:
			// SELECT oidprimary From relation
			// INNER JOIN ( requete counterparts) as CP
			// ON oidsecondary=CP.oidsaada
			// GROUP by oidprimary
			// HAVING count(*) condition_card
			this.report.addSentence("case CARD_CP");
			return this.CARD_CP(previous_retour);					
		case CARD_CLASS:
			// Index sous relation classe par classe (indexer tous les qualifers! Faire index card par classe) 
			this.report.addSentence("case CARD_CLASS");
			return this.CARD_CLASS(previous_retour);					
		case CLASS:
			this.report.addSentence("case CLASS");
			oid_MP = new LongCPIndex(Database.getCacheindex().getCorrIndex(this.metaRelation.getName(), this.index_owner_key).getKeys(),1);
			for(MetaClass mc:this.metaClassTab){
				KeyIndex corr_class_index = Database.getCacheindex().getCorrClassIndex(this.metaRelation.getName(),mc.getName(),this.index_owner_key);
				long keys[] = corr_class_index.getKeys();
				for (int j = 0; j < keys.length; j++) {
					long old_cp = oid_MP.getLongCP(keys[j])[0];
					oid_MP.setCounterparts(keys[j],new long[]{old_cp + corr_class_index.getLongCP(j).length});
				}
				corr_class_index.give();
			}
			oid_MP.selectCPGreaterThan(0L,false);
			oid_MP.selectKeysOnCPCardGreaterThan(0);
			return (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;
		case CLASS_CP:
			this.report.addSentence("case CLASS_CP");
		case CARD_CLASS_CP:
			this.report.addSentence("case CARD_CLASS_CP");
			return CARD_CLASS_CP(previous_retour);
		case QUAL:
			this.report.addSentence("case QUAL");
		case CARD_QUAL:
			this.report.addSentence("case CARD_QUAL");
			KeyIndex retour = CARD_QUAL(previous_retour);
			return retour;
		case QUAL_CLASS:
			this.report.addSentence("case QUAL_CLASS");
		case CARD_QUAL_CLASS:
			this.report.addSentence("case CARD_QUAL_CLASS");
			return CARD_QUAL_CLASS(previous_retour);
		case QUAL_CP:
			this.report.addSentence("case QUAL_CP");
		case CARD_QUAL_CP:// ici CP = atributeSaada seulement!
			this.report.addSentence("case CARD_QUAL_CP");
			return CARD_QUAL_CP(previous_retour);
		case QUAL_CLASS_CP:
			this.report.addSentence("case QUAL_CLASS_CP");
		case CARD_QUAL_CLASS_CP:
			this.report.addSentence("case CARD_QUAL_CLASS_CP");
			return CARD_QUAL_CLASS_CP(previous_retour);
		default:
			QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "No case found! (ExecPattern) in "+ getClass().getName());
		}
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "No case found! (ExecPattern) in "+ getClass().getName());
		return null;
	}

	/**
	 * Process pattern without explicit constraint: matchPattern {relation_name}
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex NOTHING(KeyIndex previous_retour) throws FatalException {
		KeyIndex oid_MP=Database.getCacheindex().getCorrIndex(this.metaRelation.getName(),this.index_owner_key);
		oid_MP.selectAllKeys();
		return (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;
	}

	/**
	 * Process patterns with just a cardinality constraint: 
	 * matchPattern {
	 *    relation_name
	 *    Cardinality ...
	 *    }
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex CARD(KeyIndex previous_retour) throws FatalException {
		KeyIndex oid_MP;
		/*
		 * Any card != 0
		 */
		if( this.card.anyNotNull() ){
			oid_MP=Database.getCacheindex().getCorrIndex(this.metaRelation.getName(),this.index_owner_key);
			oid_MP.selectAllKeys();
			return (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;
		}
		/*
		 * card = 0 or else
		 */
		else {
			oid_MP=this.getOIDsFromIndexCardAndCard(Database.getCacheindex().getCardIndex(this.metaRelation.getName(),this.index_owner_key),this.card);
			oid_MP.selectAllKeys();
			return (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;				
		}			
	}
	/**
	 * Process patterns with constraint on cardinality and on collection attributes of counterparts
	 * matchPattern {
	 *    relation_name
	 *    Cardinality ...
	 *    AssObjAttSaada{ .... }
	 *    }
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex CARD_CP(KeyIndex previous_retour) throws Exception {
		KeyIndex tmp = Database.getCacheindex().getCorrIndex(this.metaRelation.getName(),this.index_owner_key);
		//KeyIndex oid_CP = new KeyIndex(this.getOID_CP());
		SQLQuery squery = new SQLQuery();
		SaadaQLResultSet srs = new SaadaQLResultSet(squery.run(counterPartsSQL));
		KeyIndex oid_CP = new KeyIndex(srs);
		/*
		 * Check if the cadinlaity includes implicit null constraint e.g. card != 1
		 */
		boolean card_include_0 = this.card.includeCard0();
		/*
		 * Card not null and no implicit null constraint
		 */
		if( !this.card_0 && !card_include_0) {
			KeyIndex oid_MP = this.getOIDFromIndexAndCP(tmp,oid_CP,this.card); 
			squery.close();
			return (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;
		}
		else if( !this.card_0 && card_include_0) {
			/*
			 * Build a result KeyIndex with OIDs matching the not null cardinality constraint
			 */
			KeyIndex oid_MP = this.getOIDFromIndexAndCP(tmp,oid_CP,this.card); 
			KeyIndex not_null_oids = (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;
			/*
			 * Build a result KeyIndex with OIDs matching the null cardinality constraint
			 */
			oid_MP=this.getOIDsFromIndexCardAndCard(Database.getCacheindex().getCardIndex(this.metaRelation.getName(),this.index_owner_key), Card.getCard0());
			oid_MP.selectAllKeys();
			KeyIndex null_oids = (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;	
			squery.close();
			return mergeResultWithCardNullIncluded(not_null_oids, null_oids);
		}
		/*
		 * Card null 
		 */
		else {
			/*
			 * case of card 0: Select first cardinality != 0 and ...
			 */
			KeyIndex oid_MP = this.getOIDFromIndexAndCP(tmp,oid_CP,Card.getCard1());
			if(previous_retour==null){
				KeyIndex all = this.getOIDsFromIndexCardAndCard(Database.getCacheindex().getCardIndex(this.metaRelation.getName(),this.index_owner_key),Card.getCardAll());
				all.selectAllKeys();
				/*
				 * ...remove them from the whole index to keep cardinality = 0
				 */
				squery.close();
				return this.fusionOIDForCard_0(all,oid_MP);
			}
			squery.close();
			return this.fusionOIDForCard_0(previous_retour,oid_MP);
		}
	}

	/**
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex CARD_QUAL(KeyIndex previous_retour) throws FatalException {
		KeyIndex oid_MP = null; 
		for (int i = 0; i < this.qualTab.length; i++) {
			KeyIndex indexQ2 = Database.getCacheindex().getQualIndex(
					this.metaRelation.getName(), qualTab[i].getName(),
					this.index_owner_key);

			this.getIndexBitSet(indexQ2, qualTab[i]);
			if( oid_MP == null ) {
				oid_MP = indexQ2;
			}
			/*
			 * Operate an AND on bits sets tagging constraint on qualifier
			 */
			this.fusionIndexBitSet(oid_MP, indexQ2);
			indexQ2.give();
		}
		KeyIndex ki = this.mergedResult(previous_retour,oid_MP);
		return ki;
	}

	/**
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex CARD_QUAL_CLASS_org(KeyIndex previous_retour) throws FatalException {
		DoubleCPIndex oid_MP = null;
		//oid_MP = (DoubleCPIndex) Database.getCacheindex().getQualIndex(this.metaRelation.getName(),qualTab[0].getName(),this.index_owner_key);
		for (int i = 0; i < qualTab.length; i++) {	
			KeyIndex indexQ2 = Database.getCacheindex().getQualIndex(
					this.metaRelation.getName(), qualTab[i].getName(),
					this.index_owner_key);
			if( oid_MP == null ) {
				oid_MP = (DoubleCPIndex) indexQ2;
			}
			/*
			 * Tag counterparts matching constraint on the next qualifier
			 */
			this.getIndexBitSet(indexQ2, qualTab[i]);

			for (int j = 0; j < this.metaClassTab.length; j++) {
				DoubleCPIndex indexQC = (DoubleCPIndex) Database
				.getCacheindex().getCorrClassQualIndex(
						this.metaRelation.getName(),
						this.metaClassTab[j].getName(),
						qualTab[i].getName(), this.index_owner_key);
				this.unionWithDifferentKey((DoubleCPIndex)(indexQ2), indexQC);
				indexQC.give();
			}
			/*
			 * Operate an AND on bits sets tagging constraint on qualifier
			 */
			this.fusionIndexBitSet(oid_MP, indexQ2);
			indexQ2.give();
		}

		this.getOIDsFromIndexBitSetAndCard(oid_MP, this.card);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(oid_MP.getSELECTEDKeys().length
					+ " entries found");
		}

		return this.mergedResult(previous_retour,oid_MP);
	}

	/**
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex CARD_QUAL_CLASS(KeyIndex previous_retour) throws FatalException {
		DoubleCPIndex oid_MP = null;
		for (int i = 0; i < qualTab.length; i++) {	
			KeyIndex indexQ2 = null;			
			for (int j = 0; j < this.metaClassTab.length; j++) {
				DoubleCPIndex indexQC = (DoubleCPIndex) Database
				.getCacheindex().getCorrClassQualIndex(
						this.metaRelation.getName(),
						this.metaClassTab[j].getName(),
						qualTab[i].getName(), this.index_owner_key);
				this.getIndexBitSet(indexQC, qualTab[i]);
				if( indexQ2 == null ){
					indexQ2 = indexQC;
				}
				else {
					this.unionWithDifferentKey((DoubleCPIndex)(indexQ2), indexQC);
				}
				indexQC.give();
			}

			if( oid_MP == null ) {
				oid_MP = (DoubleCPIndex) indexQ2;
			}
			/*
			 * Operate an AND on bits sets tagging constraint on qualifier
			 */
			this.fusionIndexBitSet(oid_MP, indexQ2);
			indexQ2.give();
		}

		this.getOIDsFromIndexBitSetAndCard(oid_MP, this.card);
		if (Messenger.debug_mode == true) {
			this.report.addSentence(oid_MP.getSELECTEDKeys().length
					+ " entries found");
		}

		return this.mergedResult(previous_retour,oid_MP);
	}
	/**
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex CARD_QUAL_CP(KeyIndex previous_retour) throws Exception {
		KeyIndex oid_MP = Database.getCacheindex().getCorrIndex(this.metaRelation.getName(),
				this.index_owner_key);
		//KeyIndex oid_CP = new KeyIndex(this.getOID_CP());// this.counterPartsSQL.execSQLs();
		SQLQuery squery = new SQLQuery();
		KeyIndex oid_CP = new KeyIndex(new SaadaQLResultSet(squery.run(counterPartsSQL)));
		/*
		 * Select CP matching the counterpart constraint
		 */
		oid_MP.selectCPMatchingList(oid_CP.getKeys(), false);
		for (int i = 0; i < this.qualTab.length; i++) {
			KeyIndex indexQ2 = Database.getCacheindex().getQualIndex(
					this.metaRelation.getName(), qualTab[i].getName(),
					this.index_owner_key);
			/*
			 * Tag counterparts matching constraint on the next qualifier
			 */
			this.getIndexBitSet(indexQ2, qualTab[i]);
			/*
			 * Operate an AND on bits sets tagging constraint on qualifier
			 */
			this.fusionIndexBitSet(oid_MP, indexQ2);
			indexQ2.give();
		}	
		squery.close();
		return this.mergedResult(previous_retour, oid_MP);
	}

	/**
	 * Process patterns with both cardinality and class ofd counterpart constraint: 
	 * matchPattern {
	 *    relation_name
	 *    Cardinality ...
	 *    AssObjClass{......}
	 *    }
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex CARD_CLASS(KeyIndex previous_retour) throws FatalException {
		KeyIndex oid_MP = new LongCPIndex(Database.getCacheindex().getCorrIndex(this.metaRelation.getName(),this.index_owner_key).getKeys(),1);
		/*
		 * Build an index with the oid as key and the number of counterparts of requested classes as values
		 */
		for(MetaClass mc:this.metaClassTab){
			KeyIndex corr_class_index = Database.getCacheindex().getCorrClassIndex(this.metaRelation.getName(),mc.getName(),this.index_owner_key);
			long keys[] = corr_class_index.getKeys();
			for (int j = 0; j < keys.length; j++) {
				long old_cp = oid_MP.getLongCP(keys[j])[0];
				oid_MP.setCounterparts(keys[j],new long[]{old_cp+corr_class_index.getLongCP(j).length });
			}
			corr_class_index.give();
		}

		/*
		 * Check if the cadinality includes implicit null constraint e.g. card != 1
		 */
		boolean card_include_0 = this.card.includeCard0();
		if (!this.card_0 && !card_include_0){
			/*
			 * tags as selected all index values matching the cardinality
			 */
			this.getIndexBitSet(oid_MP,this.card);
			/*
			 * marks as selected all keys having at least on value tagged as selected
			 */
			oid_MP.selectKeysOnCPCardGreaterThan(0);
			return (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;
		}
		else if(!this.card_0 && card_include_0) {
			this.getIndexBitSet(oid_MP,this.card);
			/*
			 * marks as selected all keys having at least on value tagged as selected
			 */
			oid_MP.selectKeysOnCPCardGreaterThan(0);
			KeyIndex not_null_oids = (previous_retour!=null)?fusionOID(previous_retour,oid_MP):new KeyIndex(oid_MP.getSELECTEDKeySet());
			not_null_oids.selectAllKeys();
			/*
			 * tags as selected all index values matching a cardinality > 0
			 */
			this.getIndexBitSet(oid_MP, Card.getCard1());
			/*
			 * marks as selected all keys having at least on value tagged as selected
			 */
			oid_MP.selectKeysOnCPCardGreaterThan(0);
			KeyIndex null_oids;
			if(previous_retour==null){
				KeyIndex all = this.getOIDsFromIndexCardAndCard(Database.getCacheindex().getCardIndex(this.metaRelation.getName(),this.index_owner_key),Card.getCardAll());
				all.selectAllKeys();
				null_oids =  this.fusionOIDForCard_0(all,oid_MP);
			}
			else {
				null_oids = this.fusionOIDForCard_0(previous_retour,oid_MP);
			}
			return mergeResultWithCardNullIncluded(not_null_oids, null_oids);

		}
		else {
			/*
			 * tags as selected all index values matching a cardinality > 0
			 */
			this.getIndexBitSet(oid_MP, Card.getCard1());
			/*
			 * marks as selected all keys having at least on value tagged as selected
			 */
			oid_MP.selectKeysOnCPCardGreaterThan(0);
			if(previous_retour==null){
				KeyIndex all = this.getOIDsFromIndexCardAndCard(Database.getCacheindex().getCardIndex(this.metaRelation.getName(),this.index_owner_key),Card.getCardAll());
				all.selectAllKeys();
				return this.fusionOIDForCard_0(all,oid_MP);
			}
			return this.fusionOIDForCard_0(previous_retour,oid_MP);
		}
	}

	/**
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex CARD_CLASS_CP(KeyIndex previous_retour) throws Exception {
		/*
		 * The creation of an index from that Map could be replaced with operations done directly on  previous_retour
		 * (Future optimization)
		 */
		Map<Long,Collection<Long>> index_correlation = new TreeMap<Long,Collection<Long>>();
		/*
		 * Build a correlation index with all classes covered by the pattern
		 */
		for (int i = 0; i < this.metaClassTab.length; i++) {
			KeyIndex corr_class_index = Database.getCacheindex().getCorrClassIndex(this.metaRelation.getName(),this.metaClassTab[i].getName(),this.index_owner_key);
			if (previous_retour==null || this.card_0) {
				long[] keys = corr_class_index.getKeys();
				for (int k = 0; k < keys.length; k++) {
					long[] cp = corr_class_index.getLongCP(k);
					ArrayList<Long> al = new ArrayList<Long>();
					for (int c = 0; c < cp.length; c++) {
						al.add(new Long(cp[c]));
					}
					if (index_correlation.get(keys[k]) != null) {
						index_correlation.get(keys[k]).addAll(al);
					} else {
						index_correlation.put(keys[k], al);
					}
				}
			} else {// Ici se fait deja la fusion avec les OIDs presents
				// (oid_PList)
				long[] keys = corr_class_index.getKeys();
				for (int k = 0; k < keys.length; k++) {
					long[] cp = corr_class_index.getLongCP(k);
					ArrayList<Long> al = new ArrayList<Long>();
					for (int c = 0; c < cp.length; c++) {
						al.add(new Long(cp[c]));
					}
					if (index_correlation.containsKey(keys[k])) {
						index_correlation.get(keys[k]).addAll(al);
					} else if(previous_retour.hasDichotoKey(keys[k], false) != -1) {
						index_correlation.put(keys[k], al);
					}
				}
			}
			corr_class_index.give();
		}
		Database.getCacheindex().flush();
		/*
		 * Build an index with all OID matching the CP constraint
		 */
		SQLQuery squery = new SQLQuery();
		KeyIndex oid_CP = new KeyIndex(new SaadaQLResultSet(squery.run(counterPartsSQL)));
		//KeyIndex oid_CP = new KeyIndex(this.getOID_CP());// this.counterPartsSQL.execSQLs();
		LongCPIndex tab_index_corr = new LongCPIndex(index_correlation);
		KeyIndex oid_MP ;
		/*
		 * Save memory (400Mb measured on the XCATDb!!)
		 */
		index_correlation = null;
		System.gc();
		/*
		 * Check if the cadinality includes implicit null constraint e.g. card != 1
		 */
		boolean card_include_0 = this.card.includeCard0();
		if (!this.card_0 && !card_include_0){
			oid_MP = this.getOIDFromIndexAndCP(tab_index_corr,oid_CP,this.card);
			squery.close();
			return (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;			
		}
		else if(!this.card_0 && card_include_0) {
			oid_MP = this.getOIDFromIndexAndCP(tab_index_corr,oid_CP,this.card);
			KeyIndex not_null_oids = (previous_retour!=null)?fusionOID(previous_retour,oid_MP):oid_MP;			
			not_null_oids.selectAllKeys();

			KeyIndex null_oids = null;
			oid_MP = this.getOIDFromIndexAndCP(tab_index_corr,oid_CP,Card.getCard1());
			if(previous_retour==null){
				KeyIndex all = this.getOIDsFromIndexCardAndCard(Database.getCacheindex().getCardIndex(this.metaRelation.getName(),this.index_owner_key),Card.getCardAll());
				all.selectAllKeys();
				null_oids =  this.fusionOIDForCard_0(all,oid_MP);
			}
			else {
				null_oids = this.fusionOIDForCard_0(previous_retour,oid_MP);	
			}
			squery.close();
			return mergeResultWithCardNullIncluded(not_null_oids, null_oids);
		}
		/*
		 * Build an index with OIDs not matching both CP constraint and class constraint
		 */
		else  {
			oid_MP = this.getOIDFromIndexAndCP(tab_index_corr,oid_CP,Card.getCard1());
			if(previous_retour==null){
				KeyIndex all = this.getOIDsFromIndexCardAndCard(Database.getCacheindex().getCardIndex(this.metaRelation.getName(),this.index_owner_key),Card.getCardAll());
				all.selectAllKeys();
				squery.close();
				return this.fusionOIDForCard_0(all,oid_MP);
			}
			squery.close();
			return this.fusionOIDForCard_0(previous_retour,oid_MP);
		}
	}

	/**
	 * @param previous_retour
	 * @return
	 * @throws FatalException 
	 */
	private final KeyIndex CARD_QUAL_CLASS_CP(KeyIndex previous_retour) throws Exception  {
		KeyIndex oid_MP = Database.getCacheindex().getCorrIndex(this.metaRelation.getName(),
				this.index_owner_key);
		//KeyIndex oid_CP = new KeyIndex(this.getOID_CP());// this.counterPartsSQL.execSQLs();
		SQLQuery squery = new SQLQuery();
		KeyIndex oid_CP = new KeyIndex(new SaadaQLResultSet(squery.run(counterPartsSQL)));
		/*
		 * Select CP matching the counterpart constraint
		 */
		oid_MP.selectCPMatchingList(oid_CP.getKeys(), false);		

		for (int i = 0; i < this.qualTab.length; i++) {
			KeyIndex indexQ2 = Database.getCacheindex().getQualIndex(
					this.metaRelation.getName(), qualTab[i].getName(),
					this.index_owner_key);
			/*
			 * Tag counterparts matching constraint on the current qualifier
			 */
			this.getIndexBitSet(indexQ2, qualTab[i]);
			this.getIndexBitSet(oid_MP, qualTab[i]);
			for (int j = 1; j < this.metaClassTab.length; j++) {
				DoubleCPIndex indexQC = (DoubleCPIndex) Database
				.getCacheindex().getCorrClassQualIndex(
						this.metaRelation.getName(),
						this.metaClassTab[j].getName(),
						qualTab[i].getName(), this.index_owner_key);
				this.unionWithDifferentKey((DoubleCPIndex)(indexQ2), indexQC);
				indexQC.give();
			}
			/*
			 * Operate an AND on bits sets tagging constraint on qualifier
			 */
			this.fusionIndexBitSet(oid_MP, indexQ2);
			indexQ2.give();
		}	
		squery.close();
		return mergedResult(previous_retour, oid_MP);		
	}

	/**
	 * Returns a KeyIndex built from the merge of both previous_retour and oid_MP and filtered
	 * by the cardinality constraint
	 * @param previous_retour
	 * @param oid_MP
	 * @return
	 * @throws FatalException 
	 */
	private KeyIndex mergedResult(KeyIndex previous_retour, KeyIndex oid_MP) throws FatalException {
		/*
		 * Check if the cadinlaity includes implicit null constraint e.g. card != 1
		 */
		boolean card_include_0 = this.card.includeCard0();
		/*
		 * Card not null and no implicit null constraint
		 */
		if( !this.card_0 && !card_include_0) {
			return this.fusionForCardNotNull(previous_retour,oid_MP);
		}
		/*
		 * Card not null and but implicit null constraint
		 */
		else if( !this.card_0 && card_include_0) {
			/*
			 * Build a result KeyIndex with OIDs matching the not null cardinality constraint
			 */
			KeyIndex not_null_oids = fusionForCardNotNull(null,oid_MP);
			/*
			 * Build a result KeyIndex with OIDs matching the null cardinality constraint
			 */
			KeyIndex null_oids = this.fusionForCardNull(null,oid_MP);
			KeyIndex ki =  mergeResultWithCardNullIncluded(not_null_oids, null_oids);
			if( previous_retour == null ) {
				return ki;
			}
			else {
				return this.fusionOID(previous_retour, ki);
			}
		}
		/*
		 * Card null 
		 */
		else {
			return fusionForCardNull(previous_retour,oid_MP);
		}	
	}

	/**
	 * @param previous_retour
	 * @param not_null_oids
	 * @param null_oids
	 * @return
	 */
	private KeyIndex mergeResultWithCardNullIncluded(KeyIndex not_null_oids, KeyIndex null_oids) {
		/*			not_null_oids.scan();

		 * Merge both OID KeyIndex
		 */
		Set<Long> selection = not_null_oids.getSELECTEDKeySet();
		selection.addAll(null_oids.getSELECTEDKeySet());
		KeyIndex retour = new KeyIndex(selection);

		/*
		 * Mark all oid as selected and returns the result
		 */
		retour.selectAllKeys();
		return retour;
	}

	/**
	 * @param previous_retour
	 * @param oid_MP
	 * @return
	 * @throws FatalException 
	 * @throws FileNotFoundException
	 */
	private KeyIndex fusionForCardNull(KeyIndex previous_retour, KeyIndex oid_MP) throws FatalException  {
		/*
		 * Apply not null cardinality constraint on ANDed bits sets
		 */
		this.getOIDsFromIndexBitSetAndCard(oid_MP, Card.getCard1());
		/*
		 * Select oids which are not in the previous index
		 */
		if(previous_retour==null){
			KeyIndex all = this.getOIDsFromIndexCardAndCard(Database.getCacheindex().getCardIndex(this.metaRelation.getName(),this.index_owner_key),Card.getCardAll());
			//all.selectAllKeys();
			KeyIndex ki =  this.fusionOIDForCard_0(all,oid_MP);
				return ki;
		}
		KeyIndex ki =  this.fusionOIDForCard_0(previous_retour,oid_MP);
		return ki;
	}

	/**
	 * @param previous_retour
	 * @param oid_MP
	 * @return
	 */
	private KeyIndex fusionForCardNotNull(KeyIndex previous_retour, KeyIndex oid_MP) {
		/*
		 * Apply cardinality constraint on ANDed bits sets
		 */
		this.getOIDsFromIndexBitSetAndCard(oid_MP, this.card);
		/*
		 * if previous_retour is null, we must mae a copy of oid_MP because ir can be used in a further step 
		 * of the query processing
		 */
		if( previous_retour == null ) {
			KeyIndex all = new KeyIndex(oid_MP.getSELECTEDKeySet());
			all.selectAllKeys();
			return all;
		}
		else {
			KeyIndex ki = fusionOID(previous_retour,oid_MP);		
			return ki;		
		}
	}

	/**
	 * The initial value of counterPartsSQL can be changed while pattrn processing. That is why it
	 * must be get from this method.
	 * @return
	 */
	public String getCounterPartsSQL() {
		return this.counterPartsSQL;
	}

	/**
	 * @param collection
	 * @param relation
	 * @param patternclass
	 * @param qualifier
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static boolean check_CLASS_QUAL(String collection, String relation, String patternclass, String qualifier, double value) throws Exception {
		String query = "Select ENTRY From * In " + collection 
		+ " WhereRelation{ matchPattern{" + relation 
		+ ", AssObjClass{" + patternclass + "}, Qualifier{" + qualifier + " < " + value + "}  } }";

		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("Select oidsaada from " + Database.getCachemeta().getCollectionTableName(collection, Category.ENTRY));
		int cpt=0;
		char lf = 8;
		TreeSet<Long> api_result = new TreeSet<Long>();
		while( rs.next()) {
			long oid = rs.getLong(1);
			SaadaInstance si = Database.getCache().getObject(oid);
			SaadaLink[] sls = si.getEndingLinks(relation);
			for( SaadaLink sl: sls) {	
				String cpclass = SaadaOID.getClassName(sl.getEndindOID());
				double cpv = sl.getQualifierValue(qualifier);
				if( cpclass.equals(patternclass) && cpv < value)  {
					api_result.add(oid);
					cpt++;
					break;
				}
			}	
		}
		squery.close();
		System.out.println("\n"+ cpt + " entries found by scanning");
		Query q = new Query();
		System.out.println("Query " + query);
		SaadaQLResultSet srs = null;
		try {
			srs = q.runQuery(query);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if( srs == null ) {
			System.out.println("!!!! Retour null" + q.getErrorReport());
			System.exit(1);
		}
		System.out.println(srs.getSize() + " entries found by SaadaQL");
		if( cpt != srs.getSize()) {
			System.out.println("ERROR: result size does not match");
			//return false;
		}
		while( srs.next()) {
			long oid = srs.getOid();
			if( !api_result.contains(oid)) {
				System.out.println("erreur on " + Database.getCache().getObject(oid).getNameSaada());
			}
		}	
		srs.close();
		System.out.println("Test OK");
		return true;
	}

	/**
	 * Build the SaadaQL from the parameters and run it with both an API scan (reference) and query engine
	 * Returns true if both results are the same
	 * @param collection
	 * @param relation
	 * @param patternclass
	 * @param qualifiers
	 * @param op < or >
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public static boolean check_CLASS_QUALS(String collection, String relation, String patternclass, String[] qualifiers, boolean[] sup, double[] values) throws Exception {
		/*
		 * Buid the SaadaQL query
		 */
		String query = "Select ENTRY From * In " + collection 
		+ " WhereRelation{ matchPattern{" + relation 
		+ ", AssObjClass{" + patternclass + "}" ;
		/*
		 * Append qualifeir
		 */
		if( qualifiers != null && qualifiers.length > 0 ) {
			query += ", ";
			for( int i=0 ; i<qualifiers.length ; i++ ) {
				String op = "<";
				if( sup[i] ) {
					op = ">";
				}
				query += "Qualifier{" + qualifiers[i] + " " + op + " " + values[i] + "} ";
			}
		}
		query += "}}";
		System.out.println("Query: " + query);
		/*
		 * Run the query in API mode
		 * and store the result in a treeset.
		 */
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("Select oidsaada from " + Database.getCachemeta().getCollectionTableName(collection, Category.ENTRY));
		int cpt=0;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Scan DB with the API");
		TreeSet<Long> api_result = new TreeSet<Long>();
		while( rs.next()) {
			long oid = rs.getLong(1);
			SaadaInstance si = Database.getCache().getObject(oid);
			SaadaLink[] sls = si.getStartingLinks(relation);
			for( SaadaLink sl: sls) {	
				String cpclass = SaadaOID.getClassName(sl.getEndindOID());
				if( cpclass.equals(patternclass) )  {
					/*
					 * Apply qualifiers condition if exist
					 */
					if( qualifiers != null ) {
						BitSet nb_ok = new BitSet(qualifiers.length);
						for( int i=0 ; i<qualifiers.length ; i++ ) {
							double cpv = sl.getQualifierValue(qualifiers[i]);
							if( sup[i] ) {
								if( cpv > values[i]) {
									nb_ok.set(i);	
								}
							}
							else {
								if( cpv < values[i] ) {
									nb_ok.set(i);	
								}
							}
						}
						if(nb_ok.cardinality() ==  qualifiers.length ) {
							api_result.add(oid);
							cpt++;
							break;
						}
					}
					/*
					 * Else accept any counterpart
					 */
					else {						
						api_result.add(oid);
						cpt++;
						break;
					}
				}
			}	
		}
		squery.close();
		System.out.println(cpt + " entries found by scanning\n");
		/*
		 * Run the query in SaadaQL mode
		 */
		System.out.println("Run SaadaQL query");
		Query q = new Query();
		SaadaQLResultSet srs = null;
		try {
			srs = q.runQuery(query);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if( srs == null ) {
			System.out.println("!!!! Retour null" + q.getErrorReport());
			System.exit(1);
		}
		System.out.println(srs.getSize() + " entries found by SaadaQL");
		/*
		 * Compare results size of both modes
		 */
		if( cpt != srs.getSize()) {
			System.out.println("ERROR: result size does not match");
			return false;
		}
		while( srs.next()) {
			/*
			 * Compare results oid per oid
			 */
			long oid = srs.getOid();
			if( !api_result.contains(oid)) {
				System.out.println("erreur on " + Database.getCache().getObject(oid).getNameSaada() +  " (" + Long.toHexString(oid) + ")");
			}
		}	
		srs.close();
		System.out.println("Test OK");
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		Messenger.debug_mode = false;
		Database.init("XCATDBi");
		Messenger.debug_mode = false;
		try {
			check_CLASS_QUALS("CATALOGUE", "CatSrcToArchSrc", "arch_2282AEntry", null, null, null);
			check_CLASS_QUALS("CATALOGUE", "CatSrcToArchSrc", "arch_2282AEntry", new String[]{"epic_cat_dist"}, new boolean[]{false}, new double[]{5});
			check_CLASS_QUALS("CATALOGUE", "CatSrcToArchSrc", "arch_2282AEntry"
					, new String[]{"epic_cat_dist", "identif_proba"}, new boolean[]{false, false}, new double[]{5, 0.8});
			check_CLASS_QUALS("CATALOGUE", "CatSrcToArchSrc", "arch_2282AEntry"
					, new String[]{"epic_cat_dist", "identif_proba"}, new boolean[]{false, true}, new double[]{5, 0.8});
			check_CLASS_QUALS("CATALOGUE", "CatSrcToArchSrc", "arch_2282AEntry"
					, new String[]{"identif_proba"}, new boolean[]{true}, new double[]{0.8});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
