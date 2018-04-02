import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Memory {

    private Controller controller;

    private int size;
    private Address[] contents;
    private int length;

    public Memory(int size, Controller controller)
    {
        this.size = size;
        this.contents = new Address[this.size];
        for (int i = 0; i < this.size; i++)
        {
            this.contents[i] = new Address(false, "", 0);
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
        this.contents = new Address[this.size];
        for (int i = 0; i < this.size; i++)
        {
            this.contents[i] = new Address(false, "", 0);
        }
        parse(infile);
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


    public boolean write(int addr, int value)
    {
        //Need check for valid value length
        if(addr < 0 || addr >= this.size)
        {
            return(false);
        }
        else
        {
            this.contents[addr].value = value;
            this.contents[addr].instruction = false;
            return(true);
        }
    }

    public boolean writeCommand(int addr, String command)
    {
        //Need check for valid value length
        if(addr < 0 || addr >= this.size)
        {
            return(false);
        }
        else
        {
            this.contents[addr].operation = command;
            this.contents[addr].instruction = true;
            return(true);
        }
    }

    public Address read(int addr)
    {
        if(addr < 0 || addr >= this.size)
        {
            return(null);
        }
        else
        {
            return(this.contents[addr]);
        }
    }

    private void parse(File infile) throws FileNotFoundException
    {
        Scanner file_reader = new Scanner(infile);
        String temp;
        int addr = 0;

        while(file_reader.hasNextLine())
        {
            temp = file_reader.nextLine();

            //Code to break up each line goes here


            //Code to translate between ARM assembly/hex/binary goes here

            contents[addr].operation = temp;
            addr++;
        }

        this.length = addr;
    }

}
