public class Utils
{
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
