public class CPU
{
    private Address registers[] = new Address[13];
    private Address program_counter = new Address(false, "", 0);
    private Address stack_pointer = new Address(false, "", 0);
    private Address link_register = new Address(false, "", 0);


    public CPU()
    {
        for(Address addr:this.registers)
        {
            addr = new Address(false, "", 0);
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
            case 13:  //PC
                this.program_counter.value = value;
                break;
            case 14:  //SP
                this.stack_pointer.value = value;
                break;
            case 15:  //LR
                this.link_register.value = value;
                break;
            default:  //any other register
                this.registers[register].value = 0;
        }

        return(true);
    }

    public Address getRegister(int register)
    {
        if(register < 0 || register > 15)  //invalid register
        {
            return(null);
        }

        switch(register)
        {
            case 13:  //PC
                return(this.program_counter);
            case 14:  //SP
                return(this.stack_pointer);
            case 15:  //LR
                return(this.link_register);
            default:  //any other register
                return(this.registers[register]);
        }
    }

    public Address cycle()
    {


        return(new Address(false, "", 0));
    }
}
