public class CPU
{
    private Controller controller;

    private int registers[];
    private int program_counter;
    private int stack_pointer;
    private int link_register;
    
    //I don't feel like making getters and setters for all of these, so...
    //Yes, I know IntelliJ will generate them for me. I just can't be bothered.
    public boolean negative, zero, carry, overflow, jazelle, ge[] = new boolean[4], endian, imprecise_abort, IRQ_interrupt, FIQ_interrupt, thumb_state, mode[] = new boolean[5];

    public CPU(Controller controller)
    {
        this.registers = new int[13];
        this.program_counter = 0;
        this.stack_pointer = 0;
        this.link_register = 0;
        this.negative = false;
        this.zero = false;
        this.carry = false;
        this.overflow = false;
        this.jazelle = false;
        for(int i = 0; i < 4; i++)
        {
            this.ge[i] = false;
        }
        this.endian = false;
        this.imprecise_abort = false;
        this.IRQ_interrupt = false;
        this.FIQ_interrupt = false;
        this.thumb_state = false;
        for(int i = 0; i < 5; i++)
        {
            this.mode[i] = false;
        }
    }

    public boolean updateRegister(int register, int value)
    {
        if(register < 0 || register > 15)  //invalid register
        {
            return(false);
        }

        switch(register)
        {
            case Utils.PC:
                this.program_counter = value;
                break;
            case Utils.SP:
                this.stack_pointer = value;
                break;
            case Utils.LR:
                this.link_register = value;
                break;
            default:  //any other register
                this.registers[register] = value;
        }

        return(true);
    }

    public int getRegister(int register) throws IllegalArgumentException
    {
        if(register < 0 || register > 15)  //invalid register
        {
            throw new IllegalArgumentException("No such register exists!");
        }

        switch(register)
        {
            case Utils.PC:
                return(this.program_counter);
            case Utils.SP:
                return(this.stack_pointer);
            case Utils.LR:
                return(this.link_register);
            default:  //any other register
                return(this.registers[register]);
        }
    }
    
    public String getRegisterString(int register)
    {
        String temp;
        char result_array[] = new char[32];
        
        if(register < 0 || register > 15)  //invalid register
        {
            throw new IllegalArgumentException("No such register exists!");
        }
    
        switch(register)
        {
            case Utils.PC:
                temp = Integer.toUnsignedString(this.program_counter, 2);
            case Utils.SP:
                temp = Integer.toUnsignedString(this.stack_pointer, 2);
            case Utils.LR:
                temp = Integer.toUnsignedString(this.link_register, 2);
            default:  //any other register
                temp = Integer.toUnsignedString(register, 2);
        }
        
        //Write returned string to array
        for(int i = 0; i < temp.length(); i++)
        {
            result_array[31 - i] = temp.charAt(31 - i);
        }
        
        //Fill in remaining chars
        for(int i = 0; i < 32 - temp.length(); i++)
        {
            result_array[i] = '0';
        }
        
        return(String.valueOf(result_array));
    }
    
    public void incrementPC()
    {
        this.program_counter += 4;
    }
}
