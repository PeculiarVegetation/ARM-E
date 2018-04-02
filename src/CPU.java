public class CPU
{
    private Controller controller;

//    private static final int PC = 13;
//    private static final int SP = 14;
//    private static final int LR = 15;

    private int registers[];
    private int program_counter;
    private int stack_pointer;
    private int link_register;


    public CPU(Controller controller)
    {
        this.registers = new int[13];
        program_counter = 0;
        stack_pointer = 0;
        link_register = 0;
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

    public int cycle()  //If no write to memory is necessary, return -1
    {


        return(-1);
    }
}
