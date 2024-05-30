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
        // initialize constant for filename
        final String READ_FILE = filename;

        // declare variables
        char p1Symbol, p2Symbol;
        boolean p1Human, p2Human;
        int[][] p1Pos, p2Pos;
        int walls, p1Walls, p2Walls;
        ArrayList<Wall> wallList = new ArrayList<Wall>();

        // declare BufferedReader object
        BufferedReader br;

        try {
            // initialize BufferedReader object
            br = new BufferedReader(new FileReader(READ_FILE));
        }

        catch (IOException e) {
            System.out.println("Error reading file");
        }
    }
}
