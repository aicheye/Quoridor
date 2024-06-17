package state;

import state.component.Pawn;
import state.component.Wall;
import state.util.MinHeap;

import java.io.Serializable;
import java.util.*;

/**
 * state.Board class
 * <p>
 * Represents the game board for the game of Quoridor
 *
 * @author Sean Yang
 * @version 30/05/2024
 */
public class Board implements Serializable {
    // constants
    private static final long serialVersionUID = 1L;
    private static final int SIZE = 9;
    private static final int MAX_WALLS = 10;
    private static final int[] P1_START = {4, 0};
    private static final int[] P2_START = {4, 8};

    // declare variables
    private transient int[][] squares = new int[9][9];
    private Pawn p1;
    private Pawn p2;
    private Set<Wall> walls = new HashSet<Wall>();
    private transient int[] wallsRemaining = new int[2];
    private int current;

    /**
     * state.Board constructor
     * <p>
     * Constructor for building a new state.Board
     *
     * @param p2Human {@code boolean} - True if player 2 is human, false if player 2 is computer
     */
    public Board(boolean p2Human) {
        // initialize squares to 0
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                squares[i][j] = 0;
            }
        }

        // initialize pawns
        p1 = new Pawn(1, P1_START, true);
        p2 = new Pawn(2, P2_START, p2Human);

        // set walls to 10 each
        wallsRemaining[0] = MAX_WALLS;
        wallsRemaining[1] = MAX_WALLS;

        // set pawn positions
        squares[p1.getX()][p1.getY()] = 1;
        squares[p2.getX()][p2.getY()] = 2;

        // set current player
        current = 1;
    }

    /**
     * state.Board constructor for loading a game
     * <p>
     * Constructor for building a new state.Board from a loaded game
     *
     * @param p1      {@code state.component.Pawn} - Player 1's pawn
     * @param p2      {@code state.component.Pawn} - Player 2's pawn
     * @param walls   {@code Set<state.component.Wall>}The list of walls on the board
     * @param current {@code int} The current player
     */
    public Board(Pawn p1, Pawn p2, Set<Wall> walls, int current) {
        // declare variables
        int p1WallsPlaced = 0;
        int p2WallsPlaced = 0;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                squares[i][j] = 0;
            }
        }

        this.p1 = p1.copy();
        this.p2 = p2.copy();

        squares[p1.getX()][p1.getY()] = 1;
        squares[p2.getX()][p2.getY()] = 2;

        for (Wall w : walls) {
            if (w.getOwner() == 1) p1WallsPlaced++;
            else p2WallsPlaced++;
        }

        wallsRemaining[0] = MAX_WALLS - p1WallsPlaced;
        wallsRemaining[1] = MAX_WALLS - p2WallsPlaced;

        this.walls = new HashSet<Wall>(walls);

        this.current = current;
    }

    /**
     * getSize method
     * <p>
     * Getter for the size of the board
     *
     * @return {@code int} - The size of the board (side length)
     */
    public static int getSize() {
        return SIZE;
    }

    /**
     * getMaxWalls method
     * <p>
     * Getter for max walls constant
     *
     * @return {@code int} - The maximum number of walls each player has
     */
    public static int getMaxWalls() {
        return MAX_WALLS;
    }

    /**
     * getP1 method
     * <p>
     * Getter for player 1's pawn
     *
     * @return {@code state.component.Pawn} - Player 1's pawn
     */
    public Pawn getP1() {
        return p1;
    }

    /**
     * getP2 method
     * <p>
     * Getter for player 2's pawn
     *
     * @return {@code state.component.Pawn} - Player 2's pawn
     */
    public Pawn getP2() {
        return p2;
    }

    /**
     * getPawn method
     * <p>
     * Converts an integer id to a {@code state.component.Pawn} object
     *
     * @param id {@code int} The id of the pawn
     * @return {@code state.component.Pawn} - A pawn object
     */
    public Pawn getPawn(int id) {
        Pawn p;

        if (id == 1) p = p1;
        else p = p2;

        return p;
    }

    /**
     * getEnemy method
     * <p>
     * Getter for the opposite player's pawn
     *
     * @param self {@code state.component.Pawn} - The player to check
     * @return {@code state.component.Pawn} - The opposite player
     */
    public Pawn getEnemy(Pawn self) {
        if (self.getId() == 1) return p2;
        else return p1;
    }

    /**
     * getCurrentPawn method
     * <p>
     * Getter for the currently active pawn
     *
     * @return {@code state.component.Pawn} - The current pawn
     */
    public Pawn getCurrentPawn() {
        return getPawn(current);
    }

    /**
     * getAllWalls method
     * <p>
     * Getter for the set of all walls placed on the current board
     *
     * @return {@code Set<state.component.Wall>} - The set of all walls
     */
    public Set<Wall> getAllWalls() {
        return walls;
    }

    /**
     * getWallsRemaining method
     * <p>
     * Getter for the number of walls remaining
     *
     * @param self {@code state.component.Pawn} - The pawn to check
     * @return {@code int} - The number of walls remaining for that pawn
     */
    public int getWallsRemaining(Pawn self) {
        return wallsRemaining[self.getId() - 1];
    }

    /**
     * getCurrentPlayer method
     * <p>
     * Getter for the current player
     *
     * @return {@code int} - The current player (1 or 2)
     */
    public int getCurrentPlayer() {
        return current;
    }

    /**
     * nextPlayer method
     * <p>
     * Switches the current player to the next player
     */
    public void nextPlayer() {
        if (current == 1) current = 2;
        else if (current == 2) current = 1;
    }

    /**
     * prevPlayer method
     * <p>
     * Switches the current player to the previous player
     */
    public void prevPlayer() {
        nextPlayer();
    }

    /**
     * sysOut method
     * <p>
     * Prints the board to the console
     */
    public void sysOut() {
        // declare constants
        final Map<String, String> markings = new HashMap<String, String>();
        markings.put("columns", "   a   b   c   d   e   f   g   h   i");
        markings.put("horiz-border", " -------------------------------------");

        // declare variables
        char[][] display = new char[SIZE * 2 - 1][SIZE * 4 - 3];
        int row = 9;

        // fill the array with whitespaces
        for (int x=0; x<SIZE * 4 - 3; x++) {
            for (int y=0; y<SIZE * 2 - 1; y++) {
                display[y][x] = ' ';
            }
        }

        // loop over the squares and set the display each board space
        for (int x=0; x<SIZE; x++) {
            for (int y=0; y<SIZE; y++) {
                switch (squares[x][y]) {
                    case 0:
                        display[y * 2][x * 4] = '.';
                        break;
                    case 1:
                        display[y * 2][x * 4] = 'O';
                        break;
                    case 2:
                        display[y * 2][x * 4] = 'X';
                        break;
                }
            }
        }

        // loop over each wall and set the display on the applicable squares
        for (Wall w: walls) {
            if (w.isVertical()) {
                for (int i=0; i<3; i++) {
                    display[(w.getY() - 1) * 2 + i][w.getX() * 4 + 2] = '|';
                }
            }
            else {
                for (int i=0; i<5; i++) {
                    display[(w.getY() - 1) * 2 + 1][w.getX() * 4 + i] = '-';
                }
            }
        }

        // loop over each valid pawn move for player 1 set the display for each square
        if (getCurrentPawn().isHuman()) {
            for (List<Integer> pos : calcValidPawnMoves(getCurrentPawn())) {
                display[pos.get(1) * 2][pos.get(0) * 4] = '~';
            }
        }

        // output the column labels and the border
        System.out.println("\n" + markings.get("columns"));
        System.out.println(markings.get("horiz-border"));

        // loop through the row and output each row
        for (int i=SIZE * 2 - 2; i>=0; i--) {
            // output the current row if it is not a blank square
            if (i % 2 == 0) System.out.print(row);
            else System.out.print(" ");
            // output the border
            System.out.print("| ");

            // output the current square
            for (char c : display[i]) {
                System.out.print(c);
            }

            // output the border
            System.out.print(" |");
            // output the current row and decrement the row
            if (i % 2 == 0) System.out.print(+ row--);
            System.out.print('\n');
        }

        // output the column labels and the border
        System.out.println(markings.get("horiz-border"));
        System.out.println(markings.get("columns"));
    }

    /**
     * validatePawnPos method
     * <p>
     * Checks if a pawn's position is valid
     *
     * @param pos {@code int[]} The position of the pawn
     * @return {@code boolean} - If the position is valid
     */
    public static boolean validatePawnPos(int[] pos) {
        return (pos[0] >= 0 && pos[0] < SIZE && pos[1] >= 0 && pos[1] < SIZE);
    }

    /**
     * calcNewPos method
     * <p>
     * Calculates a position given an old position and a direction
     *
     * @param pos {@code int[]} - The old position
     * @param dir {@code char} - The direction
     * @return {@code int[]} - The calculated position
     */
    private static int[] calcNewPos(int[] pos, char dir) {
        // new position
        int[] newPos = new int[2];

        // switch-case using the direction
        switch (dir) {
            case 'N':
                newPos[0] = pos[0];
                newPos[1] = pos[1] + 1;
                break;
            case 'E':
                newPos[0] = pos[0] + 1;
                newPos[1] = pos[1];
                break;
            case 'S':
                newPos[0] = pos[0];
                newPos[1] = pos[1] - 1;
                break;
            case 'W':
                newPos[0] = pos[0] - 1;
                newPos[1] = pos[1];
                break;
        }

        return newPos;
    }

    /**
     * calcDistanceToGoal method
     * <p>
     * Calculates the distance between the pawn and the goal
     *
     * @param self {@code state.component.Pawn} - The pawn that the computer is controlling
     * @return {@code int} - The distance from the pawn to the opposite side of the board.
     * Returns {@code Integer.MAX_INT} if the pawn is blocked
     * from reaching the opposite side of the board.
     */
    public int calcDistanceToGoal(Pawn self) {
        // declare variables
        int[][] dijkstra = new int[SIZE][SIZE];
        List<List<Integer>> queue = new ArrayList<List<Integer>>();
        Set<List<Integer>> visited = new HashSet<List<Integer>>();
        List<Integer> curr;
        int min = Integer.MAX_VALUE;
        Set<Character> dirs = new HashSet<Character>();
        dirs.add('N');
        dirs.add('E');
        dirs.add('S');
        dirs.add('W');
        int[] nextPos;
        List<Integer> nextPosList;

        // initialize every square to infinity
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                dijkstra[i][j] = Integer.MAX_VALUE;
            }
        }

        // initialize the starting square to 0
        dijkstra[self.getX()][self.getY()] = 0;

        // check if the state.component.Pawn is already at the goal
        if (self.getY() == self.getYGoal()) min = 0;

        else {
            // add the starting square to the queue
            nextPosList = new ArrayList<Integer>();
            nextPosList.add(self.getX());
            nextPosList.add(self.getY());

            queue.add(nextPosList);

            // keep looping until the queue is empty
            while (!queue.isEmpty()) {
                // get the next position
                curr = queue.remove(0);
                // add it to the visited array
                visited.add(curr);

                // test every direction the pawn can move in
                for (char dir : dirs) {
                    nextPos = calcNewPos(new int[]{curr.get(0), curr.get(1)}, dir);

                    nextPosList = new ArrayList<Integer>();
                    nextPosList.add(nextPos[0]);
                    nextPosList.add(nextPos[1]);

                    // if the new position is valid and there is no wall blocking the path
                    if (validatePawnPos(nextPos) &&
                            !isAnyWallBlocking(new int[]{curr.get(0), curr.get(1)}, dir) &&
                            !visited.contains(nextPosList)) {
                        queue.add(nextPosList);

                        // set the distance to the new square to the distance to the current square + 1 (if it is lower)
                        dijkstra[nextPos[0]][nextPos[1]] = Math.min(dijkstra[nextPos[0]][nextPos[1]], dijkstra[curr.get(0)][curr.get(1)] + 1);
                    }
                }
            }
        }

        // find the minimum distance to the goal
        for (int i = 0; i < SIZE; i++) min = Math.min(min, dijkstra[i][self.getYGoal()]);

        return min;
    }

    /**
     * propogateSquares method
     * <p>
     * Calculates the optimal path to the goal
     *
     * @param self {@code state.component.Pawn} - The pawn to calculate
     */
    public List<List<Integer>> propogateSquares(Pawn self) {
        // declare variables
        int[][] dijkstra = new int[SIZE][SIZE];
        List<List<Integer>> queue = new ArrayList<List<Integer>>();
        Set<List<Integer>> visited = new HashSet<List<Integer>>();
        List<Integer> curr;
        Set<Character> dirs = new HashSet<Character>();
        dirs.add('N');
        dirs.add('E');
        dirs.add('S');
        dirs.add('W');
        int[] nextPos = new int[2];
        List<Integer> nextPosList;

        // initialize priority queue
        MinHeap pq = new MinHeap(81);

        // stores an ArrayList of every square in the board ordered by distance from the goal
        List<List<Integer>> propogation = new ArrayList<List<Integer>>();

        // initialize every square to infinity and initialize ArrayLists
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                dijkstra[i][j] = Integer.MAX_VALUE;
            }
        }

        // initialize the starting square to 0
        dijkstra[self.getX()][self.getY()] = 0;

        // add the starting square to the queue
        nextPosList = new ArrayList<Integer>();
        nextPosList.add(self.getX());
        nextPosList.add(self.getY());

        queue.add(nextPosList);

        // keep looping until the queue is empty
        while (!queue.isEmpty()) {
            // get the next position
            curr = queue.remove(0);

            // add it to the visited array
            visited.add(curr);

            // test every direction the pawn can move in
            for (char dir : dirs) {
                nextPos = calcNewPos(new int[]{curr.get(0), curr.get(1)}, dir);

                nextPosList = new ArrayList<Integer>();
                nextPosList.add(nextPos[0]);
                nextPosList.add(nextPos[1]);

                // if the new position is valid and there is no wall blocking the path
                if (validatePawnPos(nextPos) &&
                        !isAnyWallBlocking(new int[]{curr.get(0), curr.get(1)}, dir) &&
                        !visited.contains(nextPosList)) {
                    queue.add(nextPosList);

                    // set the distance to the new square to the distance to the current square + 1 (if it is lower)
                    dijkstra[nextPos[0]][nextPos[1]] = Math.min(dijkstra[nextPos[0]][nextPos[1]], dijkstra[curr.get(0)][curr.get(1)] + 1);
                }
            }
        }

        // propogate the dijkstra array into the priority queue
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                pq.insert(new int[]{i, j}, dijkstra[i][j]);
            }
        }

        // extract from priority queue into propogation
        while (!Arrays.equals(nextPos, new int[]{-1, -1})) {
            // add the next position to the propogation
            nextPosList = new ArrayList<Integer>();
            nextPosList.add(nextPos[0]);
            nextPosList.add(nextPos[1]);

            // add the next position to the propogation
            propogation.add(nextPosList);

            // extract the next position
            nextPos = pq.extract();
        }

        return propogation;
    }

    /**
     * validatePawnMove method
     * <p>
     * Checks if a pawn move is valid
     *
     * @param self {@code state.component.Pawn} - The pawn to check
     * @param pos {@code int[]} - The position to check
     * @return {@code boolean} - Whether the pawn move is valid
     */
    private boolean validatePawnMove(Pawn self, int[] pos) {
        // declare variables
        boolean valid = false;
        List<Integer> posList = new ArrayList<Integer>();

        posList.add(pos[0]);
        posList.add(pos[1]);

        Set<List<Integer>> validMoves = calcValidPawnMoves(self);

        for (List<Integer> move : validMoves) if (move.equals(posList)) valid = true;

        return valid;
    }

    /**
     * calcValidPawnMoves method
     * <p>
     * Calculates all valid pawn moves
     *
     * @param self {@code state.component.Pawn} - The pawn to check
     * @return {@code Set<List<Integer>>>} - The valid squares a pawn can move to
     */
    public Set<List<Integer>> calcValidPawnMoves(Pawn self) {
        // declare variable
        Pawn other;
        Set<List<Integer>> moves = new HashSet<List<Integer>>();
        Set<Character> dirs = new HashSet<Character>();
        dirs.add('N');
        dirs.add('E');
        dirs.add('S');
        dirs.add('W');
        List<Integer> newPos;
        char[] jumpChecks = new char[2];

        // set the pawns
        if (self.getId() == 1) other = p2;
        else other = p1;

        // check each direction
        for (char dir : dirs) {
            if (!isAnyWallBlocking(self.getPos(), dir)) {
                // check if the new direction is not occupied by a pawn, and that it is a valid position
                if (!Arrays.equals(calcNewPos(self.getPos(), dir), other.getPos()) &&
                        validatePawnPos(calcNewPos(self.getPos(), dir))) {
                    // calculate the new position
                    newPos = new ArrayList<Integer>();
                    newPos.add(calcNewPos(self.getPos(), dir)[0]);
                    newPos.add(calcNewPos(self.getPos(), dir)[1]);

                    // add to moves
                    moves.add(newPos);
                }

                // check if the other pawn is at the same position
                else if (Arrays.equals(calcNewPos(self.getPos(), dir), other.getPos())) {
                    // check if there are no walls blocking the direction and it is a valid position
                    if (!isAnyWallBlocking(other.getPos(), dir) &&
                            validatePawnPos(calcNewPos(other.getPos(), dir))) {
                        // calculate the new position
                        newPos = new ArrayList<Integer>();
                        newPos.add(calcNewPos(other.getPos(), dir)[0]);
                        newPos.add(calcNewPos(other.getPos(), dir)[1]);

                        // add to moves
                        moves.add(newPos);
                    } else {
                        // new jumps to check
                        switch (dir) {
                            case 'N':
                            case 'S':
                                jumpChecks[0] = 'E';
                                jumpChecks[1] = 'W';
                                break;
                            case 'E':
                            case 'W':
                                jumpChecks[0] = 'N';
                                jumpChecks[1] = 'S';
                                break;
                        }

                        //iterate over jump checks
                        for (char jumpDir : jumpChecks) {
                            // check if there are no walls blocking the direction and it is a valid position
                            if (!isAnyWallBlocking(other.getPos(), jumpDir) &&
                                    validatePawnPos(calcNewPos(other.getPos(), jumpDir))) {
                                // calculate the new position
                                newPos = new ArrayList<Integer>();
                                newPos.add(calcNewPos(other.getPos(), jumpDir)[0]);
                                newPos.add(calcNewPos(other.getPos(), jumpDir)[1]);

                                // add to jumps
                                moves.add(newPos);
                            }
                        }
                    }
                }
            }
        }

        return moves;
    }

    /**
     * movePawn method
     * <p>
     * Moves a given pawn on the board
     *
     * @param self {@code state.component.Pawn} - The pawn to move
     * @param pos  {@code int[]} - The new position of the pawn
     * @return {@code boolean} - If the move is successful
     */
    public boolean movePawn(Pawn self, int[] pos) {
        // declare variables
        boolean success = true;

        // check if the new position is a valid move
        if (validatePawnMove(self, pos)) {
            squares[self.getX()][self.getY()] = 0;
            self.move(pos);
            squares[pos[0]][pos[1]] = self.getId();
        } else success = false;

        return success;
    }

    /**
     * validateWallPlace method (static)
     * <p>
     * Checks if a wall placement is valid on the current walls
     *
     * @param wall {@code state.component.Wall} - The wall placement to check
     * @return {@code boolean} - If the placement is valid
     */
    public static boolean validateWallPos(Wall wall) {
        // declare variables
        boolean valid = true;

        // check if the wall is out of bounds
        if (wall.getX() < 0 || wall.getX() >= SIZE - 1 ||
                wall.getY() <= 0 || wall.getY() >= SIZE) {
            valid = false;
        }

        return valid;
    }

    /**
     * isWallConflicting method
     * <p>
     * Checks if two walls are conflicting
     *
     * @param w1 {@code state.component.Wall} - The first wall
     * @param w2 {@code state.component.Wall} - The second wall
     * @return {@code boolean} - If they are conflicting
     */
    private static boolean isWallConflicting(Wall w1, Wall w2) {
        // declare variables
        boolean valid = false;
        int x1 = w1.getX();
        int y1 = w1.getY();
        int x2 = w2.getX();
        int y2 = w2.getY();
        boolean v1 = w1.isVertical();
        boolean v2 = w2.isVertical();

        if (v1 && v2) {
            if (x1 == x2 && y1 == y2) valid = true;
            else if (x1 == x2 && (y1 - y2 == 1 || y2 - y1 == 1)) valid = true;
        }

        else if (!v1 && !v2) {
            if (x1 == x2 && y1 == y2) valid = true;
            else if ((x1 - x2 == 1 || x2 - x1 == 1) && y1 == y2) valid = true;
        } else {
            if (x1 == x2 && y1 == y2) valid = true;
        }

        return valid;
    }

    public static boolean validateWall(Wall wall, Set<Wall> walls) {
        boolean valid = validateWallPos(wall);

        for (Wall w : walls) {
            if (isWallConflicting(wall, w)) valid = false;
        }

        return valid;
    }

    /**
     * isAnyWallBlocking method
     * <p>
     * Checks if any wall is blocking the desired move
     *
     * @param pos {@code int[]} - The start position
     * @param dir {@code char} - The direction to travel in
     * @return {@code boolean} - Whether a wall is blocking the direction
     */
    private boolean isAnyWallBlocking(int[] pos, char dir) {
        // declare variables
        boolean blocking = false;

        // iterate over all walls
        for (Wall w : walls) if (w.isBlocking(pos, dir)) blocking = true;

        return blocking;
    }

    /**
     * isWallBlockingPath method
     * <p>
     * Checks whether it is possible for a pawn to reach the end of the board if a wall is placed
     *
     * @param self {@code state.component.Pawn} - The pawn to check
     * @param wall {@code state.component.Wall} - The new wall to place
     * @return {@code boolean} - Whether the wall blocks the pawn's path to the opposite side of the board
     */
    public boolean isWallBlockingPath(Pawn self, Wall wall) {
        // declare variables
        boolean blocking;

        // temporarily add the new wall to the set of all walls
        walls.add(wall);

        blocking = calcDistanceToGoal(self) == Integer.MAX_VALUE;

        // remove the new wall from the set of all walls and return the pawn to the original position
        walls.remove(wall);

        return blocking;
    }

    /**
     * validateWallPlace method
     * <p>
     * Checks if a wall placement is valid on the current board
     *
     * @param owner    {@code state.component.Pawn} - The owner of the wall
     * @param pos      {@code int[]} - The position of the wall
     * @param vertical {@code boolean} - Whether the wall is vertical
     * @return {@code boolean} - If the placement is valid
     */
    public boolean validateWallPlace(Pawn owner, int[] pos, boolean vertical) {
        // declare variables
        Pawn other;
        if (owner.getId() == 1) other = getP2();
        else other = getP1();

        // check if the owner has any walls left and call validateWallPos
        return wallsRemaining[owner.getId() - 1] > 0 &&
                validateWall(new Wall(owner.getId(), pos, vertical), walls) &&
                !isWallBlockingPath(other, new Wall(owner.getId(), pos, vertical));
    }

    /**
     * calcValidWallPlacements method
     * <p>
     * Calculates all valid wall placements
     *
     * @param self {@code state.component.Pawn} - The pawn placing the walls
     * @return {@code Set<state.component.Wall>} - The valid wall placements
     */
    public Set<Wall> calcValidWallPlacements(Pawn self) {
        // declare variables
        Set<Wall> validWalls = new HashSet<Wall>();
        Wall wall;

        // iterate over all possible wall placements
        for (int i = 0; i < SIZE - 1; i++) {
            for (int j = 1; j < SIZE; j++) {
                wall = new Wall(self.getId(), new int[]{i, j}, true);
                if (validateWallPlace(self, new int[]{i, j}, true)) validWalls.add(wall);

                wall = new Wall(self.getId(), new int[]{i, j}, false);
                if (validateWallPlace(self, new int[]{i, j}, false)) validWalls.add(wall);
            }
        }

        return validWalls;
    }

    /**
     * placeWall method
     * <p>
     * Places a wall on the board
     *
     * @param owner {@code state.component.Pawn} - The owner of the wall
     * @param pos {@code int} - The position of the wall
     * @param vertical {@code boolean} - Whether the wall is vertical
     * @return {@code boolean} - If the wall is successfully placed
     */
    public boolean placeWall(Pawn owner, int[] pos, boolean vertical) {
        // declare variables
        boolean success = false;

        // check if the new position is a valid move
        if (validateWallPlace(owner, pos, vertical)) {
            walls.add(new Wall(owner.getId(), pos, vertical));
            wallsRemaining[owner.getId() - 1]--;
            success = true;
        }

        return success;
    }

    /**
     * placeWallTemp method
     * <p>
     * Places a wall without modifying wallsRemaining
     *
     * @param owner {@code state.component.Pawn} - The owner of the wall
     * @param pos {@code int[]} - The position of the wall
     * @param vertical {@code boolean} - Whether the wall is vertical
     */
    public void placeWallTemp(Pawn owner, int[] pos, boolean vertical) {
        // check if the new position is a valid move
        if (validateWallPlace(owner, pos, vertical)) {
            walls.add(new Wall(owner.getId(), pos, vertical));
        }
    }

    /**
     * removeWall method
     * <p>
     * Removes a wall from the set of walls
     *
     * @param wall {@code state.component.Wall} - The position of the wall to remove
     */
    public void removeWallTemp(Wall wall) {
        // declare variables
        boolean success = false;
        Wall toRemove = null;

        // check if the wall is in the set of walls
        for (Wall w : walls) {
            if (w.getOwner() == wall.getOwner() && Arrays.equals(w.getPos(), wall.getPos()) && w.isVertical() == wall.isVertical()) {
                toRemove = w;
                success = true;
            }
        }

        // remove the wall
        if (success) walls.remove(toRemove);
    }

    /**
     * doAction
     * <p>
     * Executes an action on the board
     *
     * @param action {@code int[]} - The instruction to execute
     */
    public void doAction(int[] action) {
        // the first element of the instruction is the type of instruction (0 for move, 1 for wall place)
        // execute a pawn move
        if (action[0] == 0) getCurrentPawn().moveTemp(new int[]{action[1], action[2]});

            // execute a wall placement
        else placeWallTemp(getCurrentPawn(), new int[]{action[1], action[2]}, action[3] == 1);

        // next player
        nextPlayer();
    }

    /**
     * undoAction
     * <p>
     * Reverts an action on the board
     *
     * @param action {@code int[]} - The instruction to revert
     */
    public void undoAction(int[] action) {
        // previous player
        prevPlayer();

        // revert a pawn move
        if (action[0] == 0) getCurrentPawn().moveBackTemp();

            // revert a wall placement
        else removeWallTemp(new Wall(current, new int[]{action[1], action[2]}, action[3] == 1));
    }

    /**
     * copy method
     * <p>
     * Returns a deep copy of the current board
     *
     * @return {@code Board} - The copy of the board
     */
    public Board copy() {
        return new Board(p1.copy(), p2.copy(), new HashSet<Wall>(walls), current);
    }

    /**
     * equals method
     * <p>
     * Checks if two boards are equal
     *
     * Note: THIS IS NOT A GOOD IMPLEMENTATION OF EQUALS!!!!
     * I AM ONLY USING THIS FOR DETECTING TRANSPOSITIONS.
     *
     * @param obj {@code Object} - The other board to compare
     */
    @Override
    public boolean equals(Object obj) {
        // declare variables
        boolean equal = true;
        Board board;

        // check for class equality
        if (!(obj instanceof Board)) equal = false;

            // check for field equality
        else {
            // create a board object to compare against
            board = (Board) obj;

            // compare each field
            if (p1.getId() != board.getP1().getId() || p2.getId() != board.getP2().getId()) equal = false;
            if (p1.getX() != board.getP1().getX() || p1.getY() != board.getP1().getY()) equal = false;
            if (p2.getX() != board.getP2().getX() || p2.getY() != board.getP2().getY()) equal = false;
            if (current != board.getCurrentPlayer()) equal = false;
            if (getAllWalls().size() != board.getAllWalls().size()) equal = false;
            if (equal) {
                for (Wall w : walls) {
                    if (!board.getAllWalls().contains(w)) equal = false;
                }
            }
        }

        return equal;
    }

    /**
     * hashCode method
     * <p>
     * Generates a hashCode for a {@code state.component.Wall} object
     * <p>
     * Note: THIS IS NOT A GOOD IMPLEMENTATION OF HASHCODE!!!!
     * I AM ONLY USING THIS FOR DETECTING TRANSPOSITIONS.
     *
     * @return {@code int} - The hash
     */
    @Override
    public int hashCode() {
        // declare variables
        Object[] hashArray = new Object[walls.size() + 7];
        int counter = 7;

        // add fields to hashArray
        hashArray[0] = p1.getId();
        hashArray[1] = p2.getId();
        hashArray[2] = p1.getX();
        hashArray[3] = p1.getY();
        hashArray[4] = p2.getX();
        hashArray[5] = p2.getY();
        hashArray[6] = current;

        for (Wall wall : walls) {
            hashArray[counter] = wall;
            counter++;
        }

        // return the java array hash code
        return Arrays.hashCode(hashArray);
    }
}
