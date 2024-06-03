import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;

import java.util.*;

/**
 * Board class
 * <p>
 * Represents the game board for the game of Quoridor
 * @author Sean Yang
 * @version 30/05/2024
 */
public class Board {
    // constants
    private static final int SIZE = 9;
    private static final int MAX_WALLS = 10;

    /**
     * getSize method
     * <p>
     * Getter for size constant
     * @return int - The size of the board (side length)
     */
    public static int getSize() {
        return SIZE;
    }

    /**
     * getMaxWalls method
     * <p>
     * Getter for max walls constant
     * @return int - The maximum walls each player has
     */
    public static int getMaxWalls() {
        return MAX_WALLS;
    }

    // declare variables
    private int[][] squares = new int[9][9];
    private HashSet<Wall> walls = new HashSet<Wall>();
    private Pawn p1;
    private Pawn p2;
    private int[] wallsPlayer = new int[2];
    private int current = -1;

    /**
     * getP1
     * <p>
     * Getter for the first pawn
     * @return pawn - The pawn object
     */
    public Pawn getP1() {
        return p1;
    }

    /**
     * getP2
     * <p>
     * Getter for the second pawn
     * @return pawn - The pawn object
     */
    public Pawn getP2() {
        return p2;
    }

    /**
     * getCurrentPlayer
     * <p>
     * Getter for the current player
     * @return int - The current player (1 or 2)
     */
    public int getCurrentPlayer() {
        return current;
    }

    /**
     * Board constructor
     * <p>
     * Constructor for building a new Board
     * @param p2Human True if player 2 is human, false if player 2 is computer
     */
    public Board(boolean p2Human) {
        // initialize squares to 0
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                squares[i][j] = 0;
            }
        }

        // initialize pawns
        p1 = new Pawn(1, true);
        p2 = new Pawn(2, p2Human);

        // set walls to 0 each
        wallsPlayer[0] = 0;
        wallsPlayer[1] = 0;

        // set pawn positions
        squares[p1.getX()][p1.getY()] = 1;
        squares[p2.getX()][p2.getY()] = 2;

        // set current player
        current = 1;
    }

    /**
     * Board constructor for loading a game
     * <p>
     * Constructor for building a new Board from a loaded game
     * @param p1 Player 1's pawn
     * @param p2 Player 2's pawn
     * @param walls The list of walls on the board
     * @param current The current player
     */
    public Board(Pawn p1, Pawn p2, HashSet<Wall> walls, int current) {
        // declare variables
        int p1WallsPlaced = 0;
        int p2WallsPlaced = 0;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                squares[i][j] = 0;
            }
        }

        this.p1 = p1.copy();
        this.p2 = p2.copy();

        squares[p1.getX()][p1.getY()] = 1;
        squares[p2.getX()][p2.getY()] = 2;

        for (Wall w : walls) {
            if (w.getOwner() == 1) p1WallsPlaced++;
            else p2WallsPlaced++;
        }

        this.wallsPlayer[0] = p1WallsPlaced;
        this.wallsPlayer[1] = p2WallsPlaced;

        this.walls = new HashSet<Wall>(walls);

        this.current = current;
    }

    /**
     * out method
     * <p>
     * Prints the board to the console
     */
    public void out() {
        // declare constants
        final Map<String, String> markings = new HashMap<String, String>() {{
            put("columns", "   a   b   c   d   e   f   g   h   i");
            put("horiz-border", " –––––––––––––––––––––––––––––––––––––");
        }};

        // declare variables TODO: make constants for margins and spacing
        char[][] display = new char[SIZE * 2 - 1][SIZE * 4 - 3];
        int row = 9;

        // fill the array with whitespaces
        for (int x=0; x<SIZE * 4 - 3; x++) {
            for (int y=0; y<SIZE * 2 - 1; y++) {
                display[y][x] = ' ';
            }
        }

        // loop over the squares and set the display each board space
        for (int x=0; x<SIZE; x++) {
            for (int y=0; y<SIZE; y++) {
                switch (squares[x][y]) {
                    case 0:
                        display[y * 2][x * 4] = '.';
                        break;
                    case 1:
                        display[y * 2][x * 4] = 'O';
                        break;
                    case 2:
                        display[y * 2][x * 4] = 'X';
                        break;
                }
            }
        }

        // loop over each wall and set the display on the applicable squares
        for (Wall w: walls) {
            if (w.isVertical()) {
                for (int i=0; i<3; i++) {
                    display[(w.getY() - 1) * 2 + i][w.getX() * 4 + 2] = '|';
                }
            }
            else {
                for (int i=0; i<5; i++) {
                    display[(w.getY() - 1) * 2 + 1][w.getX() * 4 + i] = '–';
                }
            }
        }

        // output the column labels and the border
        System.out.println(markings.get("columns"));
        System.out.println(markings.get("horiz-border"));

        // loop through the row and output each row
        for (int i=SIZE * 2 - 2; i>=0; i--) {
            // output the current row if it is not a blank square
            if (i % 2 == 0) System.out.print(row);
            else System.out.print(" ");
            // output the border
            System.out.print("| ");

            // output the current square
            for (char c : display[i]) {
                System.out.print(c);
            }

            // output the border
            System.out.print(" |");
            // output the current row and decrement the row
            if (i % 2 == 0) System.out.print(+ row--);
            System.out.print('\n');
        }

        // output the column labels and the border
        System.out.println(markings.get("horiz-border"));
        System.out.println(markings.get("columns"));
    }

    /**
     * validatePawnPos method
     * <p>
     * Checks if a pawn's position is valid
     * @param pos The position of the pawn
     * @return boolean - If the position is valid
     */
    public static boolean validatePawnPos(int[] pos) {
        return (pos[0] >= 0 && pos[0] < SIZE && pos[1] >= 0 && pos[1] < SIZE);
    }

    /**
     * validateWallPlace method
     * <p>
     * Checks if a wall placement is valid on the current board
     * @param pos The position of the wall
     * @param vert Whether the wall is vertical
     * @param owner The owner of the wall
     * @return boolean - If the placement is valid
     */
    public boolean validateWallPlace(int[] pos, boolean vert, int owner) {
        // check if the owner has any walls left and call validateWallPlace
        return wallsPlayer[owner - 1] < MAX_WALLS && validateWallPos(new Wall(pos, vert, owner), walls);
    }

    /**
     * validateWallPlace method (static)
     * <p>
     * Checks if a wall placement is valid on the current walls
     * @param wall The first wall
     * @param walls The set of all walls
     * @return boolean - If the placement is valid
     */
    public static boolean validateWallPos(Wall wall, Set<Wall> walls) {
        // declare variables
        boolean valid = true;

        // check if the wall is out of bounds
        if (wall.getX() < 0 || wall.getX() >= SIZE - 1 ||
                wall.getY() <= 0 || wall.getY() >= SIZE)
            valid = false;

        // continue if the wall is not out of bounds
        if (valid) {
            // loop over all walls
            for (Wall w : walls) {
                if (!isWallConflicting(wall, w)) {
                    valid = false; // if the wall is conflicting, set valid to false
                }
            }
        }

        return valid;
    }

    /**
     * isWallConflicting method
     * <p>
     * Checks if two walls are conflicting
     * @param w1 The first wall
     * @param w2 The second wall
     * @return boolean - If they are conflicting
     */
    public static boolean isWallConflicting(Wall w1, Wall w2) {
        // declare variables
        boolean valid = true;
        int x1 = w1.getX();
        int y1 = w1.getY();
        int x2 = w2.getX();
        int y2 = w2.getY();
        boolean v1 = w1.isVertical();
        boolean v2 = w2.isVertical();

        if (v1 && v2) {
            if (x1 == x2 && y1 == y2) valid = false;
            else if (x1 == x2 && (y1 - y2 == 1 || y2 - y1 == 1)) valid = false;
        }

        else if (!v1 && !v2) {
            if (x1 == x2 && y1 == y2) valid = false;
            else if ((x1 - x2 == 1 || x2 - x1 == 1) && y1 == y2) valid = false;
        }

        else {
            if (x1 == x2 && y1 == y2) valid = false;
        }

        return valid;
    }
}
