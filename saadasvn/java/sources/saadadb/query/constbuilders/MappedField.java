package saadadb.query.constbuilders;
 
import static saadadb.query.parser.Operator.EQ;
import static saadadb.query.parser.Operator.GE;
import static saadadb.query.parser.Operator.GT;
import static saadadb.query.parser.Operator.IN_S;
import static saadadb.query.parser.Operator.LE;
import static saadadb.query.parser.Operator.LT;
import static saadadb.query.parser.Operator.NE;
import static saadadb.query.parser.Operator.OUT;
import static saadadb.query.parser.Operator.OUT_S;
import static saadadb.query.parser.SaadaQLRegex.ArgSep;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.NumericValue;
import static saadadb.query.parser.SaadaQLRegex.TextValue;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.query.parser.MappedOperators;
import saadadb.query.parser.Operator;
import saadadb.query.parser.UnitHandler;
import saadadb.util.ChangeKey;

/**
 * Model a field mapped either with a UCD or with a DM field. 
 * @author michel
 *
 */  
abstract public class MappedField extends SaadaQLConstraint {

	private final String metacolname   ;
	private final String op    ;
	private final String value ;
	private final String unit  ;
	private boolean toCastInText = false;
	
	public MappedField(String metacolname,String operateur,String value,String unit, int mapping_type) throws SaadaException{
 		super(mapping_type);
		this.metacolname = metacolname;
		this.op    = operateur;
		this.value = value;
		this.unit  = unit;
		this.sqlcolnames = new String[]{ChangeKey.getUCDNickName(this.metacolname)};
	}
	public final String getMetacolname(){return this.metacolname ;}
	public final String getOp   (){return this.op    ;}
	public final String getValue(){return this.value ;}
	public final String getUnit (){return this.unit  ;}
	
	
	protected final void isToCastInText(){ this.toCastInText=true; }
	protected final boolean toCastInText(){ return this.toCastInText; }
	

	/**
	 * @param ah
	 * @throws SaadaException
	 */
	public final String computeWhereStatement(AttributeHandler ah) throws SaadaException{
		String attrName = "@@@" + this.metacolname + "@@@";
		String[] computedValues = this.getOperands(ah);
			switch(MappedOperators.getCode(this.op)){
			case EQ:
			case GT:
			case GE:
			case LT:
			case LE:
			case NE:
				return  attrName+" "+this.getOp()+" "+computedValues[0];
			case Operator.IN:
				return  attrName+" >= "+computedValues[0] +" AND "+attrName+" <= "+computedValues[1]; 
			case OUT:
				return attrName+" <= "+computedValues[0] +" OR "+attrName+" >= "+computedValues[1];
			case IN_S:
				return  attrName+" > "+computedValues[0] +" AND "+attrName+" < "+computedValues[1];
			case OUT_S:
				return  attrName+" < "+computedValues[0] +" OR "+attrName+" > "+computedValues[1];
			default: QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Unknow operator! (It's not supposed to happen!)");return null;
			
		}
		
	}
	
	/**
	 * @param ah
	 * @return
	 * @throws SaadaException
	 */
	private final String[] getOperands(AttributeHandler ah) throws SaadaException{
		if(ah.getType().equals("String")){
			if( !this.getOp().matches("!?=\\*?") ) {
				QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "The attribute \""+ah.getNameattr()+"\" of the classe \""+ah.getClassname()+"\" type  is \"String\"! You can't use an operator different from \"=\" or \"!=\"!");				
			}
			else if( !this.getUnit().matches("((none)|(nounit))?") )  {
				QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "The attribute \""+ah.getNameattr()+"\" of the classe \""+ah.getClassname()+"\" type  is \"String\"! Can not make unit conversion");								
			}
			else if(this.getValue().matches("\\'"+TextValue+"\\'")) {
				return new String[]{this.getValue()};
			}
			else {
				return new String[]{"'"+this.getValue()+"'",null};
			}
		}
		else {
			if(this.getValue().matches(NumericValue)) {
				return new String[]{UnitHandler.computeValue(this.getValue(),this.getUnit(),ah.getUnit())};
			}
			else if(this.getValue().matches("\\("+FacWS+NumericValue+FacWS+ArgSep+FacWS+NumericValue+FacWS+"\\)")){
				String[] values = this.getValue().replaceAll("\\s*\\(\\s*","").replaceAll("\\s*\\)\\s*","").split("\\s*,\\s*");
				values[0] = UnitHandler.computeValue(values[0],this.getUnit(),ah.getUnit());
				values[1] = UnitHandler.computeValue(values[1],this.getUnit(),ah.getUnit());
				return (Double.parseDouble(values[0])<Double.parseDouble(values[1]))?values:new String[]{values[1],values[0]};
			}else{
				QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Unknow value format: \""+this.getValue()+"\" ! (It's not supposed to happen!)");
			}
		}
		return null;
	}
	
	/**
	 * @return
	 * @throws SaadaException
	 */
	private final String[] computeValues() throws SaadaException{
		//UTypeHandler uth = this.data_model.getUTypeHandler(DMc.getUType());
		UTypeHandler uth = new UTypeHandler();
		
		if(this.getValue().matches("\\'"+TextValue+"\\'")) {
			return new String[]{this.getValue()};
		}
//		if(ah.getType().equals("String")){
//			if(DMc.getOp().matches("!?=\\*?") && ah.getUnit().replace("none","").equals(DMc.getUnit().replace("none",""))){
//				return new String[]{"'"+DMc.getValue()+"'",null};getValue
//			}else{
//				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"The attribute \"+ah.getNameattr()+\" of the classe \"+ah.getClassname()+\" type  is \"String\"! You can't use an operator different from \"=\" or \"!=\"!");
//			}
//		}
		if(this.getValue().matches(NumericValue)) {
			return new String[]{UnitHandler.computeValue(this.getValue(),this.getUnit(),uth.getUnit())};
		}
		if(this.getValue().matches("\\("+FacWS+NumericValue+FacWS+ArgSep+FacWS+NumericValue+FacWS+"\\)")){
			String[] values = this.getValue().replaceAll("\\s*\\(\\s*","").replaceAll("\\s*\\)\\s*","").split("\\s*,\\s*");
			values[0] = UnitHandler.computeValue(values[0],this.getUnit(),uth.getUnit());
			values[1] = UnitHandler.computeValue(values[1],this.getUnit(),uth.getUnit());
			return (Double.parseDouble(values[0])<Double.parseDouble(values[1]))?values:new String[]{values[1],values[0]};
		}else{
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER,"Unknow value format: \""+this.getValue()+"\" ! (It's not supposed to happen!)");
		}
		return null;
	}
	
	public final AttributeHandler getConstraintHandler() {
		AttributeHandler ah = new AttributeHandler();
		ah.setUnit(unit);
		ah.setUcd(metacolname);
		ah.setNameattr(ah.getUCDNickname());
		return ah;
	
	}

}
