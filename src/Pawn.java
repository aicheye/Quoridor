/**
 * Pawn class
 * <p>
 * Represents a pawn in the game of Quoridor.
 * @author Sean Yang
 * @version 30/05/2024
 */

public class Pawn {
    // declare constants for player and computer start locations
    private static final int[] P1_START = {4, 0};
    private static final int[] P2_START = {4, 8};

    // declare local variables for pawn id, human status, and position
    private int[] pos = new int[2];
    private final int id;
    private final boolean human;

    /**
     * Pawn method
     * <p>
     * Constructor for Pawn
     * @param id The id of the player (1 or 2)
     * @param human True if the pawn is human, false if the pawn is computer
     */
    public Pawn(int id, boolean human) {
        this.id = id;
        this.human = human;

        switch (id) {
            case 1:
                pos = P1_START;
                break;
            case 2:
                pos = P2_START;
                break;
        }

        // if (!human) {
            // if the pawn is a computer, initialize a new computer object
            // this.computer = new Computer();
        // }
    }

    /**
     * Pawn method
     * <p>
     * Constructor for Pawn (with a known position)
     *
     * @param pos The position of the pawn
     * @param id The id of the player (1 or 2)
     * @param human True if the pawn is human, false if the pawn is computer
     */
    public Pawn (int[] pos, int id, boolean human) {
        this.pos = new int[] {pos[0], pos[1]};
        this.id = id;
        this.human = human;
    }

    /**
     * copy method
     * <p>
     * Returns a copy of the current pawn
     * @return Pawn - the copy
     */
    public Pawn copy() {
        return new Pawn(new int[] {pos[0], pos[1]}, id, human);
    }

    /**
     * getId method
     * <p>
     * Gets the id of the pawn: either 1 or 2
     * @return int - The id of the pawn
     */
    public int getId() {
        return id;
    }

    /**
     * isHuman method
     * <p>
     * Returns if the pawn is owned by the human player
     * @return boolean - True if the pawn is human
     */
    public boolean isHuman() {
        return human;
    }

    /**
     * getPos method
     * <p>
     * Returns the position of the pawn
     * @return int[] - The position of the pawn
     */
    public int[] getPos() {
        return pos;
    }

    /**
     * getX method
     * <p>
     * Returns the x-coordinate of the pawn
     * @return int - The x-coordinate of the pawn
     */
    public int getX() {
        return getPos()[0];
    }

    /**
     * getY method
     * <p>
     * Returns the x-coordinate of the pawn
     * @return int - The x-coordinate of the pawn
     */
    public int getY() {
        return getPos()[1];
    }

    /**
     * move method
     * <p>
     * Moves the pawn's position (does not validate)
     * @param newPos The new position
     */
    public void move(int[] newPos) {
        pos = newPos;
    }
}
