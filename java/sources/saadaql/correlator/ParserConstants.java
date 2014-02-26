package saadaql.correlator;

/**
 * @author laurentmichel
 * * @version $Id: ParserConstants.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public interface ParserConstants {

  int EOF = 0;
  int K_POPR = 6;
  int K_PFROM = 7;
  int K_SFROM = 8;
  int K_COND = 9;
  int K_CUCD = 10;
  int K_CDIST = 11;
  int K_CKNN = 12;
  int K_SQUAL = 13;
  int K_WAS = 14;
  int K_WAC = 15;
  int K_WUCD = 16;
  int D_OPEN = 17;
  int D_CLOSE = 18;
  int D_UNIT_L = 19;
  int D_UNIT_R = 20;
  int NAME = 21;
  int LIST_CLASS = 22;
  int LETTER = 23;
  int DIGIT = 24;
  int WORD_CHARS = 25;
  int COMMA = 26;
  int BETWEEN_D = 27;
  int ALMOST_ALL = 28;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\r\"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\\n\"",
    "\"PopulateRelation\"",
    "\"PrimaryFrom\"",
    "\"SecondaryFrom\"",
    "\"Condition\"",
    "\"ConditionUCD\"",
    "\"ConditionDist\"",
    "\"ConditionKnn\"",
    "\".Set\"",
    "\"WhereAttributeSaada\"",
    "\"WhereAttributeClass\"",
    "\"WhereUCD\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "<NAME>",
    "<LIST_CLASS>",
    "<LETTER>",
    "<DIGIT>",
    "<WORD_CHARS>",
    "\",\"",
    "<BETWEEN_D>",
    "<ALMOST_ALL>",
  };

}
