package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import parser.KoddQL;

import java.util.ArrayList;

public class MainTest {
    static private String fileNameKoddQL = "src/tests/testKoddQL.txt";
    static private String fileNameSQL = "src/tests/testSQL.txt";
    static private int linesInputFile;
    static private int linesOutputFile;
    static private ArrayList<String> stringsInputFile;
    static private ArrayList<String> stringsOutputFile;
    static private KoddQL instance;

    @Before
    public void setUp() throws Exception {
        linesInputFile = Lib.getLines(fileNameKoddQL);
        linesOutputFile = Lib.getLines(fileNameSQL);
        stringsInputFile = Lib.getKoddQLCommands(fileNameKoddQL);
        stringsOutputFile = Lib.getSQLCommands(fileNameSQL);

    }

    @Test
    public void getAllUsers() {
        for (int i = 0; i < linesInputFile; i++) {
            instance = new KoddQL();
            instance.dbms = KoddQL.DBMS.PostgreSQL;
            instance.toSQL(stringsInputFile.get(i));

            System.out.println("The request '" + stringsOutputFile.get(i) + "' was successful!");
            Assert.assertEquals(stringsOutputFile.get(i), instance.queries.get(0));
        }
    }

    public static void main(String[] args) {
        ArrayList<String> stringsInputFile1 =  Lib.getSQLCommands(fileNameSQL);
        System.out.println(stringsInputFile1.get(2));
        System.out.print(Lib.getLines(fileNameSQL));
    }
}
