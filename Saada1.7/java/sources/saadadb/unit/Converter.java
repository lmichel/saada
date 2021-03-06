package saadadb.unit;

/**
 *==========================================================================
 * @author  Brice GASSMANN, Francois Ochsenbein-- francois@astro.u-strasbg.fr
 * @version 0.9 15-Nov-2006: 
 *==========================================================================
 */

import java.text.ParseException;

/**
 * This class, tightly connected to the {@link Unit} class,
 * gathers <em>non-standard</em> unit conversions.<P>
 * <em>Standard</em> conversions are made between units having the same physical
 * dimension, as e.g. a conversion between <b>Hz</b> and <b>km/s/Mpc</b>.
 * Conversion bewteen units having different physical dimensions are called
 * <em>non-standard</em>, e.g. between hours (time) and degrees (angle).
 * This default class contains linear transformations only; a derived class
 * can be used if necessary to generate more complex converters.
 * @author  Brice GASSMANN, Francois Ochsenbein-- francois@astro.u-strasbg.fr
 * @version 1.0 15-Nov-2006: Finalisation
 */
public class Converter {
    /**
     * The source unit
    **/
     public Unit source;
    /**
     * The target unit
    **/
     public Unit target;
    /**
     * The conversion factor, if used (target = factor * source)
    **/
     public double factor;
    /**
     * The conversion offset, if used (target = factor * source + offset)
    **/
     // public double offset;
    /**
     * separator beween the units (a Unicode arrow)
    **/
     static char SEP = '\u27fe';

    /*==================================================================
			Constructors
     *==================================================================*/

     /**
      * Creation of a <em>standard converter</em>.
      * The standard rules of unit conversion are applied 
      * (see {@link Unit#convert})
      * @param source_unit unit of source value
      * @param target_unit unit of target value
      **/
     public Converter(String source_unit, String target_unit) {
	this.factor = 0./0.; // this.offset = 0;
	try {  this.source = new Unit(source_unit); }
	catch (Exception e) { System.err.println(e); source = null; }
	try {  this.target = new Unit(target_unit); }
	catch (Exception e) { System.err.println(e); target = null; }
	// No need to register, uses the standard Unit.convertUnit
    }

     /**
      * Creation (and registration) of a unit converter.
      * New objects of this class are known in {@link Unit#convert},
      * i.e. the {@link #convert} method defined here is applied.<p>
      * Examples could be
      * <tt> Converter("h", "deg", 15.)</tt> or 
      * <tt> Converter("\"d:m:s\"", "\"h:m:s\"", 1./15.)</tt>
      * @param source_unit unit of source value.
      * @param target_unit unit of target value
      * @param factor factor of conversion in: target = factor * source 
      **/
     public Converter(String source_unit, String target_unit, double factor) {
	// Prepare the units, and register them 
	try { this.source = new Unit(source_unit); this.source.setUnit(); }
	catch (Exception e) {
	     System.err.println(e);
	     source = null;
	}
	try { this.target = new Unit(target_unit); this.target.setUnit(); }
	catch (Exception e) {
	    System.err.println(e);
	    target =null;
	}
	this.factor = factor;
	Unit.registerConverter(source_unit, target_unit, this);
    }

     /**
      * Creation of a unit converter. An example can be:
      * <tt> Converter("h", "deg", 15.)</tt> or 
      * <tt> Converter("\"d:m:s\"", "\"h:m:s\"", 1./15.)</tt>
      * @param source_unit unit of source value.
      * @param target_unit unit of target value
      * @param factor factor of conversion in: target = factor * source + offset
      * @param offset offset of conversion in: target = factor * source + offset
      ** 
     public Converter(String source_unit, String target_unit, 
	     double factor, double offset) {
	// Prepare the units, and reigster them 
	try { this.source = new Unit(source_unit); this.source.setUnit(); }
	catch (Exception e) {
	     System.err.println(e);
	     source = null;
	}
	
	// Not necessary to define a new symbol...
    	if (!Unit.checkSymbol(target_unit)) {
	    try   { this.target = Unit.addSymbol(target_unit, target_unit); }
	    catch (Exception e) { 
		System.err.println(e);
		target =null;
	    }
	}
	try { this.target = new Unit(target_unit); this.target.setUnit(); }
	catch (Exception e) {
	    System.err.println(e);
	    target =null;
	}
	this.factor = factor;
	this.offset = offset;
	Unit.registerConverter(source_unit, target_unit, this);
    }
    **/

    /*==================================================================
			Dump
     *==================================================================*/
    /**
     * Dump the object
     * @param	title title line of the dump
     **/
    public void dump(String title) {
	System.out.println(title+"(factor="+factor+")");
	source.dump("source_unit ");
	source.dump("target_unit ");
    }

    /*==================================================================
			Conversion
     *==================================================================*/

    /**
     * Convert a number.
     * Convert the value from <em>source</em> unit into <em>target</em> unit
     * @param	value the value (expressed in source)
     * @return	the corresponding value, expressed in <em>target</em> units.
     **/
    public double convert(double value) throws ArithmeticException {
	source.setValue(value);
	if (factor!=Double.NaN) 	// factor is not NaN, apply the factor
	    target.setValue(value*factor/*+offset*/);
	else 			// Apply the standard rules:
	    Unit.convert(source, target);
	return(target.value);
    }

    /**
     * Added by F.X. Pineau
     * @param value
     * @return
     * @throws ArithmeticException
     */
    public String convertStr(double value) throws ArithmeticException {
		source.setValue(value);
		if(factor!=Double.NaN) 	// factor is not NaN, apply the factor
			return value*factor+"*UNIT";
		else 
			return Unit.convertStr(source, target);
	}
    
    /**
     * Convert a value.
     * Convert the value from <em>source</em> unit into <em>target</em> unit
     * @param	value the value (expressed in source)
     * @return	the corresponding value, expressed in <em>target</em> units.
     **/
    public double convert(String value) 
	throws ParseException, ArithmeticException {
	source.setValue(value);
	if (factor == factor) 	// factor is not NaN, apply the factor
	    target.setValue(source.value*factor);
	else 			// Apply the standard rules:
	    Unit.convert(source, target);
	return(target.value);
    }

    /**
     * Convert a value, return its edited form.
     * Convert the value from <em>source</em> unit into <em>target</em> unit
     * @param	value the value (expressed in source)
     * @return	the corresponding edited value, expressed in 
     * 		<em>target</em> units.
     **/
    public String transform(String value) 
	throws ParseException, ArithmeticException {
	source.setValue(value);
	if (factor == factor) 	// factor is not NaN, apply the factor
	    target.setValue(source.value*factor/*+offset*/);
	else 			// Apply the standard rules:
	    Unit.convert(source, target);
	return(target.editedValue());
    }

    /** * @version $Id$

     * Convert a value, return its edited form.
     * Convert the value from <em>source</em> unit into <em>target</em> unit
     * @param	value the value (expressed in source)
     * @return	the corresponding edited value, expressed in 
     * 		<em>target</em> units.
     **/
    public String transform(double value) throws ArithmeticException {
	source.setValue(value);
	if (factor == factor) 	// factor is not NaN, apply the factor
	    target.setValue(value*factor/*+offset*/);
	else 			// Apply the standard rules:
	    Unit.convert(source, target);
	return(target.editedValue());
    }

    /*==================================================================
			Edition
     *==================================================================*/

     /**
      * Standard edition of the unit converter
     **/
    public String toString() {
	return(source.symbol+"=>"+target.symbol+"(x"+factor+")");
    }

	
}
