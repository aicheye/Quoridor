import state.Board;
import state.component.Pawn;
import state.component.Wall;

import java.io.*;
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
    private static Map<Board, Integer> transpositionsEvals;
    private static Map<Board, List<List<Integer>>> transpositionsChildren = new HashMap<Board, List<List<Integer>>>();
    private int call_counter; // counts how many times minimax has been called

    /**
     * getDiff method
     * <p>
     * Getter for the difficulty of the computer
     *
     * @return {@code int} - The difficulty of the computer
     */
    public int getDiff() {
        return diff;
    }

    /**
     * deserializeTranspositions method
     * <p>
     * Gets stored transpositionEvals and transpositionChildren from disk
     *
     * @param path {@code String} - The path to the serialized data
     */
    public static void deserializeTranspositions(String path) {
        try {
            // declare variables
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj1, obj2;

            // read the objects
            obj1 = ois.readObject();
            obj2 = ois.readObject();

            transpositionsEvals = (Map<Board, Integer>) obj1;
            transpositionsChildren = (Map<Board, List<List<Integer>>>) obj2;

            System.out.println(transpositionsEvals.size() + transpositionsChildren.size() + " transpositions successfully loaded.\n");

            // close the streams
            ois.close();
            fis.close();
        }
        // catch exceptions
        catch (IOException | ClassNotFoundException | ClassCastException e) {
            transpositionsEvals = new HashMap<Board, Integer>();
            transpositionsChildren = new HashMap<Board, List<List<Integer>>>();
        }
    }

    /**
     * serializeTranspositions method
     * <p>
     * Gets stored transpositionEvals and transpositionChildren from disk
     *
     * @param path {@code String} - The path to the serialized data
     */
    public static void serializeTranspositions(String path) {
        try {
            // declare variables
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            // write the objects
            oos.writeObject(transpositionsEvals);
            oos.writeObject(transpositionsChildren);

            // flush and close the streams
            oos.flush();
            fos.flush();
            oos.close();
            fos.close();
        }
        // catch exceptions
        catch (IOException e) {
            e.printStackTrace();
        }
    }

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
     * Encodes an action into an array (pawn moves)
     *
     * @param pos {@code int[]} - The position of the move
     */
    private static int[] encodeAction(int[] pos) {
        return new int[]{0, pos[0], pos[1], 0};
    }

    /**
     * encodeAction method
     * <p>
     * Encodes an action into an array (wall placement)
     *
     * @param wall {@code state.component.Wall} - The wall to encode
     */
    private static int[] encodeAction(Wall wall) {
        return new int[]{1, wall.getX(), wall.getY(), wall.isVertical() ? 1 : 0};
    }

    /**
     * getAction method
     * <p>
     * Returns the move that the computer will make
     *
     * @param board {@code state.Board} - The current state of the board
     * @return {@code int[]} - The move that the computer will make
     */
    public int[] getAction(Board board) {
        if (diff == 0) {
            return getActionNormal(board);
        } else {
            return getActionHard(board);
        }
    }

    /**
     * getActionNormal method
     * <p>
     * Returns the move that the computer will make (normal difficulty - depth 1)
     *
     * @param board {@code state.Board} - The current state of the board
     * @return {@code int[]} - The move that the computer will make
     */
    private int[] getActionNormal(Board board) {
        // declare variables
        Pawn self = board.getCurrentPawn();
        Set<List<Integer>> validPawnMoves = board.calcValidPawnMoves(self);
        Set<Wall> validWallMoves = board.calcValidWallPlacements(self);
        int maxEval = Integer.MIN_VALUE;
        int currEval;
        int[] action = new int[5];
        boolean winning = false;

        // calculate the minimum distance with every pawn move
        for (List<Integer> move : validPawnMoves) {
            if (!winning) {
                self.moveTemp(new int[]{move.get(0), move.get(1)});

                currEval = eval(self.getId(), board);

                // check if this move immediately wins the game
                if (board.isGameOver()) {
                    maxEval = Integer.MAX_VALUE;
                    action = encodeAction(new int[]{move.get(0), move.get(1)});
                    winning = true;
                }

                // check if the move is better than the current best move
                else if (currEval > maxEval) {
                    maxEval = currEval;
                    action = encodeAction(new int[]{move.get(0), move.get(1)});
                }

                // reset the pawn position
                self.moveBackTemp();
            }
        }

        if (!winning) {
            // calculate the minimum distance with every wall placement
            for (Wall wall : validWallMoves) {
                // create a temporary board
                board.placeWallTemp(board.getPawn(wall.getOwner()), wall.getPos(), wall.isVertical());

                currEval = eval(self.getId(), board);

                // check if the move is better than the current best move
                if (currEval > maxEval) {
                    maxEval = currEval;
                    action = encodeAction(wall);
                }

                // remove the wall
                board.removeWallTemp(wall);
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
     * @param board {@code state.Board} - The current state of the board
     * @return {@code int[]} - The move that the computer will make
     */
    private int[] getActionHard(Board board) {
        // declare variables
        Pawn self = board.getCurrentPawn();
        final int SEARCH_DEPTH = 3;
        int[] algoEval;
        int[] action;

        // evaluate the move using the minimax algorithm
        System.out.print(" This may take a while...");
        algoEval = minimax(
                self.getId(),
                board,
                SEARCH_DEPTH - 1,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                new HashSet<Board>(),
                null
        );

        // decode the action
        action = new int[]{algoEval[1], algoEval[2], algoEval[3], algoEval[4]};

        return action;
    }

    /**
     * minimax method
     * <p>
     * Implements the minimax algorithm (with a-b pruning)
     *
     * @param maximizingPlayer {@code int} - The player to maximize (1 or 2)
     * @param position {@code state.Board} - The current state of the board
     * @param depth {@code int} - The depth of the search
     * @param alpha {@code int} - The alpha value for a-b pruning
     * @param beta {@code int} - The beta value for a-b pruning
     * @param visited {@code boolean} - The set of visited positions
     * @return {@code int[]} - The "best" move for the maximizing player along with its evaluation
     */
    private int[] minimax(int maximizingPlayer, Board position, int depth, int alpha, int beta, Set<Board> visited, int[] action) {
        // declare variables
        int[] evalActionPair = new int[5];
        int[] evalActionPairChild;
        int maxEval = Integer.MIN_VALUE;
        int minEval = Integer.MAX_VALUE;
        boolean alphaBetaPruned = false;

        // output a dot to indicate progress
        if (++call_counter % 100 == 0) System.out.print(".");

        // if the depth is 0, return the evaluation of the current position
        if (depth == 0) {
            evalActionPair = new int[]{eval(maximizingPlayer, position), action[0], action[1], action[2], action[3]};
        }

        // if the current player is the maximizing player, find the move with the maximum evaluation using recursion
        else if (position.getCurrentPlayer() == maximizingPlayer) {
            // loop over each possible move from the current position
            for (List<Integer> child : getChildren(position)) {
                // check if this branch has passed alpha and beta pruning
                if (!alphaBetaPruned) {
                    int[] actionChild = new int[]{child.get(0), child.get(1), child.get(2), child.get(3)};

                    // execute the action
                    position.doAction(actionChild);

                    // check if the maximizing player wins immediately on this turn
                    if (position.getPawn(maximizingPlayer).getY() ==
                            position.getPawn(maximizingPlayer).getYGoal()) {
                        visited.add(position);

                        maxEval = Integer.MAX_VALUE;
                        evalActionPair = new int[]{Integer.MAX_VALUE, actionChild[0], actionChild[1], actionChild[2], actionChild[3]};
                    }

                    // if the position has not been visited
                    else if (!visited.contains(position)) {
                        visited.add(position);

                        evalActionPairChild = minimax(
                                maximizingPlayer,
                                position,
                                depth - 1,
                                alpha,
                                beta,
                                visited,
                                action == null ? actionChild : action
                        );

                        // if the evaluation of the move is greater than the maximum evaluation,
                        // update the maximum evaluation
                        if (evalActionPairChild[0] > maxEval) {
                            maxEval = evalActionPairChild[0];
                            evalActionPair = new int[]{
                                    maxEval,
                                    // if the action is null, use the child action
                                    action == null ? actionChild[0] : action[0],
                                    action == null ? actionChild[1] : action[1],
                                    action == null ? actionChild[2] : action[2],
                                    action == null ? actionChild[3] : action[3]
                            };
                        }

                        // apply alpha-beta pruning
                        alpha = Math.max(alpha, evalActionPairChild[0]);
                        if (beta <= alpha) {
                            alphaBetaPruned = true;
                        }
                    }

                    // revert the action
                    position.undoAction(actionChild);
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

                    // execute the action
                    position.doAction(actionChild);

                    // check if the minimizing player wins immediately on this turn
                    if (position.getEnemy(position.getPawn(maximizingPlayer)).getY() ==
                            position.getEnemy(position.getPawn(maximizingPlayer)).getYGoal()) {
                        visited.add(position);

                        minEval = Integer.MIN_VALUE;
                        evalActionPair = new int[]{Integer.MIN_VALUE, actionChild[0], actionChild[1], actionChild[2], actionChild[3]};
                    }

                    // if the position has not been visited
                    else if (!visited.contains(position)) {
                        visited.add(position);

                        // recursively call the minimax function with the temporary board
                        evalActionPairChild = minimax(
                                maximizingPlayer,
                                position,
                                depth - 1,
                                alpha,
                                beta,
                                visited,
                                action == null ? actionChild : action // if the action is null, use the child action
                        );

                        // if the evaluation of the move is less than the maximum evaluation,
                        // update the maximum evaluation
                        if (evalActionPairChild[0] < minEval) {
                            minEval = evalActionPairChild[0];
                            evalActionPair = new int[]{
                                    minEval,
                                    // if the action is null, use the child action
                                    action == null ? actionChild[0] : action[0],
                                    action == null ? actionChild[1] : action[1],
                                    action == null ? actionChild[2] : action[2],
                                    action == null ? actionChild[3] : action[3]
                            };
                        }

                        // apply alpha-beta pruning
                        beta = Math.min(beta, evalActionPairChild[0]);
                        if (beta <= alpha) {
                            alphaBetaPruned = true;
                        }
                    }

                    // revert the action
                    position.undoAction(actionChild);
                }
            }
        }

        return evalActionPair;
    }

    /**
     * eval method
     * <p>
     * Evaluates the current state of the board as the difference between the distance from
     * the pawn to the goal and the distance from the enemy to their goal
     *
     * @param maximizingPlayer {@code int} - The player to maximize
     * @param position {@code state.Board} - The current state of the board
     * @return {@code int} - The evaluation of the board
     */
    private int eval(int maximizingPlayer, Board position) {
        // declare variables
        int maximizingPlayerDist;
        int minimizingPlayerDist;
        int value;

        // check if the evaluation for this position has already been calculated and put if absent
        if (transpositionsEvals.get(position) == null) {
            maximizingPlayerDist = position.calcDistanceToGoal(position.getPawn(maximizingPlayer));
            minimizingPlayerDist = position.calcDistanceToGoal(position.getEnemy(position.getPawn(maximizingPlayer)));
            value = minimizingPlayerDist - maximizingPlayerDist;

            transpositionsEvals.put(position.copy(), value);
        }

        // if the evaluation has already been calculated, get the value
        else value = transpositionsEvals.get(position);

        return value;
    }

    /**
     * getChildren method
     * <p>
     * 
     * Returns a list of all possible moves from the current position
     * @param position {@code state.Board} - The current state of the board
     * @return {@code List<List<Integer>>} - A list of all possible moves
     */
    private List<List<Integer>> getChildren(Board position) {
        // declare variables
        List<List<Integer>> children;
        int[] action;
        List<Integer> actionColl;

        // check if the children for this position have already been calculated and put if absent
        if (transpositionsChildren.get(position) == null) {
            // initialize a new ArrayList
            children = new ArrayList<List<Integer>>();

            // loop over every wall placement
            for (Wall wall : position.calcValidWallPlacements(position.getCurrentPawn())) {
                // encode it and add it to the list of children
                action = encodeAction(wall);
                actionColl = new ArrayList<Integer>();
                actionColl.add(action[0]);
                actionColl.add(action[1]);
                actionColl.add(action[2]);
                actionColl.add(action[3]);

                children.add(actionColl);
            }

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

            transpositionsChildren.put(position.copy(), children);
        }

        // if the children have already been calculated, get the value
        else children = transpositionsChildren.get(position);

        return children;
    }
}
