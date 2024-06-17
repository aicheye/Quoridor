package state.component;

import java.io.Serializable;
import java.util.Arrays;

/**
 * state.component.Wall class
 * <p>
 * Represents a wall in the game of Quoridor
 *
 * @author Sean Yang
 * @version 30/05/2024
 */
public class Wall implements Serializable {
    // declare constants
    private static final long serialVersionUID = 1L;

    // declare fields
    private final boolean vertical;
    private final int[] pos;
    private final int owner;

    /**
     * state.component.Wall constructor
     * <p>
     * Constructor for state.component.Wall
     *
     * @param owner {@code int} - The id of the player who owns the wall
     * @param pos {@code int[]} - The position of the wall
     * @param vertical {@code boolean} - {@code true} if the wall is vertical, {@code false} if the wall is horizontal
     */
    public Wall(int owner, int[] pos, boolean vertical) {
        this.pos = pos;
        this.vertical = vertical;
        this.owner = owner;
    }

    /**
     * getPos method
     * <p>
     * Getter for the position of the wall
     *
     * @return {@code int[]} - The position of the wall
     */
    public int[] getPos() {
        return pos;
    }

    /**
     * isVertical method
     * <p>
     * Getter for the vertical status of the wall
     *
     * @return {@code boolean} - If the wall is vertical
     */
    public boolean isVertical() {
        return vertical;
    }

    /**
     * getOwner method
     * <p>
     * Getter for the owner of the wall
     *
     * @return {@code int} - The id of the owner
     */
    public int getOwner() {
        return owner;
    }

    /**
     * getX method
     * <p>
     * Getter for the x position of the wall
     *
     * @return {@code int} - The x position of the wall
     */
    public int getX() {
        return pos[0];
    }

    /**
     * getY method
     * <p>
     * Getter for the y position of the wall
     *
     * @return {@code int} - The y position of the wall
     */
    public int getY() {
        return pos[1];
    }

    /**
     * isBlocking method
     * <p>
     * Checks if the current wall is blocking a path from one square
     *
     * @param pos {@code int[]} - The position to start from
     * @param dir {@code char} - The direction to check
     * @return {@code boolean} - Whether the wall is blocking the direction
     */
    public boolean isBlocking(int[] pos, char dir) {
        // declare variables
        boolean blocking = false;

        if (vertical) {
            if (pos[0] == this.pos[0]) {
                if ((pos[1] == this.pos[1] || pos[1] == this.pos[1] - 1) && dir == 'E') blocking = true;
            } else if (pos[0] == this.pos[0] + 1) {
                if ((pos[1] == this.pos[1] || pos[1] == this.pos[1] - 1) && dir == 'W') blocking = true;
            }
        } else {
            if (pos[1] == this.pos[1]) {
                if ((pos[0] == this.pos[0] || pos[0] == this.pos[0] + 1) && dir == 'S') blocking = true;
            } else if (pos[1] == this.pos[1] - 1) {
                if ((pos[0] == this.pos[0] || pos[0] == this.pos[0] + 1) && dir == 'N') blocking = true;
            }
        }

        return blocking;
    }

    /**
     * equals method
     * <p>
     * Checks if two walls are equal
     *
     * @param obj {@code Object} - The object to compare to
     * @return {@code boolean} - Whether the walls are equal
     */
    @Override
    public boolean equals(Object obj) {
        // declare variables
        boolean equal = false;

        // check for class equality and field equality
        if (obj instanceof Wall) {
            Wall wall = (Wall) obj;
            if (wall.getOwner() == this.owner &&
                    wall.getPos()[0] == this.pos[0] &&
                    wall.getPos()[1] == this.pos[1] &&
                    wall.isVertical() == this.vertical) {
                equal = true;
            }
        }

        return equal;
    }

    /**
     * hashCode method
     * <p>
     * Generates a hashCode for a {@code state.component.Wall} object
     *
     * @return {@code int} - The hash
     */
    @Override
    public int hashCode() {
        // return the java array hashcode with an array containing every field
        return Arrays.hashCode(new Object[]{
                owner,
                pos[0],
                pos[1],
                vertical
        });
    }
}
