import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Memory {

    private Controller controller;

    private int size;
    private String[] contents;
    private int length;

    public Memory(int size, Controller controller)
    {
        this.size = size;
        this.contents = new String[this.size];
        for (int i = 0; i < this.size; i++)
        {
            this.contents[i] = "00000000";
        }
        this.length = 0;
        this.controller = controller;
    }

    public Memory(int size, Controller controller, File infile) throws FileNotFoundException, IllegalArgumentException
    {
        if(size < 64 || size > 8192)
        {
            throw new IllegalArgumentException("Error: illegal memory size!");
        }
        this.size = size;
        this.contents = new String[this.size];
        for (int i = 0; i < this.size; i++)
        {
            this.contents[i] = "00000000";
        }
        readFile(infile);
        this.controller = controller;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public int getSize()
    {
        return(this.size);
    }

    public int getLength()
    {
        return(this.length);
    }


    public boolean writeByte(int addr, String value)
    {
        //Need check for valid value length
        if(addr < 0 || addr >= this.size)
        {
            return(false);
        }
        else
        {
            if(value.length() != 8)  //All values must be 8 bits long, OR ELSE!
            {
                return(false);
            }
            this.contents[addr] = value;
            return(true);
        }
    }
    
    public boolean write(int addr, String value)
    {
        //Need check for valid value length
        if(addr < 0 || addr + 3 >= this.size)
        {
            return(false);
        }
        else
        {
            if(value.length() != 32)  //All values must be 32 bits long, OR ELSE!
            {
                return(false);
            }
            for(int i = 0; i < 4; i++)
            {
                this.contents[addr + i] = value.substring(i * 8, (i + 1) * 8);
            }
            return(true);
        }
    }

    /*DEPRECATED, here for reference purposes*/
//    public boolean writeCommand(int addr, String command)
//    {
//        //Need check for valid value length
//        if(addr < 0 || addr >= this.size)
//        {
//            return(false);
//        }
//        else
//        {
//            this.contents[addr].operation = command;
//            this.contents[addr].instruction = true;
//            return(true);
//        }
//    }

    public String read(int addr) throws Exception
    {
        if(addr < 0 || addr >= this.size)
        {
            throw new Exception("Error: attempted to access memory out of bounds");
        }

        return(this.contents[addr]);
    }

    private void readFile(File infile) throws FileNotFoundException
    {
        Scanner file_reader = new Scanner(infile);
        String line;
        int addr = 0;

        while(file_reader.hasNextLine())
        {
            line = file_reader.nextLine();

            this.contents[addr] = line;
            addr++;
        }

        this.length = addr;
    }

    public void reset()
    {
        for(int i = 0; i < this.length; i++)
        {
            this.contents[i] = "00000000";
        }

        this.length = 0;
    }

}
