import java.io.File;
import java.io.FileNotFoundException;

public class Main {

    public static boolean flag_verbose = false;
    public static boolean flag_debug = false;
    public static String file_name;

    public static void main(String args[])
    {
        if (args.length == 0)
        {
            System.out.println("No args found, running in GUI");
            //no args, run as GUI
        }
        else
        {
            System.out.printf("%d args found!\n", args.length);
            for(String s: args)
            {
                if(s.charAt(0) == '-')
                {
                    //flag, set appropriate parameter
                    switch(s)
                    {
                        case "-v":  //verbose mode
                            flag_verbose = true;
                            System.out.println("Running in verbose mode..");
                            break;
                        case "-d":  //debugging mode
                            flag_debug = true;
                            System.out.println("Running in debug mode..");
                            break;
                        default:  //I got nothing
                            System.out.printf("Error: unrecognized flag %s\n", s);
                            System.exit(1);
                    }
                }
                else  //assume file if not flag
                {
                    file_name = s;
                    break;  //ensure we read nothing after the file, to prevent reading too many files
                }
            }
        }

        File infile = new File(file_name);
        try
        {
            Memory mem = new Memory(4096, infile);
            System.out.printf("Loaded file %s\n", file_name);
            System.out.printf("Program length: %d\n", mem.getLength());
            for(int i = 0; i < mem.getLength(); i++)
            {
                System.out.printf("%d: %s\n", i + 1, mem.read(i).operation);
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.printf("Error: file %s not found\n", file_name);
        }


    }
}
