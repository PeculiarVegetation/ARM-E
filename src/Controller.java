

public class Controller
{
    private boolean debug_mode;

    private CPU cpu;
    private Memory mem;
    private int addr_to_write;
    private Utils.Operation current_operation;

    public Controller(int mem_size)
    {
        this.debug_mode = false;
        this.cpu = new CPU(this);
        this.mem = new Memory(mem_size, this);
        this.addr_to_write = 0;
    }

    public void setDebug_mode(boolean debug_mode)
    {
        this.debug_mode = debug_mode;
    }

    public CPU getCPU()
    {
        return(this.cpu);
    }

    public Memory getMem()
    {
        return(this.mem);
    }

    public boolean updateRegister(int register, int value)
    {
        return(this.cpu.updateRegister(register, value));
    }

    public void resetMemory()
    {
        for(int i = 0; i < mem.getSize(); i++)
        {
            this.mem.write(i, 0);
        }
    }

    public void tick() throws Exception
    {
        if(this.mem.read(this.cpu.getRegister(Utils.PC)).instruction == false)  //If current PC location doesn't point to an instruction...
        {
            throw new Exception("Error: memory address at " + this.cpu.getRegister(Utils.PC) + " does not contain an instruction");  //...cry about it
        }

        try
        {
            parseInstruction(this.mem.read(this.cpu.getRegister(Utils.PC)).operation);
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    public void parseInstruction(String instruction) throws Exception
    {
        //insert code here... later
        String[] command = instruction.split("(, +| +,|[ ,]+)");
        int num_args = command.length;

        //Print out contents of each instruction, for debugging purposes
        String temp;
        for(int i = 0; i < num_args; i++)
        {
            temp = "command[" + i + "] = \"" + command[i] + "\"";
            Utils.printIf(temp, debug_mode);
            if(i + 1 != num_args)
            {
                Utils.printIf(", ", debug_mode);
            }
        }
        Utils.printIf("\n", debug_mode);

        //Determine what operation to perform
        switch(command[0].toUpperCase())
        {
            case "ADD":
                current_operation = Utils.Operation.ADD;
                break;
            case "SUB":
                current_operation = Utils.Operation.SUB;
                break;
            case "LDR":
                current_operation = Utils.Operation.LDR;
                break;
            case "STR":
                current_operation = Utils.Operation.STR;
                break;
            default:
                throw new Exception("Error: operation does not exist or is not yet supported! (in determination stage)");
        }

        //Perform the operation; spaghetti begins here
        if(current_operation == Utils.Operation.ADD)
        {
            //Debugging code
            Utils.printIf("ADD operation!\n", debug_mode);
            if(num_args != 4)
            {
                throw new Exception("Error: incorrect number of arguments for ADD instruction");
            }

            int destination = 0, arg1 = 0, arg2 = 0;

            //Parse destination
            if(command[1].toLowerCase().charAt(0) != 'r')  //if not storing to register, fail automatically
            {
                throw new Exception("Error: must store result to a register");
            }
            else if(command[1].length() < 2)  //if no number after r, fail
            {
                throw new Exception("Error: must define a register to store result to");
            }

            temp = command[1].substring(1);  //set temp to everything after r

            try
            {
                destination = Integer.parseInt(temp);
            }
            catch(Exception e)
            {
                throw e;
            }

            if(destination < 0 || destination > 15)
            {
                throw new Exception("Error: register must be between 0 and 15");
            }

            Utils.printIf("Destination register: " + destination + "\n", debug_mode);

            //Parse arg1
            if(command[2].toLowerCase().charAt(0) == 'r')  //register
            {
                try
                {
                    arg1 = parseRegArg(command[2]);
                }
                catch(Exception e)
                {
                    throw e;
                }
            }
            else if(command[2].charAt(0) == '#')  //literal
            {
                if(command[2].length() < 2)  //if no number after #, fail
                {
                    throw new Exception("Error: must define a number");
                }

                if(command[2].toLowerCase().contains("0x"))  //hex
                {
                    temp = temp.substring(2);

                    if(temp.equals(""))
                    {
                        throw new Exception("Error: must define a number!");
                    }

                    try
                    {
                        arg1 = Integer.parseInt(temp, 16);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg1 (from hex): " + arg1 + "\n", debug_mode);
                }
                else if(command[2].toLowerCase().contains("0b"))  //binary
                {
                    temp = temp.substring(2);

                    if(temp.equals(""))
                    {
                        throw new Exception("Error: must define a number!");
                    }

                    try
                    {
                        arg1 = Integer.parseInt(temp, 2);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg1 (from binary): " + arg1 + "\n", debug_mode);
                }
                else  //decimal
                {
                    try
                    {
                        arg1 = Integer.parseInt(temp);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg1 (from decimal): " + arg1 + "\n", debug_mode);
                }
            }

            //Parse arg2
            if(command[3].toLowerCase().charAt(0) == 'r')  //register
            {
                try
                {
                    arg2 = parseRegArg(command[3]);
                }
                catch(Exception e)
                {
                    throw e;
                }
            }
            else if(command[3].charAt(0) == '#')  //literal
            {
                if(command[3].length() < 2)  //if no number after #, fail
                {
                    throw new Exception("Error: must define a number");
                }

                if(command[3].toLowerCase().contains("0x"))  //hex
                {
                    temp = temp.substring(2);

                    if(temp.equals(""))
                    {
                        throw new Exception("Error: must define a number!");
                    }

                    try
                    {
                        arg2 = Integer.parseInt(temp, 16);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg2 (from hex): " + arg1 + "\n", debug_mode);
                }
                else if(command[3].toLowerCase().contains("0b"))  //binary
                {
                    temp = temp.substring(2);

                    if(temp.equals(""))
                    {
                        throw new Exception("Error: must define a number!");
                    }

                    try
                    {
                        arg2 = Integer.parseInt(temp, 2);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg2 (from binary): " + arg1 + "\n", debug_mode);
                }
                else  //decimal
                {
                    try
                    {
                        arg2 = Integer.parseInt(temp);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg2 (from decimal): " + arg1 + "\n", debug_mode);
                }
            }

            Utils.printIf("arg1 + arg2 = " + arg1 + " + " + arg2 + " = " + (arg1 + arg2) + "\n", debug_mode);

            cpu.updateRegister(destination, arg1 + arg2);

            Utils.printIf("Destination has " + cpu.getRegister(destination) + " after operation completion\n", debug_mode);
            if(destination != Utils.PC)
            {
                cpu.updateRegister(Utils.PC, cpu.getRegister(Utils.PC) + 1);
            }
        }
        else if(current_operation == Utils.Operation.SUB)
        {
            Utils.printIf("SUB operation!\n", debug_mode);
            if(num_args != 4)
            {
                throw new Exception("Error: incorrect number of arguments for SUB instruction");
            }

            int destination = 0, arg1 = 0, arg2 = 0;

            //Parse destination
            if(command[1].toLowerCase().charAt(0) != 'r')  //if not storing to register, fail automatically
            {
                throw new Exception("Error: must store result to a register");
            }
            else if(command[1].length() < 2)  //if no number after r, fail
            {
                throw new Exception("Error: must define a register to store result to");
            }

            temp = command[1].substring(1);  //set temp to everything after r

            try
            {
                destination = Integer.parseInt(temp);
            }
            catch(Exception e)
            {
                throw e;
            }

            if(destination < 0 || destination > 15)
            {
                throw new Exception("Error: register must be between 0 and 15");
            }

            Utils.printIf("Destination register: " + destination + "\n", debug_mode);

            //Parse arg1
            if(command[2].toLowerCase().charAt(0) == 'r')  //register
            {
                try
                {
                    arg1 = parseRegArg(command[2]);
                }
                catch(Exception e)
                {
                    throw e;
                }
            }
            else if(command[2].charAt(0) == '#')  //literal
            {
                if(command[2].length() < 2)  //if no number after #, fail
                {
                    throw new Exception("Error: must define a number");
                }

                if(command[2].toLowerCase().contains("0x"))  //hex
                {
                    temp = temp.substring(2);

                    if(temp.equals(""))
                    {
                        throw new Exception("Error: must define a number!");
                    }

                    try
                    {
                        arg1 = Integer.parseInt(temp, 16);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg1 (from hex): " + arg1 + "\n", debug_mode);
                }
                else if(command[2].toLowerCase().contains("0b"))  //binary
                {
                    temp = temp.substring(2);

                    if(temp.equals(""))
                    {
                        throw new Exception("Error: must define a number!");
                    }

                    try
                    {
                        arg1 = Integer.parseInt(temp, 2);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg1 (from binary): " + arg1 + "\n", debug_mode);
                }
                else  //decimal
                {
                    try
                    {
                        arg1 = Integer.parseInt(temp);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg1 (from decimal): " + arg1 + "\n", debug_mode);
                }
            }

            //Parse arg2
            if(command[3].toLowerCase().charAt(0) == 'r')  //register
            {
                try
                {
                    arg2 = parseRegArg(command[3]);
                }
                catch(Exception e)
                {
                    throw e;
                }
            }
            else if(command[3].charAt(0) == '#')  //literal
            {
                if(command[3].length() < 2)  //if no number after #, fail
                {
                    throw new Exception("Error: must define a number");
                }

                if(command[3].toLowerCase().contains("0x"))  //hex
                {
                    temp = temp.substring(2);

                    if(temp.equals(""))
                    {
                        throw new Exception("Error: must define a number!");
                    }

                    try
                    {
                        arg2 = Integer.parseInt(temp, 16);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg2 (from hex): " + arg1 + "\n", debug_mode);
                }
                else if(command[3].toLowerCase().contains("0b"))  //binary
                {
                    temp = temp.substring(2);

                    if(temp.equals(""))
                    {
                        throw new Exception("Error: must define a number!");
                    }

                    try
                    {
                        arg2 = Integer.parseInt(temp, 2);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg2 (from binary): " + arg1 + "\n", debug_mode);
                }
                else  //decimal
                {
                    try
                    {
                        arg2 = Integer.parseInt(temp);
                    }
                    catch(Exception e)
                    {
                        throw e;
                    }

                    Utils.printIf("arg2 (from decimal): " + arg1 + "\n", debug_mode);
                }
            }

            Utils.printIf("arg1 - arg2 = " + arg1 + " - " + arg2 + " = " + (arg1 - arg2) + "\n", debug_mode);


            cpu.updateRegister(destination, arg1 - arg2);

            Utils.printIf("Destination has " + cpu.getRegister(destination) + " after operation completion\n", debug_mode);
            if(destination != Utils.PC)
            {
                cpu.updateRegister(Utils.PC, cpu.getRegister(Utils.PC) + 1);
            }
        }
        //TODO: implement LDR and STR
        else if(current_operation == Utils.Operation.LDR)
        {
            Utils.printIf("LDR operation!\n", debug_mode);

            if(num_args != 3)
            {
                throw new Exception("Error: incorrect number of arguments for LDR operation");
            }



            int destination = 0, arg1 = 0;

        }
        else if(current_operation == Utils.Operation.STR)
        {
            Utils.printIf("STR operation!\n", debug_mode);

            if(num_args != 3)
            {
                throw new Exception("Error: incorrect number of arguments for STR operation");
            }



            int destination = 0, arg1 = 0;


        }
        else
        {
            throw new Exception("Error: operation does not exist or is not yet supported! (in operation stage)");
        }

    }

    public void parseInput(String input)
    {
        //insert code here... more later
    }

    public void setAddr_to_write(int addr)
    {
        this.addr_to_write = addr;
    }

    //Returns parsed integer
    private int parseIntArg(String arg_string) throws Exception
    {
        int result = 0;

        throw new Exception("Error: not yet implemented!");

        //return(result);
    }

    //Returns contents of address contained in register
    private int parseAddrArg(String arg_string) throws Exception
    {
        int result;

        if (arg_string.charAt(0) != '[' || arg_string.charAt(arg_string.length() - 1) != ']')
        {
            throw new Exception("Error: must be enclosed in []");
        }

        if(arg_string.length() < 3 || arg_string.length() > 4)
        {
            throw new Exception("Error: must define a register to obtain address from");
        }

        arg_string = arg_string.substring(1, arg_string.length() - 2);

        try
        {
            result = Integer.parseInt(arg_string);
        }
        catch(Exception e)
        {
            throw e;
        }

        if(result < 0 || result > 15)
        {
            throw new Exception("Error: register must be between 0 and 15");
        }

        result = this.mem.read(this.cpu.getRegister(result)).value;

        return(result);
    }

    //Returns contents of register
    private int parseRegArg(String arg_string) throws Exception
    {
        int result;
        arg_string = arg_string.toLowerCase();  //Prevents problems later

        if(arg_string.charAt(0) != 'r')
        {
            throw new Exception("Error: must be a register");
        }

        if(arg_string.length() < 2 || arg_string.length() > 3)
        {
            throw new Exception("Error: must define a register to modify");
        }

        arg_string = arg_string.substring(1);  //set arg_string to everything after r

        try
        {
            result = Integer.parseInt(arg_string);
        }
        catch(Exception e)
        {
            throw e;
        }

        if(result < 0 || result > 15)
        {
            throw new Exception("Error: register must be between 0 and 15");
        }

        result = this.cpu.getRegister(result);

        return(result);
    }


}
