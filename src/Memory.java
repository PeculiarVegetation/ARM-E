import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Memory {

    private int size;
    //private String[] contents;  //Make tuple - DONE; Address class
    private Address[] contents;

    public Memory(int size)
    {
        this.size = size;
        //this.contents = new String[this.size];
        this.contents = new Address[this.size];
//        for (String s: this.contents)
//        {
//            s = "00000000";
//        }
        for (Address a: this.contents)
        {
            a.instruction = false;
            a.operation = "";
            a.value = 0;
        }
    }

    public Memory(int size, File infile) throws FileNotFoundException {
        this.size = size;
        this.contents = new Address[this.size];
        InputStream in = new java.io.FileInputStream(infile);

    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public int getSize()
    {
        return(this.size);
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
            this.contents[addr].value = value;  //Need to parse to determine stuffs
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

}
