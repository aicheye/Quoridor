import java.util.*;
import java.io.*;

/**
 * Quoridor.java
 * <p>
 * Represents the game of Quoridor
 * @author Sean Yang
 * @version 30/05/2024
 */
public class Quoridor {
    public static void load(String filename) {
        // declare variables
        String line;
        char[] chars;
        char p1Symbol, p2Symbol;
        boolean p1Human, p2Human;
        int[] p1Pos, p2Pos;
        int walls, p1Walls, p2Walls;
        ArrayList<Wall> wallList = new ArrayList<Wall>();

        // declare BufferedReader object
        BufferedReader br;

        try {
            // initialize BufferedReader object
            br = new BufferedReader(new FileReader(filename));

            // read the first line
            line = br.readLine();

            // check if the line matches
            if (line.matches("^[OX]\\s[a-i][1-9]:\\s((Human)|(Computer))$")) {
                // split the line into characters
                chars = line.toCharArray();

                // set the symbols, positions, and human status of the players
                p1Symbol = chars[0];
                p1Pos = new int[]{chars[2] - 'a', chars[3] - '1'};
                p1Human = chars[6] == 'H';
            }

            // read the second line
            line = br.readLine();

            // check if the line matches
            if (line.matches("^[OX]\\s[a-i][1-9]:\\s((Human)|(Computer))$")) {
                // split the line into characters
                chars = line.toCharArray();

                // set the symbols, positions, and human status of the players
                p2Symbol = chars[0];
                p2Pos = new int[]{chars[2] - 'a', chars[3] - '1'};
                p2Human = chars[6] == 'H';
            }
        }

        catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

    public static void main(String[] args) {
        load("./saves/example.txt");
    }
}
