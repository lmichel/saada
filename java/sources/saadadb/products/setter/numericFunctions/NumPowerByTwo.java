package saadadb.products.setter.numericFunctions;

import net.objecthunter.exp4j.function.Function;


public class NumPowerByTwo extends Function  {

	public NumPowerByTwo(String name) {
		super(name,1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double apply(double... args) {
		// TODO Auto-generated method stub
		return Math.pow(args[0],2);
	}
}