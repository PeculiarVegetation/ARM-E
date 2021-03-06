import java.util.Scanner;

public class Main
{
    enum Dispmode
    {
        BINARY, HEX, DECIMAL
    }
    
    public static boolean flag_verbose = false;
    public static boolean flag_debug = false;
    public static boolean flag_gui = false;
    public static Dispmode disp_mode = Dispmode.HEX;
    public static String file_name;
    
    public static void main(String args[])
    {
        if (args.length == 0)
        {
            System.out.println("No args found");
            //no args, run as CLI
        }
        else
        {
            //Debug code, can't be used with flag_debug b/c assignment order
            System.out.printf("%d args found!\n", args.length);
            for (String s : args)
            {
                if (s.charAt(0) == '-')
                {
                    //flag, set appropriate parameter
                    switch (s)
                    {
                        case "-v":  //verbose mode
                            flag_verbose = true;
                            System.out.println("Running in verbose mode...");
                            break;
                        case "-d":  //debugging mode
                            flag_debug = true;
                            System.out.println("Running in debug mode...");
                            break;
                        case "-g":  //GUI mode
                            flag_gui = true;
                            System.out.println("Running in GUI mode... (not yet implemented!)");
                            break;
                        default:  //I got nothing
                            System.out.printf("Error: unrecognized flag %s\n", s);
                            System.exit(1);
                    }
                }
                else  //assume file if not flag
                {
                    System.out.printf("Loading file %s...\n", s);
                    file_name = s;
                    break;  //ensure we read nothing after the file, to prevent reading too many files
                }
            }
        }
        
        Utils.printIf("Running in CLI mode...\n", !flag_gui);
        
        Controller controller = new Controller(64);
        controller.setDebug_mode(flag_debug);
        
        boolean exit = false;
        
        //Command loop
        
        Scanner input = new Scanner(System.in);
        String input_command;
        while (!exit)
        {
            System.out.println("Enter a command:");
            System.out.println("STATUS                         : show status of all program options");
            System.out.println("DEBUG                          : toggle debug mode");
            System.out.println("VERBOSE                        : toggle verbose mode");
            System.out.println("DISPMODE <mode>                : change number base display mode to binary, hex, or decimal for registers and memory locations");
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
            
            switch (input_array[0])
            {
                case "STATUS":
                    if (flag_debug == true)
                    {
                        System.out.println("flag_debug   | true");
                    }
                    else
                    {
                        System.out.println("flag_debug   | false");
                    }
                    
                    if (flag_verbose == true)
                    {
                        System.out.println("flag_verbose | true");
                    }
                    else
                    {
                        System.out.println("flag_verbose | false");
                    }
                    
                    if (disp_mode == Dispmode.BINARY)
                    {
                        System.out.println("disp_mode    | BINARY");
                    }
                    else if (disp_mode == Dispmode.HEX)
                    {
                        System.out.println("disp_mode    | HEX");
                    }
                    else
                    {
                        System.out.println("disp_mode    | DECIMAL");
                    }
                    
                    Utils.printIf("Done.\n", flag_verbose);
                    
                    System.out.println("Press Enter to continue...");
                    input.nextLine();
                    break;
                case "DEBUG":
                    flag_debug = !flag_debug;
                    break;
                case "VERBOSE":
                    flag_verbose = !flag_verbose;
                    break;
                case "DISPMODE":
                    Utils.printIf("Changing display mode...\n", flag_verbose || flag_debug);
                    
                    if (input_array.length != 2)
                    {
                        System.out.println("Error: incorrect number of arguments");
                        break;
                    }
                    
                    if (input_array[1].toUpperCase().equals("BINARY") || input_array[1].toUpperCase().equals("BIN"))
                    {
                        Utils.printIf("Setting display mode to binary...\n", flag_verbose);
                        disp_mode = Dispmode.BINARY;
                    }
                    else if (input_array[1].toUpperCase().equals("HEXADECIMAL") || input_array[1].toUpperCase().equals("HEX"))
                    {
                        Utils.printIf("Setting display mode to hexadecimal...\n", flag_verbose);
                        disp_mode = Dispmode.HEX;
                    }
                    else if (input_array[1].toUpperCase().equals("DECIMAL") || input_array[1].toUpperCase().equals("DEC"))
                    {
                        Utils.printIf("Setting display mode to decimal...\n", flag_verbose);
                        disp_mode = Dispmode.DECIMAL;
                    }
                    else
                    {
                        System.out.printf("Error: unknown or unsupported base %s\n", input_array[1].toUpperCase());
                    }
                    
                    Utils.printIf("Done.\n", flag_verbose);
                    break;
                case "STEP":
                    
                    try
                    {
                        Utils.printIf("Executing instruction at address " + controller.getCPU().getRegister(Utils.PC) + "...\n", flag_debug | flag_verbose);
                    }
                    catch (Exception e)
                    {
                        Utils.printIf(e + "\n", flag_debug | flag_verbose);
                    }
                    
                    try
                    {
                        controller.tick();
                    }
                    catch (Exception e)
                    {
                        Utils.printIf("Error: unable to execute instruction. See stack trace for details:\n", flag_debug);
                        e.printStackTrace();
                        break;
                    }
                    
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "SHOWMEM":
                    if (input_array.length != 2)
                    {
                        System.out.println("Error: incorrect number of arguments");
                        break;
                    }
                    
                    try
                    {
                        Integer.parseInt(input_array[1]);
                    }
                    catch (Exception e)
                    {
                        Utils.printIf(e + "\n", flag_debug);
                        System.out.println("Error: could not parse " + input_array[1]);
                        break;
                    }
                    
                    if (Integer.parseInt(input_array[1]) < 0 || Integer.parseInt(input_array[1]) > controller.getMem().getSize())
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
                    if (input_array.length != 3)
                    {
                        System.out.println("Error: incorrect number of arguments");
                        break;
                    }
                    
                    if (Integer.parseInt(input_array[1]) < 0 || Integer.parseInt(input_array[1]) > 15)
                    {
                        System.out.println("Error: register must be between 0 and 15");
                        break;
                    }
                    
                    Utils.printIf("Setting register " + input_array[1] + " to " + input_array[2] + "...\n", flag_verbose);
                    if (input_array[1].toUpperCase().equals("PC"))  //PC
                    {
                        controller.getCPU().updateRegister(Utils.PC, Integer.parseInt(input_array[2]));
                    }
                    else if (input_array[1].toUpperCase().equals("SP"))  //SP
                    {
                        controller.getCPU().updateRegister(Utils.SP, Integer.parseInt(input_array[2]));
                    }
                    else if (input_array[1].toUpperCase().equals("LR"))  //LR
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
                    if (input_array.length != 3)
                    {
                        System.out.println("Error: incorrect number of arguments");
                        break;
                    }
                    Utils.printIf("Setting memory address " + input_array[1] + " to " + input_array[2] + "...\n", flag_verbose);
                    if(input_array[2].length() == 8)
                    {
                        controller.getMem().writeByte(Integer.parseInt(input_array[1]), input_array[2]);
                    }
                    else if(input_array[2].length() == 32)
                    {
                        controller.getMem().write(Integer.parseInt(input_array[1]), input_array[2]);
                    }
                    else
                    {
                        System.out.println("Error: cannot write data not of size 8 or 32");
                    }
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "SETMEM_COM":
                    
                    System.out.println("CURRENTLY UNDER RENOVATION. CHECK BACK LATER.");
                    break;
                
                //                    if (input_array.length < 3)
                //                    {
                //                        System.out.println("Error: incorrect number of arguments");
                //                        break;
                //                    }
                //
                //                    //This section handles commands with spaces that would have been split up beforehand; for example, ADD r1,r2,r2 would have been split into ADD and r1,r2,r2
                //                    String com = "";
                //                    for (int i = 2; i < input_array.length; i++)
                //                    {
                //                        com += input_array[i] + " ";
                //                    }
                //
                //                    Utils.printIf("Setting memory address " + input_array[1] + " to command " + com + "...\n", flag_verbose);
                //                    try
                //                    {
                //                        controller.getMem().writeCommand(Integer.parseInt(input_array[1]), com);
                //                    }
                //                    catch (Exception e)
                //                    {
                //                        System.out.println("Error: see stack trace for details:");
                //                        e.printStackTrace();
                //                    }
                //                    Utils.printIf("Done\n", flag_verbose);
                //                    break;
                case "CLRMEM":
                    Utils.printIf("Clearing memory...\n", flag_verbose);
                    controller.resetMemory();
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "CLRREG":
                    Utils.printIf("Clearing registers...\n", flag_verbose);
                    for (int i = 0; i < 16; i++)
                    {
                        controller.getCPU().updateRegister(i, 0);
                    }
                    Utils.printIf("Done\n", flag_verbose);
                    break;
                case "CLRALL":
                    Utils.printIf("Clearing memory and registers...\n", flag_verbose);
                    controller.resetMemory();
                    for (int i = 0; i < 16; i++)
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
    }
    
    public static void displayMemory(Controller c, int mem_lines)
    {
        System.out.println("----------------------------------------");
        
        System.out.println("addr |value");
        for (int i = 0; i < mem_lines; i++)
        {
            //Oh look, it's expensive String conversion time!
            if (disp_mode == Dispmode.BINARY)
            {
                try
                {
                    System.out.printf("0x%3s| 0b%32s\n", Integer.toHexString(i).toUpperCase(), c.getMem().read(i).toUpperCase());
                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
            }
            else if (disp_mode == Dispmode.HEX)
            {
                try
                {
                    System.out.printf("0x%3s| 0x%32s\n", Integer.toHexString(i).toUpperCase(), Integer.toHexString(Integer.parseUnsignedInt(c.getMem().read(i), 2)).toUpperCase());
                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
            }
            else  //decimal
            {
                try
                {
                    System.out.printf("0x%3s|   %32s\n", Integer.toHexString(i).toUpperCase(), Integer.toString(Integer.parseUnsignedInt(c.getMem().read(i), 2)).toUpperCase());
                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
            }
        }
        
        System.out.println("----------------------------------------");
    }
    
    public static void displayRegisters(Controller c)
    {
        System.out.println("----------------------------------------");
        
        System.out.println("reg  |value");
        for (int i = 0; i < 16; i++)
        {
            if (i == Utils.PC)
            {
                if (disp_mode == Dispmode.BINARY)
                {
                    System.out.printf("PC  | 0b%4s\n", Integer.toBinaryString(c.getCPU().getRegister(i)).toUpperCase());
                }
                else if (disp_mode == Dispmode.HEX)
                {
                    System.out.printf("PC  | 0x%4s\n", Integer.toHexString(c.getCPU().getRegister(i)).toUpperCase());
                }
                else
                {
                    System.out.printf("PC  |   %4s\n", Integer.toString(c.getCPU().getRegister(i)).toUpperCase());
                }
            }
            else if (i == Utils.SP)
            {
                if (disp_mode == Dispmode.BINARY)
                {
                    System.out.printf("SP  | 0b%4s\n", Integer.toBinaryString(c.getCPU().getRegister(i)).toUpperCase());
                }
                else if (disp_mode == Dispmode.HEX)
                {
                    System.out.printf("SP  | 0x%4s\n", Integer.toHexString(c.getCPU().getRegister(i)).toUpperCase());
                }
                else
                {
                    System.out.printf("SP  |   %4s\n", Integer.toString(c.getCPU().getRegister(i)).toUpperCase());
                }
            }
            else if (i == Utils.LR)
            {
                if (disp_mode == Dispmode.BINARY)
                {
                    System.out.printf("LR  | 0b%4s\n", Integer.toBinaryString(c.getCPU().getRegister(i)).toUpperCase());
                }
                else if (disp_mode == Dispmode.HEX)
                {
                    System.out.printf("LR  | 0x%4s\n", Integer.toHexString(c.getCPU().getRegister(i)).toUpperCase());
                }
                else
                {
                    System.out.printf("LR  |   %4s\n", Integer.toString(c.getCPU().getRegister(i)).toUpperCase());
                }
            }
            else
            {
                if (disp_mode == Dispmode.BINARY)
                {
                    System.out.printf("r%3d| 0b%4s\n", i, Integer.toBinaryString(c.getCPU().getRegister(i)).toUpperCase());
                }
                else if (disp_mode == Dispmode.HEX)
                {
                    System.out.printf("r%3d| 0x%4s\n", i, Integer.toHexString(c.getCPU().getRegister(i)).toUpperCase());
                }
                else
                {
                    System.out.printf("r%3d|   %4s\n", i, Integer.toString(c.getCPU().getRegister(i)).toUpperCase());
                }
            }
        }
        System.out.println("----------------------------------------");
    }
}
