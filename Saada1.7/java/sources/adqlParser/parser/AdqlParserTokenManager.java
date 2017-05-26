package adqlParser.parser;

/** Token Manager. */
public class AdqlParserTokenManager implements AdqlParserConstants
{

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_3(int pos, long active0, long active1)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_3(int pos, long active0, long active1)
{
   return jjMoveNfa_3(jjStopStringLiteralDfa_3(pos, active0, active1), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_3()
{
   switch(curChar)
   {
      case 34:
         return jjStartNfaWithStates_3(0, 96, 1);
      default :
         return jjMoveNfa_3(0, 0);
   }
}
private int jjStartNfaWithStates_3(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_3(state, pos + 1);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private int jjMoveNfa_3(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xfffffffbffffffffL & l) != 0L)
                  {
                     if (kind > 95)
                        kind = 95;
                  }
                  else if (curChar == 34)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 1:
                  if (curChar == 34 && kind > 95)
                     kind = 95;
                  break;
               case 2:
                  if (curChar == 34)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  kind = 95;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((jjbitVec0[i2] & l2) != 0L && kind > 95)
                     kind = 95;
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
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_0(int pos, long active0, long active1)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0xefff1ffdffb40000L) != 0L || (active1 & 0xf0fff4L) != 0L)
         {
            jjmatchedKind = 97;
            return 37;
         }
         if ((active0 & 0x1000a00200400000L) != 0L || (active1 & 0xf0002L) != 0L)
         {
            jjmatchedKind = 97;
            return 16;
         }
         if ((active0 & 0x10L) != 0L)
            return 38;
         if ((active0 & 0x400000000000L) != 0L || (active1 & 0x9L) != 0L)
         {
            jjmatchedKind = 97;
            return 13;
         }
         if ((active0 & 0xc000L) != 0L)
            return 3;
         if ((active0 & 0x200L) != 0L)
            return 19;
         return -1;
      case 1:
         if ((active0 & 0xf7ffceebbeb40000L) != 0L || (active1 & 0xfdfdfeL) != 0L)
         {
            if (jjmatchedPos != 1)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 1;
            }
            return 37;
         }
         if ((active0 & 0x800311441400000L) != 0L || (active1 & 0x20200L) != 0L)
            return 37;
         if ((active1 & 0x1L) != 0L)
         {
            if (jjmatchedPos != 1)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 1;
            }
            return 12;
         }
         return -1;
      case 2:
         if ((active1 & 0x1L) != 0L)
         {
            if (jjmatchedPos != 2)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 2;
            }
            return 11;
         }
         if ((active0 & 0x17a00a00100000L) != 0L || (active1 & 0xf001d2L) != 0L)
            return 37;
         if ((active0 & 0xffe85ee1bfa40000L) != 0L || (active1 & 0xffc2cL) != 0L)
         {
            if (jjmatchedPos != 2)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 2;
            }
            return 37;
         }
         return -1;
      case 3:
         if ((active0 & 0xefe81e4187840000L) != 0L || (active1 & 0xac2cL) != 0L)
         {
            if (jjmatchedPos != 3)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 3;
            }
            return 37;
         }
         if ((active0 & 0x100040a038200000L) != 0L || (active1 & 0xf5000L) != 0L)
            return 37;
         if ((active1 & 0x80L) != 0L)
         {
            if (jjmatchedPos != 3)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 3;
            }
            return 21;
         }
         if ((active1 & 0x1L) != 0L)
         {
            if (jjmatchedPos != 3)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 3;
            }
            return 10;
         }
         return -1;
      case 4:
         if ((active0 & 0xef601e4000840000L) != 0L || (active1 & 0x880dL) != 0L)
         {
            jjmatchedKind = 97;
            jjmatchedPos = 4;
            return 37;
         }
         if ((active0 & 0x88000187000000L) != 0L || (active1 & 0x2420L) != 0L)
            return 37;
         if ((active1 & 0x80080L) != 0L)
            return 21;
         return -1;
      case 5:
         if ((active0 & 0x2400a0000040000L) != 0L)
            return 37;
         if ((active0 & 0x6000000000000000L) != 0L)
            return 21;
         if ((active0 & 0x8d20004000800000L) != 0L || (active1 & 0x880dL) != 0L)
         {
            jjmatchedKind = 97;
            jjmatchedPos = 5;
            return 37;
         }
         if ((active0 & 0x140000000000L) != 0L)
         {
            if (jjmatchedPos < 4)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 4;
            }
            return -1;
         }
         return -1;
      case 6:
         if ((active0 & 0x8c20000000000000L) != 0L || (active1 & 0x8001L) != 0L)
         {
            jjmatchedKind = 97;
            jjmatchedPos = 6;
            return 37;
         }
         if ((active0 & 0x100004000800000L) != 0L || (active1 & 0x80cL) != 0L)
            return 37;
         if ((active0 & 0x140000000000L) != 0L)
         {
            if (jjmatchedPos < 4)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 4;
            }
            return -1;
         }
         return -1;
      case 7:
         if ((active0 & 0x800000000000000L) != 0L)
         {
            jjmatchedKind = 97;
            jjmatchedPos = 7;
            return 37;
         }
         if ((active0 & 0x8420000000000000L) != 0L || (active1 & 0x8001L) != 0L)
            return 37;
         if ((active0 & 0x140000000000L) != 0L)
         {
            if (jjmatchedPos < 4)
            {
               jjmatchedKind = 97;
               jjmatchedPos = 4;
            }
            return -1;
         }
         return -1;
      case 8:
         if ((active0 & 0x800000000000000L) != 0L)
         {
            jjmatchedKind = 97;
            jjmatchedPos = 8;
            return 37;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0, long active1)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 34:
         return jjStopAtPos(0, 94);
      case 39:
         return jjStopAtPos(0, 91);
      case 40:
         return jjStopAtPos(0, 2);
      case 41:
         return jjStopAtPos(0, 3);
      case 42:
         return jjStopAtPos(0, 10);
      case 43:
         return jjStopAtPos(0, 8);
      case 44:
         return jjStopAtPos(0, 5);
      case 45:
         return jjStartNfaWithStates_0(0, 9, 19);
      case 46:
         return jjStartNfaWithStates_0(0, 4, 38);
      case 47:
         return jjStopAtPos(0, 11);
      case 59:
         return jjStopAtPos(0, 6);
      case 60:
         jjmatchedKind = 14;
         return jjMoveStringLiteralDfa1_0(0x8000L, 0x0L);
      case 61:
         return jjStopAtPos(0, 12);
      case 62:
         jjmatchedKind = 16;
         return jjMoveStringLiteralDfa1_0(0x20000L, 0x0L);
      case 65:
      case 97:
         return jjMoveStringLiteralDfa1_0(0x1000a00200400000L, 0xf0002L);
      case 66:
      case 98:
         return jjMoveStringLiteralDfa1_0(0x10004000000000L, 0x0L);
      case 67:
      case 99:
         return jjMoveStringLiteralDfa1_0(0xe468000000000000L, 0x300004L);
      case 68:
      case 100:
         return jjMoveStringLiteralDfa1_0(0x400000000000L, 0x9L);
      case 69:
      case 101:
         return jjMoveStringLiteralDfa1_0(0x20000000000L, 0x10L);
      case 70:
      case 102:
         return jjMoveStringLiteralDfa1_0(0x10200000L, 0x20L);
      case 71:
      case 103:
         return jjMoveStringLiteralDfa1_0(0x40000000000L, 0x0L);
      case 72:
      case 104:
         return jjMoveStringLiteralDfa1_0(0x80000000000L, 0x0L);
      case 73:
      case 105:
         return jjMoveStringLiteralDfa1_0(0x800011001000000L, 0x0L);
      case 74:
      case 106:
         return jjMoveStringLiteralDfa1_0(0x20000000L, 0x0L);
      case 76:
      case 108:
         return jjMoveStringLiteralDfa1_0(0x8008000000L, 0xc0L);
      case 77:
      case 109:
         return jjMoveStringLiteralDfa1_0(0x3000000000000L, 0x100L);
      case 78:
      case 110:
         return jjMoveStringLiteralDfa1_0(0x2800800000L, 0x0L);
      case 79:
      case 111:
         return jjMoveStringLiteralDfa1_0(0x100442000000L, 0x0L);
      case 80:
      case 112:
         return jjMoveStringLiteralDfa1_0(0x180000000000000L, 0x600L);
      case 82:
      case 114:
         return jjMoveStringLiteralDfa1_0(0x200000004000000L, 0x3800L);
      case 83:
      case 115:
         return jjMoveStringLiteralDfa1_0(0x4000000040000L, 0x404000L);
      case 84:
      case 116:
         return jjMoveStringLiteralDfa1_0(0x100000L, 0x808000L);
      case 85:
      case 117:
         return jjMoveStringLiteralDfa1_0(0x80000000L, 0x0L);
      case 87:
      case 119:
         return jjMoveStringLiteralDfa1_0(0x100000000L, 0x0L);
      case 124:
         return jjMoveStringLiteralDfa1_0(0x80L, 0x0L);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0, long active1)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0, active1);
      return 1;
   }
   switch(curChar)
   {
      case 61:
         if ((active0 & 0x8000L) != 0L)
            return jjStopAtPos(1, 15);
         else if ((active0 & 0x20000L) != 0L)
            return jjStopAtPos(1, 17);
         break;
      case 65:
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x1080000800000L, active1, 0x801800L);
      case 66:
      case 98:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x2L);
      case 67:
      case 99:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x10000L);
      case 69:
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x220404008040000L, active1, 0xcL);
      case 72:
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0x100000000L, active1, 0L);
      case 73:
      case 105:
         if ((active1 & 0x200L) != 0L)
            return jjStartNfaWithStates_0(1, 73, 37);
         return jjMoveStringLiteralDfa2_0(active0, 0x42008004000000L, active1, 0x400001L);
      case 76:
      case 108:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x20L);
      case 78:
      case 110:
         if ((active0 & 0x40000000L) != 0L)
            return jjStartNfaWithStates_0(1, 30, 37);
         else if ((active0 & 0x10000000000L) != 0L)
         {
            jjmatchedKind = 40;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x800000201000000L, active1, 0L);
      case 79:
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0xe598000820100000L, active1, 0x3025c0L);
      case 81:
      case 113:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x4000L);
      case 82:
      case 114:
         if ((active0 & 0x400000000L) != 0L)
         {
            jjmatchedKind = 34;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x1000140000200000L, active1, 0x8000L);
      case 83:
      case 115:
         if ((active0 & 0x400000L) != 0L)
         {
            jjmatchedKind = 22;
            jjmatchedPos = 1;
         }
         else if ((active0 & 0x1000000000L) != 0L)
            return jjStartNfaWithStates_0(1, 36, 37);
         return jjMoveStringLiteralDfa2_0(active0, 0x200080000000L, active1, 0x20000L);
      case 84:
      case 116:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0xc0000L);
      case 85:
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x4002012000000L, active1, 0L);
      case 86:
      case 118:
         return jjMoveStringLiteralDfa2_0(active0, 0x800000000000L, active1, 0L);
      case 88:
      case 120:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000000000L, active1, 0x10L);
      case 124:
         if ((active0 & 0x80L) != 0L)
            return jjStopAtPos(1, 7);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0, active1);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(0, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0, active1);
      return 2;
   }
   switch(curChar)
   {
      case 65:
      case 97:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0xc0000L);
      case 67:
      case 99:
         if ((active0 & 0x200000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 45, 37);
         break;
      case 68:
      case 100:
         if ((active0 & 0x200000000L) != 0L)
            return jjStartNfaWithStates_0(2, 33, 37);
         else if ((active1 & 0x100L) != 0L)
            return jjStartNfaWithStates_0(2, 72, 37);
         return jjMoveStringLiteralDfa3_0(active0, 0x100000000000L, active1, 0x800L);
      case 69:
      case 101:
         return jjMoveStringLiteralDfa3_0(active0, 0x1000000100000000L, active1, 0L);
      case 70:
      case 102:
         return jjMoveStringLiteralDfa3_0(active0, 0x8000000L, active1, 0L);
      case 71:
      case 103:
         if ((active0 & 0x800000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 47, 37);
         else if ((active1 & 0x40L) != 0L)
         {
            jjmatchedKind = 70;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x200000004000000L, active1, 0x88L);
      case 73:
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x800200a0000000L, active1, 0x20004L);
      case 75:
      case 107:
         return jjMoveStringLiteralDfa3_0(active0, 0x8000000000L, active1, 0L);
      case 76:
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x100002010040000L, active1, 0L);
      case 77:
      case 109:
         if ((active0 & 0x4000000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 50, 37);
         break;
      case 78:
      case 110:
         if ((active0 & 0x2000000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 49, 37);
         else if ((active1 & 0x400000L) != 0L)
            return jjStartNfaWithStates_0(2, 86, 37);
         else if ((active1 & 0x800000L) != 0L)
            return jjStartNfaWithStates_0(2, 87, 37);
         return jjMoveStringLiteralDfa3_0(active0, 0x420000001000000L, active1, 0x1000L);
      case 79:
      case 111:
         return jjMoveStringLiteralDfa3_0(active0, 0xe000040000200000L, active1, 0x10020L);
      case 80:
      case 112:
         if ((active0 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(2, 20, 37);
         else if ((active1 & 0x10L) != 0L)
            return jjStartNfaWithStates_0(2, 68, 37);
         break;
      case 82:
      case 114:
         return jjMoveStringLiteralDfa3_0(active0, 0x40000000000000L, active1, 0x4000L);
      case 83:
      case 115:
         if ((active1 & 0x2L) != 0L)
            return jjStartNfaWithStates_0(2, 65, 37);
         else if ((active1 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(2, 84, 37);
         return jjMoveStringLiteralDfa3_0(active0, 0x400000000000L, active1, 0x1L);
      case 84:
      case 116:
         if ((active0 & 0x800000000L) != 0L)
            return jjStartNfaWithStates_0(2, 35, 37);
         else if ((active1 & 0x200000L) != 0L)
            return jjStartNfaWithStates_0(2, 85, 37);
         return jjMoveStringLiteralDfa3_0(active0, 0x800004002800000L, active1, 0L);
      case 85:
      case 117:
         return jjMoveStringLiteralDfa3_0(active0, 0x8000000000000L, active1, 0xa000L);
      case 86:
      case 118:
         return jjMoveStringLiteralDfa3_0(active0, 0x80000000000L, active1, 0L);
      case 87:
      case 119:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x400L);
      case 88:
      case 120:
         if ((active0 & 0x1000000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 48, 37);
         else if ((active0 & 0x10000000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 52, 37);
         break;
      default :
         break;
   }
   return jjStartNfa_0(1, active0, active1);
}
private int jjMoveStringLiteralDfa3_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(1, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0, active1);
      return 3;
   }
   switch(curChar)
   {
      case 49:
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x80L);
      case 65:
      case 97:
         if ((active0 & 0x1000000000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 60, 37);
         break;
      case 67:
      case 99:
         if ((active0 & 0x400000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 46, 37);
         return jjMoveStringLiteralDfa4_0(active0, 0x40000000000000L, active1, 0L);
      case 68:
      case 100:
         if ((active1 & 0x1000L) != 0L)
            return jjStartNfaWithStates_0(3, 76, 37);
         break;
      case 69:
      case 101:
         if ((active0 & 0x8000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 39, 37);
         return jjMoveStringLiteralDfa4_0(active0, 0x800100003040000L, active1, 0x400L);
      case 72:
      case 104:
         return jjMoveStringLiteralDfa4_0(active0, 0x4000000L, active1, 0L);
      case 73:
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x200080000000000L, active1, 0x800L);
      case 76:
      case 108:
         if ((active0 & 0x10000000L) != 0L)
            return jjStartNfaWithStates_0(3, 28, 37);
         else if ((active0 & 0x2000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 37, 37);
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x4L);
      case 77:
      case 109:
         if ((active0 & 0x200000L) != 0L)
            return jjStartNfaWithStates_0(3, 21, 37);
         break;
      case 78:
      case 110:
         if ((active0 & 0x20000000L) != 0L)
            return jjStartNfaWithStates_0(3, 29, 37);
         else if ((active1 & 0x20000L) != 0L)
            return jjStartNfaWithStates_0(3, 81, 37);
         else if ((active1 & 0x40000L) != 0L)
         {
            jjmatchedKind = 82;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x88000080000000L, active1, 0x8a000L);
      case 79:
      case 111:
         return jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x20L);
      case 82:
      case 114:
         return jjMoveStringLiteralDfa4_0(active0, 0xe000000100000000L, active1, 0x8L);
      case 83:
      case 115:
         if ((active1 & 0x10000L) != 0L)
            return jjStartNfaWithStates_0(3, 80, 37);
         return jjMoveStringLiteralDfa4_0(active0, 0x20000000000L, active1, 0L);
      case 84:
      case 116:
         if ((active0 & 0x8000000L) != 0L)
            return jjStartNfaWithStates_0(3, 27, 37);
         else if ((active1 & 0x4000L) != 0L)
            return jjStartNfaWithStates_0(3, 78, 37);
         return jjMoveStringLiteralDfa4_0(active0, 0x420000000000000L, active1, 0x1L);
      case 85:
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x40000800000L, active1, 0L);
      case 87:
      case 119:
         return jjMoveStringLiteralDfa4_0(active0, 0x4000000000L, active1, 0L);
      case 89:
      case 121:
         return jjMoveStringLiteralDfa4_0(active0, 0x100000000000000L, active1, 0L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0, active1);
}
private int jjMoveStringLiteralDfa4_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(2, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0, active1);
      return 4;
   }
   switch(curChar)
   {
      case 48:
         if ((active1 & 0x80L) != 0L)
            return jjStartNfaWithStates_0(4, 71, 21);
         break;
      case 50:
         if ((active1 & 0x80000L) != 0L)
            return jjStartNfaWithStates_0(4, 83, 21);
         break;
      case 65:
      case 97:
         return jjMoveStringLiteralDfa5_0(active0, 0x400000000000000L, active1, 0x801L);
      case 67:
      case 99:
         return jjMoveStringLiteralDfa5_0(active0, 0x40000L, active1, 0x8000L);
      case 68:
      case 100:
         if ((active1 & 0x2000L) != 0L)
            return jjStartNfaWithStates_0(4, 77, 37);
         return jjMoveStringLiteralDfa5_0(active0, 0xe000000000000000L, active1, 0L);
      case 69:
      case 101:
         if ((active0 & 0x100000000L) != 0L)
            return jjStartNfaWithStates_0(4, 32, 37);
         return jjMoveStringLiteralDfa5_0(active0, 0x4000000000L, active1, 0x8L);
      case 71:
      case 103:
         if ((active0 & 0x80000000L) != 0L)
            return jjStartNfaWithStates_0(4, 31, 37);
         return jjMoveStringLiteralDfa5_0(active0, 0x100000000000000L, active1, 0L);
      case 73:
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0L, active1, 0x4L);
      case 76:
      case 108:
         return jjMoveStringLiteralDfa5_0(active0, 0x40000000000000L, active1, 0L);
      case 78:
      case 110:
         return jjMoveStringLiteralDfa5_0(active0, 0x80000000000L, active1, 0L);
      case 79:
      case 111:
         return jjMoveStringLiteralDfa5_0(active0, 0x200000000000000L, active1, 0L);
      case 80:
      case 112:
         return jjMoveStringLiteralDfa5_0(active0, 0x40000000000L, active1, 0L);
      case 82:
      case 114:
         if ((active0 & 0x1000000L) != 0L)
            return jjStartNfaWithStates_0(4, 24, 37);
         else if ((active0 & 0x2000000L) != 0L)
            return jjStartNfaWithStates_0(4, 25, 37);
         else if ((active1 & 0x20L) != 0L)
            return jjStartNfaWithStates_0(4, 69, 37);
         else if ((active1 & 0x400L) != 0L)
            return jjStartNfaWithStates_0(4, 74, 37);
         return jjMoveStringLiteralDfa5_0(active0, 0x820100000800000L, active1, 0L);
      case 84:
      case 116:
         if ((active0 & 0x4000000L) != 0L)
            return jjStartNfaWithStates_0(4, 26, 37);
         else if ((active0 & 0x8000000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 51, 37);
         else if ((active0 & 0x80000000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 55, 37);
         return jjMoveStringLiteralDfa5_0(active0, 0x20000000000L, active1, 0L);
      default :
         break;
   }
   return jjStartNfa_0(3, active0, active1);
}
private int jjMoveStringLiteralDfa5_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(3, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0, active1);
      return 5;
   }
   switch(curChar)
   {
      case 32:
         return jjMoveStringLiteralDfa6_0(active0, 0x140000000000L, active1, 0L);
      case 49:
         if ((active0 & 0x2000000000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 61, 21);
         break;
      case 50:
         if ((active0 & 0x4000000000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 62, 21);
         break;
      case 65:
      case 97:
         return jjMoveStringLiteralDfa6_0(active0, 0x800000L, active1, 0x8000L);
      case 69:
      case 101:
         if ((active0 & 0x40000000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 54, 37);
         return jjMoveStringLiteralDfa6_0(active0, 0x4000000000L, active1, 0x8L);
      case 71:
      case 103:
         if ((active0 & 0x80000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 43, 37);
         break;
      case 73:
      case 105:
         return jjMoveStringLiteralDfa6_0(active0, 0x400000000000000L, active1, 0L);
      case 78:
      case 110:
         if ((active0 & 0x200000000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 57, 37);
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x805L);
      case 79:
      case 111:
         return jjMoveStringLiteralDfa6_0(active0, 0x120000000000000L, active1, 0L);
      case 83:
      case 115:
         if ((active0 & 0x20000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 41, 37);
         return jjMoveStringLiteralDfa6_0(active0, 0x8800000000000000L, active1, 0L);
      case 84:
      case 116:
         if ((active0 & 0x40000L) != 0L)
            return jjStartNfaWithStates_0(5, 18, 37);
         break;
      default :
         break;
   }
   return jjStartNfa_0(4, active0, active1);
}
private int jjMoveStringLiteralDfa6_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(4, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0, active1);
      return 6;
   }
   switch(curChar)
   {
      case 66:
      case 98:
         return jjMoveStringLiteralDfa7_0(active0, 0x140000000000L, active1, 0L);
      case 67:
      case 99:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0x1L);
      case 69:
      case 101:
         return jjMoveStringLiteralDfa7_0(active0, 0x800000000000000L, active1, 0L);
      case 71:
      case 103:
         if ((active1 & 0x4L) != 0L)
            return jjStartNfaWithStates_0(6, 66, 37);
         break;
      case 73:
      case 105:
         return jjMoveStringLiteralDfa7_0(active0, 0x20000000000000L, active1, 0L);
      case 76:
      case 108:
         if ((active0 & 0x800000L) != 0L)
            return jjStartNfaWithStates_0(6, 23, 37);
         break;
      case 78:
      case 110:
         if ((active0 & 0x4000000000L) != 0L)
            return jjStartNfaWithStates_0(6, 38, 37);
         else if ((active0 & 0x100000000000000L) != 0L)
            return jjStartNfaWithStates_0(6, 56, 37);
         return jjMoveStringLiteralDfa7_0(active0, 0x400000000000000L, active1, 0L);
      case 83:
      case 115:
         if ((active1 & 0x8L) != 0L)
            return jjStartNfaWithStates_0(6, 67, 37);
         else if ((active1 & 0x800L) != 0L)
            return jjStartNfaWithStates_0(6, 75, 37);
         break;
      case 84:
      case 116:
         return jjMoveStringLiteralDfa7_0(active0, 0L, active1, 0x8000L);
      case 89:
      case 121:
         return jjMoveStringLiteralDfa7_0(active0, 0x8000000000000000L, active1, 0L);
      default :
         break;
   }
   return jjStartNfa_0(5, active0, active1);
}
private int jjMoveStringLiteralDfa7_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(5, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0, active1);
      return 7;
   }
   switch(curChar)
   {
      case 67:
      case 99:
         return jjMoveStringLiteralDfa8_0(active0, 0x800000000000000L, active1, 0L);
      case 68:
      case 100:
         if ((active0 & 0x20000000000000L) != 0L)
            return jjStartNfaWithStates_0(7, 53, 37);
         break;
      case 69:
      case 101:
         if ((active1 & 0x1L) != 0L)
            return jjStartNfaWithStates_0(7, 64, 37);
         else if ((active1 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(7, 79, 37);
         break;
      case 83:
      case 115:
         if ((active0 & 0x400000000000000L) != 0L)
            return jjStartNfaWithStates_0(7, 58, 37);
         else if ((active0 & 0x8000000000000000L) != 0L)
            return jjStartNfaWithStates_0(7, 63, 37);
         break;
      case 89:
      case 121:
         if ((active0 & 0x40000000000L) != 0L)
            return jjStopAtPos(7, 42);
         else if ((active0 & 0x100000000000L) != 0L)
            return jjStopAtPos(7, 44);
         break;
      default :
         break;
   }
   return jjStartNfa_0(6, active0, active1);
}
private int jjMoveStringLiteralDfa8_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(6, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(7, active0, 0L);
      return 8;
   }
   switch(curChar)
   {
      case 84:
      case 116:
         return jjMoveStringLiteralDfa9_0(active0, 0x800000000000000L);
      default :
         break;
   }
   return jjStartNfa_0(7, active0, 0L);
}
private int jjMoveStringLiteralDfa9_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(7, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(8, active0, 0L);
      return 9;
   }
   switch(curChar)
   {
      case 83:
      case 115:
         if ((active0 & 0x800000000000000L) != 0L)
            return jjStartNfaWithStates_0(9, 59, 37);
         break;
      default :
         break;
   }
   return jjStartNfa_0(8, active0, 0L);
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 37;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 12:
               case 21:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 97)
                     kind = 97;
                  jjCheckNAdd(21);
                  break;
               case 38:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 100)
                        kind = 100;
                     jjCheckNAdd(27);
                  }
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 37:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 97)
                     kind = 97;
                  jjCheckNAdd(21);
                  break;
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 101)
                        kind = 101;
                     jjCheckNAddStates(0, 6);
                  }
                  else if ((0x100002600L & l) != 0L)
                  {
                     if (kind > 1)
                        kind = 1;
                  }
                  else if (curChar == 46)
                     jjCheckNAddTwoStates(23, 27);
                  else if (curChar == 45)
                     jjCheckNAdd(19);
                  else if (curChar == 33)
                     jjstateSet[jjnewStateCnt++] = 5;
                  else if (curChar == 60)
                     jjstateSet[jjnewStateCnt++] = 3;
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 11:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 97)
                     kind = 97;
                  jjCheckNAdd(21);
                  break;
               case 16:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 97)
                     kind = 97;
                  jjCheckNAdd(21);
                  break;
               case 13:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 97)
                     kind = 97;
                  jjCheckNAdd(21);
                  break;
               case 10:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 97)
                     kind = 97;
                  jjCheckNAdd(21);
                  break;
               case 1:
                  if (curChar == 10 && kind > 1)
                     kind = 1;
                  break;
               case 2:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 3:
                  if (curChar == 62)
                     kind = 13;
                  break;
               case 4:
                  if (curChar == 60)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 5:
                  if (curChar == 61)
                     kind = 13;
                  break;
               case 6:
                  if (curChar == 33)
                     jjstateSet[jjnewStateCnt++] = 5;
                  break;
               case 18:
                  if (curChar == 45)
                     jjCheckNAdd(19);
                  break;
               case 19:
                  if (curChar != 45)
                     break;
                  if (kind > 88)
                     kind = 88;
                  jjCheckNAdd(19);
                  break;
               case 22:
                  if (curChar == 46)
                     jjCheckNAddTwoStates(23, 27);
                  break;
               case 23:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 25:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(26);
                  break;
               case 26:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 99)
                     kind = 99;
                  jjCheckNAdd(26);
                  break;
               case 27:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 100)
                     kind = 100;
                  jjCheckNAdd(27);
                  break;
               case 28:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 101)
                     kind = 101;
                  jjCheckNAddStates(0, 6);
                  break;
               case 29:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(29, 24);
                  break;
               case 30:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(30, 31);
                  break;
               case 31:
                  if (curChar == 46)
                     jjCheckNAdd(32);
                  break;
               case 32:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(32, 24);
                  break;
               case 33:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(33, 34);
                  break;
               case 34:
                  if (curChar == 46)
                     jjCheckNAdd(35);
                  break;
               case 35:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 100)
                     kind = 100;
                  jjCheckNAdd(35);
                  break;
               case 36:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 101)
                     kind = 101;
                  jjCheckNAdd(36);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 12:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAdd(21);
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAddTwoStates(20, 21);
                  }
                  if ((0x8000000080000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 11;
                  break;
               case 37:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAdd(21);
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAddTwoStates(20, 21);
                  }
                  break;
               case 0:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAddTwoStates(20, 21);
                  }
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 16;
                  else if ((0x1000000010L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 11:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAdd(21);
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAddTwoStates(20, 21);
                  }
                  if ((0x10000000100000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 16:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAdd(21);
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAddTwoStates(20, 21);
                  }
                  if ((0x100000001000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 15;
                  break;
               case 13:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAdd(21);
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAddTwoStates(20, 21);
                  }
                  if ((0x20000000200L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 12;
                  break;
               case 10:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAdd(21);
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 97)
                        kind = 97;
                     jjCheckNAddTwoStates(20, 21);
                  }
                  if ((0x20000000200L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 7:
                  if ((0x10000000100000L & l) != 0L && kind > 19)
                     kind = 19;
                  break;
               case 8:
                  if ((0x800000008L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 9:
                  if ((0x400000004000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 14:
                  if ((0x1000000010L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 15:
                  if ((0x100000001000L & l) != 0L && kind > 19)
                     kind = 19;
                  break;
               case 17:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 16;
                  break;
               case 20:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 97)
                     kind = 97;
                  jjCheckNAddTwoStates(20, 21);
                  break;
               case 21:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 97)
                     kind = 97;
                  jjCheckNAdd(21);
                  break;
               case 24:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(7, 8);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
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
      if ((i = jjnewStateCnt) == (startsAt = 37 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_2(int pos, long active0, long active1)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_2(int pos, long active0, long active1)
{
   return jjMoveNfa_2(jjStopStringLiteralDfa_2(pos, active0, active1), pos + 1);
}
private int jjMoveStringLiteralDfa0_2()
{
   switch(curChar)
   {
      case 39:
         return jjStartNfaWithStates_2(0, 93, 1);
      default :
         return jjMoveNfa_2(0, 0);
   }
}
private int jjStartNfaWithStates_2(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_2(state, pos + 1);
}
private int jjMoveNfa_2(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xffffff7fffffffffL & l) != 0L)
                  {
                     if (kind > 92)
                        kind = 92;
                  }
                  else if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 1:
                  if (curChar == 39 && kind > 92)
                     kind = 92;
                  break;
               case 2:
                  if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  kind = 92;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((jjbitVec0[i2] & l2) != 0L && kind > 92)
                     kind = 92;
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
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private int jjMoveStringLiteralDfa0_1()
{
   return jjMoveNfa_1(0, 0);
}
private int jjMoveNfa_1(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x2400L & l) != 0L)
                  {
                     if (kind > 89)
                        kind = 89;
                  }
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 1:
                  if (curChar == 10 && kind > 89)
                     kind = 89;
                  break;
               case 2:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
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
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   29, 30, 31, 24, 33, 34, 36, 25, 26, 
};

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, "\50", "\51", "\56", "\54", "\73", "\174\174", "\53", "\55", "\52", 
"\57", "\75", null, "\74", "\74\75", "\76", "\76\75", null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
   "WithinComment",
   "WithinString",
   "WithinDelimitedId",
};

/** Lex State array. */
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 0, -1, 2, -1, 0, 3, -1, 0, -1, -1, -1, 
   -1, -1, -1, 
};
static final long[] jjtoToken = {
   0xfffffffffffffffdL, 0x3b20ffffffL, 
};
static final long[] jjtoSkip = {
   0x2L, 0x2000000L, 
};
static final long[] jjtoMore = {
   0x0L, 0xdd000000L, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[37];
private final int[] jjstateSet = new int[74];
protected char curChar;
/** Constructor. */
public AdqlParserTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public AdqlParserTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 37; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 4 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
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

   for (;;)
   {
     switch(curLexState)
     {
       case 0:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_0();
         break;
       case 1:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_1();
         if (jjmatchedPos == 0 && jjmatchedKind > 90)
         {
            jjmatchedKind = 90;
         }
         break;
       case 2:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_2();
         break;
       case 3:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_3();
         break;
     }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else if ((jjtoSkip[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
      if (jjnewLexState[jjmatchedKind] != -1)
        curLexState = jjnewLexState[jjmatchedKind];
        curPos = 0;
        jjmatchedKind = 0x7fffffff;
        try {
           curChar = input_stream.readChar();
           continue;
        }
        catch (java.io.IOException e1) { }
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

private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}
