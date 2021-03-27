package tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Lib {
    public static int getLines(String fileName) {
        int lines = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            while (reader.readLine() != null) lines++;
            reader.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return lines;
    }

    public static ArrayList<String> getKoddQLCommands(String fileName) {
        return getStrings(fileName, '\n');
    }

    public static ArrayList<String> getSQLCommands(String fileName) {
        return getStrings(fileName, ';');
    }

    private static ArrayList<String> getStrings(String fileName, char symbol) {
        ArrayList<String> strings = new ArrayList<String>();

        try (FileReader reader = new FileReader(fileName)) {
            int c;
            boolean flag = false;
            StringBuilder tempStr = new StringBuilder();
            while ((c = reader.read()) != -1) {
                if ((char) c == symbol) {
                    if (symbol == ';')
                        tempStr.append(";");
                    tempStr = new StringBuilder(tempStr.toString().trim());
                    strings.add(tempStr.toString());
                    tempStr = new StringBuilder();
                    flag = true;
                } else
                    tempStr.append((char) c);
            }
            strings.add(tempStr.toString());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        return strings;
    }
}
