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
                    Utils.printIf("Executing instruction at address " + controller.getMem().read(controller.getCPU().getRegister(Utils.PC)).value + "...\n", flag_debug | flag_verbose);

                    try
                    {
                        controller.tick();
                    }
                    catch(Exception e)
                    {
                        Utils.printIf("Error: unable to execute instruction. See stack trace for details:\n", flag_debug);
                        e.printStackTrace();
                        break;
                    }

                    Utils.printIf("Done\n", flag_verbose);
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

                    if(Integer.parseInt(input_array[2]) < 0 || Integer.parseInt(input_array[2]) > 15)
                    {
                        System.out.println("Error: register must be between 0 and 15");
                        break;
                    }

                    Utils.printIf("Setting register " + input_array[1] + " to " + input_array[2] + "...\n", flag_verbose);
                    if(input_array[1].toUpperCase().equals("PC"))  //PC
                    {
                        controller.getCPU().updateRegister(Utils.PC, Integer.parseInt(input_array[2]));
                    }
                    else if(input_array[1].toUpperCase().equals("SP"))  //SP
                    {
                        controller.getCPU().updateRegister(Utils.SP, Integer.parseInt(input_array[2]));
                    }
                    else if(input_array[1].toUpperCase().equals("LR"))  //LR
                    {
                        controller.getCPU().updateRegister(Utils.LR, Integer.parseInt(input_array[2]));
                    }
                    else  //number
                    {
                        controller.getCPU().updateRegister(Integer.parseInt(input_array[1]), Integer.parseInt(input_array[2]));
                    }

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
                    if(input_array.length < 3)
                    {
                        System.out.println("Error: incorrect number of arguments");
                        break;
                    }

                    //This section handles commands with spaces that would have been split up beforehand; for example, ADD r1,r2,r2 would have been split into ADD and r1,r2,r2
                    String com = "";
                    for(int i = 2; i < input_array.length; i++)
                    {
                        com += input_array[i] + " ";
                    }

                    Utils.printIf("Setting memory address " + input_array[1] + " to command " + com + "...\n", flag_verbose);
                    controller.getMem().writeCommand(Integer.parseInt(input_array[1]), com);
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
