import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

    public static void main(String args[])
    {
        if (args.length == 0)
        {
            System.out.println("No args found, running in GUI");
            //no args, run as GUI
        } else if (args.length == 1)
        {
            System.out.printf("1 arg found: %s\n", args[0]);
        } else
        {
            System.out.printf("Error: too many args (%d)\n", args.length);
            System.exit(1);
        }

        File infile = new File(args[0]);
        try
        {
            Memory mem = new Memory(4096, infile);
            System.out.printf("Loaded file %s\n", args[0]);
            System.out.printf("Program length: %d\n", mem.getLength());
            for(int i = 0; i < mem.getLength(); i++)
            {
                System.out.printf("%d: %s\n", i + 1, mem.read(i).operation);
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.printf("Error: file %s not found\n", args[0]);
        }


    }
}
