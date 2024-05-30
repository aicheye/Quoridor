/**
 * Pawn class
 * <p>
 * Represents a pawn in the game of Quoridor.
 *
 * @author Sean Yang
 * @version 30/05/2024
 */

public class Pawn {
    // declare constants for player and computer start locations
    private static final int[] P1_START = {4, 0};
    private static final int[] P2_START = {4, 8};

    // declare local variables for pawn id, human status, and position
    private int id;
    private boolean human;
    private int[] pos = new int[2];

    /**
     * Pawn method
     * <p>
     * Constructor for Pawn
     *
     * @param human True if the pawn is human, false if the pawn is computer
     */
    public Pawn(int id, boolean human) {
        this.human = human;

        switch (id) {
            case 1:
                this.id = 1;
                pos = P1_START;
                break;
            case 2:
                this.id=2;
                pos = P2_START;
                break;
        }

        if (!human) {
            // if the pawn is a computer, initialize a new computer object
            // this.computer = new Computer();
        }
    }

    /**
     * getId method
     * <p>
     * Gets the id of the pawn: either 1 or 2
     *
     * @return int - The id of the pawn
     */
    public int getId() {
        return id;
    }

    /**
     * isHuman method
     * <p>
     * Returns if the pawn is owned by the human player
     *
     * @return boolean - True if the pawn is human
     */
    public boolean isHuman() {
        return human;
    }

    /**
     * getPos method
     * <p>
     * Returns the position of the pawn
     *
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

    public void setPos(int[] newPos) {
        pos = newPos;
    }
}
