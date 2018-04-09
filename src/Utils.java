public class Utils
{
    public static final int WORD_SIZE = 32;
    public static final int BITS_PER_ADDR = 8;
    public static final int ADDRS_PER_WORD = WORD_SIZE / BITS_PER_ADDR;

    public static final int PC = 13;
    public static final int SP = 14;
    public static final int LR = 15;

    public enum Operation
    {
        ADD, SUB, LDR, STR
    }

    public static void printIf(String s, boolean toPrint)
    {
        if(toPrint)
        {
            System.out.print(s);
        }
    }
}
