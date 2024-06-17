import state.Board;
import state.component.Pawn;
import state.component.Wall;

import java.util.*;
import java.io.*;

/**
 * Quoridor.java
 * <p>
 * Represents the game of Quoridor
 *
 * @author Sean Yang
 * @version 30/05/2024
 */
public class Quoridor {
    // declare constants
    private static final int SIZE = Board.getSize();
    private static final int MAX_WALLS = Board.getMaxWalls();
    private static final String TRANSPOSITIONS_PATH = "./transpositions.ser";

    // declare variables
    private static Scanner sc = new Scanner(System.in);
    private static Board board;
    private static Agent p2Agent;
    private static boolean abort = false;
    private static final Set<Character> yesNo = new HashSet<Character>(Arrays.asList('Y', 'N'));

    /**
     * posStrToArr method
     * <p>
     * Helper method to convert a {@code String} position (such as e7) to an {@code int[]}
     *
     * @param posStr {@code String} - The string to convert
     * @return {@code int[]} - The position as an array
     */
    private static int[] posStrToArr(String posStr) {
        int x = posStr.charAt(0) - 'a';
        int y = posStr.charAt(1) - '1';

        return new int[]{x, y};
    }

    /**
     * posArrToStr method
     * <p>
     * Helper method to convert an {@code int[]} to a {@code String} position (such as e7)
     *
     * @param posArr {@code int[]} - The array to convert
     * @return {@code String} - The position as a string
     */
    private static String posArrToStr(int[] posArr) {
        char x = (char) (posArr[0] + 'a');
        char y = (char) (posArr[1] + '1');

        return String.valueOf(x) + y;
    }

    /**
     * nextChar method
     * <p>
     * Helper method to allow inputs to get the first character of the user's input
     *
     * @return {@code char} - The option selected
     */
    private static char inputOneChar() {
        String input = sc.nextLine();
        char choice = ' ';
        if (input.length() > 0) choice = input.charAt(0);

        return Character.toUpperCase(choice);
    }

    /**
     * validateInput method
     * <p>
     * Helper method to validate the user's input
     *
     * @param output  {@code String} - Whether to output a selection menu
     * @param options {@code Set<Character>} - The options to validate against
     * @return {@code char} - The selection of the user
     */
    private static char validateInput(String output, Set<Character> options) {
        char sel;

        do {
            if (output == null) System.out.print("Enter a Selection > ");
            else System.out.print(output);

            // take user input
            sel = inputOneChar();

            // check if selection is invalid
            if (!options.contains(Character.toUpperCase(sel))) {
                System.out.println("**ERR: Please select a valid option.**");
            }
        } while (!options.contains(Character.toUpperCase(sel)));

        return sel;
    }

    /**
     * validateInput method
     * <p>
     * Helper method to validate the user's input
     *
     * @param options {@code Set<Character>} - The options to validate against
     * @return {@code char} - The selection of the user
     */
    private static char validateInput(Set<Character> options) {
        char sel;

        sel = validateInput(null, options);

        return sel;
    }

    /**
     * menu method
     * <p>
     * Outputs the main menu
     *
     * @return {@code char} - The selection of the user
     */
    private static char mainMenu() {
        // declare variables
        Set<Character> options = new HashSet<Character>();
        options.add('N');
        options.add('L');
        options.add('H');
        options.add('Q');

        System.out.println("\n---------- Welcome to Quoridor! -----------\n");

        System.out.println(  "{     <N>ew Game                          }");
        System.out.println(  "{     <L>oad Save                         }");
        System.out.println(  "{     <H>elp                              }");
        System.out.println(  "{     <Q>uit                              }");

        System.out.println("\n-------------------------------------------\n");

        return validateInput(options);
    }

    /**
     * loadMenu method
     * <p>
     * Outputs the load game menu
     *
     * @param showFiles {@code boolean} - Whether to show the files again
     * @return {@code String} - The save file chosen
     */
    private static String loadMenu(boolean showFiles) {
        // declare variables
        final File folder = new File("./saves");
        File[] listOfSaves = folder.listFiles();
        List<String> saves = new ArrayList<String>();
        String input;
        int choice;
        String file = null;

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
            for (int i = 0; i < saves.size(); i++) {
                String s = saves.get(i);

                System.out.print("{     ");
                System.out.printf("%-36s", "<" + (i + 1) + "> " + s);
                System.out.println("}");
            }

            System.out.println("\n-------------------------------------------\n");
        }

        // keep looping until we get a valid choice
        while (file == null) {
            // take user input and return it if valid
            System.out.print("Enter a selection (or Q to quit) > ");
            input = sc.nextLine();

            try {
                choice = Integer.parseInt(input) - 1;

                // check if it is a valid file
                if (0 <= choice && choice < saves.size()) file = "./saves/" + saves.get(choice) + ".txt";
                    // output error
                else System.out.println("*ERR: Please select a valid file (or Q to quit)**");
            }
            // catch if the input string is not a number
            catch (NumberFormatException e) {
                // check if the user chose to quit the loop
                if (input.length() > 0 && Character.toUpperCase(input.charAt(0)) == 'Q') file = "Q";
                // output error
                else System.out.println("**ERR: Please select a valid file (or Q to quit)**");
            }
        }

        // return the filename
        return file;
    }

    /**
     * saveMenu method
     * <p>
     * Method to allow the user to choose the name of their save file
     *
     * @return {@code String} - the name of the file
     */
    private static String saveMenu() {
        // declare variables
        final File folder = new File("./saves");
        File[] listOfSaves = folder.listFiles();
        final String FILENAME_REGEX = "^[a-zA-Z0-9\\-_]*$";
        final String[] forbidden = new String[]{"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        String filename = "";
        String userString;
        boolean valid;

        List<String> saves = new ArrayList<String>();

        // loop over each file
        if (listOfSaves != null) {
            for (File save : listOfSaves) {
                if (save.isFile()) {
                    saves.add(save.getName().substring(0, save.getName().length() - 4)); // add to ArrayList
                }
            }
        }

        // keep looping until we have a valid filename
        while (filename.isEmpty()) {
            valid = true;

            // take user input
            System.out.println("\nName your save file below using alphanumeric, hyphens, and underscores");
            System.out.print("Enter between 4-32 characters (or Q to quit) > ");
            userString = sc.nextLine();

            if (userString.equalsIgnoreCase("Q")) filename = "Q";

            // check if the user string uses the permitted characters
            if (!userString.matches(FILENAME_REGEX)) {
                System.out.println("**ERR: Please only use permitted characters.**");
                valid = false;
            }

            // check if the user string is too short or too long
            if ((userString.length() < 3 || userString.length() > 32) && !userString.equalsIgnoreCase("Q")) {
                System.out.println("**ERR: Please enter between 4 and 32 characters.**");
                valid = false;
            }

            // check if the save file already exists
            for (String s : saves) {
                if (s.equals(userString)) {
                    System.out.println("This save file already exists.");
                    if (validateInput("Do you want to override your previous save (Y/N)? > ", yesNo) == 'Y')
                        valid = true;
                    else valid = false;
                }
            }

            // check if the file name is allowed in windows
            for (String s : forbidden) {
                if (s.equals(userString)) {
                    System.out.println("**ERR: This filename is not allowed.**");
                    valid = false;
                }
            }

            if (valid) filename = userString;
        }

        return filename;
    }

    /**
     * turnMenu method
     * <p>
     * Menu for selecting a turn option
     *
     * @return {@code char} - the choice of the user
     */
    private static char turnMenu() {
        // declare variables
        Set<Character> options = new HashSet<Character>();
        options.add('M');
        options.add('P');
        options.add('F');
        options.add('S');

        // output menu
        if (board.getP2().isHuman()) {
            System.out.printf("\n------ P%d (" + (board.getCurrentPlayer() == 1 ? "O" : "X") +
                    "): Make a Selection -------\n\n", board.getCurrentPlayer());
        } else {
            System.out.println("\n---------- Make a Selection -----------\n");
        }

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
     * @return {@code int[]} - The position the user selects
     */
    private static int[] moveMenu() {
        // declare variables
        int current = board.getCurrentPlayer();
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
                System.out.println("*ERR: Please select a valid move (or Q to quit)**");
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
     * Displays a menu to allow the user to place a wall on the board
     *
     * @return {@code int[]} - An array containing the x position of the wall, the y position of the wall, and whether the wall is vertical.
     * Returns an array filled with -1 if the user chooses to quit.
     */
    private static int[] wallMenu() {
        // declare variables
        int current = board.getCurrentPlayer();
        List<Set<List<Integer>>> allValidWalls = new ArrayList<Set<List<Integer>>>();
        List<Integer> newWall;
        int vertical = -1;
        char orientationChoice = ' ';
        String posChoice;
        List<String> allValidWallStrings = new ArrayList<String>();
        int[] userWall = new int[]{-1, -1, -1, -1};
        boolean quit = false;

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
                    if (board.validateWallPlace(board.getPawn(current), new int[]{x, y}, true)) {
                        newWall = new ArrayList<Integer>();
                        newWall.add(x);
                        newWall.add(y);
                        // add to ArrayList
                        allValidWalls.get(1).add(newWall);
                    }
                    // validate the horizontal wall
                    if (board.validateWallPlace(board.getPawn(current), new int[]{x, y}, false)) {
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
                if (validateInput("You can only place a horizontal wall. Continue (Y/N)? > ", yesNo) == 'Y')
                    vertical = 0;
                else quit = true;
            }
            if (allValidWalls.get(0).size() == 0) {
                if (validateInput("You can only place a vertical wall. Continue (Y/N)? > ", yesNo) == 'Y')
                    vertical = 1;
                else quit = true;
            }
            if (allValidWalls.get(1).size() > 0 && allValidWalls.get(0).size() > 0 && !quit) {
                // output menu
                System.out.println("\n-------- Select an Orientation --------\n");

                System.out.println("{     <V>ertical                      }");
                System.out.println("{     <H>oriziontal                   }");

                System.out.println("\n---------------------------------------\n");

                // take user input
                while (vertical == -1 && orientationChoice != 'Q') {
                    System.out.print("Enter a selection (or Q to quit) > ");
                    orientationChoice = inputOneChar();

                    // validate input
                    if (orientationChoice == 'V') vertical = 1;
                    else if (orientationChoice == 'H') vertical = 0;
                    else if (orientationChoice == 'Q') quit = true;
                    else System.out.println("**ERR: Please select a valid orientation (or Q to quit)**");
                }
            }

            // output the menu based on the given orientation (if the user has chosen to continue)
            if (vertical != -1 && !quit) {
                // get all valid wall placements as strings
                for (List<Integer> move : allValidWalls.get(vertical)) {
                    allValidWallStrings.add(posArrToStr(new int[]{move.get(0), move.get(1)}));
                }

                // keep looping until we get a valid choice
                do {
                    // take user input
                    System.out.print("Enter the NW square of the wall you would like to place (or Q to quit) > ");
                    posChoice = sc.nextLine().toLowerCase();

                    // check if it is a valid move
                    if (posChoice.length() > 0 && posChoice.charAt(0) == 'q') quit = true;
                    else if (!allValidWallStrings.contains(posChoice))
                        System.out.println("**ERR: You cannot place a wall on that square.**");
                } while (!allValidWallStrings.contains(posChoice) && !quit);

                // create a new array to return
                if (!quit)
                    userWall = new int[]{posStrToArr(posChoice)[0], posStrToArr(posChoice)[1], vertical, current};
            }
        }

        return userWall;
    }

    /**
     * load method
     * <p>
     * Loads a board from a save file
     *
     * @param filename {@code String} - The file to load
     */
    private static void load(String filename) {
        // regex strings
        final String PLAYERS_REGEX = "^[OX]\\s[a-i][1-9]:\\s((Human)|(Computer \\(difficulty: ((normal)|(hard))\\)))$";
        final String WALLS_REGEX = "^([0-9]|(1[0-9])|(20))\\s\\{O:\\s([0-9]|(10))\\s,\\sX:\\s([0-9]|(10))\\s}:$";
        final String WALL_REGEX = "^[|–]\\s[a-i][1-9]:\\s[OX]$";

        // declare variables
        boolean valid = true;
        String line;
        char[] chars;
        String[] split;
        int[] p1Pos, p2Pos, wallPos;
        boolean p1Human, p2Human;
        int p2AgentDiff = -1;
        Pawn p1 = null, p2 = null;
        int totWalls = -1, p1Walls = -1, p2Walls = -1, wallOwner;
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
            if (line.matches(PLAYERS_REGEX)) {
                // split the line into characters
                chars = line.toCharArray();

                // set the positions, and human status of the players
                p1Pos = posStrToArr(String.valueOf(chars[2]) + chars[3]);
                p1Human = chars[6] == 'H';

                // check if the pawn position is valid
                if (Board.validatePawnPos(p1Pos) && p1Human) {
                    p1 = new Pawn(1, p1Pos, true); // initialize a new pawn object
                }

                else valid = false;
            }
            else {valid=false;}

            // read the second line
            line = br.readLine();

            // check if the line matches
            if (line.matches(PLAYERS_REGEX) && valid) {
                // split the line into characters
                chars = line.toCharArray();

                // set the symbols, positions, and human status of the players
                p2Pos = posStrToArr(String.valueOf(chars[2]) + chars[3]);
                p2Human = chars[6] == 'H';

                // check if the pawn position is valid
                if (Board.validatePawnPos(p2Pos)) {
                    p2 = new Pawn(2, p2Pos, p2Human); // initialize a new pawn object

                    // get the agent difficulty if the player is a computer
                    if (!p2Human) {
                        if (chars[28] == 'n') p2AgentDiff = 0;
                        else if (chars[28] == 'h') p2AgentDiff = 1;
                    }

                    p2Agent = p2Human ? null : new Agent(p2AgentDiff); // initialize a new computer object
                }

                else valid = false;
            }
            else {valid=false;}

            // read the fourth line
            if (!br.readLine().isEmpty()) valid = false;
            line = br.readLine();

            // check if the line matches
            if (line.matches(WALLS_REGEX) && valid) {
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
                if (valid) {
                    // read the next line
                    line = br.readLine();

                    if (line.matches(WALL_REGEX)) {
                        // split the line into characters
                        chars = line.toCharArray();

                        // set the orientation, position, and owner of the wall
                        wallVertical = chars[0] == '|';
                        wallPos = posStrToArr(String.valueOf(chars[2]) + chars[3]);
                        wallOwner = chars[6] == 'O' ? 1 : 2;

                        // add the wall to the list if it is valid
                        if (Board.validateWallPos(new Wall(wallOwner, wallPos, wallVertical), wallSet)) {
                            wallSet.add(new Wall(wallOwner, wallPos, wallVertical));
                        }
                    } else {
                        valid = false;
                    }
                }
            }

            // loop over the walls
            for (Wall w : wallSet) {
                if (w.getOwner() == 1) p1Walls--;
                else p2Walls--;
            }

            // check if the number of walls placed matches
            if (p1Walls != 0 || p2Walls != 0) valid = false;

            // get the current player
            if (!br.readLine().isEmpty()) valid = false;
            line = br.readLine();
            if (line.matches("^Next Move: [OX]$")) current = line.charAt(11) == 'O' ? 1 : 2;
            else valid = false;

            // check for expected EOF
            if (br.readLine() != null) valid = false;


            // initialize the board if the load is valid
            if (valid && p1 != null && p2 != null && wallSet.size() <= MAX_WALLS * 2) {
                board = new Board(p1, p2, wallSet, current);
                System.out.println("File loaded successfully.");
            }
            else {
                System.out.println("**ERR: Invalid save file.**");
                load(loadMenu(false));
            }
        } catch (Exception e) {
            System.out.println("**ERR: Invalid save file - file read error.**");
        }
    }

    /**
     * save method
     * <p>
     * saves the game state in a file
     *
     * @param filename {@code String} - the name of the save file
     */
    private static void save(String filename) {
        try {
            // init buffered writer
            BufferedWriter bw = new BufferedWriter(new FileWriter("./saves/" + filename + ".txt", false));

            // write each pawn
            bw.write("O ");
            bw.write(posArrToStr(board.getP1().getPos()));
            bw.write(": ");
            bw.write(board.getP1().isHuman() ? "Human\n" : "Computer\n");

            bw.write("X ");
            bw.write(posArrToStr(board.getP2().getPos()));
            bw.write(": ");
            bw.write(board.getP2().isHuman() ? "Human\n\n" : "Computer\n\n");

            // write the walls
            bw.write(String.valueOf(MAX_WALLS * 2 - board.getWallsRemaining(board.getP1()) - board.getWallsRemaining(board.getP2())));
            bw.write(" {O: ");
            bw.write(String.valueOf(MAX_WALLS - board.getWallsRemaining(board.getP1())));
            bw.write(" , X: ");
            bw.write(String.valueOf(MAX_WALLS - board.getWallsRemaining(board.getP2())));
            bw.write(" }:\n");

            // write each wall
            for (Wall w : board.getAllWalls()) {
                bw.write(w.isVertical() ? "| " : "– ");
                bw.write(posArrToStr(w.getPos()));
                bw.write(": ");
                bw.write(w.getOwner() == 1 ? "O\n" : "X\n");
            }

            // write the current player
            bw.write("\nNext Move: ");
            bw.write(board.getCurrentPlayer() == 1 ? "O\n" : "X\n");

            // close bufferedwriter
            bw.close();
        } catch (IOException e) {
            System.out.println("**ERR: File write error (do you have permissions to write to this folder?)**");
        }
    }

    /**
     * gameLoop method
     * <p>
     * Handles the main game loop
     */
    public static void gameLoop() {
        boolean gameEnd = false;

        // loops until the game ends
        while (!gameEnd && !abort) {
            // make the next turn
            turn();

            // check if the game is over
            if (checkWinner() != -1) {
                gameEnd = true;
            }

            // next player
            board.nextPlayer();
        }
    }

    /**
     * turn method
     * <p>
     * Handles each turn
     */
    private static void turn() {
        // declare variables
        int current = board.getCurrentPlayer();
        boolean menuSuccess = false;
        char menuChoice = ' ';
        int[] pawnMove = new int[2];
        int[] wallPlace = new int[3];
        String file = "";

        // output the board
        board.sysOut();

        if (board.getPawn(current).isHuman()) {
            // switch-case for the turn menu
            while (!menuSuccess) {
                switch (menuChoice = turnMenu()) {
                    case 'M':
                        pawnMove = moveMenu();
                        if (pawnMove[0] != -1) menuSuccess = true;
                        break;
                    case 'P':
                        wallPlace = wallMenu();
                        if (wallPlace[0] != -1) menuSuccess = true;
                        break;
                    case 'S':
                        file = saveMenu();
                        if (!file.equals("Q")) menuSuccess = true;
                        break;
                    case 'F':
                        if (validateInput("Are you sure you want to forfeit (Y/N) ? > ", yesNo) == 'Y')
                            menuSuccess = true;
                        break;
                    default:
                        System.out.println("**ERR: Please select a valid option.*\n");
                        break;
                }
            }

            switch (menuChoice) {
                case 'M':
                    if (!board.movePawn(board.getPawn(current), pawnMove)) {
                        System.out.println("**ERR: an unexpected issue has occurred when trying to move your pawn.**");
                        abort = true;
                    }
                    break;
                case 'P':
                    if (!board.placeWall(board.getPawn(wallPlace[3]), new int[]{wallPlace[0], wallPlace[1]}, wallPlace[2] == 1)) {
                        System.out.println("**ERR: an unexpected issue has occurred when trying to place a wall.**");
                        abort = true;
                    }
                    break;
                case 'S':
                    save(file);
                    abort = true;
                    break;
                case 'F':
                    board.getPawn(current == 1 ? 2 : 1).move(new int[]{0, current == 1 ? 0 : 8});
                    break;
                default:
                    System.out.println("*ERR: Please select a valid option.*\n");
                    break;
            }
        }

        // computer's move
        else {
            // output display
            System.out.println("\n    _______  _______   _______   ________  ________  ________   _______   _______ \n" +
                    "  //       \\/       \\\\/       \\\\/        \\/    /   \\/        \\//       \\//       \\\n" +
                    " //        /        //        //         /         /        _//        //        /\n" +
                    "/       --/         /         //      __/        ///       //        _/        _/ \n" +
                    "\\________/\\________/\\__/__/__/\\\\_____/  \\_______// \\_____// \\________/\\____/___/ ");

            // get the move from the computer
            System.out.print("\nThinking...");
            int[] move = p2Agent.getAction(board);

            // check if the move is a wall or a pawn
            if (move[0] == 0) {
                // move the pawn
                if (board.movePawn(board.getCurrentPawn(), new int[]{move[1], move[2]})) {
                    System.out.println("\nThe computer has moved its pawn to " + posArrToStr(new int[]{move[1], move[2]}));

                    // speedbump for the user
                    System.out.print("Press [ENTER] to continue > ");
                    sc.nextLine();
                }
                // output an error message if the move is invalid
                else {
                    System.out.println("\n**ERR: An unexpected issue has occurred when trying to execute the computer's move.**");
                    abort = true;
                }
            } else {
                // place the wall
                if (board.placeWall(board.getCurrentPawn(), new int[]{move[1], move[2]}, move[3] == 1)) {
                    System.out.println("\nThe computer has placed a " + (move[3] == 1 ? "vertical" : "horizontal") + " wall at " + posArrToStr(new int[]{move[1], move[2]}));

                    // speedbump for the user
                    System.out.print("Press [ENTER] to continue > ");
                    sc.nextLine();
                }
                // output an error message if the wall placement is invalid
                else {
                    System.out.println("**\nERR: An unexpected issue has occurred when trying to execute the computer's placement.**");
                    abort = true;
                }
            }
        }
    }

    /**
     * checkWinner method
     * <p>
     * Checks if the game is over
     * @return {@code int} - The winner of the game (-1 if the game is not over)
     */
    private static int checkWinner() {
        int winner = -1;

        if (board.getP1().getY() == board.getP1().getYGoal()) winner = 1;
        else if (board.getP2().getY() == board.getP2().getYGoal()) winner = 2;

        return winner;
    }

    /**
     * main method
     * <p>
     * Main driver code for the entire game
     *
     * @param args {@code String[]} - The command line arguments
     */
    public static void main(String[] args) {
        // declare variables
        int winner;
        String file;
        char choice = ' ';

        // deserialize from file
        System.out.println("Deserializing transpositions...");
        Agent.deserializeTranspositions(TRANSPOSITIONS_PATH);

        // output main menu
        System.out.println("  ___                   _     _            \n" +
                " / _ \\ _   _  ___  _ __(_) __| | ___  _ __ \n" +
                "| | | | | | |/ _ \\| '__| |/ _` |/ _ \\| '__|\n" +
                "| |_| | |_| | (_) | |  | | (_| | (_) | |   \n" +
                " \\__\\_\\\\__,_|\\___/|_|  |_|\\__,_|\\___/|_|");

        while (!abort) {
            // switch statement for menu selection: keep looping until the board is instantiated
            while (board == null && !abort) {
                switch (mainMenu()) {
                    case 'N':
                        // ask for user confirmation to play against computer
                        if (validateInput("Do you want to play against a computer (Y/N)? > ", yesNo) == 'Y') {
                            board = new Board(false);

                            // output menu for the difficulty of the computer
                            System.out.println("\n----- Select a Difficulty -----\n");

                            System.out.println("{     <N>ormal                }");
                            System.out.println("{     <H>ard                  }");

                            System.out.println("\n-------------------------------\n");

                            choice = validateInput(new HashSet<Character>(Arrays.asList('N', 'H')));
                        }
                        else board = new Board(true);

                        p2Agent = board.getP2().isHuman() ? null : new Agent(choice == 'N' ? 0 : 1);

                        break;
                    case 'L':
                        file = loadMenu(true);
                        if (!file.equals("Q")) load(file);
                        break;
                    case 'H':
                        // output the rules
                        System.out.println("\n------------ Rules of the Game ------------\n");

                        System.out.println("{    Both you and your opponent have      }");
                        System.out.println("{    pawns (O and X). Your goal is to     }");
                        System.out.println("{    get your own pawn to the other       }");
                        System.out.println("{    side and block your opponent's       }");
                        System.out.println("{    pawn.                                }");
                        System.out.println("{                                         }");
                        System.out.println("{    Player 1 (O) starts at row 1.        }");
                        System.out.println("{    Player 2 (X) starts at row 9.        }");
                        System.out.println("{                                         }");
                        System.out.println("{    On each turn, you may either move    }");
                        System.out.println("{    your pawn or place a wall.           }");
                        System.out.println("{                                         }");
                        System.out.println("{    The squares which you can move to    }");
                        System.out.println("{    are represented using '~'. You may   }");
                        System.out.println("{    place a wall anywhere that is not    }");
                        System.out.println("{    occupied, but there must always be   }");
                        System.out.println("{    a valid path to the other side.      }");
                        System.out.println("{                                         }");
                        System.out.println("{    Try loading the exampleSave to see   }");
                        System.out.println("{    an example of a game.                }");

                        System.out.println("\n-------------------------------------------\n");

                        // speedbump
                        System.out.print("Press [ENTER] to go back > ");
                        sc.nextLine();
                        break;
                    case 'Q':
                        abort = true;
                        break;
                }
            }

            if (!abort) {
                // begin the game loop
                gameLoop();
                winner = checkWinner();

                // check if the game is pvp and output the win screens
                if (board.getP2().isHuman()) {
                    if (winner == 1) {
                        System.out.println("\n ____  _                         _  __        ___           _ \n" +
                                "|  _ \\| | __ _ _   _  ___ _ __  / | \\ \\      / (_)_ __  ___| |\n" +
                                "| |_) | |/ _` | | | |/ _ \\ '__| | |  \\ \\ /\\ / /| | '_ \\/ __| |\n" +
                                "|  __/| | (_| | |_| |  __/ |    | |   \\ V  V / | | | | \\__ \\_|\n" +
                                "|_|   |_|\\__,_|\\__, |\\___|_|    |_|    \\_/\\_/  |_|_| |_|___(_)\n" +
                                "               |___/                                          \n");
                    } else if (winner == 2) {
                        System.out.println("\n ____  _                         ____   __        ___           _ \n" +
                                "|  _ \\| | __ _ _   _  ___ _ __  |___ \\  \\ \\      / (_)_ __  ___| |\n" +
                                "| |_) | |/ _` | | | |/ _ \\ '__|   __) |  \\ \\ /\\ / /| | '_ \\/ __| |\n" +
                                "|  __/| | (_| | |_| |  __/ |     / __/    \\ V  V / | | | | \\__ \\_|\n" +
                                "|_|   |_|\\__,_|\\__, |\\___|_|    |_____|    \\_/\\_/  |_|_| |_|___(_)\n" +
                                "               |___/                                              \n");
                    }
                }

                // output generic win screens if it is not pvp
                else {
                    if (winner == 1) {
                        System.out.println("\n__   __           __        ___       _ \n" +
                                "\\ \\ / /__  _   _  \\ \\      / (_)_ __ | |\n" +
                                " \\ V / _ \\| | | |  \\ \\ /\\ / /| | '_ \\| |\n" +
                                "  | | (_) | |_| |   \\ V  V / | | | | |_|\n" +
                                "  |_|\\___/ \\__,_|    \\_/\\_/  |_|_| |_(_)\n");
                    } else if (winner == 2) {
                        System.out.println("\n__   __            _                         \n" +
                                "\\ \\ / /__  _   _  | |    ___  ___  ___       \n" +
                                " \\ V / _ \\| | | | | |   / _ \\/ __|/ _ \\      \n" +
                                "  | | (_) | |_| | | |__| (_) \\__ \\  __/_ _ _ \n" +
                                "  |_|\\___/ \\__,_| |_____\\___/|___/\\___(_|_|_)\n");
                    }
                }

                // speedbump
                if (!abort) {
                    System.out.print("Press [ENTER] to continue > ");
                    sc.nextLine();

                    // reset the board
                    board = null;
                }
            }
        }

        // serialize to file
        System.out.println("Serializing tranpositions...");
        Agent.serializeTranspositions(TRANSPOSITIONS_PATH);
    }
}
