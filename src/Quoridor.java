import java.util.*;
import java.io.*;

/**
 * Quoridor.java
 * <p>
 * Represents the game of Quoridor
 * @author Sean Yang
 * @version 30/05/2024
 */
public class Quoridor {
    private static final Map<String, String> REGEX_STRINGS = new HashMap<String, String>() {{
        put("player", "^[OX]\\s[a-i][1-9]:\\s((Human)|(Computer))$");
        put("walls", "^([1-9]|(1[0-9])|(20))\\s\\{O:\\s([1-9]|(10]))\\s,\\sX:\\s([1-9]|(10]))\\s}:$");
        put("wall", "^[|–]\\s[a-i][1-9]:\\s[OX]$");
    }};
    private static final int SIZE = Board.getSize();
    private static final int MAX_WALLS = Board.getMaxWalls();

    // declare variables
    static Scanner sc = new Scanner(System.in);
    static Board board;

    /**
     * posStrToArr method
     * <p>
     * Helper method to convert a string position (such as e7) to an integer array
     *
     * @param posStr The string to convert
     * @return int[] - The position as an array
     */
    private static int[] posStrToArr(String posStr) {
        int x = posStr.charAt(0) - 'a';
        int y = posStr.charAt(1) - '1';

        return new int[]{x, y};
    }

    /**
     * posArrToStr method
     * <p>
     * Helper method to convert an integer array to a string position (such as e7)
     *
     * @param posArr The array to convert
     * @return String - The position as a string
     */
    private static String posArrToStr(int[] posArr) {
        char x = (char) (posArr[0] + 'a');
        char y = (char) (posArr[1] + '1');

        return String.valueOf(x) + y;
    }


    /**
     * menu method
     * <p>
     * Outputs the main menu
     * @return char - The selection of the user
     */
    public static char mainMenu() {
        // declare variables
        Set<Character> options = new HashSet<Character>();
        options.add('N');
        options.add('L');
        options.add('H');
        options.add('Q');

        System.out.println("\n---------- Welcome to QuoridorJ! ----------\n");

        System.out.println(  "{     <N>ew Game                          }");
        System.out.println(  "{     <L>oad Save                         }");
        System.out.println(  "{     <H>elp                              }");
        System.out.println(  "{     <Q>uit                              }");

        System.out.println("\n-------------------------------------------\n");

        return validateInput(options);
    }

    private static char validateInput(Set<Character> options) {
        char sel;

        do {
            // take user input
            System.out.print("Enter a Selection > ");
            sel = Character.toUpperCase(sc.nextLine().charAt(0));

            // check if selection is invalid
            if (!options.contains(Character.toUpperCase(sel))) {
                System.out.println("**ERR: Please select a valid option.**\n");
            }
        } while (!options.contains(Character.toUpperCase(sel)));

        return sel;
    }

    /**
     * savesMenu method
     * <p>
     * Outputs the load game menu
     * @param showFiles Whether to show the files again
     * @return String - The save file chosen
     */
    public static String savesMenu(boolean showFiles) {
        // declare variables
        File folder = new File("./saves");
        File[] listOfSaves = folder.listFiles();

        List<String> saves = new ArrayList<String>();
        saves.add("Q");
        saves.add("q");

        String choice;
        String file;

        // loop over each file
        if (listOfSaves != null) {
            for (File save : listOfSaves) {
                if (save.isFile()) {
                    saves.add(save.getName().substring(0, save.getName().length() - 4)); // add to ArrayList
                }
            }
        }


        if (showFiles) {
            // output save menu
            System.out.println("\n--------- Select a Save to Load ----------\n");

            // loop over each save and output the name
            for (String s : saves) {
                if (!s.equals("Q") && !s.equals("q")) {
                    System.out.print("{     ");
                    System.out.printf("%-36s", "<" + s + ">");
                    System.out.println("}");
                }
            }

            System.out.println("\n-------------------------------------------\n");
        }

        // keep looping until we get a valid choice
        do {
            // take user input and return it if valid
            System.out.print("Enter a selection (or Q to quit) > ");
            choice = sc.nextLine();

            // check if it is a valid file
            if (!saves.contains(choice)) {
                System.out.println("*ERR: Please select a valid file (or Q to quit)**\n");
            }
        } while (!saves.contains(choice));

        // return the filename
        if (!(choice.equals("Q") || choice.equals("q"))) file = "./saves/" + choice + ".txt";
        else file = "Quit";

        return file;
    }

    /**
     * turnMenu method
     * <p>
     * Menu for selecting a turn option
     * @return the choice of the user
     */
    public static char turnMenu() {
        // declare variables
        Set<Character> options = new HashSet<Character>();
        options.add('M');
        options.add('P');
        options.add('F');
        options.add('S');

        // output menu
        System.out.println("\n---------- Make a Selection -----------\n");

        System.out.println(  "{     <M>ove your pawn                }");
        System.out.println(  "{     <P>lace a wall                  }");
        System.out.println(  "{     <F>orfeit                       }");
        System.out.println(  "{     <S>ve game and exit             }");

        System.out.println("\n---------------------------------------\n");

        // keep looping until we get a valid choice
        return validateInput(options);
    }

    /**
     * moveMenu method
     * <p>
     * Displays a menu to allow the user to choose a square to move to
     *
     * @param current The current player (1 or 2)
     * @return int[] - The position the user selects
     */
    public static int[] moveMenu(int current) {
        // declare variables
        String choice;
        Set<String> validMoves = new HashSet<String>();
        validMoves.add("q");
        int[] pos;

        // output menu
        System.out.println("\n----- Select a Square to Move to ------\n");

        for (List<Integer> move : board.calcValidPawnMoves(board.getPawn(current))) {
            System.out.print("{     <");
            System.out.print(posArrToStr(new int[]{move.get(0), move.get(1)}));
            System.out.printf(">%29s\n", "}");
            validMoves.add(posArrToStr(new int[]{move.get(0), move.get(1)}));
        }

        System.out.println("\n---------------------------------------\n");

        // keep looping until we get a valid choice
        do {
            // take user input
            System.out.print("Enter a selection (or Q to quit) > ");
            choice = sc.nextLine().toLowerCase();

            // check if it is a valid move
            if (!validMoves.contains(choice)) {
                System.out.println("*ERR: Please select a valid move (or Q to quit)**\n");
            }
        } while (!validMoves.contains(choice));

        // check if the user chose to quit the current operation
        if (!choice.equals("q")) pos = posStrToArr(choice);
        else pos = new int[]{-1, -1};

        return pos;
    }

    /**
     * wallMenu method
     * <p>
     * Displays a menu to allow the user to choose where to place
     *
     * @param current The current player
     * @return int[] - An array containing the x position of the wall, the y position of the wall, and whether the wall is vertical.
     * Returns an array filled with -1 if the user chooses to quit.
     */
    public static int[] wallMenu(int current) {
        // declare variables
        List<Set<List<Integer>>> allValidWalls = new ArrayList<Set<List<Integer>>>();
        List<Integer> newWall;
        int vertical = -1;
        char orientationChoice = ' ';
        String posChoice;
        List<String> allValidWallStrings = new ArrayList<String>();
        int[] userWall = new int[]{-1, -1, -1, -1};

        // check if the user has any remaining walls
        if (board.getWallsRemaining(board.getPawn(current)) <= 0) {
            System.out.println("**ERR: You have no walls remaining.**");
        } else {
            // initialize the vertical and horizontal parts of allValidWalls
            allValidWalls.add(new HashSet<List<Integer>>());
            allValidWalls.add(new HashSet<List<Integer>>());

            // loop over every position and check if both horizontal and vertical walls are possible
            for (int x = 0; x <= SIZE - 1; x++) {
                for (int y = 1; y <= SIZE; y++) {
                    // validate the vertical wall
                    if (board.validateWallPlace(new int[]{x, y}, true, board.getPawn(current))) {
                        newWall = new ArrayList<Integer>();
                        newWall.add(x);
                        newWall.add(y);
                        // add to ArrayList
                        allValidWalls.get(1).add(newWall);
                    }
                    // validate the horizontal wall
                    if (board.validateWallPlace(new int[]{x, y}, false, board.getPawn(current))) {
                        newWall = new ArrayList<Integer>();
                        newWall.add(x);
                        newWall.add(y);
                        // add to ArrayList
                        allValidWalls.get(0).add(newWall);
                    }
                }
            }

            // get the user's preferred orientation
            if (allValidWalls.get(1).size() == 0) {
                System.out.print("You can only place a horizontal wall. Continue (Y/N)? > ");
                if (Character.toUpperCase(sc.nextLine().charAt(0)) == 'Y') vertical = 0;
                // TODO: fix crash when sc.nextLine is empty
            }
            if (allValidWalls.get(0).size() == 0) {
                System.out.print("You can only place a vertical wall. Continue (Y/N)? > ");
                if (Character.toUpperCase(sc.nextLine().charAt(0)) == 'Y') vertical = 1;
            } else {
                // output menu
                System.out.println("\n-------- Select an Orientation --------\n");

                System.out.println("{     <V>ertical                      }");
                System.out.println("{     <H>oriziontal                   }");

                System.out.println("\n---------------------------------------\n");

                // take user input
                while (vertical == -1 && orientationChoice != 'Q') {
                    System.out.print("Enter a selection (or Q to quit) > ");
                    orientationChoice = Character.toUpperCase(sc.nextLine().charAt(0));

                    // validate input
                    if (orientationChoice == 'V') vertical = 1;
                    else if (orientationChoice == 'H') vertical = 0;
                    else System.out.println("**ERR: Please select a valid orientation (or Q to quit)**\n");
                }
            }

            // output the menu based on the given orientation (if the user has chosen to continue)
            if (vertical != -1) {
                // get all valid wall placements as strings
                for (List<Integer> move : allValidWalls.get(vertical)) {
                    allValidWallStrings.add(posArrToStr(new int[]{move.get(0), move.get(1)}));
                }

                // keep looping until we get a valid choice
                do {
                    // take user input
                    System.out.print("\nEnter the NW square of the wall you would like to place (or Q to quit) > ");
                    posChoice = sc.nextLine().toLowerCase();

                    // check if it is a valid move
                    if (!allValidWallStrings.contains(posChoice)) {
                        System.out.println("**ERR: You cannot place a wall on that square.**");
                    }
                } while (!allValidWallStrings.contains(posChoice));

                // create a new array to return
                userWall = new int[]{posStrToArr(posChoice)[0], posStrToArr(posChoice)[1], vertical, current};
            }
        }

        return userWall;
    }

    /**
     * load method
     * <p>
     * Loads a board from a save file
     * @param filename The file to load
     */
    public static void load(String filename) {
        // declare variables
        boolean valid = true;
        String line;
        char[] chars;
        String[] split;
        int[] p1Pos, p2Pos, wallPos;
        boolean p1Human, p2Human;
        Pawn p1 = null, p2 = null;
        int totWalls = 0, p1Walls = 11, p2Walls = 11, wallOwner;
        boolean wallVertical;
        HashSet<Wall> wallSet = new HashSet<Wall>();
        int current = -1;

        // declare BufferedReader object
        BufferedReader br;

        try {
            // initialize BufferedReader object
            br = new BufferedReader(new FileReader(filename));

            // read the first line
            line = br.readLine();

            // check if the line matches
            if (line.matches(REGEX_STRINGS.get("player"))) {
                // split the line into characters
                chars = line.toCharArray();

                // set the positions, and human status of the players
                p1Pos = posStrToArr(String.valueOf(chars[2]) + chars[3]);
                p1Human = chars[6] == 'H';

                // check if the pawn position is valid
                if (Board.validatePawnPos(p1Pos)) {
                    p1 = new Pawn(p1Pos, 1, p1Human); // initialize a new pawn object
                }
                else valid = false;
            }
            else {valid=false;}

            // read the second line
            line = br.readLine();

            // check if the line matches
            if (line.matches(REGEX_STRINGS.get("player")) && valid) {
                // split the line into characters
                chars = line.toCharArray();

                // set the symbols, positions, and human status of the players
                p2Pos = posStrToArr(String.valueOf(chars[2]) + chars[3]);
                p2Human = chars[6] == 'H';

                // check if the pawn position is valid
                if (Board.validatePawnPos(p2Pos)) {
                    p2 = new Pawn(p2Pos, 2, p2Human); // initialize a new pawn object
                }
                else valid = false;
            }
            else {valid=false;}

            // read the fourth line
            br.readLine();
            line = br.readLine();

            // check if the line matches
            if (line.matches(REGEX_STRINGS.get("walls")) && valid) {
                // split the line into characters
                split = line.split("\\s");

                // set the number of totWalls
                try {
                    totWalls = Integer.parseInt(String.valueOf(split[0]));
                }
                catch (NumberFormatException e) {valid= false;}

                // set the number of totWalls placed by player 1
                try {
                    p1Walls = Integer.parseInt(String.valueOf(split[2]));
                }
                catch (NumberFormatException e) {valid= false;}

                // set the number of totWalls placed by player 2
                try {
                    p2Walls = Integer.parseInt(String.valueOf(split[5]));
                }
                catch (NumberFormatException e) {valid= false;}

                // check if the number of totWalls is valid
                if (valid &&
                        (p1Walls + p2Walls != totWalls ||
                        p1Walls > MAX_WALLS || p2Walls > MAX_WALLS ||
                        totWalls > MAX_WALLS * 2)) {
                    valid = false;
                }
            }
            else {valid = false;}

            // loop over all walls
            for (int i=0; i<totWalls; i++) {
                // read the next line
                line = br.readLine();

                if (line.matches(REGEX_STRINGS.get("wall")) && valid) {
                    // split the line into characters
                    chars = line.toCharArray();

                    // set the orientation, position, and owner of the wall
                    wallVertical = chars[0] == '|';
                    wallPos = posStrToArr(String.valueOf(chars[2]) + chars[3]);
                    wallOwner = chars[6] == 'O' ? 1 : 2;

                    // add the wall to the list if it is valid
                    if (Board.validateWallPos(new Wall(wallPos, wallVertical, wallOwner), wallSet)) {
                        wallSet.add(new Wall(wallPos, wallVertical, wallOwner));
                    }
                }
                else {valid = false;}
            }

            // loop over the walls
            for (Wall w : wallSet) {
                if (w.getOwner() == 1) p1Walls--;
                else p2Walls--;
            }

            // check if the number of walls placed matches
            if (p1Walls != 0 || p2Walls != 0) valid = false;

            // get the current player
            br.readLine();
            line = br.readLine();
            if (line.matches("^Current Move: Player [12]$")) current = Integer.parseInt(String.valueOf(line.charAt(21)));
            else valid = false;


            // initialize the board if the load is valid
            if (valid && p1 != null && p2 != null && wallSet.size() > 0) {
                board = new Board(p1, p2, wallSet, current);
                System.out.println("File loaded successfully.");
            }
            else {
                System.out.println("**ERR: Invalid save file**");
                load(savesMenu(false));
            }
        }

        catch (IOException e) {
            System.out.println("**ERR: File read error (does this file exist?)**\n");
        }
    }

    /**
     * gameLoop method
     * <p>
     * Handles the main game loop
     * @return int - The winner of the game
     */
    public static int gameLoop() {
        int winner = -1;
        boolean gameEnd = false;

        // loops until the game ends
        while (!gameEnd) {
            // make the next turn
            turn(board.getCurrentPlayer());

            // next player
            board.nextPlayer();

            // return the current winner if the game ends
            if ((winner = checkWinner()) != -1) gameEnd = true;
        }

        return winner;
    }

    /**
     * turn method
     * <p>
     * Handles each turn
     * @param current The current player
     */
    public static void turn(int current) {
        // declare variables
        boolean menuSuccess = false;
        char menuChoice = ' ';
        int[] pawnMove = new int[2];
        int[] wallPlace = new int[3];

        if (board.getPawn(current).isHuman()) {
            // output the board
            board.out();

            // switch-case for the turn menu
            while (!menuSuccess) {
                switch (menuChoice = turnMenu()) {
                    case 'M':
                        pawnMove = moveMenu(current);
                        if (pawnMove[0] != -1) menuSuccess = true;
                        break;
                    case 'P':
                        wallPlace = wallMenu(current);
                        if (wallPlace[0] != -1) menuSuccess = true;
                        break;
                    case 'S':
                        System.out.println("user selected to save the game");
                        menuSuccess = true;
                        break;
                    case 'F':
                        System.out.println("user selected to forfeit");
                        menuSuccess = true;
                        break;
                    default:
                        System.out.println("**ERR: Please select a valid option.*\n");
                        break;
                }
            }

            switch (menuChoice) {
                case 'M':
                    if (!board.movePawn(board.getPawn(current), pawnMove))
                        System.out.println("**ERR: an unexpected issue has occurred when trying to move the pawn**");
                    break;
                case 'P':
                    if (!board.placeWall(new int[]{wallPlace[0], wallPlace[1]}, wallPlace[2] == 1, board.getPawn(wallPlace[3])))
                        System.out.println("**ERR: an unexpected issue has occurred when trying to place a wall**");
                    break;
                case 'S':
                    System.out.println("user selected to save the game");
                    break;
                case 'F':
                    System.out.println("user selected to forfeit");
                    break;
                default:
                    System.out.println("*ERR: Please select a valid option.*\n");
                    break;
            }
        }
    }

    /**
     * checkWinner method
     * <p>
     * Checks if the game is over
     * @return int - The winner of the game (-1 if the game is not over)
     */
    public static int checkWinner() {
        int winner = -1;

        if (board.getP1().getY() == SIZE - 1) winner = 1;
        else if (board.getP2().getY() == 0) winner = 2;

        return winner;
    }

    /**
     * main method
     * <p>
     * Main driver code for the entire game
     */
    public static void main(String[] args) {
        // declare variables
        boolean quit = false;
        int winner;
        String file;

        // output main menu
        System.out.println("\n  ___                   _     _            \n" +
                " / _ \\ _   _  ___  _ __(_) __| | ___  _ __ \n" +
                "| | | | | | |/ _ \\| '__| |/ _` |/ _ \\| '__|\n" +
                "| |_| | |_| | (_) | |  | | (_| | (_) | |   \n" +
                " \\__\\_\\\\__,_|\\___/|_|  |_|\\__,_|\\___/|_|");

        while (!quit) {
            // switch statement for menu selection
            switch (mainMenu()) {
                case 'N':
                    board = new Board(true);
                    break;
                case 'L':
                    file = savesMenu(true);
                    if (!file.equals("Quit")) load(file);
                    break;
                case 'H':
                    System.out.println("user selected help");
                    break;
                case 'Q':
                    System.out.println("user selected quit");
                    quit = true;
                    break;
            }

            if (!quit) {
                // begin the game loop
                winner = gameLoop();
                System.out.printf("Player %d has won.\n", winner);
            }
        }
    }
}
