package saadadb.query.constbuilders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.DbmsWrapper;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.products.Coord;
import saadadb.query.parser.PositionParser;
import saadadb.util.Messenger;
import saadadb.util.PositionList;
import saadadb.util.RegExp;
 
public class PositionConstraint extends SaadaQLConstraint{
	private final String operator;     // isInCirle , isInBox , Autre...
	private String position;
	private final String size;
	private String coordEquinox; // J1950 ou J2000
	private String coordSystem;  // FK4 ou FK5 ou Glactic ou ICRS
	private PositionList positions;
	private double r  ;

	/**
	 * @param op
	 * @param pos
	 * @param si
	 * @param cE
	 * @param cS
	 * @throws SaadaException 
	 */
 	public PositionConstraint(String op, String pos, String si, String cE, String cS) throws SaadaException{
 		super(SaadaQLConstraint.POSITION);
		this.operator     = op ;
		this.position     = pos; // radec
		this.size         = si ;
		this.coordEquinox = cE ;
		this.coordSystem  = cS ;
		this.computeRaDecR();		
		this.sqlcolnames = new String[]{"pos_ra_csa", "pos_dec_csa"};
		this.where = this.computeSqlConstraint("");
	}

	protected final String getoperator    (){return this.operator    ;}
	protected final String getposition    (){return this.position    ;}
	protected final String getsize        (){return this.size        ;}
	protected final String getcoordEquinox(){return this.coordEquinox;}
	protected final String getcoordSystem (){return this.coordSystem ;}


	/**
	 * @throws SaadaException
	 */
	private final void computeRaDecR() throws SaadaException {
		this.r = Double.parseDouble(this.size);
		Pattern pattern = Pattern.compile("\\s*poslist\\s*:\\s*(" + RegExp.FILEPATH + ")");
		/*
		 * Size expressed in minute is more conveniant
		 */
		this.r /= 60;
		Matcher m = pattern.matcher(this.position);
		/*
		 * List of positions
		 */
		if(m.find()){
			positions = new PositionList(m.group(1), Coord.getAstroframe(this.coordSystem,this.coordEquinox));
		}
		/*
		 * Single position
		 */
		else {
			try{
				/*
				 * Position parser normalizes the input coordinate. It returns the input coordinates 
				 * as values expressed in the input system 
				 */
				PositionParser pp = new PositionParser(this.position, Coord.getAstroframe(this.coordSystem,this.coordEquinox));
				/*
				 * The position must now be converted in database system
				 */
				this.positions.addPos(pp.getRa(), pp.getDec());
			}catch (Exception e){
				Messenger.printStackTrace(e);
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Can't resolve name \""+this.position+"\" with Astroframe! " + e.getMessage());
			}
		}

	}

	/**
	 * @return
	 * @throws SaadaException
	 */
	public final String getSqlConstraint() throws SaadaException{
		return computeSqlConstraint("");
	}
	/**
	 * @param str
	 * @return
	 * @throws SaadaException
	 */
	public final String getSqlConstraint(String str) throws SaadaException{
		//str+=".pos";
		return computeSqlConstraint(str);
	}

	// Effectue le calcul pour transformer en contrainte SQL
	// le string passe en argument doit etre un non de collection
	public final String computeSqlConstraint(String pos) throws SaadaException{
		String retour = "";
		if(this.operator.equals("isInBox")) {
			r/= 2.0;
		}
		int s = this.positions.size();
		if( s == 0 ) {
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"No position given");   			
		}
		for( int i=0 ; i<s ; i++ ) {
			if( retour.length() > 0 ) {
				retour += "\nOR\n";
			}
			if(this.operator.equals("isInBox")){ 
				QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION,"IsInBox operation not supported");   			
			}
			else if(this.operator.equals("isInCircle")){
				retour += DbmsWrapper.getIsInCircleConstraint(pos, this.positions.getRa(i), this.positions.getDec(i), r);
			}
			else {
				QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Operator keyWord \""+this.operator+"\" not supported!");   
			}
		}
		return retour;  
	}
}
