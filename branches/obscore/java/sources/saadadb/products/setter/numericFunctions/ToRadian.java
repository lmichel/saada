package saadadb.products.setter.numericFunctions;

import net.objecthunter.exp4j.function.Function;

public class ToRadian extends Function  {

	public ToRadian(String name) {
		super(name,1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double apply(double... args) {
		// TODO Auto-generated method stub
		return args[0]*Math.PI/180;
	}

}
