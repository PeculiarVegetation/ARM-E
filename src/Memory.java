import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class Memory {

    private int size;
    private Address[] contents;
    private int length;

    public Memory(int size)
    {
        this.size = size;
        //this.contents = new String[this.size];
        this.contents = new Address[this.size];
//        for (String s: this.contents)
//        {
//            s = "00000000";
//        }
        for (int i = 0; i < this.size; i++)
        {
            this.contents[i] = new Address(false, "", 0);
        }
        this.length = 0;
    }

    public Memory(int size, File infile) throws FileNotFoundException
    {
        this.size = size;
        this.contents = new Address[this.size];
        for (int i = 0; i < this.size; i++)
        {
            this.contents[i] = new Address(false, "", 0);
        }
        //InputStream in = new java.io.FileInputStream(infile);
        parse(infile);

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
            this.contents[addr].value = value;  //Need to parse to determine Address
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
