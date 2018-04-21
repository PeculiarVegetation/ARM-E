public class Utils
{
    public static final int WORD_SIZE = 32;
    public static final int BITS_PER_ADDR = 8;
    public static final int ADDRS_PER_WORD = WORD_SIZE / BITS_PER_ADDR;

    public static final int PC = 15;
    public static final int SP = 13;
    public static final int LR = 14;
    
    public static final String ZERO_WORD = "00000000000000000000000000000000";
    public static final String ZERO_BYTE = "00000000";
    public static final String ONE_WORD = "11111111111111111111111111111111";
    public static final String ONE_BYTE = "11111111";

    public enum Operation
    {
        ADD, SUB, LDR, STR
    }

    public enum Condition
    {
        EQ, NE, CS, CC, MI, PL, VS, VC, HI, LS, GE, LT, GT, LE, AL, UNCONDITIONAL_INSTRUCTION
    }

    public enum Processing
    {
        AND, EOR, SUB, RSB, ADD, ADC, SBC, RSC, TST, TEQ, CMP, CMN, ORR, MOV, BIC, MVN
    }

    public enum Shift
    {
        LSL /*00*/, LSR /*01*/, ASR /*10*/, ROR /*11*/, RRX /*11*/
    }
    
    public enum Instruction
    {
        DATA_PROC, DATA_PROC_IMM, LOAD_STORE_IMM_OFFSET, LOAD_STORE_REG_OFFSET, LOAD_STORE_MULT, BRANCH, CO_LOAD_STORE, CO_DATA_PROC
    }
    
    public enum Branch
    {
        B, BL, BLX, BX, BXJ
    }
    
    public enum Load_Store
    {
        LDR, LDRB, LDRBT, LDRD, LDREX, LDRH, LDRSB, LDRSH, LDRT, STR, STRB, STRBT, STRD, STREX, STRH, STRT
    }

    public static void printIf(String s, boolean toPrint)
    {
        if(toPrint)
        {
            System.out.print(s);
        }
    }
}
