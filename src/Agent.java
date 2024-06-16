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
     * @param diff {@code int} - The difficulty of the computer (0 for normal, 1 for hard)
     */
    public Agent(int diff) {
        this.diff = diff;
    }

    /**
     * encodeAction method
     * <p>
     * Encodes an action into an array (wall placement)
     *
     * @param pos {@code int[]} - The position of the wall
     * @param vertical {@code boolean} - True if the wall is vertical, false if the wall is horizontal
     */
    private static int[] encodeAction(int[] pos, boolean vertical) {
        return new int[]{1, pos[0], pos[1], vertical ? 1 : 0};
    }

    /**
     * encodeAction method
     * <p>
     * Encodes an action into an array (pawn moves)
     *
     * @param pos {@code int[]} - The position of the move
     */
    private static int[] encodeAction(int[] pos) {
        return new int[]{0, pos[0], pos[1], 0};
    }

    /**
     * calcWallDiffThreshold
     * <p>
     * Calculates the wall difference threshold based on the number of walls remaining
     * @param wallsRemaining {@code int} - The number of walls remaining
     * @return {@code int} - The wall difference threshold
     */
    private static int calcWallDiffThreshold(int wallsRemaining) {
        int WALL_DIFF_THRESHOLD;

        // calculate the threshold
        if (wallsRemaining == 10) {
            WALL_DIFF_THRESHOLD = 0;
        } else if (wallsRemaining >= 7) {
            WALL_DIFF_THRESHOLD = 1;
        } else if (wallsRemaining >= 3) {
            WALL_DIFF_THRESHOLD = 2;
        } else {
            WALL_DIFF_THRESHOLD = 3;
        }

        return WALL_DIFF_THRESHOLD;
    }

    /**
     * getActionNormal method
     * <p>
     * Returns the move that the computer will make (normal difficulty - depth 1)
     *
     * @param self {@code Pawn} - The pawn that the computer is controlling
     * @param board {@code Board} - The current state of the board
     * @return {@code int[]} - The move that the computer will make
     */
    private int[] getActionNormal(Pawn self, Board board) {
        // hidden constant: adjusted based on the number of walls already on the board
        final int WALL_DIFF_THRESHOLD = calcWallDiffThreshold(board.getWallsRemaining(self));

        // declare variables
        Set<List<Integer>> validPawnMoves = board.calcValidPawnMoves(self);
        Set<Wall> validWallMoves = board.calcValidWallPlacements(self);
        int selfCurrDist = board.calcDistance(self);
        int enemyCurrDist = board.calcDistance(board.getEnemy(self));
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
            if (selfCurrDist - board.calcDistance(self) > maxPawnDifference) {
                maxPawnDifference = selfCurrDist - board.calcDistance(self);
                maxPawnMove = new int[]{move.get(0), move.get(1)};
            }
            self.move(old);
        }

        // calculate the minimum distance with every wall placement
        for (Wall wall : validWallMoves) {
            board.placeWallTemp(board.getPawn(wall.getOwner()), wall.getPos(), wall.isVertical());
            if (board.calcDistance(board.getEnemy(self)) - enemyCurrDist > maxWallDifference) {
                maxWallDifference = board.calcDistance(board.getEnemy(self)) - enemyCurrDist;
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

        // if there are no immediately winning moves, apply the threshold and choose the best move given the threshold
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
     * getActionHard method
     * <p>
     * Returns the move that the computer will make (hard)
     * Implements the minimax algorithm with a depth of 3 (with a-b pruning)
     *
     * @param self {@code Pawn} - The pawn that the computer is controlling
     * @param board {@code Board} - The current state of the board
     * @return {@code int[]} - The move that the computer will make
     */
    private int[] getActionHard(Pawn self, Board board) {
        // declare variables
        final int SEARCH_DEPTH = 3;
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
                int eval = minimax(self.getId(), next, SEARCH_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, new HashSet<Board>());

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
     * Implements the minimax algorithm (with a-b pruning)
     *
     * @param maximizingPlayer {@code int} - The player to maximize (1 or 2)
     * @param position {@code Board} - The current state of the board
     * @param depth {@code int} - The depth of the search
     * @param alpha {@code int} - The alpha value for a-b pruning
     * @param beta {@code int} - The beta value for a-b pruning
     * @param visited {@code boolean} - The set of visited positions
     * @return {@code int[]} - The evaluation of the move
     */
    private int minimax(int maximizingPlayer, Board position, int depth, int alpha, int beta, Set<Board> visited) {
        // declare variables
        int eval = -1;
        int maxEval = Integer.MIN_VALUE;
        int minEval = Integer.MAX_VALUE;
        Board nextBoard;
        int evalChild;
        boolean alphaBetaPruned = false;

        // if the depth is 0 or the game is over, return the static evaluation of the move
        if (depth == 0 || position.isGameOver()) {
            eval = eval(maximizingPlayer, position);
        }

        // if the current player is the maximizing player, find the move with the maximum evaluation using recursion
        else if (position.getCurrentPlayer() == maximizingPlayer) {
            // loop over each possible move from the current position
            for (List<Integer> child : getChildren(position)) {
                // check if this branch has passed alpha and beta pruning
                if (!alphaBetaPruned) {
                    int[] actionChild = new int[]{child.get(0), child.get(1), child.get(2), child.get(3)};

                    // create a temporary board and apply the action
                    nextBoard = position.copy();
                    nextBoard.doAction(actionChild);

                    // recursively call the minimax function with the temporary board
                    // if the position has not been visited
                    // and if the move is not significantly worse than the current eval
                    if (!visited.contains(nextBoard) && eval(maximizingPlayer, position) >= eval(maximizingPlayer, nextBoard)) {
                        visited.add(nextBoard);
                        evalChild = minimax(maximizingPlayer, nextBoard, depth - 1, alpha, beta, visited);

                        // if the evaluation of the move is greater than the maximum evaluation,
                        // update the maximum evaluation
                        if (evalChild > maxEval) {
                            maxEval = evalChild;
                            eval = eval(maximizingPlayer, nextBoard);
                        }

                        // apply alpha-beta pruning
                        alpha = Math.max(alpha, evalChild);
                        if (beta <= alpha) {
                            alphaBetaPruned = true;
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
                if (!alphaBetaPruned) {
                    int[] actionChild = new int[]{child.get(0), child.get(1), child.get(2), child.get(3)};

                    // create a temporary board and apply the action
                    nextBoard = position.copy();
                    nextBoard.doAction(actionChild);

                    // recursively call the minimax function with the temporary board
                    // if the position has not been visited
                    if (!visited.contains(nextBoard)) {
                        visited.add(nextBoard);
                        evalChild = minimax(maximizingPlayer, nextBoard, depth - 1, alpha, beta, visited);

                        // if the evaluation of the move is less than the maximum evaluation,
                        // update the maximum evaluation
                        if (evalChild < minEval) {
                            minEval = evalChild;
                            eval = eval(maximizingPlayer, nextBoard);
                        }

                        // apply alpha-beta pruning
                        beta = Math.min(beta, evalChild);
                        if (beta <= alpha) {
                            alphaBetaPruned = true;
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
     *
     * @param maximizingPlayer {@code int} - The player to maximize
     * @param position {@code Board} - The current state of the board
     * @return {@code int} - The evaluation of the board
     */
    private int eval(int maximizingPlayer, Board position) {
        return (position.calcDistance(position.getEnemy(position.getPawn(maximizingPlayer))) -
                position.calcDistance(position.getPawn(maximizingPlayer)));
    }

    /**
     * getChildren method
     * <p>
     * Returns a list of all possible moves from the current position
     * @param position {@code Board} - The current state of the board
     * @return {@code List<List<Integer>>} - A list of all possible moves
     */
    private List<List<Integer>> getChildren(Board position) {
        // declare variables
        List<List<Integer>> children = new ArrayList<List<Integer>>();
        int[] action;
        List<Integer> actionColl;

        // loop over every pawn move
        for (List<Integer> move : position.calcValidPawnMoves(position.getCurrentPawn())) {
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
        for (Wall wall : position.calcValidWallPlacements(position.getCurrentPawn())) {
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

    /**
     * getAction method
     * <p>
     * Returns the move that the computer will make
     *
     * @param self  {@code Pawn} - The pawn that the computer is controlling
     * @param board {@code Board} - The current state of the board
     * @return {@code int[]} - The move that the computer will make
     */
    public int[] getAction(Pawn self, Board board) {
        if (diff == 0) {
            return getActionNormal(self, board);
        } else {
            return getActionHard(self, board);
        }
    }
}
