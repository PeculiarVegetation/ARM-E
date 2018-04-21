//TODO: ACCOUNT FOR FETCH/DECODE/EXECUTE'S EFFECTS ON PC VALUE!!!!!!!!!!!

public class Controller
{
    private boolean debug_mode;
    
    //oh no
    private CPU cpu;
    private Memory mem;
    private int addr_to_write;
    private Utils.Operation current_operation;
    private Utils.Condition current_condition;
    private Utils.Processing current_opcode;
    private Utils.Instruction current_inst_type;
    private Utils.Shift current_shift;
    private Utils.Branch current_branch;
    private Utils.Load_Store current_load_store;
    private String current_instruction_string, cond_code, inst_type, misc, opcode, rn, rn_value_string, rd, rd_value_string, rm, rm_value_string, rs_rotate, rs_rotate_value_string, shift_type, shift_amount, inmed_8, current_branch_string;
    private int rn_value, rd_value, rm_value, rs_rotate_value;
    private boolean current_carry;
    private char P, U, B, W, L;
    
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
        return (this.cpu);
    }
    
    public Memory getMem()
    {
        return (this.mem);
    }
    
    public boolean updateRegister(int register, int value)
    {
        return (this.cpu.updateRegister(register, value));
    }
    
    public void resetMemory()
    {
        for (int i = 0; i < mem.getSize(); i++)
        {
            this.mem.write(i, "00000000");
        }
    }
    
    public void tick() throws Exception
    {
        try
        {
            parseInstruction(combineAddrs(this.cpu.getRegister(Utils.PC)));  //Parse binary string, maybe????
        }
        catch (Exception e)
        {
            throw e;
        }
    }
    
    private String combineAddrs(int addr1) throws Exception
    {
        String temp = "";
        
        for (int cur = addr1; cur < addr1 + 4; cur++)
        {
            try
            {
                temp += this.mem.read(cur);
            }
            catch (Exception e)
            {
                throw (e);
            }
        }
        
        return (temp);
    }
    
    //Utterly fucked
    //TODO: unfuck
    public void parseInstruction(String instruction) throws Exception
    {
        //code for interpreting a String of binary digits as an ARM instruction
        if (instruction.length() != 32)
        {
            throw new Exception("Error: improper instruction length");
        }
        
        this.current_instruction_string = instruction;
        this.cond_code = this.current_instruction_string.substring(0, 4);  //First 4 chars
        this.inst_type = this.current_instruction_string.substring(4, 7);  //Next 3 chars
        this.misc = this.current_instruction_string.substring(7, 9);  //Is it a misc instruction?
        this.opcode = this.current_instruction_string.substring(7, 11);  //Initialize opcode
        this.current_carry = false;  //Reset current_carry to false
        //Initialize registers
        this.rn = this.current_instruction_string.substring(12, 16);
        this.rd = this.current_instruction_string.substring(16, 20);
        this.rm = this.current_instruction_string.substring(28);
        this.rs_rotate = this.current_instruction_string.substring(20, 24);
        //Initialize register values
        this.rn_value = this.cpu.getRegister(Integer.parseUnsignedInt(this.rn, 2));
        this.rd_value = this.cpu.getRegister(Integer.parseUnsignedInt(this.rd, 2));
        this.rm_value = this.cpu.getRegister(Integer.parseUnsignedInt(this.rm, 2));
        this.rs_rotate_value = this.cpu.getRegister(Integer.parseUnsignedInt(this.rs_rotate, 2));
        //Initialize register value strings
        this.rn_value_string = this.cpu.getRegisterString(Integer.parseUnsignedInt(this.rn, 2));
        this.rd_value_string = this.cpu.getRegisterString(Integer.parseUnsignedInt(this.rd, 2));
        this.rm_value_string = this.cpu.getRegisterString(Integer.parseUnsignedInt(this.rm, 2));
        this.rs_rotate_value_string = this.cpu.getRegisterString(Integer.parseUnsignedInt(this.rs_rotate, 2));
        //Initialize shift info
        this.shift_type = this.current_instruction_string.substring(25, 27);
        this.shift_amount = this.current_instruction_string.substring(20, 25);
        this.inmed_8 = this.current_instruction_string.substring(24);
        //Initialize load/store flags info
        this.P = this.current_instruction_string.charAt(7);  //Offset or pre-index addressing if 1, post-indexed addressing if 0
        this.U = this.current_instruction_string.charAt(8);  //Offset added to base if 1, Offset subtracted if 0
        this.B = this.current_instruction_string.charAt(9);  //Unsigned byte if 1, signed word if 0
        this.W = this.current_instruction_string.charAt(10);
        //
        //if P is 0: LDRBT, LDRT, STRBT, STRT if 1, LDR, LDRB, STR, STRB if 0
        //if P is 1: calculated memory address written back to base register if 1, base register not updated if 0
        //
        this.L = this.current_instruction_string.charAt(11);  //Load if 1, Store if 0
        
        parseCondCode(cond_code);
        parseShiftType(shift_type, rs_rotate);
        
        String parsed_shift;
        String temp_result;
        
        //TODO: handle undefined behavior, also literally everything else
        //TODO: handle shifting
        switch (inst_type)
        {
            case "000":  //Data processing (non-immediate)
                if (!checkConditions())
                {
                    break;
                }
                this.current_inst_type = Utils.Instruction.DATA_PROC;
                
                if (instruction.charAt(27) == '0')  //Immediate shift or misc. instruction
                {
                    parseOpcode(opcode);
                    parsed_shift = parseShift(true);
                    
                    if (misc.equals("10"))  //Misc. instruction
                    {
                        throw new Exception("Error: miscellaneous instructions not yet implemented");
                    }
                    else  //Data processing immediate shift
                    {
                        Utils.printIf("rd = " + Integer.parseUnsignedInt(this.rd, 2) + "\n", debug_mode);
                        
                        switch (this.current_opcode)
                        {
                            case AND:
                                //Logical AND
                                temp_result = AND(this.rn_value_string, parsed_shift);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case EOR:
                                //Logical XOR
                                temp_result = XOR(this.rn_value_string, parsed_shift);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case SUB:
                                //Subtraction
                                temp_result = SUB(this.rn_value_string, parsed_shift);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case RSB:
                                //Reverse subtraction (arg2 - arg1)
                                temp_result = RSB(this.rn_value_string, parsed_shift);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case ADD:
                                //Addition
                                temp_result = ADD(this.rn_value_string, parsed_shift);
                                Utils.printIf("temp_result = " + temp_result + "\n", debug_mode);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case ADC:
                                //Addition with carry bit
                                temp_result = ADC(this.rn_value_string, parsed_shift);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case SBC:
                                //Subtraction with carry bit
                                temp_result = SBC(this.rn_value_string, parsed_shift);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case RSC:
                                //Reverse subtraction with carry bit
                                temp_result = RSC(this.rn_value_string, parsed_shift);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case TST:
                                //Update flags after logical AND
                                TST(this.rn_value_string, parsed_shift);
                                break;
                            case TEQ:
                                //Update flags after logical EOR
                                TEQ(this.rn_value_string, parsed_shift);
                                break;
                            case CMP:
                                //Update flags after arg1 - arg2
                                CMP(this.rn_value_string, parsed_shift);
                                break;
                            case CMN:
                                //Update flags after arg1 + arg2
                                CMN(this.rn_value_string, parsed_shift);
                                break;
                            case ORR:
                                //Logical (inclusive) OR
                                temp_result = OR(this.rn_value_string, parsed_shift);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case MOV:
                                //Move (arg2 only)
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(parsed_shift, 2));
                                break;
                            case BIC:
                                //Bit clear (arg1 AND NOT arg2)
                                temp_result = BIC(this.rn_value_string, parsed_shift);
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                break;
                            case MVN:
                                //Move not (NOT arg2 only)
                                this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(NOT(parsed_shift), 2));
                                break;
                            default:
                                throw new Exception("Error: unsupported or invalid opcode");
                        }
                    }
                }
                else  //Register shift or misc. instruction or multiplies/extra load/stores
                {
                    if (instruction.charAt(24) == '0')  //Register shift or misc. instruction
                    {
                        parseOpcode(opcode);
                        parsed_shift = parseShift(false);
                        if (misc.equals("10"))  //Misc. instruction
                        {
                            throw new Exception("Error: miscellaneous instructions not yet implemented");
                        }
                        else  //Data processing register shift
                        {
                            switch (this.current_opcode)
                            {
                                case AND:
                                    //Logical AND
                                    temp_result = AND(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case EOR:
                                    //Logical XOR
                                    temp_result = XOR(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case SUB:
                                    //Subtraction
                                    temp_result = SUB(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case RSB:
                                    //Reverse subtraction (arg2 - arg1)
                                    temp_result = RSB(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case ADD:
                                    //Addition
                                    temp_result = ADD(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case ADC:
                                    //Addition with carry bit
                                    temp_result = ADC(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case SBC:
                                    //Subtraction with carry bit
                                    temp_result = SBC(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case RSC:
                                    //Reverse subtraction with carry bit
                                    temp_result = RSC(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case TST:
                                    //Update flags after logical AND
                                    TST(this.rn_value_string, parsed_shift);
                                    break;
                                case TEQ:
                                    //Update flags after logical EOR
                                    TEQ(this.rn_value_string, parsed_shift);
                                    break;
                                case CMP:
                                    //Update flags after arg1 - arg2
                                    CMP(this.rn_value_string, parsed_shift);
                                    break;
                                case CMN:
                                    //Update flags after arg1 + arg2
                                    CMN(this.rn_value_string, parsed_shift);
                                    break;
                                case ORR:
                                    //Logical (inclusive) OR
                                    temp_result = OR(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case MOV:
                                    //Move (arg2 only)
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(parsed_shift, 2));
                                    break;
                                case BIC:
                                    //Bit clear (arg1 AND NOT arg2)
                                    temp_result = BIC(this.rn_value_string, parsed_shift);
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                                    break;
                                case MVN:
                                    //Move not (NOT arg2 only)
                                    this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(NOT(parsed_shift), 2));
                                    break;
                                default:
                                    throw new Exception("Error: unsupported or invalid opcode");
                            }
                        }
                    }
                    else  //Multiplies/extra load/stores
                    {
                        throw new Exception("Error: multiplication and extra load/stores not yet implemented");
                    }
                }
                break;
            case "001":  //Data processing (immediate), possibly undefined behavior
                if (!checkConditions())
                {
                    break;
                }
                this.current_inst_type = Utils.Instruction.DATA_PROC_IMM;
                
                if (misc.equals("10") && instruction.charAt(10) == '0')  //Undefined instruction
                {
                    throw new Exception("Error: undefined instruction");
                }
                else if (misc.equals("10") && instruction.charAt(10) == '1')  //Move immediate to status register
                {
                    throw new Exception("Error: move immediate to status register not yet implemented");
                }
                else  //Data processing immediate
                {
                    parseOpcode(opcode);
                    parsed_shift = parseShift(true);
                    
                    switch (this.current_opcode)
                    {
                        case AND:
                            //Logical AND
                            temp_result = AND(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case EOR:
                            //Logical XOR
                            temp_result = XOR(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case SUB:
                            //Subtraction
                            temp_result = SUB(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case RSB:
                            //Reverse subtraction (arg2 - arg1)
                            temp_result = RSB(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case ADD:
                            //Addition
                            temp_result = ADD(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case ADC:
                            //Addition with carry bit
                            temp_result = ADC(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case SBC:
                            //Subtraction with carry bit
                            temp_result = SBC(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case RSC:
                            //Reverse subtraction with carry bit
                            temp_result = RSC(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case TST:
                            //Update flags after logical AND
                            TST(this.rn_value_string, parsed_shift);
                            break;
                        case TEQ:
                            //Update flags after logical EOR
                            TEQ(this.rn_value_string, parsed_shift);
                            break;
                        case CMP:
                            //Update flags after arg1 - arg2
                            CMP(this.rn_value_string, parsed_shift);
                            break;
                        case CMN:
                            //Update flags after arg1 + arg2
                            CMN(this.rn_value_string, parsed_shift);
                            break;
                        case ORR:
                            //Logical (inclusive) OR
                            temp_result = OR(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case MOV:
                            //Move (arg2 only)
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(parsed_shift, 2));
                            break;
                        case BIC:
                            //Bit clear (arg1 AND NOT arg2)
                            temp_result = BIC(this.rn_value_string, parsed_shift);
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(temp_result, 2));
                            break;
                        case MVN:
                            //Move not (NOT arg2 only)
                            this.cpu.updateRegister(Integer.parseUnsignedInt(this.rd, 2), Integer.parseUnsignedInt(NOT(parsed_shift), 2));
                            break;
                        default:
                            throw new Exception("Error: unsupported or invalid opcode");
                    }
                }
                
                break;
            
            //ONLY LDR AND STR WORK! NO OTHER LOADING/STORING OPERATIONS WILL FUNCTION!
            case "010":  //Load/store immediate offset
                this.current_inst_type = Utils.Instruction.LOAD_STORE_IMM_OFFSET;
                if (!checkConditions())
                {
                    break;
                }
                parseLoadStore();
                String offset_12 = this.current_instruction_string.substring(20);
                
                try
                {
                    LDR_STR(offset_12);
                }
                catch (Exception e)
                {
                    throw e;
                }
                break;
            case "011":  //Load/store register offset, media instructions, possibly undefined behavior
                this.current_inst_type = Utils.Instruction.LOAD_STORE_REG_OFFSET;
                if (!checkConditions())
                {
                    break;
                }
                parseLoadStore();
                
                try
                {
                    LDR_STR(this.rm_value_string);
                }
                catch (Exception e)
                {
                    throw e;
                }
                break;
            case "100":  //Load/store multiple
                this.current_inst_type = Utils.Instruction.LOAD_STORE_MULT;
                throw new Exception("Error: loading and storing multiple not yet implemented");
            case "101":  //Branch, branch with link
                this.current_inst_type = Utils.Instruction.BRANCH;
                if (!checkConditions())
                {
                    break;
                }
                
                String signed_immed_24 = this.current_instruction_string.substring(8);
                
                if(this.current_instruction_string.charAt(7) == '0')
                {
                    this.current_branch = Utils.Branch.B;
                }
                else
                {
                    this.current_branch = Utils.Branch.BL;
                }
                
                switch(this.current_branch)
                {
                    case B:
                        B(signed_immed_24);
                        break;
                    case BL:
                        BL(signed_immed_24);
                        break;
                    default:
                        throw new Exception("Error: branch type unsupported or not yet implemented!");
                }
                break;
            case "110":  //Coprocessor load/store, coprocessor double register transfers
                this.current_inst_type = Utils.Instruction.CO_LOAD_STORE;
                throw new Exception("Error: coprocessor instructions not yet implemented");
            case "111":  //Coprocessor data processing, coprocessor register transfers, SWI
                this.current_inst_type = Utils.Instruction.CO_DATA_PROC;
                throw new Exception("Error: coprocessor instructions not yet implemented");
            default:  //Dunno mate
                throw new Exception("Error: unable to parse instruction category code");
        }
        
        this.cpu.incrementPC();  //ALWAYS INCREMENT PC BY 4, EVEN WHEN BRANCHING/OTHERWISE MOVING VALUES TO PC!
        //THIS MAY NEED TO BE CHANGED LATER, BUT FOR NOW IT'S OK!
    }
    
    public boolean assembleInstruction(String instruction) throws Exception
    {
        //insert code here... later
        String[] command = instruction.split("(, +| +,|[ ,]+)");
        int num_args = command.length;
        
        //Print out contents of each instruction, for debugging purposes
        String temp;
        for (int i = 0; i < num_args; i++)
        {
            temp = "command[" + i + "] = \"" + command[i] + "\"";
            Utils.printIf(temp, debug_mode);
            if (i + 1 != num_args)
            {
                Utils.printIf(", ", debug_mode);
            }
        }
        Utils.printIf("\n", debug_mode);
        
        //Determine what operation to perform
        switch (command[0].toUpperCase())
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
        if (current_operation == Utils.Operation.ADD)
        {
            //Debugging code
            Utils.printIf("ADD operation!\n", debug_mode);
            if (num_args != 4)
            {
                throw new Exception("Error: incorrect number of arguments for ADD instruction");
            }
            
            int destination = 0, arg1 = 0, arg2 = 0;
            
            //Parse destination
            if (command[1].toLowerCase().charAt(0) != 'r')  //if not storing to register, fail automatically
            {
                throw new Exception("Error: must store result to a register");
            }
            else if (command[1].length() < 2)  //if no number after r, fail
            {
                throw new Exception("Error: must define a register to store result to");
            }
            
            temp = command[1].substring(1);  //set temp to everything after r
            
            try
            {
                destination = Integer.parseInt(temp);
            }
            catch (Exception e)
            {
                throw e;
            }
            
            if (destination < 0 || destination > 15)
            {
                throw new Exception("Error: register must be between 0 and 15");
            }
            
            Utils.printIf("Destination register: " + destination + "\n", debug_mode);
            
            //Parse arg1
            if (command[2].toLowerCase().charAt(0) == 'r')  //register
            {
                try
                {
                    arg1 = parseRegArg(command[2]);
                }
                catch (Exception e)
                {
                    throw e;
                }
            }
            else if (command[2].charAt(0) == '#')  //literal
            {
                try
                {
                    arg1 = parseIntArg(command[2].toLowerCase());
                }
                catch (Exception e)
                {
                    throw e;
                }
            }
            
            //Parse arg2
            if (command[3].toLowerCase().charAt(0) == 'r')  //register
            {
                try
                {
                    arg2 = parseRegArg(command[3]);
                }
                catch (Exception e)
                {
                    throw e;
                }
            }
            else if (command[3].charAt(0) == '#')  //literal
            {
                try
                {
                    arg2 = parseIntArg(command[3].toLowerCase());
                }
                catch (Exception e)
                {
                    throw e;
                }
            }
            
            Utils.printIf("arg1 + arg2 = " + arg1 + " + " + arg2 + " = " + (arg1 + arg2) + "\n", debug_mode);
            
            cpu.updateRegister(destination, arg1 + arg2);
            
            Utils.printIf("Destination has " + cpu.getRegister(destination) + " after operation completion\n", debug_mode);
            if (destination != Utils.PC)
            {
                cpu.updateRegister(Utils.PC, cpu.getRegister(Utils.PC) + 1);
            }
        }
        else if (current_operation == Utils.Operation.SUB)
        {
            Utils.printIf("SUB operation!\n", debug_mode);
            if (num_args != 4)
            {
                throw new Exception("Error: incorrect number of arguments for SUB instruction");
            }
            
            int destination = 0, arg1 = 0, arg2 = 0;
            
            //Parse destination
            if (command[1].toLowerCase().charAt(0) != 'r')  //if not storing to register, fail automatically
            {
                throw new Exception("Error: must store result to a register");
            }
            else if (command[1].length() < 2)  //if no number after r, fail
            {
                throw new Exception("Error: must define a register to store result to");
            }
            
            temp = command[1].substring(1);  //set temp to everything after r
            
            try
            {
                destination = Integer.parseInt(temp);
            }
            catch (Exception e)
            {
                throw e;
            }
            
            if (destination < 0 || destination > 15)
            {
                throw new Exception("Error: register must be between 0 and 15");
            }
            
            Utils.printIf("Destination register: " + destination + "\n", debug_mode);
            
            //Parse arg1
            if (command[2].toLowerCase().charAt(0) == 'r')  //register
            {
                try
                {
                    arg1 = parseRegArg(command[2]);
                }
                catch (Exception e)
                {
                    throw e;
                }
            }
            else if (command[2].charAt(0) == '#')  //literal
            {
                try
                {
                    arg1 = parseIntArg(command[2].toLowerCase());
                }
                catch (Exception e)
                {
                    throw e;
                }
            }
            
            //Parse arg2
            if (command[3].toLowerCase().charAt(0) == 'r')  //register
            {
                try
                {
                    arg2 = parseRegArg(command[3]);
                }
                catch (Exception e)
                {
                    throw e;
                }
            }
            else if (command[3].charAt(0) == '#')  //literal
            {
                try
                {
                    arg2 = parseIntArg(command[3].toLowerCase());
                }
                catch (Exception e)
                {
                    throw e;
                }
            }
            
            Utils.printIf("arg1 - arg2 = " + arg1 + " - " + arg2 + " = " + (arg1 - arg2) + "\n", debug_mode);
            
            
            cpu.updateRegister(destination, arg1 - arg2);
            
            Utils.printIf("Destination has " + cpu.getRegister(destination) + " after operation completion\n", debug_mode);
            if (destination != Utils.PC)
            {
                cpu.updateRegister(Utils.PC, cpu.getRegister(Utils.PC) + 1);
            }
        }
        //TODO: implement LDR and STR
        else if (current_operation == Utils.Operation.LDR)
        {
            Utils.printIf("LDR operation!\n", debug_mode);
            
            if (num_args != 3)
            {
                throw new Exception("Error: incorrect number of arguments for LDR operation");
            }
            
            
            int destination = 0, arg1 = 0;
            
        }
        else if (current_operation == Utils.Operation.STR)
        {
            Utils.printIf("STR operation!\n", debug_mode);
            
            if (num_args != 3)
            {
                throw new Exception("Error: incorrect number of arguments for STR operation");
            }
            
            
            int destination = 0, arg1 = 0;
            
            
        }
        else
        {
            throw new Exception("Error: operation does not exist or is not yet supported! (in operation stage)");
        }
        
        return (false);
    }
    
    //Returns parsed integer
    private int parseIntArg(String arg_string) throws Exception
    {
        int result = 0;
        
        if (arg_string.charAt(0) != '#')
        {
            throw new Exception("Error: must be a number");
        }
        
        if (arg_string.length() < 2)
        {
            throw new Exception("Error: must define a number");
        }
        
        arg_string = arg_string.substring(1);
        
        if (arg_string.toLowerCase().contains("0x"))  //hex
        {
            arg_string = arg_string.substring(2);
            
            if (arg_string.equals(""))
            {
                throw new Exception("Error: must define a number!");
            }
            
            try
            {
                result = Integer.parseInt(arg_string, 16);
            }
            catch (Exception e)
            {
                throw e;
            }
            
            Utils.printIf("result (from hex): " + result + "\n", debug_mode);
        }
        else if (arg_string.toLowerCase().contains("0b"))
        {
            arg_string = arg_string.substring(2);
            
            if (arg_string.equals(""))
            {
                throw new Exception("Error: must define a number!");
            }
            
            try
            {
                result = Integer.parseInt(arg_string, 2);
            }
            catch (Exception e)
            {
                throw e;
            }
            
            Utils.printIf("result (from binary): " + result + "\n", debug_mode);
        }
        else
        {
            try
            {
                result = Integer.parseInt(arg_string);
            }
            catch (Exception e)
            {
                throw e;
            }
            
            Utils.printIf("result (from decimal): " + result + "\n", debug_mode);
        }
        
        return (result);
    }
    
    //Returns contents of register
    private int parseRegArg(String arg_string) throws Exception
    {
        int result;
        arg_string = arg_string.toLowerCase();  //Prevents problems later
        
        if (arg_string.charAt(0) != 'r')
        {
            throw new Exception("Error: must be a register");
        }
        
        if (arg_string.length() < 2 || arg_string.length() > 3)
        {
            throw new Exception("Error: must define a register to modify");
        }
        
        arg_string = arg_string.substring(1);  //set arg_string to everything after r
        
        try
        {
            result = Integer.parseInt(arg_string);
        }
        catch (Exception e)
        {
            throw e;
        }
        
        if (result < 0 || result > 15)
        {
            throw new Exception("Error: register must be between 0 and 15");
        }
        
        result = this.cpu.getRegister(result);
        
        return (result);
    }
    
    private void parseCondCode(String s) throws Exception
    {
        if (s.length() != 4)
        {
            throw new Exception("Error: incorrect condition code length");
        }
        switch (s)
        {
            case "0000":
                this.current_condition = Utils.Condition.EQ;
                break;
            case "0001":
                this.current_condition = Utils.Condition.NE;
                break;
            case "0010":
                this.current_condition = Utils.Condition.CS;
                break;
            case "0011":
                this.current_condition = Utils.Condition.CC;
                break;
            case "0100":
                this.current_condition = Utils.Condition.MI;
                break;
            case "0101":
                this.current_condition = Utils.Condition.PL;
                break;
            case "0110":
                this.current_condition = Utils.Condition.VS;
                break;
            case "0111":
                this.current_condition = Utils.Condition.VC;
                break;
            case "1000":
                this.current_condition = Utils.Condition.HI;
                break;
            case "1001":
                this.current_condition = Utils.Condition.LS;
                break;
            case "1010":
                this.current_condition = Utils.Condition.GE;
                break;
            case "1011":
                this.current_condition = Utils.Condition.LT;
                break;
            case "1100":
                this.current_condition = Utils.Condition.GT;
                break;
            case "1101":
                this.current_condition = Utils.Condition.LE;
                break;
            case "1110":
                this.current_condition = Utils.Condition.AL;
                break;
            case "1111":
                this.current_condition = Utils.Condition.UNCONDITIONAL_INSTRUCTION;
                break;
            default:
                throw new Exception("Error: unable to parse condition code");
        }
    }
    
    private void parseShiftType(String s, String r) throws Exception
    {
        if (s.length() != 2)
        {
            throw new Exception("Error: incorrect shift type length");
        }
        switch (s)
        {
            case "00":
                this.current_shift = Utils.Shift.LSL;
                break;
            case "01":
                this.current_shift = Utils.Shift.LSR;
                break;
            case "10":
                this.current_shift = Utils.Shift.ASR;
                break;
            case "11":
                if (r.equals("0000"))
                {
                    this.current_shift = Utils.Shift.RRX;
                }
                else
                {
                    this.current_shift = Utils.Shift.ROR;
                }
                break;
            default:
                throw new Exception("Error: invalid shift type");
        }
    }
    
    private void parseOpcode(String s) throws Exception
    {
        if (s.length() != 4)
        {
            throw new Exception("Error: incorrect opcode length");
        }
        switch (s)
        {
            case "0000":
                this.current_opcode = Utils.Processing.AND;
                break;
            case "0001":
                this.current_opcode = Utils.Processing.EOR;
                break;
            case "0010":
                this.current_opcode = Utils.Processing.SUB;
                break;
            case "0011":
                this.current_opcode = Utils.Processing.RSB;
                break;
            case "0100":
                this.current_opcode = Utils.Processing.ADD;
                break;
            case "0101":
                this.current_opcode = Utils.Processing.ADC;
                break;
            case "0110":
                this.current_opcode = Utils.Processing.SBC;
                break;
            case "0111":
                this.current_opcode = Utils.Processing.RSC;
                break;
            case "1000":
                this.current_opcode = Utils.Processing.TST;
                break;
            case "1001":
                this.current_opcode = Utils.Processing.TEQ;
                break;
            case "1010":
                this.current_opcode = Utils.Processing.CMP;
                break;
            case "1011":
                this.current_opcode = Utils.Processing.CMN;
                break;
            case "1100":
                this.current_opcode = Utils.Processing.ORR;
                break;
            case "1101":
                this.current_opcode = Utils.Processing.MOV;
                break;
            case "1110":
                this.current_opcode = Utils.Processing.BIC;
                break;
            case "1111":
                this.current_opcode = Utils.Processing.MVN;
                break;
            default:
                throw new Exception("Error: unrecognized or invalid opcode");
        }
        
    }
    
    private void parseLoadStore() throws Exception
    {
        /*if (this.P == '0' && this.B == '1' && this.W == '1' && this.L == '1')
        {
            this.current_load_store = Utils.Load_Store.LDRBT;
        }
        else if (this.P == '0' && this.B == '1' && this.W == '1' && this.L == '0')
        {
            this.current_load_store = Utils.Load_Store.STRBT;
        }
        else if (this.P == '0' && this.B == '0' && this.W == '1' && this.L == '1')
        {
            this.current_load_store = Utils.Load_Store.LDRT;
        }
        else if (this.P == '0' && this.B == '0' && this.W == '1' && this.L == '0')
        {
            this.current_load_store = Utils.Load_Store.STRT;
        }
        else if (this.B == '1' && this.L == '1')
        {
            this.current_load_store = Utils.Load_Store.LDRB;
        }
        else if (this.B == '1' && this.L == '0')
        {
            this.current_load_store = Utils.Load_Store.STRB;
        }
        else */
        if (this.B == '0' && this.L == '1')
        {
            this.current_load_store = Utils.Load_Store.LDR;
        }
        else if (this.B == '0' && this.L == '0')
        {
            this.current_load_store = Utils.Load_Store.STR;
        }
        else
        {
            throw new Exception("Error: unsupported load/store operation");
        }
    }
    
    private boolean checkConditions() throws Exception
    {
        switch (current_condition)
        {
            case EQ:
                return (this.cpu.zero);
            case NE:
                return (!this.cpu.zero);
            case CS:
                return (this.cpu.carry);
            case CC:
                return (!this.cpu.carry);
            case MI:
                return (this.cpu.negative);
            case PL:
                return (!this.cpu.negative);
            case VS:
                return (this.cpu.overflow);
            case VC:
                return (!this.cpu.overflow);
            case HI:
                return (this.cpu.carry && !this.cpu.zero);
            case LS:
                return (!this.cpu.carry || this.cpu.zero);
            case GE:
                return (this.cpu.negative == this.cpu.overflow);
            case LT:
                return (this.cpu.negative != this.cpu.overflow);
            case GT:
                return (!this.cpu.zero && (this.cpu.negative == this.cpu.overflow));
            case LE:
                return (this.cpu.zero || this.cpu.negative != this.cpu.overflow);
            case AL:
                return (true);
            case UNCONDITIONAL_INSTRUCTION:
                return (true);
            default:
                throw new Exception("Error: could not parse condition code");
        }
    }
    
    //TODO: clean me up!
    private String parseShift(boolean is_immediate_shift) throws Exception
    {
        String result_string;
        char result_string_array[];
        char carry;
        int rm = Integer.parseUnsignedInt(this.rm, 2);
        int rm_value;
        if (rm == 15)  //PC, add 8 (0b1000)
        {
            rm_value = this.cpu.getRegister(rm) + 0b1000;
        }
        else
        {
            rm_value = this.cpu.getRegister(rm);
        }
        int rs_rotate = Integer.parseUnsignedInt(this.rs_rotate, 2);
        String rs_rotate_value_string;
        
        if (this.current_inst_type == Utils.Instruction.DATA_PROC)
        {
            int shift_amount = Integer.parseUnsignedInt(this.shift_amount, 2);
            //result_string = String.format("%032d", rm_value);
            result_string = Utils.ZERO_WORD.substring(Integer.toUnsignedString(rm_value, 2).length()) + Integer.toUnsignedString(rm_value, 2);
            Utils.printIf("result_string = " + result_string + "\n", debug_mode);
            result_string_array = result_string.toCharArray();
            
            if (is_immediate_shift)
            {
                switch (this.current_shift)
                {
                    case LSL:
                        if (shift_amount != 0)
                        {
                            carry = result_string_array[shift_amount - 1];
                            
                            for (int i = 0; i < 32; i++)
                            {
                                if (i + shift_amount < 32)
                                {
                                    result_string_array[i] = result_string_array[i + shift_amount];
                                }
                                else
                                {
                                    result_string_array[i] = '0';
                                }
                            }
                            
                            result_string = String.valueOf(result_string_array);
                            this.cpu.carry = (carry == '1');
                            return (result_string);
                        }
                        else  //no shift, return result_string
                        {
                            return (result_string);
                        }
                    case LSR:
                        if (shift_amount == 0)
                        {
                            carry = result_string_array[0];
                            this.cpu.carry = (carry == '1');
                            return ("00000000000000000000000000000000");
                        }
                        else
                        {
                            carry = result_string_array[31 - shift_amount + 1];
                            
                            for (int i = 31; i > 0; i--)
                            {
                                if (i - shift_amount >= 0)
                                {
                                    result_string_array[i] = result_string_array[i - shift_amount];
                                }
                                else
                                {
                                    result_string_array[i] = '0';
                                }
                            }
                            
                            result_string = String.valueOf(result_string_array);
                            this.cpu.carry = (carry == '1');
                            return (result_string);
                        }
                    case ASR:
                        if (shift_amount == 0)
                        {
                            if (result_string_array[0] == '0')
                            {
                                this.cpu.carry = false;
                                return ("00000000000000000000000000000000");
                            }
                            else
                            {
                                this.cpu.carry = true;
                                return ("11111111111111111111111111111111");
                            }
                        }
                        else
                        {
                            carry = result_string_array[31 - shift_amount + 1];
                            
                            for (int i = 31; i > 0; i--)
                            {
                                if (i - shift_amount >= 0)
                                {
                                    result_string_array[i] = result_string_array[i - shift_amount];
                                }
                                else
                                {
                                    result_string_array[i] = result_string_array[0];  //sign bit copied into all filled spaces
                                }
                            }
                            
                            result_string = String.valueOf(result_string_array);
                            this.cpu.carry = (carry == '1');
                            return (result_string);
                        }
                    case ROR:
                        carry = result_string_array[31 - shift_amount + 1];
                        
                        for (int i = 31; i > 0; i--)
                        {
                            result_string_array[i] = result_string_array[(i - shift_amount) % 32];  //Woo, rotation!
                        }
                        
                        result_string = String.valueOf(result_string_array);
                        this.cpu.carry = (carry == '1');
                        return (result_string);
                    case RRX:
                        carry = result_string_array[31];
                        for (int i = 31; i > 0; i--)
                        {
                            result_string_array[i] = result_string_array[i - 1];
                        }
                        if (this.cpu.carry)
                        {
                            result_string_array[0] = '1';
                        }
                        else
                        {
                            result_string_array[0] = '0';
                        }
                        
                        result_string = String.valueOf(result_string_array);
                        this.cpu.carry = (carry == '1');
                        return (result_string);
                    default:
                        throw new Exception("Error: unsupported or invalid shift type");
                }
            }
            else  //register shift
            {
                if (rs_rotate == 15)  //PC, special rules
                {
                    rs_rotate_value_string = Integer.toUnsignedString(cpu.getRegister(rs_rotate) + 0b1000, 2);
                }
                else
                {
                    rs_rotate_value_string = Integer.toUnsignedString(cpu.getRegister(rs_rotate), 2);
                }
                
                shift_amount = Integer.parseUnsignedInt(rs_rotate_value_string.substring(24), 2);
                
                switch (this.current_shift)
                {
                    case LSL:
                        if (shift_amount == 0)
                        {
                            return (result_string);
                        }
                        else if (shift_amount < 32)
                        {
                            carry = result_string_array[shift_amount - 1];
                            
                            for (int i = 0; i < 32; i++)
                            {
                                if (i + shift_amount < 32)
                                {
                                    result_string_array[i] = result_string_array[i + shift_amount];
                                }
                                else
                                {
                                    result_string_array[i] = '0';
                                }
                            }
                            
                            result_string = String.valueOf(result_string_array);
                            this.cpu.carry = (carry == '1');
                            return (result_string);
                        }
                        else if (shift_amount == 32)
                        {
                            this.cpu.carry = (result_string_array[0] == '1');
                            return ("00000000000000000000000000000000");
                        }
                        else
                        {
                            this.cpu.carry = false;
                            return ("00000000000000000000000000000000");
                        }
                    case LSR:
                        if (shift_amount == 0)
                        {
                            return (result_string);
                        }
                        else if (shift_amount < 32)
                        {
                            carry = result_string_array[31 - shift_amount + 1];
                            
                            for (int i = 31; i > 0; i--)
                            {
                                if (i - shift_amount >= 0)
                                {
                                    result_string_array[i] = result_string_array[i - shift_amount];
                                }
                                else
                                {
                                    result_string_array[i] = '0';
                                }
                            }
                            
                            result_string = String.valueOf(result_string_array);
                            this.cpu.carry = (carry == '1');
                            return (result_string);
                        }
                        else if (shift_amount == 32)
                        {
                            this.cpu.carry = (result_string_array[0] == '1');
                            return ("00000000000000000000000000000000");
                        }
                        else
                        {
                            this.cpu.carry = false;
                            return ("00000000000000000000000000000000");
                        }
                    case ASR:
                        if (shift_amount == 0)
                        {
                            return (result_string);
                        }
                        else if (shift_amount < 32)
                        {
                            carry = result_string_array[31 - shift_amount + 1];
                            
                            for (int i = 31; i > 0; i--)
                            {
                                if (i - shift_amount >= 0)
                                {
                                    result_string_array[i] = result_string_array[i - shift_amount];
                                }
                                else
                                {
                                    result_string_array[i] = result_string_array[0];  //sign bit copied into all filled spaces
                                }
                            }
                            
                            result_string = String.valueOf(result_string_array);
                            this.cpu.carry = (carry == '1');
                            return (result_string);
                        }
                        else
                        {
                            if (result_string_array[0] == '0')
                            {
                                this.cpu.carry = false;
                                return ("00000000000000000000000000000000");
                            }
                            else
                            {
                                this.cpu.carry = true;
                                return ("11111111111111111111111111111111");
                            }
                        }
                    case ROR:
                        //Here's an ugly variable name for you...
                        String rs_rotate_mini_value_string = rs_rotate_value_string.substring(27);
                        if (shift_amount == 0)
                        {
                            return (result_string);
                        }
                        else if (Integer.parseUnsignedInt(rs_rotate_mini_value_string, 2) == 0)
                        {
                            this.cpu.carry = (result_string_array[0] == '1');
                            return (result_string);
                        }
                        else
                        {
                            int temp_shift_amount = Integer.parseUnsignedInt(rs_rotate_mini_value_string, 2);
                            carry = result_string_array[31 - temp_shift_amount + 1];
                            
                            for (int i = 31; i > 0; i--)
                            {
                                //I <3 modulus
                                result_string_array[i] = result_string_array[(i - temp_shift_amount) % 32];  //Woo, rotation!
                            }
                            
                            result_string = String.valueOf(result_string_array);
                            this.cpu.carry = (carry == '1');
                            return (result_string);
                        }
                    case RRX:
                        carry = result_string_array[31];
                        for (int i = 31; i > 0; i--)
                        {
                            result_string_array[i] = result_string_array[i - 1];
                        }
                        if (this.cpu.carry)
                        {
                            result_string_array[0] = '1';
                        }
                        else
                        {
                            result_string_array[0] = '0';
                        }
                        
                        result_string = String.valueOf(result_string_array);
                        this.cpu.carry = (carry == '1');
                        return (result_string);
                    default:
                        throw new Exception("Error: unsupported or invalid shift type");
                }
            }
        }
        else if (this.current_inst_type == Utils.Instruction.DATA_PROC_IMM)
        {
            result_string = String.format("%032d", Integer.parseInt(this.inmed_8, 2));  //ew
            result_string_array = result_string.toCharArray();
            
            if (rs_rotate != 0)
            {
                for (int i = 31; i > 0; i--)
                {
                    result_string_array[i] = result_string_array[(i - (rs_rotate * 2)) % 32];
                }
                
                this.cpu.carry = (result_string_array[0] == '1');
                result_string = String.valueOf(result_string_array);
                return (result_string);
            }
            else
            {
                return (result_string);
            }
            
        }
        else
        {
            throw new Exception("Error: incorrect instruction type for shifting");
        }
    }
    
    private String AND(String s1, String s2)
    {
        char temp[] = new char[32];
        for (int i = 31; i >= 0; i--)
        {
            if (s1.charAt(i) == '1' && s2.charAt(i) == '1')
            {
                temp[i] = '1';
            }
            else
            {
                temp[i] = '0';
            }
        }
        
        return (String.valueOf(temp));
    }
    
    private String OR(String s1, String s2)
    {
        char temp[] = new char[32];
        for (int i = 31; i >= 0; i--)
        {
            if (s1.charAt(i) == '1' || s2.charAt(i) == '1')
            {
                temp[i] = '1';
            }
            else
            {
                temp[i] = '0';
            }
        }
        
        return (String.valueOf(temp));
    }
    
    private String XOR(String s1, String s2)
    {
        char temp[] = new char[32];
        for (int i = 31; i >= 0; i--)
        {
            if (s1.charAt(i) != s2.charAt(i))
            {
                temp[i] = '1';
            }
            else
            {
                temp[i] = '0';
            }
        }
        
        return (String.valueOf(temp));
    }
    
    private String NOT(String s)
    {
        char temp[] = new char[32];
        for (int i = 31; i >= 0; i--)
        {
            if (s.charAt(i) == '1')
            {
                temp[i] = '0';
            }
            else
            {
                temp[i] = '1';
            }
        }
        
        return (String.valueOf(temp));
    }
    
    private String ADD(String s1, String s2)
    {
//        Utils.printIf("s1 = " + s1 + "\n", debug_mode);
//        Utils.printIf("s2 = " + s2 + "\n", debug_mode);
        char temp[] = new char[32];
        boolean carry_bit = false;
        int bit_count;
        
        //Icky, but it works
        for (int i = 31; i >= 0; i--)
        {
            bit_count = 0;
            
            if (carry_bit)
            {
                bit_count++;
            }
            if (s1.charAt(i) == '1')
            {
                bit_count++;
            }
            if (s2.charAt(i) == '1')
            {
                bit_count++;
            }
            
            if (bit_count == 3)
            {
                carry_bit = true;
                temp[i] = '1';
            }
            else if (bit_count == 2)
            {
                carry_bit = true;
                temp[i] = '0';
            }
            else if (bit_count == 1)
            {
                carry_bit = false;
                temp[i] = '1';
            }
            else
            {
                carry_bit = false;
                temp[i] = '0';
            }
    
            //Utils.printIf("Put " + temp[i] + " in position " + i + "\n", debug_mode);
        }
        
        return (String.valueOf(temp));
    }
    
    private String SUB(String s1, String s2)
    {
        String temp_string;
        
        temp_string = NOT(s2);
        temp_string = ADD(temp_string, "00000000000000000000000000000001");
        return (ADD(s1, temp_string));
    }
    
    private String RSB(String s1, String s2)
    {
        return (SUB(s2, s1));
    }
    
    private String ADC(String s1, String s2)
    {
        char temp[] = new char[32];
        boolean carry_bit = false;
        int bit_count;
        
        for (int i = 31; i >= 0; i--)
        {
            bit_count = 0;
            
            if (carry_bit)
            {
                bit_count++;
            }
            if (s1.charAt(i) == '1')
            {
                bit_count++;
            }
            if (s2.charAt(i) == '1')
            {
                bit_count++;
            }
            
            if (bit_count == 3)
            {
                carry_bit = true;
                temp[i] = '1';
            }
            else if (bit_count == 2)
            {
                carry_bit = true;
                temp[i] = '0';
            }
            else if (bit_count == 1)
            {
                carry_bit = false;
                temp[i] = '1';
            }
            else
            {
                carry_bit = false;
                temp[i] = '0';
            }
        }
        
        this.current_carry = carry_bit;
        
        return (String.valueOf(temp));
    }
    
    private String SBC(String s1, String s2)
    {
        String temp_string;
        
        temp_string = NOT(s2);
        temp_string = ADD(temp_string, "00000000000000000000000000000001");
        return (ADC(s1, temp_string));
    }
    
    private String RSC(String s1, String s2)
    {
        return (SBC(s2, s1));
    }
    
    private String BIC(String s1, String s2)
    {
        return (AND(s1, NOT(s2)));
    }
    
    private void TST(String s1, String s2)
    {
        String alu_out = AND(s1, s2);
        
        this.cpu.negative = (alu_out.charAt(0) == '1');
        this.cpu.zero = (!alu_out.contains("1"));
    }
    
    private void TEQ(String s1, String s2)
    {
        String alu_out = XOR(s1, s2);
        
        this.cpu.negative = (alu_out.charAt(0) == '1');
        this.cpu.zero = (!alu_out.contains("1"));
    }
    
    private void CMP(String s1, String s2)
    {
        String alu_out = SBC(s1, s2);
        
        this.cpu.negative = (alu_out.charAt(0) == '1');
        this.cpu.zero = (!alu_out.contains("1"));
    }
    
    private void CMN(String s1, String s2)
    {
        String alu_out = ADC(s1, s2);
        
        this.cpu.negative = (alu_out.charAt(0) == '1');
        this.cpu.zero = (!alu_out.contains("1"));
    }
    
    private void B(String s)
    {
        char temp_array[] = new char[32];
        temp_array[30] = '0';
        temp_array[31] = '0';
        
        for(int i = 31; i > 7; i--)
        {
            temp_array[i - 2] = s.charAt(i);
        }
        
        for(int i = 5; i >= 0; i--)
        {
            temp_array[i] = s.charAt(0);  //extend sign bit
        }
        
        String temp_string = String.valueOf(temp_array);
        String temp_PC = this.cpu.getRegisterString(Utils.PC);
        temp_string = ADD(temp_PC, temp_string);
        this.cpu.updateRegister(Utils.PC, Integer.parseUnsignedInt(temp_string, 2));
    }
    
    private void BL(String s)
    {
        this.cpu.updateRegister(Utils.LR, this.cpu.getRegister(Utils.PC) + 4);
        B(s);
    }
    
    private void LDR_STR(String offset_in) throws Exception
    {
        int dest_register = Integer.parseUnsignedInt(this.rd, 2);
        int offset_value = Integer.parseUnsignedInt(offset_in, 2);
        int temp_addr;
        
        switch (this.current_load_store)
        {
            case LDR:
                //get the address
                if (this.U == '1')
                {
                    temp_addr = this.rn_value + offset_value;
                }
                else
                {
                    temp_addr = this.rn_value - offset_value;
                }
                
                //update the register
                if (this.B == '0')
                {
                    this.cpu.updateRegister(dest_register, Integer.parseUnsignedInt(combineAddrs(temp_addr), 2));
                }
                else
                {
                    this.cpu.updateRegister(dest_register, Integer.parseUnsignedInt(this.mem.read(temp_addr), 2));
                }
                break;
            case STR:
                //get the address
                if (this.U == '1')
                {
                    temp_addr = this.rn_value + offset_value;
                }
                else
                {
                    temp_addr = this.rn_value - offset_value;
                }
                
                //update the memory address, no support for single-byte writes
                this.mem.writeByte(temp_addr, this.rd_value_string);
                break;
            default:
                throw new Exception("Error: unsupported or unrecognized load/store operation");
        }
    }
}
