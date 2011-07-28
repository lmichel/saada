package saadaql.correlator;

public class ParserTokenManager implements ParserConstants
{
  public static  java.io.PrintStream debugStream = System.out;
  public static  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private static final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x20000L) != 0L)
            return 2;
         if ((active0 & 0x1dfc0L) != 0L)
         {
            jjmatchedKind = 21;
            return 10;
         }
         return -1;
      case 1:
         if ((active0 & 0x1dfc0L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 1;
            return 10;
         }
         return -1;
      case 2:
         if ((active0 & 0x1dfc0L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 2;
            return 10;
         }
         return -1;
      case 3:
         if ((active0 & 0x1dfc0L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 3;
            return 10;
         }
         return -1;
      case 4:
         if ((active0 & 0x1dfc0L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 4;
            return 10;
         }
         return -1;
      case 5:
         if ((active0 & 0x1dfc0L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 5;
            return 10;
         }
         return -1;
      case 6:
         if ((active0 & 0x1dfc0L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 6;
            return 10;
         }
         return -1;
      case 7:
         if ((active0 & 0x10000L) != 0L)
            return 10;
         if ((active0 & 0xdfc0L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 7;
            return 10;
         }
         return -1;
      case 8:
         if ((active0 & 0x1e00L) != 0L)
            return 10;
         if ((active0 & 0xc1c0L) != 0L)
         {
            if (jjmatchedPos != 8)
            {
               jjmatchedKind = 21;
               jjmatchedPos = 8;
            }
            return 10;
         }
         return -1;
      case 9:
         if ((active0 & 0xddc0L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 9;
            return 10;
         }
         return -1;
      case 10:
         if ((active0 & 0x80L) != 0L)
            return 10;
         if ((active0 & 0xdd40L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 10;
            return 10;
         }
         return -1;
      case 11:
         if ((active0 & 0x1400L) != 0L)
            return 10;
         if ((active0 & 0xc940L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 11;
            return 10;
         }
         return -1;
      case 12:
         if ((active0 & 0x900L) != 0L)
            return 10;
         if ((active0 & 0xc040L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 12;
            return 10;
         }
         return -1;
      case 13:
         if ((active0 & 0xc040L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 13;
            return 10;
         }
         return -1;
      case 14:
         if ((active0 & 0xc040L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 14;
            return 10;
         }
         return -1;
      case 15:
         if ((active0 & 0xc000L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 15;
            return 10;
         }
         if ((active0 & 0x40L) != 0L)
            return 10;
         return -1;
      case 16:
         if ((active0 & 0xc000L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 16;
            return 10;
         }
         return -1;
      case 17:
         if ((active0 & 0xc000L) != 0L)
         {
            jjmatchedKind = 21;
            jjmatchedPos = 17;
            return 10;
         }
         return -1;
      default :
         return -1;
   }
}
private static final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
static private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
static private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 13:
         jjmatchedKind = 2;
         return jjMoveStringLiteralDfa1_0(0x20L);
      case 44:
         return jjStopAtPos(0, 26);
      case 46:
         return jjMoveStringLiteralDfa1_0(0x2000L);
      case 67:
         return jjMoveStringLiteralDfa1_0(0x1e00L);
      case 80:
         return jjMoveStringLiteralDfa1_0(0xc0L);
      case 83:
         return jjMoveStringLiteralDfa1_0(0x100L);
      case 87:
         return jjMoveStringLiteralDfa1_0(0x1c000L);
      case 91:
         return jjStopAtPos(0, 19);
      case 93:
         return jjStopAtPos(0, 20);
      case 123:
         return jjStartNfaWithStates_0(0, 17, 2);
      case 125:
         return jjStopAtPos(0, 18);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
static private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 10:
         if ((active0 & 0x20L) != 0L)
            return jjStopAtPos(1, 5);
         break;
      case 83:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000L);
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x100L);
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0x1c000L);
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x1e40L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x80L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
static private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa3_0(active0, 0x100L);
      case 101:
         return jjMoveStringLiteralDfa3_0(active0, 0x1e000L);
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x80L);
      case 110:
         return jjMoveStringLiteralDfa3_0(active0, 0x1e00L);
      case 112:
         return jjMoveStringLiteralDfa3_0(active0, 0x40L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
static private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 100:
         return jjMoveStringLiteralDfa4_0(active0, 0x1e00L);
      case 109:
         return jjMoveStringLiteralDfa4_0(active0, 0x80L);
      case 111:
         return jjMoveStringLiteralDfa4_0(active0, 0x100L);
      case 114:
         return jjMoveStringLiteralDfa4_0(active0, 0x1c000L);
      case 116:
         if ((active0 & 0x2000L) != 0L)
            return jjStopAtPos(3, 13);
         break;
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x40L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
static private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa5_0(active0, 0x80L);
      case 101:
         return jjMoveStringLiteralDfa5_0(active0, 0x1c000L);
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0x1e00L);
      case 108:
         return jjMoveStringLiteralDfa5_0(active0, 0x40L);
      case 110:
         return jjMoveStringLiteralDfa5_0(active0, 0x100L);
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
static private final int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa6_0(active0, 0xc000L);
      case 85:
         return jjMoveStringLiteralDfa6_0(active0, 0x10000L);
      case 97:
         return jjMoveStringLiteralDfa6_0(active0, 0x40L);
      case 100:
         return jjMoveStringLiteralDfa6_0(active0, 0x100L);
      case 114:
         return jjMoveStringLiteralDfa6_0(active0, 0x80L);
      case 116:
         return jjMoveStringLiteralDfa6_0(active0, 0x1e00L);
      default :
         break;
   }
   return jjStartNfa_0(4, active0);
}
static private final int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0);
      return 6;
   }
   switch(curChar)
   {
      case 67:
         return jjMoveStringLiteralDfa7_0(active0, 0x10000L);
      case 97:
         return jjMoveStringLiteralDfa7_0(active0, 0x100L);
      case 105:
         return jjMoveStringLiteralDfa7_0(active0, 0x1e00L);
      case 116:
         return jjMoveStringLiteralDfa7_0(active0, 0xc040L);
      case 121:
         return jjMoveStringLiteralDfa7_0(active0, 0x80L);
      default :
         break;
   }
   return jjStartNfa_0(5, active0);
}
static private final int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(5, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0);
      return 7;
   }
   switch(curChar)
   {
      case 68:
         if ((active0 & 0x10000L) != 0L)
            return jjStartNfaWithStates_0(7, 16, 10);
         break;
      case 70:
         return jjMoveStringLiteralDfa8_0(active0, 0x80L);
      case 101:
         return jjMoveStringLiteralDfa8_0(active0, 0x40L);
      case 111:
         return jjMoveStringLiteralDfa8_0(active0, 0x1e00L);
      case 114:
         return jjMoveStringLiteralDfa8_0(active0, 0x100L);
      case 116:
         return jjMoveStringLiteralDfa8_0(active0, 0xc000L);
      default :
         break;
   }
   return jjStartNfa_0(6, active0);
}
static private final int jjMoveStringLiteralDfa8_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(6, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(7, active0);
      return 8;
   }
   switch(curChar)
   {
      case 82:
         return jjMoveStringLiteralDfa9_0(active0, 0x40L);
      case 110:
         if ((active0 & 0x200L) != 0L)
         {
            jjmatchedKind = 9;
            jjmatchedPos = 8;
         }
         return jjMoveStringLiteralDfa9_0(active0, 0x1c00L);
      case 114:
         return jjMoveStringLiteralDfa9_0(active0, 0xc080L);
      case 121:
         return jjMoveStringLiteralDfa9_0(active0, 0x100L);
      default :
         break;
   }
   return jjStartNfa_0(7, active0);
}
static private final int jjMoveStringLiteralDfa9_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(7, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(8, active0);
      return 9;
   }
   switch(curChar)
   {
      case 68:
         return jjMoveStringLiteralDfa10_0(active0, 0x800L);
      case 70:
         return jjMoveStringLiteralDfa10_0(active0, 0x100L);
      case 75:
         return jjMoveStringLiteralDfa10_0(active0, 0x1000L);
      case 85:
         return jjMoveStringLiteralDfa10_0(active0, 0x400L);
      case 101:
         return jjMoveStringLiteralDfa10_0(active0, 0x40L);
      case 105:
         return jjMoveStringLiteralDfa10_0(active0, 0xc000L);
      case 111:
         return jjMoveStringLiteralDfa10_0(active0, 0x80L);
      default :
         break;
   }
   return jjStartNfa_0(8, active0);
}
static private final int jjMoveStringLiteralDfa10_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(8, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(9, active0);
      return 10;
   }
   switch(curChar)
   {
      case 67:
         return jjMoveStringLiteralDfa11_0(active0, 0x400L);
      case 98:
         return jjMoveStringLiteralDfa11_0(active0, 0xc000L);
      case 105:
         return jjMoveStringLiteralDfa11_0(active0, 0x800L);
      case 108:
         return jjMoveStringLiteralDfa11_0(active0, 0x40L);
      case 109:
         if ((active0 & 0x80L) != 0L)
            return jjStartNfaWithStates_0(10, 7, 10);
         break;
      case 110:
         return jjMoveStringLiteralDfa11_0(active0, 0x1000L);
      case 114:
         return jjMoveStringLiteralDfa11_0(active0, 0x100L);
      default :
         break;
   }
   return jjStartNfa_0(9, active0);
}
static private final int jjMoveStringLiteralDfa11_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(9, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(10, active0);
      return 11;
   }
   switch(curChar)
   {
      case 68:
         if ((active0 & 0x400L) != 0L)
            return jjStartNfaWithStates_0(11, 10, 10);
         break;
      case 97:
         return jjMoveStringLiteralDfa12_0(active0, 0x40L);
      case 110:
         if ((active0 & 0x1000L) != 0L)
            return jjStartNfaWithStates_0(11, 12, 10);
         break;
      case 111:
         return jjMoveStringLiteralDfa12_0(active0, 0x100L);
      case 115:
         return jjMoveStringLiteralDfa12_0(active0, 0x800L);
      case 117:
         return jjMoveStringLiteralDfa12_0(active0, 0xc000L);
      default :
         break;
   }
   return jjStartNfa_0(10, active0);
}
static private final int jjMoveStringLiteralDfa12_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(10, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(11, active0);
      return 12;
   }
   switch(curChar)
   {
      case 109:
         if ((active0 & 0x100L) != 0L)
            return jjStartNfaWithStates_0(12, 8, 10);
         break;
      case 116:
         if ((active0 & 0x800L) != 0L)
            return jjStartNfaWithStates_0(12, 11, 10);
         return jjMoveStringLiteralDfa13_0(active0, 0xc040L);
      default :
         break;
   }
   return jjStartNfa_0(11, active0);
}
static private final int jjMoveStringLiteralDfa13_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(11, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(12, active0);
      return 13;
   }
   switch(curChar)
   {
      case 101:
         return jjMoveStringLiteralDfa14_0(active0, 0xc000L);
      case 105:
         return jjMoveStringLiteralDfa14_0(active0, 0x40L);
      default :
         break;
   }
   return jjStartNfa_0(12, active0);
}
static private final int jjMoveStringLiteralDfa14_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(12, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(13, active0);
      return 14;
   }
   switch(curChar)
   {
      case 67:
         return jjMoveStringLiteralDfa15_0(active0, 0x8000L);
      case 83:
         return jjMoveStringLiteralDfa15_0(active0, 0x4000L);
      case 111:
         return jjMoveStringLiteralDfa15_0(active0, 0x40L);
      default :
         break;
   }
   return jjStartNfa_0(13, active0);
}
static private final int jjMoveStringLiteralDfa15_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(13, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(14, active0);
      return 15;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa16_0(active0, 0x4000L);
      case 108:
         return jjMoveStringLiteralDfa16_0(active0, 0x8000L);
      case 110:
         if ((active0 & 0x40L) != 0L)
            return jjStartNfaWithStates_0(15, 6, 10);
         break;
      default :
         break;
   }
   return jjStartNfa_0(14, active0);
}
static private final int jjMoveStringLiteralDfa16_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(14, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(15, active0);
      return 16;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa17_0(active0, 0xc000L);
      default :
         break;
   }
   return jjStartNfa_0(15, active0);
}
static private final int jjMoveStringLiteralDfa17_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(15, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(16, active0);
      return 17;
   }
   switch(curChar)
   {
      case 100:
         return jjMoveStringLiteralDfa18_0(active0, 0x4000L);
      case 115:
         return jjMoveStringLiteralDfa18_0(active0, 0x8000L);
      default :
         break;
   }
   return jjStartNfa_0(16, active0);
}
static private final int jjMoveStringLiteralDfa18_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(16, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(17, active0);
      return 18;
   }
   switch(curChar)
   {
      case 97:
         if ((active0 & 0x4000L) != 0L)
            return jjStartNfaWithStates_0(18, 14, 10);
         break;
      case 115:
         if ((active0 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(18, 15, 10);
         break;
      default :
         break;
   }
   return jjStartNfa_0(17, active0);
}
static private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
static private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
static private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
static private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
static private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 10;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 10:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                     jjCheckNAddTwoStates(6, 7);
                  }
                  else if (curChar == 44)
                     jjstateSet[jjnewStateCnt++] = 8;
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 21)
                        kind = 21;
                     jjCheckNAdd(5);
                  }
                  break;
               case 0:
                  if (curChar == 42 && kind > 22)
                     kind = 22;
                  break;
               case 2:
                  jjAddStates(0, 1);
                  break;
               case 5:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAdd(5);
                  break;
               case 6:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddTwoStates(6, 7);
                  break;
               case 7:
                  if (curChar == 44)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 9:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddTwoStates(7, 9);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 10:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                     jjCheckNAddTwoStates(6, 7);
                  }
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 21)
                        kind = 21;
                     jjCheckNAdd(5);
                  }
                  break;
               case 0:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 21)
                        kind = 21;
                     jjCheckNAddStates(2, 4);
                  }
                  else if (curChar == 123)
                     jjCheckNAdd(2);
                  break;
               case 1:
                  if (curChar == 123)
                     jjCheckNAdd(2);
                  break;
               case 2:
                  if ((0xd7ffffffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(2, 3);
                  break;
               case 3:
                  if (curChar == 125)
                     kind = 27;
                  break;
               case 4:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAddStates(2, 4);
                  break;
               case 5:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAdd(5);
                  break;
               case 6:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddTwoStates(6, 7);
                  break;
               case 8:
               case 9:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddTwoStates(7, 9);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 2:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(0, 1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 10 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   2, 3, 5, 6, 7, 
};
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, 
"\120\157\160\165\154\141\164\145\122\145\154\141\164\151\157\156", "\120\162\151\155\141\162\171\106\162\157\155", 
"\123\145\143\157\156\144\141\162\171\106\162\157\155", "\103\157\156\144\151\164\151\157\156", 
"\103\157\156\144\151\164\151\157\156\125\103\104", "\103\157\156\144\151\164\151\157\156\104\151\163\164", 
"\103\157\156\144\151\164\151\157\156\113\156\156", "\56\123\145\164", 
"\127\150\145\162\145\101\164\164\162\151\142\165\164\145\123\141\141\144\141", 
"\127\150\145\162\145\101\164\164\162\151\142\165\164\145\103\154\141\163\163", "\127\150\145\162\145\125\103\104", "\173", "\175", "\133", "\135", null, null, 
null, null, null, "\54", null, null, };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0xc7fffc1L, 
};
static final long[] jjtoSkip = {
   0x3eL, 
};
static protected SimpleCharStream input_stream;
static private final int[] jjrounds = new int[10];
static private final int[] jjstateSet = new int[20];
static protected char curChar;
public ParserTokenManager(SimpleCharStream stream){
   if (input_stream != null)
      throw new TokenMgrError("ERROR: Second call to constructor of static lexer. You must use ReInit() to initialize the static variables.", TokenMgrError.STATIC_LEXER_ERROR);
   input_stream = stream;
}
public ParserTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}
static public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
static private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 10; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
static public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
static public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

static protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

static int curLexState = 0;
static int defaultLexState = 0;
static int jjnewStateCnt;
static int jjround;
static int jjmatchedPos;
static int jjmatchedKind;

public static Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100000600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
