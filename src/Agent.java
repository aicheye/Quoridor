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
    private static Map<Board, List<Integer>> transpositionsOptimals;
    private static Map<Board, Integer> transpositionsEvals;
    private static Map<Board, List<List<Integer>>> transpositionsChildren = new HashMap<Board, List<List<Integer>>>();
    private int searchDepth = 3;
    private int callCounter; // counts how many times minimax has been called

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
            // declare variables and open new streams for optimals
            FileInputStream fis = new FileInputStream(path + "optimals.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject(); // read the object

            transpositionsOptimals = (Map<Board, List<Integer>>) obj; // cast the object

            // close the streams
            ois.close();
            fis.close();

            // open new streams for evaluations
            fis = new FileInputStream(path + "evals.ser");
            ois = new ObjectInputStream(fis);
            obj = ois.readObject(); // read the object

            transpositionsEvals = (Map<Board, Integer>) obj; // cast the object

            // close the streams
            ois.close();
            fis.close();

            // open new streams for children
            fis = new FileInputStream(path + "children.ser");
            ois = new ObjectInputStream(fis);
            obj = ois.readObject(); // read the object

            transpositionsChildren = (Map<Board, List<List<Integer>>>) obj; // cast the object

            // close the streams
            ois.close();
            fis.close();

            System.out.printf("\n%d transpositions successfully loaded.\n",
                    transpositionsOptimals.size() + transpositionsEvals.size() + transpositionsChildren.size());

            // close the streams
            ois.close();
            fis.close();
        }
        // catch exceptions
        catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.out.println("\n**ERR: Unknown issue occured while loading transpositions. Generating new tables...**");

            transpositionsOptimals = new HashMap<Board, List<Integer>>();
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
            // declare variables and open new streams
            FileOutputStream fos = new FileOutputStream(path + "optimals.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(transpositionsOptimals); // write the optimal transpositions to disk

            // flush and close the streams
            oos.flush();
            fos.flush();
            oos.close();
            fos.close();

            // open new streams
            fos = new FileOutputStream(path + "evals.ser");
            oos = new ObjectOutputStream(fos);

            oos.writeObject(transpositionsEvals); // write the evaluations to disk

            // flush and close the streams
            oos.flush();
            fos.flush();
            oos.close();
            fos.close();

            // declare variables and open new streams
            fos = new FileOutputStream(path + "children.ser");
            oos = new ObjectOutputStream(fos);

            oos.writeObject(transpositionsChildren); // write the children to disk

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
     * getWallDiffThreshold method
     * <p>
     * Returns the wall difference threshold based on the number of walls remaining
     *
     * @param board {@code state.Board} - The current state of the board
     * @return {@code int} - The wall difference threshold
     */
    private int calcWallDiffThreshold(Board board) {
        // declare variables
        int wallsReamaining = board.getWallsRemaining(board.getCurrentPawn());
        int threshold;

        // calculate the threshold
        if (wallsReamaining == 10) threshold = 0;
        else if (wallsReamaining >= 7) threshold = 1;
        else if (wallsReamaining >= 3) threshold = 2;
        else threshold = 3;

        return threshold;
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
        Pawn self = board.getCurrentPawn();

        // hidden constant: adjusted based on the number of walls already on the board
        final int WALL_DIFF_THRESHOLD = calcWallDiffThreshold(board);

        // declare variables
        Set<List<Integer>> validPawnMoves = board.calcValidPawnMoves(self);
        Set<Wall> validWallMoves = board.calcValidWallPlacements(self);
        int selfCurrDist = board.calcDistanceToGoal(self);
        int enemyCurrDist = board.calcDistanceToGoal(board.getEnemy(self));
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
            if (selfCurrDist - board.calcDistanceToGoal(self) > maxPawnDifference) {
                maxPawnDifference = selfCurrDist - board.calcDistanceToGoal(self);
                maxPawnMove = new int[]{move.get(0), move.get(1)};
            }
            self.move(old);
        }

        // calculate the minimum distance with every wall placement
        for (Wall wall : validWallMoves) {
            board.placeWallTemp(board.getPawn(wall.getOwner()), wall.getPos(), wall.isVertical());

            // check if the current wall placement is better than the previous best
            if (board.calcDistanceToGoal(board.getEnemy(self)) - enemyCurrDist > maxWallDifference) {
                maxWallDifference = board.calcDistanceToGoal(board.getEnemy(self)) - enemyCurrDist;
                maxWall = new Wall(wall.getOwner(), wall.getPos(), wall.isVertical());
            }

            board.removeWallTemp(wall);
        }

        // prioritize moves which win immediately
        for (List<Integer> move : validPawnMoves) {
            if (move.get(1) == self.getYGoal()) {
                instruction = encodeAction(new int[]{move.get(0), move.get(1)});
                winning = true;
            }
        }

        // if there are no immediately winning moves, apply the threshold
        if (!winning) {
            if (maxWallDifference >= WALL_DIFF_THRESHOLD && maxWallDifference >= maxPawnDifference) {
                instruction = encodeAction(maxWall);
            } else {
                instruction = encodeAction(maxPawnMove);
            }
        }

        return instruction;
    }

    /**
     * calcSearchDepth
     * <p>
     * Calculates the search depth for the minimax algorithm based on two (arbitrary) factors:
     * 1. The number of walls remaining
     * 2. The distance of both pawnsto the goal
     *
     * @param board {@code state.Board} - The current state of the board
     * @return {@code int} - The search depth
     */
    private int calcSearchDepth(Board board) {
        // declare variables
        int wallsRemaining = board.getWallsRemaining(board.getCurrentPawn());
        wallsRemaining += board.getWallsRemaining(board.getEnemy(board.getCurrentPawn()));
        int searchDepth;

        // calculate the search depth
        if (wallsRemaining == 1 ||
                board.calcDistanceToGoal(board.getCurrentPawn()) <= 2 &&
                        board.calcDistanceToGoal(board.getEnemy(board.getCurrentPawn())) <= 2)
            searchDepth = 5;
        else if (board.calcDistanceToGoal(board.getCurrentPawn()) <= 4 &&
                board.calcDistanceToGoal(board.getEnemy(board.getCurrentPawn())) <= 4)
            searchDepth = 4;
        else
            searchDepth = 3;

        return searchDepth;
    }

    /**
     * getActionHard method
     * <p>
     * Returns the move that the computer will make (hard)
     * Implements the minimax algorithm (variable search depth of 3-5, with a-b pruning)
     *
     * @param board {@code state.Board} - The current state of the board
     * @return {@code int[]} - The move that the computer will make
     */
    private int[] getActionHard(Board board) {
        // declare variables
        Pawn self = board.getCurrentPawn();
        final int SEARCH_DEPTH = calcSearchDepth(board);
        searchDepth = SEARCH_DEPTH;
        int[] algoEval;
        int[] action;
        List<Integer> actionList;

        // check if there are any walls left
        if (board.getWallsRemaining(self) == 0) {
            action = beeline(board);
        }

        // check if this position has already been calculated
        else if (transpositionsOptimals.get(board) != null) {
            actionList = transpositionsOptimals.get(board);
            // convert the list to an array
            action = new int[]{actionList.get(0), actionList.get(1), actionList.get(2), actionList.get(3)};
        }

        // if the position has not been calculated, calculate it
        else {
            // check if the search depth is valid
            if (searchDepth > 0) {
                // evaluate the move using the minimax algorithm
                System.out.printf(" This may take a while (depth=%d)...", SEARCH_DEPTH);
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

                // if the minimax algorithm cannot make a move, revert to the normal computer
                if (Arrays.equals(action, new int[]{0, 0, 0, 0})) {
                    System.out.print("Error in minimax algorithm. Reverting to normal computer...");
                    action = getActionNormal(board);
                }
            }

            // if it is not valid, revert to the normal computer
            else {
                System.out.print("Error in minimax algorithm. Reverting to normal computer...");
                action = getActionNormal(board);
            }

            // put the optimal move in the transposition map
            transpositionsOptimals.put(
                    board.copy(),
                    Arrays.asList(action[0], action[1], action[2], action[3])
            );
        }

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
        final int DOT_INTERVAL = (int) (50 * (Math.pow(2, searchDepth - 2)));
        int[] evalActionPair = new int[5];
        int[] evalActionPairChild;
        int maxEval = Integer.MIN_VALUE;
        int minEval = Integer.MAX_VALUE;
        boolean alphaBetaPruned = false;

        // output a dot to indicate progress
        if (++callCounter % DOT_INTERVAL == 0) System.out.print(".");

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
        final int PAWNMOVE_HEURISTIC_LOC = 16; // heuristic location for pawn moves (arbitrary)
        List<List<Integer>> children;
        int[] action;
        List<Integer> actionColl;

        // check if the children for this position have already been calculated and put if absent
        if (transpositionsChildren.get(position) == null) {
            // initialize a new ArrayList
            children = new ArrayList<List<Integer>>();
            boolean pawnMovesCalculated = false;

            // loop over every wall placement
            for (List<Integer> pos : position.propagateSquares(position.getCurrentPawn())) {
                // initialize a vertical and horizontal wall object at this position and check if it is valid
                if (position.validateWallPlace(position.getCurrentPawn(), new int[]{pos.get(0), pos.get(1)}, true)) {
                    // encode it and add it to the list of children
                    action = encodeAction(new Wall(position.getCurrentPlayer(), new int[]{pos.get(0), pos.get(1)}, true));
                    actionColl = new ArrayList<Integer>();
                    actionColl.add(action[0]);
                    actionColl.add(action[1]);
                    actionColl.add(action[2]);
                    actionColl.add(action[3]);

                    children.add(actionColl);
                }

                if (position.validateWallPlace(position.getCurrentPawn(), new int[]{pos.get(0), pos.get(1)}, false)) {
                    // encode it and add it to the list of children
                    action = encodeAction(new Wall(position.getCurrentPlayer(), new int[]{pos.get(0), pos.get(1)}, false));
                    actionColl = new ArrayList<Integer>();
                    actionColl.add(action[0]);
                    actionColl.add(action[1]);
                    actionColl.add(action[2]);
                    actionColl.add(action[3]);

                    children.add(actionColl);
                }

                // if the size of the children is greater than the pawn heuristic, calculate the pawn moves
                if (children.size() >= PAWNMOVE_HEURISTIC_LOC && !pawnMovesCalculated) {
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

                    pawnMovesCalculated = true;
                }
            }

            // check if the pawn moves hae not yet been calculated
            if (!pawnMovesCalculated) {
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
            }

            transpositionsChildren.put(position.copy(), children);
        }

        // if the children have already been calculated, get the value
        else children = transpositionsChildren.get(position);

        return children;
    }

    /**
     * beeline method
     * <p>
     * Calculates the beeline path from the current position to the goal
     *
     * @param board {@code Pawn} - The pawn to calculate the path for
     * @return {@code int[]} - The next action
     */
    private int[] beeline(Board board) {
        // declare variables
        Pawn self = board.getCurrentPawn();
        int currDist = board.calcDistanceToGoal(self);
        int newDist;
        int delta;
        int maxDelta = Integer.MIN_VALUE;
        int[] old = self.getPos();
        int[] action = new int[5];

        // loop through every possible pawn move
        for (List<Integer> move : board.calcValidPawnMoves(self)) {
            // execute the move
            self.moveTemp(new int[]{move.get(0), move.get(1)});

            // calculate the distance to the goal
            newDist = board.calcDistanceToGoal(self);
            delta = currDist - newDist;

            // if the delta is greater than the maximum delta, update the maximum delta and store the best move
            if (delta > maxDelta) {
                maxDelta = delta;
                action = encodeAction(new int[]{move.get(0), move.get(1)});
            }

            // revert the move
            self.moveTemp(old);
        }

        return action;
    }
}
