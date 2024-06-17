package state.component;

import java.io.Serializable;
import java.util.*;

/**
 * state.component.Pawn class
 * <p>
 * Represents a pawn in the game of Quoridor.
 *
 * @author Sean Yang
 * @version 30/05/2024
 */

public class Pawn implements Serializable {
    // declare constants
    private static final long serialVersionUID = 1L;

    // declare local variables for pawn id, human status, and position
    private final int id;
    private int[] pos;
    private transient List<List<Integer>> posHistory = new ArrayList<List<Integer>>();
    private final boolean human;

    /**
     * state.component.Pawn constructor (empty history)
     * <p>
     * Constructor for state.component.Pawn
     *
     * @param id {@code int} - The id of the player (1 or 2)
     * @param pos {@code int[]} - The position of the pawn
     * @param human True if the pawn is human, false if the pawn is computer
     */
    public Pawn(int id, int[] pos, boolean human) {
        this.id = id;
        this.pos = new int[]{pos[0], pos[1]};
        List<Integer> posList = new ArrayList<Integer>();
        this.human = human;

        // add the position to the history
        posList.add(pos[0]);
        posList.add(pos[1]);
        posHistory.add(posList);
    }

    /**
     * state.component.Pawn constructor
     * <p>
     * Constructor for state.component.Pawn (with a known position)
     *
     * @param id         {@code int} - The id of the player (1 or 2)
     * @param pos        {@code int[]} - The position of the pawn
     * @param posHistory {@code List<List<Integer>>} - The previous positions of the pawn
     * @param human      True if the pawn is human, false if the pawn is computer
     */
    public Pawn(int id, int[] pos, List<List<Integer>> posHistory, boolean human) {
        this.id = id;
        this.pos = new int[]{pos[0], pos[1]};
        this.posHistory = new ArrayList<>(posHistory);
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
        return pos[0];
    }

    /**
     * getY method
     * <p>
     * Returns the y-coordinate of the pawn
     *
     * @return {@code int} - The y-coordinate of the pawn
     */
    public int getY() {
        return pos[1];
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
        // initialize a variable newPosList to store the current position
        List<Integer> newPosList = new ArrayList<Integer>();
        newPosList.add(newPos[0]);
        newPosList.add(newPos[1]);

        // add newPosList to the history of positions
        posHistory.add(newPosList);

        // set the position to the new position
        pos = new int[]{newPos[0], newPos[1]};
    }

    /**
     * moveTemp method
     * <p>
     * Moves the pawn's position without changing the history (does not validate)
     *
     * @param newPos {@code int[]} - The new position
     */
    public void moveTemp(int[] newPos) {
        pos = new int[]{newPos[0], newPos[1]};
    }

    /**
     * moveBackTemp method
     * <p>
     * Moves the pawn's position to the last stored position (does not validate)
     */
    public void moveBackTemp() {
        pos = new int[]{
                posHistory.get(posHistory.size() - 1).get(0),
                posHistory.get(posHistory.size() - 1).get(1)
        };
    }

    /**
     * copy method
     * <p>
     * Returns a copy of the current pawn
     *
     * @return {@code state.component.Pawn} - the copy
     */
    public Pawn copy() {
        return new Pawn(id, new int[]{pos[0], pos[1]}, posHistory, human);
    }
}
