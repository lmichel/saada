package saadaql.correlator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author laurentmichel
 * * @version $Id: Parser.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class Parser implements ParserConstants {
        private static final String   UNIT       = "[+\\-/\\.\\[\\]\\w]+";
        private static final String   K_VALUE    = "[+\\-]?[1-9]\\d*";
        private static final String   DIST_VALUE = "\\+?(?:\\d*\\.)?\\d+(?:[Ee][+\\-]?\\d*)?";

        public static final class PrincipalNode {
                private final String relation_name;
                private final From primary;
                private final From secondary;
                private final List<Condition> condList = new ArrayList<Condition>();
                private final List<ConditionUCD> condListUCD = new ArrayList<ConditionUCD>();
                private final List<Qualif> qualList = new ArrayList<Qualif>();
                private ConditionDist condDist;
                private ConditionKnn condKnn;
                private PrincipalNode(String relation,From prim, From sec){
                        this.relation_name = relation;
                        this.primary = prim;
                        this.secondary = sec;
                }
                private final void addCondition(Condition cond){this.condList.add(cond);}
                private final void addConditionUCD(ConditionUCD cond){this.condListUCD.add(cond);}
                private final void addQualif(Qualif qualif){this.qualList.add(qualif);}
                private final void setConditionDist(ConditionDist cd){this.condDist=cd;}
                private final void setConditionKnn(ConditionKnn cknn){this.condKnn=cknn;}


                public final String getRelationName(){ return this.relation_name; }
                public final From getPrimary()  { return this.primary;  }
                public final From getSecondary(){ return this.secondary;}
                public final List<Condition>    getCondList(){ return this.condList; }
                public final List<ConditionUCD> getCondListUCD(){ return this.condListUCD; }
                public final List<Qualif>       getQualList(){ return this.qualList; }
                public final ConditionDist getCondDist(){ return this.condDist; }
                public final ConditionKnn getCondKnn(){ return this.condKnn; }
        }

        public static final class From {
                private final String[] listClass;
                private String was;
                private String wac;
                private String wucd;
                private From(String[] lclass){
                        this.listClass = lclass;
                }
                public final String[] getListClass(){ return this.listClass; }
                public final boolean hasWAS(){ return this.was!=null; }
                public final boolean hasWAC(){ return this.wac!=null; }
                public final boolean hasWUCD(){ return this.wucd!=null; }
                public final String getWAS(){ return this.was; }
                public final String getWAC(){ return this.wac; }
                public final String getWUCD(){ return this.wucd; }
        }

        public static final class Condition{
                private final String str;
                private Condition(String value){this.str = value;}
                public final String getStr(){ return this.str; }
        }
        public static final class ConditionUCD{
                private final String str;
                private ConditionUCD(String value){this.str = value;}
                public final String getStr(){ return this.str; }
        }

        //private interface ConditionWithUnit{
        //	private void setUnit(String unit);
        //}

        public static final class ConditionDist { // implements ConditionWithUnit {
                private final String in;
                private double dist;
                private String unit = "none";
                private ConditionDist(String str){ this.in = str; parse(); }
                private final void parse(){
                        Pattern p = Pattern.compile("^\\{\\s*("+DIST_VALUE+")\\s*(?:\\[("+UNIT+")\\])?\\s*\\}$");
                        Matcher m = p.matcher(this.in);
                        if(m.find()){
                                this.dist = Double.parseDouble(m.group(1));
                                if(m.group(2)!=null && !m.group(2).equals("")){ this.unit = m.group(2); }
                        }else{
                                throw new IllegalArgumentException("ConditionDist \""+this.in+"\" doesn't respect the syntax \"{ distance ( [unit] ) }\" !");
                        }
                }
                //private ConditionDist(double dist){ this.dist = dist; }
                //private final void setUnit(String unti){ this.unit=unit; }
                public final double getDist(){ return this.dist; }
                public final String getUnit(){ return this.unit; }
        }

        public static final class ConditionKnn {// implements ConditionWithUnit {
                private final String in;
                private int k;
                private double dist = Double.MAX_VALUE;
                private String unit = "none";
                private ConditionKnn(String str){ this.in = str; parse(); }
                private final void parse(){
                        Pattern p = Pattern.compile("^\\{\\s*("+K_VALUE+")\\s*(?:,\\s*("+DIST_VALUE+")\\s*(?:\\[("+UNIT+")\\])?)?\\s*\\}$");
                        Matcher m = p.matcher(this.in);
                        if(m.find()){
                                this.k = Integer.parseInt(m.group(1));
                                if(m.group(2)!=null && !m.group(2).equals("")){ this.dist = Double.parseDouble(m.group(2)); }
                                if(m.group(3)!=null && !m.group(3).equals("")){ this.unit = m.group(3); }
                        }else{
                                throw new IllegalArgumentException("ConditionKnn \""+this.in+"\" doesn't respect the syntax \"{ integer (, distance ( [unit] ) ) }\" !");
                        }
                }
                //private ConditionKnn(int k){this.k = k;}
                //private final void setDist(double dist){ this.dist = dist; }
                //private final void setUnit(String unti){ this.unit = unit; }
                public final int getK(){ return this.k; }
                public final double getDist(){ return this.dist; }
                public final String getUnit(){ return this.unit; }
        }

        public static final class Qualif{
                private final String name;
                private final String str;
                private Qualif(String name,String value){
                        this.name = name;
                        this.str = value;
                }
                public final String getName(){ return this.name;}
                public final String getStr() { return this.str; }
        }



  public static void main(String args[]) throws ParseException {
    String query =  "PopulateRelation myr\n"+
                     "PrimaryFrom fdqsf \n"+
                     "  WhereUCD{ [ucd] > 5 [unit] } \n"+
                     "  WhereAttributeClass{ c.ra > 4 } \n"+
                     "  WhereAttributeSaada{ gsd.ss > 4 } \n"+
                     "SecondaryFrom sec1,sec2 \n"+
                     "  WhereAttributeSaada{ c.ra > 4 } \n"+
                     "q_name.Set { letruc > 0 }\n"+
                     "other_q.Set { substring(toto,3) ~ 'ouioui' }\n"+
                     "Condition{ ra<5 and dec>10 }\n";
    new Parser(new java.io.BufferedReader(new java.io.StringReader( query ) ));
    try {
        PrincipalNode pnode = Parser.parse();
        System.out.println("Parse Successfull") ;
        System.out.println("RelationName: "+pnode.relation_name) ;
        System.out.println("Prim: "+Arrays.toString(pnode.primary.listClass)) ;
        if(pnode.primary.was!=null) System.out.println(" - was: "+pnode.primary.was) ;
        if(pnode.primary.wac!=null) System.out.println(" - wac: "+pnode.primary.wac) ;
        if(pnode.primary.wucd!=null) System.out.println(" - wucd: "+pnode.primary.wucd) ;
        System.out.println("Sec: "+Arrays.toString(pnode.secondary.listClass)) ;
        if(pnode.secondary.was!=null) System.out.println(" - was: "+pnode.secondary.was) ;
        if(pnode.secondary.wac!=null) System.out.println(" - wac: "+pnode.secondary.wac) ;
        if(pnode.secondary.wucd!=null) System.out.println(" - wucd: "+pnode.secondary.wucd) ;
        if(pnode.condList.size()>0){
                for(Condition cond : pnode.condList) System.out.println("Condition: "+cond.str);
        }
        if(pnode.qualList.size()>0){
                for(Qualif qual : pnode.qualList) System.out.println("Qualif: "+qual.name+ " SQL: "+qual.str);
        }
        } catch (Exception e) {
        System.out.println("Exception: "+e.getMessage());
        } catch (Error e) {
        System.out.println("Error: "+e.getMessage());
    }
  }

  static final public PrincipalNode parse() throws ParseException {
          PrincipalNode pn = new PrincipalNode(relationName(),pfrom(),sfrom());
    condQual(pn);
    jj_consume_token(0);
      {if (true) return pn;}
    throw new Error("Missing return statement in function");
  }

  static final public String relationName() throws ParseException {
        Token t;
    jj_consume_token(K_POPR);
    t = jj_consume_token(NAME);
                   {if (true) return t.image ;}
    throw new Error("Missing return statement in function");
  }

  static final public From pfrom() throws ParseException {
        Token t;
    jj_consume_token(K_PFROM);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NAME:
      t = jj_consume_token(NAME);
                           From f1 = new From(new String[]{t.image}) ; withAttrClass(f1)    ; {if (true) return f1;}
      break;
    case LIST_CLASS:
      t = jj_consume_token(LIST_CLASS);
                           From f2 = new From(  t.image.split(",") ) ; wihtoutAttrClass(f2) ; {if (true) return f2;}
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  static final public From sfrom() throws ParseException {
        Token t;
    jj_consume_token(K_SFROM);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NAME:
      t = jj_consume_token(NAME);
                           From f1 = new From(new String[]{t.image}) ; withAttrClass(f1)    ; {if (true) return f1;}
      break;
    case LIST_CLASS:
      t = jj_consume_token(LIST_CLASS);
                           From f2 = new From(  t.image.split(",") ) ; wihtoutAttrClass(f2) ; {if (true) return f2;}
      break;
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  static final public void withAttrClass(From lcw) throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case K_WUCD:
      parseWUCD(lcw);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case K_WAS:
        parseWAS(lcw);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case K_WAC:
          parseWAC(lcw);
          break;
        default:
          jj_la1[2] = jj_gen;

        }
        break;
      case K_WAC:
        parseWAC(lcw);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case K_WAS:
          parseWAS(lcw);
          break;
        default:
          jj_la1[3] = jj_gen;

        }
        break;
      default:
        jj_la1[4] = jj_gen;

      }
      break;
    case K_WAS:
      parseWAS(lcw);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case K_WUCD:
        parseWUCD(lcw);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case K_WAC:
          parseWAC(lcw);
          break;
        default:
          jj_la1[5] = jj_gen;

        }
        break;
      case K_WAC:
        parseWAC(lcw);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case K_WUCD:
          parseWUCD(lcw);
          break;
        default:
          jj_la1[6] = jj_gen;

        }
        break;
      default:
        jj_la1[7] = jj_gen;

      }
      break;
    case K_WAC:
      parseWAC(lcw);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case K_WUCD:
        parseWUCD(lcw);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case K_WAS:
          parseWAS(lcw);
          break;
        default:
          jj_la1[8] = jj_gen;

        }
        break;
      case K_WAS:
        parseWAS(lcw);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case K_WUCD:
          parseWUCD(lcw);
          break;
        default:
          jj_la1[9] = jj_gen;

        }
        break;
      default:
        jj_la1[10] = jj_gen;

      }
      break;
    default:
      jj_la1[11] = jj_gen;

    }
  }

  static final public void wihtoutAttrClass(From lcw) throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case K_WUCD:
      parseWUCD(lcw);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case K_WAS:
        parseWAS(lcw);
        break;
      default:
        jj_la1[12] = jj_gen;

      }
      break;
    case K_WAS:
      parseWAS(lcw);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case K_WUCD:
        parseWUCD(lcw);
        break;
      default:
        jj_la1[13] = jj_gen;

      }
      break;
    default:
      jj_la1[14] = jj_gen;

    }
  }

  static final public void parseWUCD(From lcw) throws ParseException {
Token t;
    jj_consume_token(K_WUCD);
    t = jj_consume_token(BETWEEN_D);
                        lcw.wucd = t.image.replace("{","").replace("}","") ;
  }

  static final public void parseWAS(From lcw) throws ParseException {
        Token t;
    jj_consume_token(K_WAS);
    t = jj_consume_token(BETWEEN_D);
                        lcw.was = t.image.replace("{","").replace("}","") ;
  }

  static final public void parseWAC(From lcw) throws ParseException {
        Token t;
    jj_consume_token(K_WAC);
    t = jj_consume_token(BETWEEN_D);
                        lcw.wac = t.image.replace("{","").replace("}","") ;
  }

  static final public void condQual(PrincipalNode pn) throws ParseException {
        Token t;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case K_COND:
      case K_CUCD:
      case NAME:
        ;
        break;
      default:
        jj_la1[15] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case K_COND:
        t = jj_consume_token(K_COND);
                       parseCond(pn);
        break;
      case K_CUCD:
        t = jj_consume_token(K_CUCD);
                                                       parseCondUCD(pn);
        break;
      case NAME:
        t = jj_consume_token(NAME);
                                                                                        parseQual(pn,t.image);
        break;
      default:
        jj_la1[16] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case K_CKNN:
      t = jj_consume_token(K_CKNN);
                        parseCondKnn(pn); condQual2(pn);
      break;
    case K_CDIST:
      t = jj_consume_token(K_CDIST);
                                                                           parseCondDist(pn); condQual2(pn);
      break;
    default:
      jj_la1[17] = jj_gen;

    }
  }

  static final public void condQual2(PrincipalNode pn) throws ParseException {
        Token t;
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case K_COND:
      case K_CUCD:
      case NAME:
        ;
        break;
      default:
        jj_la1[18] = jj_gen;
        break label_2;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case K_COND:
        t = jj_consume_token(K_COND);
                       parseCond(pn);
        break;
      case K_CUCD:
        t = jj_consume_token(K_CUCD);
                                                       parseCondUCD(pn);
        break;
      case NAME:
        t = jj_consume_token(NAME);
                                                                                        parseQual(pn,t.image);
        break;
      default:
        jj_la1[19] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
  }

  static final public void parseCond(PrincipalNode pn) throws ParseException {
        Token t_sql;
    t_sql = jj_consume_token(BETWEEN_D);
                            pn.addCondition(new Condition(t_sql.image.replace("{","").replace("}","")));
  }

  static final public void parseCondUCD(PrincipalNode pn) throws ParseException {
        Token t_sql;
    t_sql = jj_consume_token(BETWEEN_D);
                            pn.addConditionUCD(new ConditionUCD(t_sql.image.replace("{","").replace("}","")));
  }

  static final public void parseCondDist(PrincipalNode pn) throws ParseException {
        Token t_in;
    t_in = jj_consume_token(BETWEEN_D);
                           pn.setConditionDist(new ConditionDist(t_in.image));
  }

  static final public void parseCondKnn(PrincipalNode pn) throws ParseException {
        Token t_in;
    t_in = jj_consume_token(BETWEEN_D);
                           pn.setConditionKnn(new ConditionKnn(t_in.image));
  }

//void parseUnit(ConditionWithUnit cwu):{
//	Token t_unit;
//}{
//	<D_UNIT_L>
//	t_unit=<UNITS> { cwu.setUnit(t_unit); }
//	<D_UNIT_R>
//}
  static final public void parseQual(PrincipalNode pn,String qname) throws ParseException {
        Token t_sql;
    jj_consume_token(K_SQUAL);
    t_sql = jj_consume_token(BETWEEN_D);
                            pn.addQualif(new Qualif(qname,t_sql.image.replace("{","").replace("}","")));
  }

  static private boolean jj_initialized_once = false;
  static public ParserTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  static public Token token, jj_nt;
  static private int jj_ntk;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[20];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x600000,0x600000,0x8000,0x4000,0xc000,0x8000,0x10000,0x18000,0x4000,0x10000,0x14000,0x1c000,0x4000,0x10000,0x14000,0x200600,0x200600,0x1800,0x200600,0x200600,};
   }

  public Parser(java.io.InputStream stream) {
     this(stream, null);
  }
  public Parser(java.io.InputStream stream, String encoding) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  static public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  public Parser(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  public Parser(ParserTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  public void ReInit(ParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  static final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.Vector<int[]> jj_expentries = new java.util.Vector<int[]>();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  static public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[29];
    for (int i = 0; i < 29; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 20; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 29; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  static final public void enable_tracing() {
  }

  static final public void disable_tracing() {
  }

}
