package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //TODO: Initialize random number generator
        rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        //TODO: Generate random string of letters of length n
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(CHARACTERS[rand.nextInt(CHARACTERS.length)]);
        }
        return sb.toString();
    }

    public void drawFrame(String s) {
        // Clear the screen for a new frame
        StdDraw.clear();

        // Centered game over message
        if (gameOver) {
            String end = "Game Over! You made it to round: " + round;
            StdDraw.text((double) this.width / 2, (double) this.height / 2, end);
            StdDraw.show();
            return;
        }

        // Draw game information (top UI)
        String rounds = "Round: " + round;
        String tmp = ENCOURAGEMENT[rand.nextInt(ENCOURAGEMENT.length)];
        double padding = 1; // Padding for text positioning

        // Top-left: Round information
        StdDraw.text(padding * tmp.length() / 2, this.height - padding, rounds);
        System.out.println(rounds.length() + "" + padding * rounds.length() / 2);

        // Top-center: Player status (Type/Watch)
        if (playerTurn) {
            StdDraw.text((double) this.width / 2, this.height - padding, "Type!");
        } else {
            StdDraw.text((double) this.width / 2, this.height - padding, "Watch!");
        }

        // Top-right: Encouragement text
        StdDraw.text(this.width - padding * tmp.length() / 2, this.height - padding, tmp);

        // Draw boundary line
        StdDraw.line(0, this.height - 2 * padding, this.width, this.height - 2 * padding);

        // Display the main text in the center of the screen
        StdDraw.text((double) this.width / 2, (double) this.height / 2, s);

        // Adjust the delay based on whether the string is empty
        if (s.isEmpty()) {
            StdDraw.show(500);
        } else {
            StdDraw.show(1000);
        }
    }


    public void flashSequence(String letters) {
        //TODO: Display each character in letters, making sure to blank the screen between letters
        for (char i : letters.toCharArray()) {
            drawFrame(String.valueOf(i));
            StdDraw.show(500);
            StdDraw.clear();
        }
    }

    public String solicitNCharsInput(int n) {
        // StringBuilder to store the user's input
        StringBuilder sb = new StringBuilder();

        // Continue until we collect exactly n characters
        while (sb.length() < n) {
            drawFrame(sb.toString());
            // Check if a key has been typed
            if (StdDraw.hasNextKeyTyped()) {
                // Append the typed character to the result
                sb.append(StdDraw.nextKeyTyped());
            }
        }

        // Indicate input is complete (optional)
        System.out.println("Input complete: " + sb.toString());
        return sb.toString();
    }


    public void startGame() {
        //TODO: Set any relevant variables before the game starts

        //TODO: Establish Engine loop
        round = 1;
        while (!gameOver) {
            drawFrame("Round: " + round);
            String randomString = generateRandomString(round);
            flashSequence(randomString);
            playerTurn = true;
            String player = solicitNCharsInput(round);
            drawFrame(player);
            System.out.println(player + "player complete");
            if (player.equals(randomString)) {
                playerTurn = false;
                round++;
            } else {
                gameOver = true;
            }
        }
        drawFrame("Game Over!");
    }

}
