/**
 * Pawn class
 * <p>
 * Represents a pawn in the game of Quoridor.
 *
 * @author Sean Yang
 * @version 30/05/2024
 */

public class Pawn {
    // declare local variables for pawn id, human status, and position
    private int[] pos;
    private final int id;
    private final boolean human;

    /**
     * Pawn method
     * <p>
     * Constructor for Pawn (with a known position)
     *
     * @param id {@code int} - The id of the player (1 or 2)
     * @param pos {@code int[]} - The position of the pawn
     * @param human True if the pawn is human, false if the pawn is computer
     */
    public Pawn(int id, int[] pos, boolean human) {
        this.pos = new int[] {pos[0], pos[1]};
        this.id = id;
        this.human = human;
    }

    /**
     * getId method
     * <p>
     * Gets the id of the pawn: either 1 or 2
     *
     * @return {@code int} - The id of the pawn
     */
    public int getId() {
        return id;
    }

    /**
     * isHuman method
     * <p>
     * Returns if the pawn is owned by the human player
     *
     * @return {@code boolean} - True if the pawn is human
     */
    public boolean isHuman() {
        return human;
    }

    /**
     * getPos method
     * <p>
     * Returns the position of the pawn
     *
     * @return {@code int[]} - The position of the pawn
     */
    public int[] getPos() {
        return pos;
    }

    /**
     * getX method
     * <p>
     * Returns the x-coordinate of the pawn
     * @return {@code int} - The x-coordinate of the pawn
     */
    public int getX() {
        return getPos()[0];
    }

    /**
     * getY method
     * <p>
     * Returns the y-coordinate of the pawn
     *
     * @return {@code int} - The y-coordinate of the pawn
     */
    public int getY() {
        return getPos()[1];
    }

    /**
     * getYGoal method
     * <p>
     * Returns the y-coordinate of the goal for the pawn
     *
     * @return {@code int} - The y-coordinate of the goal
     */
    public int getYGoal() {
        return id == 1 ? 8 : 0;
    }

    /**
     * move method
     * <p>
     * Moves the pawn's position (does not validate)
     *
     * @param newPos {@code int[]} - The new position
     */
    public void move(int[] newPos) {
        pos = newPos;
    }

    /**
     * copy method
     * <p>
     * Returns a copy of the current pawn
     *
     * @return {@code Pawn} - the copy
     */
    public Pawn copy() {
        return new Pawn(id, new int[]{pos[0], pos[1]}, human);
    }
}
