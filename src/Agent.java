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
     * @param diff The difficulty of the computer (0 for normal, 1 for hard)
     */
    public Agent(int diff) {
        this.diff = diff;
    }

    /**
     * encodeAction method
     * <p>
     * Encodes an action into an array (wall)
     *
     * @param pos      The position of the wall
     * @param vertical True if the wall is vertical, false if the wall is horizontal
     */
    private static int[] encodeAction(int[] pos, boolean vertical) {
        return new int[]{1, pos[0], pos[1], vertical ? 1 : 0};
    }

    /**
     * encodeAction method
     * <p>
     * Encodes an action into an array (move)
     *
     * @param pos The position of the move
     */
    private static int[] encodeAction(int[] pos) {
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
    public int[] getAction(Pawn self, Board board) {
        if (diff == 0) {
            return getActionNormal(self, board);
        } else {
            return getActionHard(self, board);
        }
    }

    /**
     * getActionNormal method
     * <p>
     * Returns the move that the computer will make (normal)
     *
     * @param self  The pawn that the computer is controlling
     * @param board The current state of the board
     * @return int[] - The move that the computer will make
     */
    public int[] getActionNormal(Pawn self, Board board) {
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
        int[] action = new int[4];
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
                action = encodeAction(new int[]{move.get(0), move.get(1)});
                winning = true;
            }
        }

        // if there are no immediately winning moves, apply the threshold
        if (!winning) {
            if (maxWallDifference >= WALL_DIFF_THRESHOLD && maxWallDifference >= maxPawnDifference) {
                action = encodeAction(maxWall.getPos(), maxWall.isVertical());
            } else {
                action = encodeAction(maxPawnMove);
            }
        }

        return action;
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
     * getActionHard method
     * <p>
     * Returns the move that the computer will make (hard)
     * Implements the minimax algorithm with a depth of 3 (without a-b pruning)
     * @param self  The pawn that the computer is controlling
     * @param board The current state of the board
     * @return int[] - The move that the computer will make
     */
    public int[] getActionHard(Pawn self, Board board) {
        List<List<Integer>> children = getChildren(board);
        int[] action = new int[5];
        int maxEval = Integer.MIN_VALUE;

        // check if the computer has no more walls to place
        if (board.getWallsRemaining(self) == 0) {
            action = getActionNormal(self, board);
        }

        // if the computer still has walls, run the minimax algorithm
        else {
            for (List<Integer> child : children) {
                int[] actionChild = new int[]{child.get(0), child.get(1), child.get(2), child.get(3)};
                Board next = board.copy();
                next.doAction(actionChild);
                int eval = minimax(next, 2, self.getId(), Integer.MIN_VALUE, Integer.MAX_VALUE, new HashSet<Board>());

                // check if the evaluation is greater than the maximum evaluation
                if (eval > maxEval) {
                    maxEval = eval;
                    action = actionChild;
                }
            }
        }

        if (maxEval == -1) action = getActionNormal(self, board);

        return action;
    }

    /**
     * minimax method
     * <p>
     * Implements the minimax algorithm (without a-b pruning)
     * @param position The current state of the board
     * @param depth The depth of the search
     * @param maximizingPlayer The player to maximize
     * @return int[] - The evaluation of the move
     */
    private int minimax(Board position, int depth, int alpha, int beta, int maximizingPlayer, Set<Board> visited) {
        // declare variables
        int eval = -1;
        int maxEval = Integer.MIN_VALUE;
        int minEval = Integer.MAX_VALUE;
        Board next;
        int evalChild;
        boolean alphaBetaPass = true;

        // if the depth is 0 or the game is over, return the static evaluation of the move
        if (depth == 0 || position.isGameOver()) {
            eval = eval(position, maximizingPlayer);
        }

        // if the current player is the maximizing player, find the move with the maximum evaluation using recursion
        else if (position.getCurrentPlayer() == maximizingPlayer) {
            // loop over each possible move from the current position
            for (List<Integer> child : getChildren(position)) {
                // check if this branch has passed alpha and beta pruning
                if (alphaBetaPass) {
                    int[] actionChild = new int[]{child.get(0), child.get(1), child.get(2), child.get(3)};

                    // create a temporary board and apply the action
                    next = position.copy();
                    next.doAction(actionChild);

                    // recursively call the minimax function with the temporary board
                    // if the position has not been visited
                    // and if the move is not significantly worse than the current eval
                    if (!visited.contains(next) && eval(position, maximizingPlayer) >= eval(next, maximizingPlayer)) {
                        visited.add(next);
                        evalChild = minimax(next, depth - 1, maximizingPlayer, alpha, beta, visited);

                        // if the evaluation of the move is greater than the maximum evaluation,
                        // update the maximum evaluation
                        if (evalChild > maxEval) {
                            maxEval = evalChild;
                            eval = eval(next, maximizingPlayer);
                        }

                        // apply alpha-beta pruning
                        alpha = Math.max(alpha, evalChild);
                        if (beta <= alpha) {
                            alphaBetaPass = false;
                        }
                    }
                }
            }
        }

        // if the current player is the minimizing player, find the move with the minimum evaluation using recursion
        else {
            // loop over each possible move from the current position
            for (List<Integer> child : getChildren(position)) {
                // check if this branch has passed alpha and beta pruning
                if (alphaBetaPass) {
                    int[] actionChild = new int[]{child.get(0), child.get(1), child.get(2), child.get(3)};

                    // create a temporary board and apply the action
                    next = position.copy();
                    next.doAction(actionChild);

                    // recursively call the minimax function with the temporary board
                    // if the position has not been visited
                    if (!visited.contains(next)) {
                        visited.add(next);
                        evalChild = minimax(next, depth - 1, maximizingPlayer, alpha, beta, visited);

                        // if the evaluation of the move is less than the maximum evaluation,
                        // update the maximum evaluation
                        if (evalChild < minEval) {
                            minEval = evalChild;
                            eval = eval(next, maximizingPlayer);
                        }

                        // apply alpha-beta pruning
                        beta = Math.min(beta, evalChild);
                        if (beta <= alpha) {
                            alphaBetaPass = false;
                        }
                    }
                }
            }
        }

        return eval;
    }

    /**
     * eval method
     * <p>
     * Evaluates the current state of the board as the difference between the distance from
     * the pawn to the goal and the distance from the enemy to their goal
     * @param position The current state of the board
     * @param maximizingPlayer The player to maximize
     * @return int - The evaluation of the board
     */
    private int eval(Board position, int maximizingPlayer) {
        return (calcDistance(position.getEnemy(position.getPawn(maximizingPlayer)), position) -
                calcDistance(position.getPawn(maximizingPlayer), position));
    }

    /**
     * getChildren method
     * <p>
     * Returns a list of all possible moves from the current position
     * @param position The current state of the board
     * @return List<List < Integer>> - A list of all possible moves
     */
    private List<List<Integer>> getChildren(Board position) {
        // declare variables
        List<List<Integer>> children = new ArrayList<List<Integer>>();
        int[] action;
        List<Integer> actionColl;

        // loop over every pawn move
        for (List<Integer> move : position.calcValidPawnMoves(position.getPawn(position.getCurrentPlayer()))) {
            // encode it and add it to the list of children
            action = encodeAction(new int[]{move.get(0), move.get(1)});
            actionColl = new ArrayList<Integer>();
            actionColl.add(action[0]);
            actionColl.add(action[1]);
            actionColl.add(action[2]);
            actionColl.add(action[3]);

            children.add(actionColl);
        }

        // loop over every wall placement
        for (Wall wall : position.calcValidWallPlacements(position.getPawn(position.getCurrentPlayer()))) {
            // encode it and add it to the list of children
            action = encodeAction(wall.getPos(), wall.isVertical());
            actionColl = new ArrayList<Integer>();
            actionColl.add(action[0]);
            actionColl.add(action[1]);
            actionColl.add(action[2]);
            actionColl.add(action[3]);

            children.add(actionColl);
        }

        return children;
    }
}
