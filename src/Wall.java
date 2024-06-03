/**
 * Wall class
 * <p>
 * Represents a wall in the game of Quoridor
 * @author Sean Yang
 * @version 30/05/2024
 */
public class Wall {
    // declare fields
    private final boolean vertical;
    private final int[] pos;
    private final int owner;

    /**
     * Wall method
     * <p>
     * Constructor for Wall
     * @param pos      The position of the wall
     * @param vertical True if the wall is vertical, false if the wall is horizontal
     * @param owner    The id of the player who owns the wall
     */
    public Wall(int[] pos, boolean vertical, int owner) {
        this.pos = pos;
        this.vertical = vertical;
        this.owner = owner;
    }

    /**
     * getPos method
     * <p>
     * Getter for the position of the wall
     * @return int[] - The position of the wall
     */
    public int[] getPos() {
        return pos;
    }

    /**
     * isVertical method
     * <p>
     * Getter for the vertical status of the wall
     * @return boolean - If the wall is vertical
     */
    public boolean isVertical() {
        return vertical;
    }

    /**
     * getOwner method
     * <p>
     * Getter for the owner of the wall
     * @return int - The id of the owner
     */
    public int getOwner() {
        return owner;
    }

    /**
     * getX method
     * <p>
     * Getter for the x position of the wall
     * @return int - The x position of the wall
     */
    public int getX() {
        return getPos()[0];
    }

    /**
     * getY method
     * <p>
     * Getter for the y position of the wall
     * @return int - The y position of the wall
     */
    public int getY() {
        return getPos()[1];
    }

    /**
     * isBlocking method
     * <p>
     * Checks if the current wall is blocking a path from one square
     * @param pos The position to start from
     * @param dir The direction to check
     * @return boolean - Whether the wall is blocking the direction
     */
    public boolean isBlocking(int[] pos, char dir) {
        // declare variables
        boolean blocking = false;

        if (vertical) {
            if (pos[0] == this.pos[0]) {
                if (pos[1] == this.pos[1] || pos[1] == this.pos[1] - 1 && dir == 'E') blocking = true;
            } else if (pos[0] == this.pos[0] + 1) {
                if (pos[1] == this.pos[1] || pos[1] == this.pos[1] - 1 && dir == 'W') blocking = true;
            }
        } else {
            if (pos[1] == this.pos[1]) {
                if (pos[0] == this.pos[0] || pos[0] == this.pos[0] + 1 && dir == 'S') blocking = true;
            } else if (pos[1] == this.pos[1] - 1) {
                if (pos[0] == this.pos[0] || pos[0] == this.pos[0] + 1 && dir == 'N') blocking = true;
            }
        }

        return blocking;
    }
}
