/**
 * Computer class
 * <p>
 * Represents two types of automatic move algorithms in Quoridor
 *
 * @author Sean Yang
 * @version 30/05/2024
 */
public class Computer {
    // declare local variables
    private final int diff;

    /**
     * Computer method
     * <p>
     * Constructor for Computer
     * @param diff The difficulty of the computer (0 for easy, 1 for hard)
     */
    public Computer(int diff) {
        this.diff = diff;
    }

    /**
     * encodeInstruction method
     * <p>
     * Encodes an instruction into an array (wall)
     *
     * @param pos      The position of the wall
     * @param vertical True if the wall is vertical, false if the wall is horizontal
     */
    private static int[] encodeInstruction(int[] pos, boolean vertical) {
        return new int[]{1, pos[0], pos[1], vertical ? 1 : 0};
    }

    /**
     * encodeInstruction method
     * <p>
     * Encodes an instruction into an array (move)
     *
     * @param pos The position of the move
     */
    private static int[] encodeInstruction(int[] pos) {
        return new int[]{0, pos[0], pos[1], 0};
    }

    /**
     * getMove method
     * <p>
     * Returns the move that the computer will make
     *
     * @param self  The pawn that the computer is controlling
     * @param board The current state of the board
     * @return int[] - The move that the computer will make
     */
    public int[] getInstruction(Pawn self, Board board) {
        return encodeInstruction(new int[]{4, 7});
    }
}
