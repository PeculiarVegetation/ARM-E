import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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
            //Debug code, can't be used with flag_debug b/c assignment order
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
        Controller controller = new Controller(64);
        controller.setDebug_mode(flag_debug);

        try
        {
            controller.parseInstruction("ADD r2,#2, #2");
        }
        catch(Exception e)
        {
            System.out.println(e);
        }

        boolean exit = false;

        //Command loop

        Scanner input = new Scanner(System.in);
        String input_command;
        while(!exit)
        {
            System.out.println("Enter a command:");
            System.out.println("DEBUG                          : toggle debug mode");
            System.out.println("VERBOSE                        : toggle verbose mode");
            System.out.println("STEP <number>                  : step <number> operations through the program");
            System.out.println("SHOWMEM <number>               : display <number> lines of memory, starting from 0");
            System.out.println("SHOWREG                        : display contents of all registers");
            System.out.println("SETREG <register> <value>      : set a register to hold a given value");
            System.out.println("SETMEM_VAL <address> <value>   : set a memory location to hold a given value");
            System.out.println("SETMEM_COM <address> <command> : set a memory location to hold a given command");
            System.out.println("CLRMEM                         : reset all memory locations to hold 0");
            System.out.println("CLRREG                         : reset all registers to hold 0");
            System.out.println("CLRALL                         : perform CLRMEM and CLRREG");
            System.out.println("EXIT                           : exit the program");
            System.out.print(">: ");

            input_command = input.nextLine().toUpperCase();
            String input_array[] = input_command.split(" +");

            switch(input_array[0])
            {
                case "DEBUG":
                    flag_debug = !flag_debug;
                    break;
                case "VERBOSE":
                    flag_verbose = !flag_verbose;
                    break;
                case "STEP":
                    System.out.println("Not yet implemented!");
                    break;
                case "SHOWMEM":
                    if(input_array.length != 2)
                    {
                        System.out.println("Error: incorrect number of arguments");
                        break;
                    }

                    try
                    {
                        Integer.parseInt(input_array[1]);
                    }
                    catch(Exception e)
                    {
                        Utils.printIf(e + "\n", flag_debug);
                        System.out.println("Error: could not parse " + input_array[1]);
                        break;
                    }

                    if(Integer.parseInt(input_array[1]) < 0 || Integer.parseInt(input_array[1]) > controller.getMem().getSize())
                    {
                        System.out.println("Error: memory requested is out of bounds");
                        break;
                    }

                    Utils.printIf("Displaying " + input_array[1] + " lines of memory...\n", flag_verbose);
                    displayMemory(controller, Integer.parseInt(input_array[1]));
                    Utils.printIf("Done\n", flag_verbose);
                    System.out.print("Press Enter to continue");
                    input.nextLine();
                    break;
                case "SHOWREG":
                    Utils.printIf("Displaying registers...\n", flag_verbose);
                    displayRegisters(controller);
                    Utils.printIf("Done\n", flag_verbose);
                    System.out.print("Press Enter to continue");
                    input.nextLine();
                    break;
                case "SETREG":
                    if(input_array.length != 3)
                    {
                        System.out.println("Error: incorrect number of arguments");
                        break;
                    }
                    Utils.printIf("Setting register " + input_array[1] + " to " + input_array[2] + "...\n", flag_verbose);
                    controller.getCPU().updateRegister(Integer.parseInt(input_array[1]), Integer.parseInt(input_array[2]));
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "SETMEM_VAL":
                    if(input_array.length != 3)
                    {
                        System.out.println("Error: incorrect number of arguments");
                        break;
                    }
                    Utils.printIf("Setting memory address " + input_array[1] + " to " + input_array[2] + "...\n", flag_verbose);
                    controller.getMem().write(Integer.parseInt(input_array[1]), Integer.parseInt(input_array[2]));
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "SETMEM_COM":
                    if(input_array.length != 3)
                    {
                        System.out.println("Error: incorrect number of arguments");
                        break;
                    }
                    Utils.printIf("Setting memory address " + input_array[1] + " to command " + input_array[2] + "...\n", flag_verbose);
                    controller.getMem().writeCommand(Integer.parseInt(input_array[1]), input_array[2]);
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "CLRMEM":
                    Utils.printIf("Clearing memory...\n", flag_verbose);
                    controller.resetMemory();
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "CLRREG":
                    Utils.printIf("Clearing registers...\n", flag_verbose);
                    for(int i = 0; i < 16; i++)
                    {
                        controller.getCPU().updateRegister(i, 0);
                    }
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "CLRALL":
                    Utils.printIf("Clearing memory and registers...\n", flag_verbose);
                    controller.resetMemory();
                    for(int i = 0; i < 16; i++)
                    {
                        controller.getCPU().updateRegister(i, 0);
                    }
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "EXIT":
                    Utils.printIf("Exiting...\n", flag_verbose || flag_debug);
                    System.exit(0);
                    break;
                default:
                    System.out.printf("Unrecognized command %s\n", input_array[0]);
            }
        }

//        System.out.printf("CPU contents: r2 = %d\n", controller.getCPU().getRegister(2));
//
//        displayMemory(controller, 32);
    }

    public static void displayMemory(Controller c, int mem_lines)
    {
        System.out.println("----------------------------------------");

        System.out.println("addr |value");
        for(int i = 0; i < mem_lines; i++)
        {
            if(c.getMem().read(i).instruction)
            {
                System.out.printf("0x%3s|   %32s\n", Integer.toHexString(i).toUpperCase(), c.getMem().read(i).operation);
            }
            else
            {
                System.out.printf("0x%3s| 0b%32s\n", Integer.toHexString(i).toUpperCase(), Integer.toBinaryString(c.getMem().read(i).value).toUpperCase());
            }
        }

        System.out.println("----------------------------------------");
    }

    public static void displayRegisters(Controller c)
    {
        System.out.println("----------------------------------------");

        System.out.println("reg  |value");
        for(int i = 0; i < 16; i++)
        {
            if (i == Utils.PC)
            {
                System.out.printf("PC  | 0x%4s\n", Integer.toHexString(c.getCPU().getRegister(i)).toUpperCase());
            }
            else if (i == Utils.SP)
            {
                System.out.printf("SP  | 0x%4s\n", Integer.toHexString(c.getCPU().getRegister(i)).toUpperCase());
            }
            else if (i == Utils.LR)
            {
                System.out.printf("LR  | 0x%4s\n", Integer.toHexString(c.getCPU().getRegister(i)).toUpperCase());
            }
            else
            {
                System.out.printf("r%3d| 0x%4s\n", i, Integer.toHexString(c.getCPU().getRegister(i)).toUpperCase());
            }
        }
        System.out.println("----------------------------------------");
    }
}
