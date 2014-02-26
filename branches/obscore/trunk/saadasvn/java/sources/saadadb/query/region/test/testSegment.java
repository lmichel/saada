package saadadb.query.region.test;

import saadadb.query.region.request.Between;
import saadadb.query.region.request.Segment;
import saadadb.query.region.request.Singleton;



public class testSegment {

	public static void main(String[] args) {
		long pix1=1;
		long pix2=2;
		long pix3=3;
		long pix4=4;
		Singleton sing1 = new Singleton (pix1);
		Singleton sing2 = new Singleton (pix2);
		Singleton sing3 = new Singleton (pix3);
		Between bet12 = new Between (pix1,pix2);
		Between bet13=new Between (pix1,pix3);
		Between bet23=new Between (pix2,pix3);
		Between bet24=new Between (pix2,pix4);
		Between bet34=new Between (pix3,pix4);
		
		
		degrade(sing1,bet12);
		bet12 = new Between (pix1,pix2);
		sing1 = new Singleton (pix1);
		
		degrade(sing1,bet23);
		sing1 = new Singleton (pix1);
		bet23=new Between (pix2,pix3);
		
		degrade(sing1,sing1);
		sing1 = new Singleton (pix1);
		
		degrade(sing1,sing2);
		sing2 = new Singleton (pix2);
		sing1 = new Singleton (pix1);
		
		degrade(bet12,sing1);
		bet12 = new Between (pix1,pix2);
		sing1 = new Singleton (pix1);
		
		degrade(bet12,sing2);
		bet12 = new Between (pix1,pix2);
		sing2 = new Singleton (pix2);
		
		degrade(bet12,sing3);
		bet12 = new Between (pix1,pix2);
		sing3 = new Singleton (pix3);
		
		degrade(bet13,sing1);
		bet13 = new Between (pix1,pix3);
		sing1 = new Singleton (pix1);
		
		degrade(bet13,sing2);
		sing2 = new Singleton (pix2);
		bet13=new Between (pix1,pix3);
		
		degrade(bet13,sing3);
		sing3 = new Singleton (pix3);
		bet13=new Between (pix1,pix3);
				
		degrade(bet12,bet12);
		bet12 = new Between (pix1,pix2);
		
		degrade(bet12,bet13);
		bet12 = new Between (pix1,pix2);
		bet13=new Between (pix1,pix3);
		
		degrade(bet12,bet23);
		bet12 = new Between (pix1,pix2);
		bet23=new Between (pix2,pix3);
		
		degrade(bet12,bet34);
		bet12 = new Between (pix1,pix2);
		bet34=new Between (pix3,pix4);
		
		degrade(bet13,bet12);
		bet12 = new Between (pix1,pix2);
		bet13=new Between (pix1,pix3);
		
		degrade(bet13,bet23);
		bet13 = new Between (pix1,pix3);
		bet23=new Between (pix2,pix3);
		
		degrade(bet13,bet24);
		bet13 = new Between (pix1,pix3);
		bet24=new Between (pix2,pix4);

	}
	
	public static void degrade (Segment previous, Segment next) {
			int count=0;
			System.out.println("Prev : "+previous);
			System.out.println("Next : "+next);
			
		
			//if the previous segment can merge with the next one (between - segment )
			if (previous.getEnd()+1 >= next.getStart() && previous.getEnd()!=0) {
				previous.fusion(next);
				System.out.println("Fusion : "+previous);
				count++;
			}
			// if two equals singletons are following (singleton - singleton)
			if (previous.getStart() == next.getStart() && previous.getEnd()==0 && next.getEnd()==0 && count==0) {
				System.out.println("Supression : "+previous);	
				count++;
			}
			//if the previous singleton can merge with the next singleton, we create a Between (singleton - singleton)
			if (previous.getStart()+1 == next.getStart() && previous.getEnd()==0 && next.getEnd()==0 && count==0) {
				Between between = new Between(previous.getStart(),next.getStart());
				System.out.println("Double Singleton : "+between);
				count++;
			}
			//if the previous singleton can merge with the next between (singleton - between)
			if (previous.getStart()+1 >= next.getStart() && previous.getEnd()==0 && next.getEnd()!=0 && count==0) {
				Between between = new Between(previous.getStart(),next.getEnd());
				System.out.println("Fusiona : "+between);
			}
			System.out.println("");
		}

}
