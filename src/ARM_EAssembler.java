
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.HashMap;

public class ARM-EAssembler
    {

    public static void main(String[] args)
        {
        if (args.length != 1)
        {
            System.out.println("Error: the assembler requires exactly one argument to properly run");
            return;
        }

        String sourceFileName = args[0];

        if (!sourceFileName.endsWith(".asm"))
        {
            System.out.println("Error: the Hack assembler requires a file with a .asm extension");
            return;
        }

        String fileName = sourceFileName.substring(0, sourceFileName.length() - 3);

        File sourceFile = new File(sourceFileName);

        if (!sourceFile.exists())
        {
            System.out.println("Error: cannot find source file specified");
            return;
        }

        Scanner fileReader = null;

        try
        {
            fileReader = new Scanner(sourceFile);
        }
        catch (Exception e)
        {
            System.out.println("Error: problem when initializing Scanner to read source file");
            return;
        }

        ArrayList<String> assemblerSource = new ArrayList<>();
        ArrayList<String> hackSource = new ArrayList<>();

        String temp = "";

        while (fileReader.hasNextLine())
        {
            temp = fileReader.nextLine();

            temp = temp.replaceAll(" ", "");

            if (temp.contains("//"))
            {
                temp = temp.substring(0, temp.indexOf("//"));
            }

            if (!temp.isEmpty())
            {
                assemblerSource.add(temp);
            }
        }

        fileReader.close();

        HashMap symbolTable = new HashMap();

        //Adds reserved variables
        symbolTable.put("add", "0");
        symbolTable.put("sub", "1");
        symbolTable.put("mul", "2");
        symbolTable.put("and", "3");
        symbolTable.put("orr", "4");
        symbolTable.put("eor", "5");
        symbolTable.put("orr", "6");
        symbolTable.put("lsl", "7");
        symbolTable.put("lsr", "8");
        symbolTable.put("asr", "9");
        symbolTable.put("ror", "10");
        symbolTable.put("R0", "0");
        symbolTable.put("R1", "1");
        symbolTable.put("R2", "2");
        symbolTable.put("R3", "3");
        symbolTable.put("R4", "4");
        symbolTable.put("R5", "5");
        symbolTable.put("R6", "6");
        symbolTable.put("R7", "7");
        symbolTable.put("R8", "8");
        symbolTable.put("R9", "9");
        symbolTable.put("R10", "10");
        symbolTable.put("R11", "11");
        symbolTable.put("R12", "12");
        symbolTable.put("R13", "13");
        symbolTable.put("R14", "14");
        symbolTable.put("R15", "15");
        symbolTable.put("SCREEN", "16384");
        symbolTable.put("KBD", "24576");

        for (int i = 0; i < assemblerSource.size(); i++)
        {
            temp = assemblerSource.get(i);

            if (temp.charAt(0) == '(')
            {
                symbolTable.put(temp.substring(1, temp.length() - 1), Integer.toString(i));
                assemblerSource.remove(i);
		if(assemblerSource.get(i).charAt(0) == '(')
		{
			i--;
		}
            }
        }

        int currentRAMAddress = 16;

        String prefix = "";
        String addr = "";
        String a = "";
        String comp = "";
        String dest = "";
        String branch = "";

        for (int j = 0; j < assemblerSource.size(); j++)
        {
            temp = assemblerSource.get(j);

            if (temp.charAt(0) == '@')
            {

                if (!temp.substring(1, temp.length()).matches("^[0-9]+$"))
                {

                    if (temp.substring(1, 2).matches("^[0-9]+$"))
                    {
                        System.out.println("Error: invalid variable/symbol name at or near line " + j + " : " + temp);
                        return;
                    }

                    if (symbolTable.containsKey(temp.substring(1, temp.length())))
                    {
                        String tempSymbol = temp.substring(1, temp.length());
                        temp = "@" + symbolTable.get(temp.substring(1, temp.length()));
                    }
                    else
                    {
                        symbolTable.put(temp.substring(1, temp.length()), Integer.toString(currentRAMAddress));
                        currentRAMAddress++;
                        temp = "@" + symbolTable.get(temp.substring(1, temp.length()));
                    }
                }
            }

            assemblerSource.set(j, temp);

            if (temp.charAt(0) == '@')
            {
                prefix = "0";
                addr = Integer.toBinaryString(Integer.parseInt(temp.substring(1, temp.length())));

                while (addr.length() < 15)
                {
                    addr = "0" + addr;
                }

                temp = prefix + addr;
                hackSource.add(temp);
            }
            else
            {
                prefix = "111";
                
                String commandArray[];

                if (temp.charAt(1) == ';')
                {
                    commandArray = temp.split(";");

                    switch (commandArray[0])
                    {
                        case "0":
                            comp = "101010";
                            a = "0";
                            break;
                        case "A":
                            comp = "110000";
                            a = "0";
                            break;
                        case "M":
                            comp = "110000";
                            a = "1";
                            break;
                        case "D":
                            comp = "001100";
                            a = "0";
                            break;
                        default:
                            System.out.println("Error: unrecognized branch command at line " + j + ": " + temp);
                    }

                    switch (commandArray[1])
                    {
                        case "b":
                            branch = "111";
                            break;
                        case "ble":
                            branch = "110";
                            break;
                        case "bne":
                            branch = "101";
                            break;
                        case "blt":
                            branch = "100";
                            break;
                        case "bge":
                            branch = "011";
                            break;
                        case "beq":
                            branch = "010";
                            break;
                        case "bgt":
                            branch = "001";
                            break;
                        default:
                            branch = "000";
                    }

                    dest = "000";
                    temp = prefix + a + comp + dest + branch;
                    hackSource.add(temp);

                }
                else
                {
                    commandArray = temp.split("=");

                    if (commandArray[1].contains("M"))
                    {
                        a = "1";
                    }
                    else
                    {
                        a = "0";
                    }

                    switch (commandArray[1])
                    {
                        case "0":
                            comp = "101010";
                            break;
                        case "1":
                            comp = "111111";
                            break;
                        case "-1":
                            comp = "111010";
                            break;
                        case "D":
                            comp = "001100";
                            break;
                        case "A":
                            comp = "110000";
                            break;
                        case "M":
                            comp = "110000";
                            break;
                        case "!D":
                            comp = "001101";
                            break;
                        case "!A":
                            comp = "110001";
                            break;
                        case "!M":
                            comp = "110001";
                            break;
                        case "-D":
                            comp = "001111";
                            break;
                        case "-A":
                            comp = "110011";
                            break;
                        case "-M":
                            comp = "110011";
                            break;
                        case "D+1":
                            comp = "011111";
                            break;
                        case "A+1":
                            comp = "110111";
                            break;
                        case "M+1":
                            comp = "110111";
                            break;
                        case "D-1":
                            comp = "001110";
                            break;
                        case "A-1":
                            comp = "110010";
                            break;
                        case "M-1":
                            comp = "110010";
                            break;
                        case "D+A":
                            comp = "000010";
                            break;
                        case "D+M":
                            comp = "000010";
                            break;
                        case "D-A":
                            comp = "010011";
                            break;
                        case "D-M":
                            comp = "010011";
                            break;
                        case "A-D":
                            comp = "000111";
                            break;
                        case "M-D":
                            comp = "000111";
                            break;
                        case "D&A":
                            comp = "000000";
                            break;
                        case "D&M":
                            comp = "000000";
                            break;
                        case "D|A":
                            comp = "010101";
                            break;
                        case "D|M":
                            comp = "010101";
                            break;
                        default:
                            System.out.println("Error: unrecognized command " + commandArray[1] + " at " + j);
                            return;
                    }

                    switch (commandArray[0])
                    {
                        case "AMD":
                            dest = "111";
                            break;
                        case "AD":
                            dest = "110";
                            break;
                        case "AM":
                            dest = "101";
                            break;
                        case "A":
                            dest = "100";
                            break;
                        case "MD":
                            dest = "011";
                            break;
                        case "D":
                            dest = "010";
                            break;
                        case "M":
                            dest = "001";
                            break;
                        default:
                            dest = "000";
                    }
                    
                    branch = "000";
                    temp = prefix + a + comp + dest + branch;
                    hackSource.add(temp);
                }

            }

        }

        //File output = new File(hackFileName);
        FileWriter outputWriter = null;
        try
        {
            outputWriter = new FileWriter(hackFileName);
        }
        catch (Exception e)
        {
            System.out.println("Error: there was a problem creating the outut file");
            return;
        }

        for (String s : hackSource)
        {
            try
            {
                outputWriter.write(s + '\n');
            }
            catch (Exception e)
            {
                System.out.println("Error: there was an error writing the line " + s + " to the output file");
            }
        }

        try
        {
            outputWriter.close();
        }
        catch (Exception e)
        {
            System.out.println("Error: there was an error trying to close the output FileWriter");
        }

        }

    }
