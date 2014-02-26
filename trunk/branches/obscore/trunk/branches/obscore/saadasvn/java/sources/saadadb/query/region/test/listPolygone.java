package saadadb.query.region.test;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import saadadb.query.region.triangule.Point;
import saadadb.query.region.triangule.Polygone;



public class listPolygone {

	private static HashMap<String,Polygone> Lp = new HashMap<String,Polygone>();
	
	public static Polygone getPoly (String key) {
		return Lp.get(key);
	}
	
	public static ArrayList<Polygone> getListPoly () {
		return new ArrayList<Polygone>(Lp.values());
	}
	
	public static Collection<Entry<String,Polygone>> getNomPoly() {
		return Lp.entrySet();
	}
	
	static {
		
		try {
			ArrayList<Point> poly = new ArrayList<Point>();

			 

			//carré
			poly.add( new Point(-5,5));
			poly.add( new Point(-5,-5));
			poly.add( new Point(5,-5));
			poly.add( new Point(5,5));

			Lp.put("carré",new Polygone(poly));
			poly.clear();


			//novice concavien

			poly.add(new Point(-1,1));
			poly.add( new Point(-1,-1));
			poly.add( new Point(0,0));
			poly.add( new Point(1,-1));
			poly.add(new Point(1,1));

			Lp.put("novice concave",new Polygone(poly));
			poly.clear();
			
			//piticarré
			poly.add(new Point(1,1));
			poly.add( new Point(3,1));
			poly.add( new Point(3,3));
			poly.add(new Point(1,3));

			Lp.put("pticar",new Polygone(poly));
			poly.clear();
			
			
			//segment vraiment sécant

			poly.add(new Point(-5,5));
			poly.add( new Point(5,-5));
			poly.add( new Point(5,5));
			poly.add(new Point(-5,-5));

			//Lp.put("secant",new Polygone(poly));
			poly.clear();


			poly.add( new Point(6,-5));
			poly.add( new Point(1,-6));
			poly.add( new Point(-2,-3));
			poly.add( new Point(-3,2));
			poly.add( new Point(-1,6));
			poly.add( new Point(3,8));
			poly.add( new Point(5,7));
			poly.add( new Point(2,5));
			poly.add( new Point(-1,4));
			poly.add( new Point(-1,-1));
			poly.add( new Point(1,-3));

			Lp.put("petit croissant",new Polygone(poly));
			poly.clear();
			//tshirt

			poly.add( new Point(-2,0));
			poly.add( new Point(-2,1));
			poly.add( new Point(-1,1));
			poly.add( new Point(0,0));
			poly.add( new Point(1,1));
			poly.add( new Point(2,1));
			poly.add( new Point(2,0));
			poly.add( new Point(1,0));
			poly.add( new Point(1,-1));
			poly.add( new Point(-1,-1));
			poly.add( new Point(-1,0));

			Lp.put("tshirt",new Polygone(poly));
			poly.clear();

			poly.add( new Point(-1,2));
			poly.add( new Point(0,2));
			poly.add( new Point(0,0));
			poly.add( new Point(1,0));
			poly.add( new Point(1,1));
			poly.add(new Point(0,1));
			poly.add( new Point(0,2));
			poly.add( new Point(2,2));
			poly.add(new Point(2,-1));
			poly.add( new Point(-1,-1));

			Lp.put("cube",new Polygone(poly));
			poly.clear();

			//petite horloge

			poly.add( new Point(-3,6));
			poly.add( new Point(0,6));
			poly.add(new Point(0,2));
			poly.add( new Point(-2,0));
			poly.add(new Point(0,-2));
			poly.add(new Point(3,1));
			poly.add( new Point(1,3));
			poly.add(new Point(1,6));
			poly.add( new Point(4,6));
			poly.add( new Point(4,-3));
			poly.add( new Point(-3,-3));

			Lp.put("horloge",new Polygone(poly));
			poly.clear();


			//triple horloge


			poly.add( new Point(-10,10));
			poly.add(new Point(-10,-10));
			poly.add( new Point(-1,-10));
			poly.add(new Point(-1,4));
			poly.add(new Point(-3,6));
			poly.add(new Point(0,9));
			poly.add(new Point(2,6));
			poly.add(new Point(0,4));
			poly.add(new Point(0,-10));
			poly.add( new Point(10,-10));
			poly.add( new Point(10,10));
			poly.add( new Point(4,10));
			poly.add( new Point(4,1));
			poly.add( new Point(7,-3));
			poly.add( new Point(8,-6));
			poly.add( new Point(5,-8));
			poly.add( new Point(3,-7));
			poly.add(new Point(1,-1));
			poly.add( new Point(3,1));
			poly.add( new Point(3,10));
			poly.add( new Point(-5,10));
			poly.add( new Point(-5,-1));
			poly.add( new Point(-2,1));
			poly.add( new Point(-6,-8));
			poly.add( new Point(-9,-4));
			poly.add( new Point(-6,-1));
			poly.add( new Point(-6,10));

			Lp.put("trihorloge",new Polygone(poly));
			poly.clear();

			//croissant delicieux
			poly.add( new Point(-10,0));
			poly.add( new Point(-10,4));
			poly.add(new Point(-7,8));
			poly.add( new Point(-4,8));
			poly.add(new Point(0,10));
			poly.add(new Point(2,9));
			poly.add( new Point(6,9));
			poly.add(new Point(8,7));
			poly.add(new Point(10,0));
			poly.add(new Point(5,-4));
			poly.add(new Point(1,-3));
			poly.add( new Point(-1,-1));
			poly.add( new Point(-2,2));
			poly.add( new Point(-1,4));
			poly.add( new Point(2,4));
			poly.add( new Point(4,2));
			poly.add( new Point(5,0));
			poly.add( new Point(6,3));
			poly.add( new Point(5,6));
			poly.add( new Point(-3,6));
			poly.add( new Point(-5,4));
			poly.add( new Point(-5,0));

			Lp.put("croissantdelicieux",new Polygone(poly));
			poly.clear();

			//demiboss
			poly.add( new Point(-10,10));
			poly.add(new Point(-10,-10));
			poly.add( new Point(-1,-10));
			poly.add(new Point(-1,10));
			poly.add( new Point(-4,10));
			poly.add(new Point(-4,-3));
			poly.add(new Point(-3,-4));
			poly.add( new Point(-2,-4));
			poly.add(new Point(-2,-5));
			poly.add( new Point(-3,-5));
			poly.add(new Point(-4,-6));
			poly.add( new Point(-3,-6));
			poly.add( new Point(-4,-7));
			poly.add( new Point(-5,-7));
			poly.add(new Point(-5,-6));
			poly.add( new Point(-6,-7));
			poly.add( new Point(-6,-6));
			poly.add( new Point(-5,-5));
			poly.add( new Point(-6,-5));
			poly.add(new Point(-5,-4));
			poly.add( new Point(-5,10));

			Lp.put("godhand",new Polygone(poly));
			poly.clear();
			
			//pitigodhand
			
			poly.add( new Point(-0.1,0.1));
			poly.add(new Point(-0.1,-0.1));
			poly.add( new Point(-0.1,-0.1));
			poly.add(new Point(-0.01,0.1));
			poly.add( new Point(-0.04,0.1));
			poly.add(new Point(-0.04,-0.03));
			poly.add(new Point(-0.03,-0.04));
			poly.add( new Point(-0.02,-0.04));
			poly.add(new Point(-0.02,-0.05));
			poly.add( new Point(-0.03,-0.05));
			poly.add(new Point(-0.04,-0.06));
			poly.add( new Point(-0.03,-0.06));
			poly.add( new Point(-0.04,-0.07));
			poly.add( new Point(-0.05,-0.07));
			poly.add(new Point(-0.05,-0.06));
			poly.add( new Point(-0.06,-0.07));
			poly.add( new Point(-0.06,-0.06));
			poly.add( new Point(-0.05,-0.05));
			poly.add( new Point(-0.06,-0.05));
			poly.add(new Point(-0.05,-0.04));
			poly.add( new Point(-0.05,0.1));
			
			//Lp.put("godhand2",new Polygone(poly));
			poly.clear();

			//semiboss
			poly.add( new Point(-10,10));
			poly.add(new Point(-6,10));
			poly.add(new Point(-6,2));
			poly.add(new Point(0,2));
			poly.add(new Point(-3,5));
			poly.add(new Point(4,5));
			poly.add(new Point(0,2));
			poly.add(new Point(6,2));
			poly.add(new Point(6,10));
			poly.add( new Point(10,10));
			poly.add( new Point(10,0));
			poly.add( new Point(-10,0));

			Lp.put("youpi",new Polygone(poly));
			poly.clear();

			//bossdelamorkitu

			poly.add( new Point(0,1.0));
			poly.add(new Point(0,.5));
			poly.add( new Point(-.5,.5));
			poly.add( new Point(-.5,-.5));
			poly.add( new Point(.5,-.5));
			poly.add(new Point(.5,.5));
			poly.add( new Point(0,.5));
			poly.add( new Point(.5,.1));
			poly.add(new Point(0,-.5));
			poly.add( new Point(-.5,-.1));
			poly.add( new Point(0,.5));
			poly.add( new Point(0,1.0));
			poly.add(new Point(1.0,1.0));
			poly.add( new Point(1.0,-1.0));
			poly.add(new Point(-1.0,-1.0));
			poly.add(new Point(-1.0,1.0));

			Lp.put("boss",new Polygone(poly));
			poly.clear();

			//polygone troué
			poly.add( new Point(-10,-10));
			poly.add( new Point(-10,0));
			poly.add(new Point(0,5));
			poly.add( new Point(5,-5));
			poly.add(new Point(9,5));
			poly.add( new Point(0,5));
			poly.add( new Point(10,10));
			poly.add( new Point(10,-10));


			Lp.put("trou",new Polygone(poly));
			poly.clear();

			//sablier niv1
			poly.add( new Point(-10,-10));
			poly.add( new Point(0,0));
			poly.add( new Point(-10,10));
			poly.add( new Point(10,10));
			poly.add( new Point(0,0));
			poly.add( new Point(10,-10));


			Lp.put("sablier1",new Polygone(poly));
			poly.clear();




			//sablier niv2
			poly.add( new Point(-10,-10));
			poly.add( new Point(0,0));
			poly.add( new Point(-5,5));
			poly.add(new Point(0,10));
			poly.add( new Point(5,5));
			poly.add( new Point(0,0));
			poly.add( new Point(10,-10));

			Lp.put("sablier2",new Polygone(poly));
			poly.clear();


			//wut

			poly.add( new Point(-5,8));
			poly.add(new Point(0,10));
			poly.add( new Point(5,8));
			poly.add( new Point(0,5));
			poly.add(new Point(2,0));
			poly.add( new Point(0,-5));
			poly.add( new Point(-2,0));
			poly.add( new Point(-5,-2));
			poly.add(new Point(-8,0));
			poly.add( new Point(-5,2));
			poly.add( new Point(-2,0));
			poly.add( new Point(0,5));


			Lp.put("wut",new Polygone(poly));
			poly.clear();

			//big3

			poly.add( new Point(-3,-4));
			poly.add(new Point(-4,-2));
			poly.add( new Point(-2,0));
			poly.add( new Point(-4,2));
			poly.add(new Point(-1,5));
			poly.add( new Point(-5,8));
			poly.add( new Point(0,10));
			poly.add( new Point(2,7));
			poly.add(new Point(1,4));
			poly.add( new Point(2,-1));


			Lp.put("big3",new Polygone(poly));
			poly.clear();

			poly.add( new Point(-10,10));
			poly.add(new Point(-5,10));
			poly.add( new Point(-3,8));
			poly.add( new Point(-5,7));
			poly.add(new Point(-3,4));
			poly.add( new Point(-4,1));
			poly.add( new Point(4,7));
			poly.add( new Point(6,10));
			poly.add(new Point(6,-3));
			poly.add( new Point(2,-8));
			poly.add( new Point(-1,-6));
			poly.add(new Point(-1,-3));
			poly.add( new Point(-4,-7));
			poly.add( new Point(-6,-5));
			poly.add(new Point(-9,-8));	

			Lp.put("papyrus",new Polygone(poly));
			poly.clear();


			poly.add( new Point(-7,10));
			poly.add( new Point(10,10));
			poly.add( new Point(10,-10));
			poly.add( new Point(-7,-10));
			poly.add( new Point(-7,2));
			poly.add( new Point(-6,-1));
			poly.add(new Point(-4,-4));
			poly.add( new Point(-1,-5));
			poly.add( new Point(2,-5));
			poly.add( new Point(5,-4));
			poly.add( new Point(7,-1));
			poly.add( new Point(8,2));
			poly.add( new Point(8,5));
			poly.add( new Point(6,8));
			poly.add( new Point(3,9));
			poly.add( new Point(0,8));
			poly.add(new Point(-7,3));

			Lp.put("bubulle",new Polygone(poly));
			poly.clear();

			//pitibubulle
			
			poly.add( new Point(-0.07,0.10));
			poly.add( new Point(0.10,0.10));
			poly.add( new Point(0.10,-0.10));
			poly.add( new Point(-0.07,-0.10));
			poly.add( new Point(-0.07,0.02));
			poly.add( new Point(-0.06,-0.01));
			poly.add(new Point(-0.04,-0.04));
			poly.add( new Point(-0.01,-0.05));
			poly.add( new Point(0.02,-0.05));
			poly.add( new Point(0.05,-0.04));
			poly.add( new Point(0.07,-0.01));
			poly.add( new Point(0.08,0.02));
			poly.add( new Point(0.08,0.05));
			poly.add( new Point(0.06,0.08));
			poly.add( new Point(0.03,0.09));
			poly.add( new Point(0,0.08));
			poly.add(new Point(-0.07,0.03));
			Lp.put("bubulle2",new Polygone(poly));
			poly.clear();
			
			//papa

			poly.add( new Point(-1.83,-0.44));
			poly.add( new Point(-1.45,0.28));
			poly.add( new Point(-0.47,0.59));
			poly.add( new Point(0.4,0.26));
			poly.add( new Point(0.71,-0.45));
			poly.add( new Point(0.46,-1.3));
			poly.add( new Point(-0.2,-2.01));
			poly.add( new Point(-0.95,-2.35));//H
			poly.add( new Point(-2.12,-2.49));
			poly.add( new Point(-3.34,-2.35));
			poly.add( new Point(-3.79,-1.62));
			poly.add( new Point(-4.02,-0.23));
			poly.add( new Point(-3.86,1.02));
			poly.add( new Point(-3.36,1.61));
			poly.add( new Point(-2.18,2.4));
			poly.add( new Point(-0.32,2.57));
			poly.add( new Point(1.18,2.42));//Q
			poly.add( new Point(2.22,1.67));
			poly.add( new Point(3.27,0.53));
			poly.add( new Point(3.63,-0.38));
			poly.add( new Point(3.7,-1.39));
			poly.add( new Point(3.28,-2.23));//V
			poly.add( new Point(1.74,-3.73));
			poly.add( new Point(-0.22,-4.41));
			poly.add( new Point(-3.54,-4.41));
			poly.add( new Point(-3.28,-2.97));
			poly.add( new Point(-0.89,-3.73));
			poly.add( new Point(-0.11,-2.66));//C1
			poly.add( new Point(2.0,-2.77));
			poly.add( new Point(1.71,-1.15));
			poly.add( new Point(2.46,-0.01));
			poly.add( new Point(0.62,0.85));
			poly.add( new Point(-0.17,1.98));//H1
			poly.add( new Point(-1.54,0.73));
			poly.add( new Point(-3.24,0.53));
			poly.add( new Point(-2.51,-0.97));
			poly.add( new Point(-2.97,-1.68));
			poly.add( new Point(-2.06,-1.89));
			poly.add( new Point(-1.45,-0.91));
			poly.add( new Point(-0.72,-1.18));
			poly.add( new Point(0.03,-0.15));
			poly.add( new Point(-0.87,0.04));

			Lp.put("spiral",new Polygone(poly));
			poly.clear();

			//fourchetteeee

			poly.add( new Point(-4.12,-3.09));
			poly.add( new Point(-5.25,-2.71));
			poly.add( new Point(-6.11,-1.94));
			poly.add( new Point(-6.43,-0.53));
			poly.add( new Point(-6.51,1.12));
			poly.add( new Point(-6.05,2.42));
			poly.add( new Point(-4.64,3.85));
			poly.add( new Point(-4.59,4.44));//h
			poly.add( new Point(-5.72,2.91));
			poly.add( new Point(-4.92,4.53));
			poly.add( new Point(-5.94,3.05));
			poly.add( new Point(-5.17,4.62));
			poly.add( new Point(-6.23,3.22));
			poly.add( new Point(-5.56,4.81));
			poly.add( new Point(-6.08,4.58));//o
			poly.add( new Point(-6.57,3.03));
			poly.add( new Point(-6.71,-2.63));
			poly.add( new Point(-6.83,-3.18));
			poly.add( new Point(-6.23,-2.49));
			poly.add( new Point(-6.83,-3.81));
			poly.add( new Point(-5.76,-2.85));
			poly.add( new Point(-6.58,-4.49));
			poly.add( new Point(-5.73,-3.46));//w
			poly.add( new Point(-5.33,-4.16));

			Lp.put("fourchette",new Polygone(poly));
			poly.clear();

			poly.add(new Point(-3.92,1.89));
			poly.add(new Point(-3.9,0.8));
			poly.add(new Point(-2.72,1.28));
			poly.add(new Point(-2.69,-0.09));//d
			poly.add(new Point(-3.9,0.8));//b
			poly.add(new Point(-3.9,-0.7));//e
			poly.add(new Point(-1.3,-0.71));//f
			poly.add(new Point(-3.48,-0.46));
			poly.add(new Point(-3.42,0.04));
			poly.add(new Point(-2.25,-0.3));
			poly.add(new Point(-2.36,0.96));
			poly.add(new Point(-0.53,0.96));
			poly.add(new Point(-1.67,-0.15));//l
			poly.add(new Point(-0.46,-0.13));
			poly.add(new Point(-1.27,-0.4));
			poly.add(new Point(-1.3,-0.71));//f
			poly.add(new Point(0.72,-0.69));//o
			poly.add(new Point(0.72,0.4));
			poly.add(new Point(-0.3,-0.59));
			poly.add(new Point(-0.24,0.13));
			poly.add(new Point(-0.88,0.11));
			poly.add(new Point(0.72,0.48));
			poly.add(new Point(0.71,1.88));//22
			poly.add(new Point(-1.37,1.89));//23
			poly.add(new Point(0.26,1.06));//25
			poly.add(new Point(-0.88,0.11));
			poly.add(new Point(-0.28,1.07));
			poly.add(new Point(-1.48,1.69));
			poly.add(new Point(-1.45,1.34));
			poly.add(new Point(-1.19,1.36));
			poly.add(new Point(-1.22,1.12));
			poly.add(new Point(-1.79,1.11));
			poly.add(new Point(-2.93,1.62));
			poly.add(new Point(-3.66,1.05));
			poly.add(new Point(-2.92,1.8));
			poly.add(new Point(-1.98,1.48));
			poly.add(new Point(-1.37,1.89));

			Lp.put("yeti",new Polygone(poly));
			poly.clear();

			poly.add(new Point(-5,10));
			poly.add(new Point(5,10));
			poly.add(new Point(0,5));
			poly.add(new Point(5,0));
			poly.add(new Point(10,5));
			poly.add(new Point(10,-5));
			poly.add(new Point(5,0));
			poly.add(new Point(0,-5));
			poly.add(new Point(5,-10));
			poly.add(new Point(-5,-10));
			//poly.add(new Point(0,-5));
			poly.add(new Point(5,0));
			poly.add(new Point(0,5));


			//Lp.put("triforce",new Polygone(poly));
			poly.clear();

			poly.add( new Point(-0.9,0.5));
			poly.add( new Point(-0.9,-0.8));
			poly.add( new Point(-0.3,-0.4));
			poly.add( new Point(-0.5,-0.3));
			poly.add( new Point(-0.1,-0.3));
			poly.add( new Point(-0.3,-0.4));
			poly.add( new Point(0.2,-0.4));
			poly.add( new Point(-0.4,0));
			poly.add( new Point(-0.7,-0.4));
			poly.add( new Point(-0.8,-0.3));
			poly.add( new Point(-0.4,0.1));
			poly.add( new Point(0.2,-0.1));
			poly.add( new Point(0.7,0.4));
			poly.add( new Point(0.2,0.8));
			poly.add( new Point(0.2,0.4));
			poly.add( new Point(-0.3,0.3));

			Lp.put("weirdo",new Polygone(poly));
			poly.clear();
			
			poly.add( new Point(0,86));
			poly.add( new Point(90,86));
			poly.add( new Point(180,86));
			poly.add( new Point(270,86));
			
			Lp.put("paul",new Polygone(poly));
			poly.clear();
			
			poly.add( new Point(0,87));
			poly.add( new Point(90,87));
			poly.add( new Point(135,89.8));
			poly.add( new Point(180,87));
			poly.add( new Point(270,87));
			
			Lp.put("paul2",new Polygone(poly));
			poly.clear();
			
			poly.add( new Point(45,89));
			poly.add( new Point(135,88.5));
			poly.add( new Point(225,89));
			poly.add( new Point(315,88.5));
			
			//Lp.put("luap",new Polygone(poly));
			poly.clear();
			
			poly.add(new Point(-5,10));
			poly.add(new Point(6,10));
			poly.add(new Point(-4,0));
			poly.add(new Point(4,-9));
			poly.add(new Point(-6,-9));
			poly.add(new Point(-1,-5));
			poly.add(new Point(-5,-1));
			poly.add(new Point(-10,-5));
			poly.add(new Point(-10,6));
			poly.add(new Point(-5,0));
			poly.add(new Point(-1,5));
			
			Lp.put("triforce2",new Polygone(poly));
			poly.clear();
			
			poly.add(new Point(5,5));
			poly.add(new Point(0,0));
			poly.add(new Point(5,-5));
			poly.add(new Point(-5,-5));
			//poly.add(new Point(0,0));
			poly.add(new Point(5,5));
			
			Lp.put("monoforce",new Polygone(poly));
			poly.clear();
			
			poly.add(new Point(-5,10));
			poly.add(new Point(5,10));
			poly.add(new Point(0,5));
			poly.add(new Point(0,-5));
			poly.add(new Point(5,-10));
			poly.add(new Point(-5,-10));
			poly.add(new Point(0,-5));
			poly.add(new Point(0,5));
			
			Lp.put("biforce",new Polygone(poly));
			poly.clear();
			
			poly.add(new Point(0,0.6));
			poly.add(new Point(0.5,0.5));
			poly.add(new Point(0.6,0));
			poly.add(new Point(0.5,-0.5));
			poly.add(new Point(0,-0.6));
			poly.add(new Point(-0.5,-0.5));
			poly.add(new Point(-0.6,0));
			poly.add(new Point(-0.5,0.5));
			
			Lp.put("convex", new Polygone(poly));
			poly.clear();
			
			poly.add(new Point(-2,-1));
			poly.add(new Point(-2,5));
			poly.add(new Point(0,5));
			poly.add(new Point(0,2));
			poly.add(new Point(-1,2));
			poly.add(new Point(0,1));
			
			Lp.put("point", new Polygone(poly));
			poly.clear();
			
			poly.add(new Point(-0.5,-0.5));
			poly.add(new Point(-0.5,0.5));
			poly.add(new Point(0.5,0.5));
			poly.add(new Point(0.5,-0.5));
			
			Lp.put("minicar", new Polygone(poly));
			poly.clear();
			
			poly.add(new Point(-5,5));
			poly.add(new Point(5,5));
			poly.add(new Point(0,0));
			poly.add(new Point(-10,-10));
			poly.add(new Point(10,-5));
			//poly.add(new Point(0,0));
			
			Lp.put("sable", new Polygone(poly));
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
