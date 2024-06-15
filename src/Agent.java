import java.util.*;

/**
 * Computer class
 * <p>
 * Represents two types of automatic move algorithms in Quoridor
 *
 * @author Sean Yang
 * @version 30/05/2024
 */
public class Agent {
    // declare local variables
    private final int diff;

    /**
     * Computer method
     * <p>
     * Constructor for Computer
     *
     * @param diff The difficulty of the computer (0 for easy, 1 for hard)
     */
    public Agent(int diff) {
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
        if (diff == 0) {
            return getInstructionEasy(self, board);
        } else {
            return getInstructionHard(self, board);
        }
    }

    /**
     * getInstructionEasy method
     * <p>
     * Returns the move that the computer will make (easy)
     *
     * @param self  The pawn that the computer is controlling
     * @param board The current state of the board
     * @return int[] - The move that the computer will make
     */
    public int[] getInstructionEasy(Pawn self, Board board) {
        // hidden constant: adjusted based on the number of walls already on the board
        int WALL_DIFF_THRESHOLD;
        if (board.getWallsRemaining(self) == 10) {
            WALL_DIFF_THRESHOLD = 0;
        } else if (board.getWallsRemaining(self) <= 7) {
            WALL_DIFF_THRESHOLD = 1;
        } else if (board.getWallsRemaining(self) >= 3) {
            WALL_DIFF_THRESHOLD = 2;
        } else {
            WALL_DIFF_THRESHOLD = 3;
        }

        // declare variables
        Set<List<Integer>> validPawnMoves = board.calcValidPawnMoves(self);
        Set<Wall> validWallMoves = board.calcValidWallPlacements(self);
        int selfCurrDist = calcDistance(self, board);
        int enemyCurrDist = calcDistance(board.getEnemy(self), board);
        int maxWallDifference = -1;
        Wall maxWall = new Wall(0, new int[]{0, 0}, false);
        int maxPawnDifference = -1;
        int[] maxPawnMove = new int[2];
        int[] old = self.getPos();
        int[] instruction = new int[4];
        boolean winning = false;

        // calculate the minimum distance with every pawn move
        for (List<Integer> move : validPawnMoves) {
            self.move(new int[]{move.get(0), move.get(1)});
            if (selfCurrDist - calcDistance(self, board) > maxPawnDifference) {
                maxPawnDifference = selfCurrDist - calcDistance(self, board);
                maxPawnMove = new int[]{move.get(0), move.get(1)};
            }
            self.move(old);
        }

        // calculate the minimum distance with every wall placement
        for (Wall wall : validWallMoves) {
            board.placeWallTemp(board.getPawn(wall.getOwner()), wall.getPos(), wall.isVertical());
            if (calcDistance(board.getEnemy(self), board) - enemyCurrDist > maxWallDifference) {
                maxWallDifference = calcDistance(board.getEnemy(self), board) - enemyCurrDist;
                maxWall = new Wall(wall.getOwner(), wall.getPos(), wall.isVertical());
            }
            board.removeWall(wall.getPos());
        }

        // prioritize moves which win immediately
        for (List<Integer> move : validPawnMoves) {
            if (move.get(1) == self.getYGoal()) {
                instruction = encodeInstruction(new int[]{move.get(0), move.get(1)});
                winning = true;
            }
        }

        // if there are no immediately winning moves, apply the threshold
        if (!winning) {
            if (maxWallDifference >= WALL_DIFF_THRESHOLD && maxWallDifference >= maxPawnDifference) {
                instruction = encodeInstruction(maxWall.getPos(), maxWall.isVertical());
            } else {
                instruction = encodeInstruction(maxPawnMove);
            }
        }

        return instruction;
    }

    /**
     * calcDistance method
     * <p>
     * Calculates the distance between the pawn and the goal
     *
     * @param self The pawn that the computer is controlling
     */
    public int calcDistance(Pawn self, Board board) {
        // declare variables
        int[][] dp = new int[Board.getSize()][Board.getSize()];
        int[] old = self.getPos();
        List<Integer> oldColl = new ArrayList<Integer>();
        List<List<Integer>> queue = new ArrayList<List<Integer>>();
        Set<List<Integer>> visited = new HashSet<List<Integer>>();
        List<Integer> curr;
        int min = Integer.MAX_VALUE;
        Set<Character> dirs = new HashSet<Character>();
        dirs.add('N');
        dirs.add('E');
        dirs.add('S');
        dirs.add('W');
        int[] move;
        List<Integer> moveColl;

        for (int i = 0; i < Board.getSize(); i++) {
            for (int j = 0; j < Board.getSize(); j++) {
                dp[i][j] = Integer.MAX_VALUE;
            }
        }

        dp[self.getPos()[0]][self.getPos()[1]] = 0;

        oldColl.add(old[0]);
        oldColl.add(old[1]);

        queue.add(oldColl);

        while (!queue.isEmpty()) {
            curr = queue.remove(0);
            visited.add(curr);

            self.move(new int[]{curr.get(0), curr.get(1)});

            for (char dir : dirs) {
                move = Board.calcNewPos(self.getPos(), dir);
                moveColl = new ArrayList<Integer>();
                moveColl.add(move[0]);
                moveColl.add(move[1]);

                if (Board.validatePawnPos(move) && !board.isAnyWallBlocking(self.getPos(), dir) && !visited.contains(moveColl)) {
                    queue.add(moveColl);
                    dp[move[0]][move[1]] = Math.min(dp[move[0]][move[1]], dp[curr.get(0)][curr.get(1)] + 1);
                }
            }
        }

        for (int i = 0; i < Board.getSize(); i++) {
            min = Math.min(min, dp[i][self.getYGoal()]);
        }

        self.move(old);

        return min;
    }

    /**
     * getInstructionHard method
     * <p>
     * Returns the move that the computer will make (hard)
     *
     * @param self  The pawn that the computer is controlling
     * @param board The current state of the board
     * @return int[] - The move that the computer will make
     */
    public int[] getInstructionHard(Pawn self, Board board) {
        return getInstructionEasy(self, board);
    }
}