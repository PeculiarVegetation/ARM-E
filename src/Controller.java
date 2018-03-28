public class Controller
{

    private CPU cpu;
    private Memory mem;

    public Controller(int mem_size)
    {
        this.cpu = new CPU();
        this.mem = new Memory(mem_size);
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

    public void tick()
    {
        String current_instruction = this.cpu.getRegister(13).operation;
        parseInstruction(current_instruction);  //need some way to determine what memory address to write to, if necessary
        Address result = this.cpu.cycle();
    }

    private void parseInstruction(String instruction)
    {
        //insert code here... later
    }

    public void parseInput(String input)
    {
        //insert code here... more later
    }


}
