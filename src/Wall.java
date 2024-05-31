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
     *
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
}
