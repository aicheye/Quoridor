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
    private static final Map<String, String> REGEX_STRINGS = new HashMap<String, String>()
    {{put("player", "^[OX]\\s[a-i][1-9]:\\s((Human)|(Computer))$");
            put("walls", "^([1-9]|(1[0-9])|(20))\\s\\{O:\\s([1-9]|(10]))\\s,\\sX:\\s([1-9]|(10]))\\s}:$");
            put("wall", "^[|–]\\s[a-i][1-9]:\\s[OX]$");
        }};
    private static final int SIZE = Board.getSize();
    private static final int MAX_WALLS = Board.getMaxWalls();

    // declare private variable board
    static Board board;

    public static void load(String filename) {
        // declare variables
        boolean valid = true;
        String line;
        char[] chars;
        String[] split;
        int[] p1Pos, p2Pos, wallPos;
        boolean p1Human, p2Human;
        Pawn p1 = null, p2 = null;
        int totWalls = 0, p1Walls = 11, p2Walls = 11, wallOwner;
        boolean wallVertical;
        HashSet<Wall> wallSet = new HashSet<Wall>();

        // declare BufferedReader object
        BufferedReader br;

        try {
            // initialize BufferedReader object
            br = new BufferedReader(new FileReader(filename));

            // read the first line
            line = br.readLine();

            // check if the line matches
            if (line.matches(REGEX_STRINGS.get("player"))) {
                // split the line into characters
                chars = line.toCharArray();

                // set the positions, and human status of the players
                p1Pos = new int[]{chars[2] - 'a', chars[3] - '1'};
                p1Human = chars[6] == 'H';

                // check if the pawn position is valid
                if (Board.validatePawnPos(p1Pos)) {
                    p1 = new Pawn(p1Pos, 1, p1Human); // initialize a new pawn object
                }
                else valid = false;
            }
            else {valid=false;}

            // read the second line
            line = br.readLine();

            // check if the line matches
            if (line.matches(REGEX_STRINGS.get("player")) && valid) {
                // split the line into characters
                chars = line.toCharArray();

                // set the symbols, positions, and human status of the players
                p2Pos = new int[]{chars[2] - 'a', chars[3] - '1'};
                p2Human = chars[6] == 'H';

                // check if the pawn position is valid
                if (Board.validatePawnPos(p2Pos)) {
                    p2 = new Pawn(p2Pos, 2, p2Human); // initialize a new pawn object
                }
                else valid = false;
            }
            else {valid=false;}

            // read the fourth line
            br.readLine();
            line = br.readLine();

            // check if the line matches
            if (line.matches(REGEX_STRINGS.get("walls")) && valid) {
                // split the line into characters
                split = line.split("\\s");

                // set the number of totWalls
                try {
                    totWalls = Integer.parseInt(String.valueOf(split[0]));
                }
                catch (NumberFormatException e) {valid= false;}

                // set the number of totWalls placed by player 1
                try {
                    p1Walls = Integer.parseInt(String.valueOf(split[2]));
                }
                catch (NumberFormatException e) {valid= false;}

                // set the number of totWalls placed by player 2
                try {
                    p2Walls = Integer.parseInt(String.valueOf(split[5]));
                }
                catch (NumberFormatException e) {valid= false;}

                // check if the number of totWalls is valid
                if (valid &&
                        (p1Walls + p2Walls != totWalls ||
                        p1Walls > MAX_WALLS || p2Walls > MAX_WALLS ||
                        totWalls > MAX_WALLS * 2)) {
                    valid = false;
                }
            }
            else {valid = false;}

            // loop over all walls
            for (int i=0; i<totWalls; i++) {
                // read the next line
                line = br.readLine();

                if (line.matches(REGEX_STRINGS.get("wall")) && valid) {
                    // split the line into characters
                    chars = line.toCharArray();

                    // set the orientation, position, and owner of the wall
                    wallVertical = chars[0] == '|';
                    wallPos = new int[]{chars[2] - 'a', chars[3] - '1'};
                    wallOwner = chars[6] == 'O' ? 1 : 2;

                    // add the wall to the list if it is valid
                    if (Board.validateWallPos(new Wall(wallPos, wallVertical, wallOwner), wallSet)) {
                        wallSet.add(new Wall(wallPos, wallVertical, wallOwner));
                    }
                }
                else {valid = false;}
            }

            // loop over the walls
            for (Wall w : wallSet) {
                if (w.getOwner() == 1) p1Walls--;
                else p2Walls--;
            }

            // check if the number of walls placed matches
            if (p1Walls != 0 || p2Walls != 0) valid = false;

            // initialize the board if the load is valid
            if (valid && p1 != null && p2 != null && wallSet.size() > 0) {
                board = new Board(p1, p2, wallSet);
                System.out.println("Loaded!");
            }
            else {
                System.out.println("**ERR: Invalid save file**");
            }
        }

        catch (IOException e) {
            System.out.println("**ERR: File read error (does this file exist?)**");
        }
    }

    public static void main(String[] args) {
        load("./saves/example.txt");
    }
}
