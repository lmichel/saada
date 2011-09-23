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
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.query.parser.Operator;
import saadadb.query.parser.UnitHandler;

public class DMField extends UTypeField {

	public DMField(VOResource vor, String uType,String operateur,String value,String unit) throws SaadaException{
		super(vor, uType, operateur, value, unit);
	}

	/* (non-Javadoc)
	 * @see saadadb.query.constbuilders.SaadaQLConstraint#computeWhereStatement(java.lang.String)
	 */
	public String computeWhereStatement(String computed_column) throws SaadaException{
		String[] computedValues = this.getOperands();
		switch(Operator.getCode(this.getOp())){
		case EQ:
		case GT:
		case GE:
		case LT:
		case LE:
		case NE:
			return computed_column+" "+this.getOp()+" "+computedValues[0];
		case Operator.IN:
			return computed_column+" >= "+computedValues[0] +" AND "+computed_column+" <= "+computedValues[1]; 
		case OUT:
			return computed_column+" <= "+computedValues[0] +" AND "+computed_column+" >= "+computedValues[1];
		case IN_S:
			return computed_column+" > "+computedValues[0] +" AND "+computed_column+" < "+computedValues[1];
		case OUT_S:
			return computed_column+" < "+computedValues[0] +" AND "+computed_column+" > "+computedValues[1];
		default: QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Unknow operator! (It's not supposed to happen!)");

		}
		return computed_column;
	}
	
	/**
	 * @param DMc
	 * @return
	 * @throws SaadaException
	 */
	private final String[] getOperands() throws SaadaException{
		UTypeHandler uth = this.vor.getUTypeHandler(this.getMetacolname());
		
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

}
