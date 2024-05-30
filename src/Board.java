import sun.security.util.math.intpoly.P256OrderField;

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
    public static final int SIZE = 9;

    // declare variables
    private int[][] squares = new int[9][9];
    private ArrayList<Wall> walls = new ArrayList<>();
    private Pawn p1;
    private Pawn p2;
    private int p1Walls, p2Walls;

    /**
     * Board constructor
     * <p>
     * Constructor for building a new Board
     * @param p2Human True if player 2 is human, false if player 2 is computer
     */
    public Board(boolean p2Human) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                squares[i][j] = 0;
            }
        }

        p1 = new Pawn(1, true);
        p2 = new Pawn(2, p2Human);

        p1Walls = 10;
        p2Walls = 10;

        squares[p1.getX()][p1.getY()] = 1;
        squares[p2.getX()][p2.getY()] = 2;
    }

    /**
     * Board constructor for loading a game
     * <p>
     * Constructor for building a new Board from a loaded game
     *
     */
    public Board(int[] p1Pos, int[] p2Pos, boolean p2Human, int p1Walls, int p2Walls, ArrayList<Wall> walls) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                squares[i][j] = 0;
            }
        }

        p1 = new Pawn(1, true);
        p2 = new Pawn(2, p2Human);

        p1.setPos(p1Pos);
        p2.setPos(p2Pos);

        squares[p1.getX()][p1.getY()] = 1;
        squares[p2.getX()][p2.getY()] = 2;

        this.p1Walls = p1Walls;
        this.p2Walls = p2Walls;

        // this.walls = walls;
    }
}
