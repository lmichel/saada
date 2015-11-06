package saadadb.query.parser;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZQuery;
import Zql.ZStatement;
import Zql.ZqlParser;

/**
 * Made a long time ago...
 * @author pineau 
 */
public final class SqlParser{
	
	/**
	 * @param strQuery
	 * @return
	 * @throws QueryException
	 */
	public static final String[] getAttributes(String strQuery) throws QueryException{
		String str = "select * from tab where "+strQuery+" ;exit;"; // To be parse by zql
		List<String> myal = parse(str);
		for(Iterator<String> it = myal.iterator();it.hasNext();){
			str =it.next(); 
		    if(str.matches(RegExp.NO_ATTR)){
		       it.remove();
		    } 
		}
		return (String[])myal.toArray(new String[0]);	
	}
	
	public static final List<String> parse(String strQuery) throws QueryException{
		List<String> al = new ArrayList<String>();
		try{
			/*
			 *  Replace & with + because ZQL doesn't support (a & b) == c
			 *  Used with Vizier KW
			 */
			byte[] byteArray = strQuery.replaceAll("&","+").getBytes("ISO-8859-1"); // choose a charset
			ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
			ZqlParser p = new ZqlParser( bais );
			ZStatement st = null;
			while((st = p.readStatement()) != null) {
				if(st instanceof ZQuery) {
					ZExp exp = ((ZQuery)st).getWhere();
					if(exp instanceof ZExpression){
						Vector vzexp = ((ZExpression)exp).getOperands();
						//Recursivite pour parser les imbrications
						int taille = vzexp.size();
						vzexp = myfunc(vzexp);
						while(taille!=vzexp.size()){
							taille = vzexp.size();
							vzexp = myfunc(vzexp);
						}
						//On se retrouve avec les briques de base, qui doivent toutes etre des "constant"
						for(int i=0;i<vzexp.size();i++){
							//System.out.println( ((ZExp)vzexp.get(i)).toString() );
							ZExp exp2 = ((ZExp)vzexp.get(i));
							if(exp2 instanceof ZConstant){
								//System.out.println("Constant: " + ((ZConstant)exp2).toString() );
								boolean found = false;
								for( int z=0 ; z<al.size() ; z++ ) {
									String content = (String)(al.get(z));
									if( content.equals(((ZConstant)exp2).toString()) ) {
										found = true;
										break;
									}
								}
								if( found == false ) {
									al.add(((ZConstant)exp2).toString());
								}
							}
						}
					}
				} 
			}
		}catch(Exception e){
			if( e.getMessage().indexOf("ndefined function") > 0 ) {
				Messenger.printMsg(Messenger.WARNING, "ZQL lack: " + e.getMessage() + ": Constrained columns won't be put in the query result");
			}
			else {
				Messenger.printStackTrace(e);
				QueryException.throwNewException(SaadaException.SYNTAX_ERROR, e);
			}
		}
		return al;
	}
	
	/**
	 * @param args
	 * Cette fonction renvoie un vecteur en parsant le contenue des ZExpressions
	 * appele recursivement, elle parse tous les niveau de parenthsage et le vecteur
	 * de sortie ne contient plus aucune ZExpression
	 */
	@SuppressWarnings("unchecked")
	public static final Vector myfunc(Vector myVect){
		Vector returnedVect = new Vector();
		for(int i=0;i<myVect.size();i++){
			if(myVect.get(i) instanceof ZExpression){
				returnedVect.addAll(((ZExpression)myVect.get(i)).getOperands());
			}
			else{
				returnedVect.add(myVect.get(i));
			}
		}
		return returnedVect;
	}
	
	public static final String[] parseAttributes(String str) throws QueryException{
	    str = str.replaceAll("_","a_");//Obligatoire  cause d'un bug de zql
	    String[] strTab = getAttributes(str);
	    for(int i=0;i<strTab.length;i++){
	        strTab[i] = strTab[i].replaceAll("a_","_");
		//System.out.println("Attribut detecte par parsing sql: "+strTab[i]);
	    }
	    return strTab;
	}
}
