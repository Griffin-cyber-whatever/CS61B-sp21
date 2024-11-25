package byow;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;

public class Render {
    int Width;
    int Height;
    boolean quit;
    boolean menu;
    World world;
    TERenderer renderer;

    public Render(int Width, int Height) {
        this.Width = Width;
        this.Height = Height;
        quit = false;
        menu = true;

        StdDraw.setCanvasSize(this.Width * 16, this.Height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.Width);
        StdDraw.setYscale(0, this.Height);
        StdDraw.clear();
        StdDraw.enableDoubleBuffering();
    }


    public void renderGame(){
        drawMenu();
        while (!quit) {
            String userInput;
            while (true){
                if (StdDraw.hasNextKeyTyped()) {
                    userInput = String.valueOf(StdDraw.nextKeyTyped());
                    break;
                }
            }
            System.out.println(userInput);
            // menu
            if (menu){
                switch (userInput) {
                    case "q" -> quit = true;
                    case "n" -> drawPrompt();
                    case "l" -> {
                    }
                }
            } else {
                switch (userInput) {
                    case "w" -> {
                        world.moveUp();
                        RenderWorld();
                    }
                    case "s" -> {
                        world.moveDown();
                        RenderWorld();
                    }
                    case "a" -> {
                        world.moveLeft();
                        RenderWorld();
                    }
                    case "d" -> {
                        world.moveRight();
                        RenderWorld();
                    }
                }
            }
        }
    }

    private void initializingWorld(Long seed){
        StdDraw.clear(StdDraw.GRAY);
        menu = false;
        this.world = new World(Width, Height, seed);
        renderer = new TERenderer();
        renderer.initialize(Width, Height);
        RenderWorld();
    }


    private void RenderWorld(){
        System.out.println("rendering world");
        renderer.renderFrame(world.getWorld());
    }

    private void drawPrompt(){
        StringBuilder sb = new StringBuilder();
        while(true){
            drawSeedPrompt(sb.toString());
            if (StdDraw.hasNextKeyTyped()) {
                String tmp = String.valueOf(StdDraw.nextKeyTyped());
                if (tmp.equals("S") || tmp.equals("s")) {
                    initializingWorld(Long.valueOf(sb.toString()));
                    break;
                } else {
                    sb.append(tmp);
                }
            }
        }
    }

    private void drawMenu(){
        StdDraw.clear();
        double midX = (double) this.Width / 2;

        Font titleFont = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(titleFont);
        StdDraw.setPenRadius(0.02);
        StdDraw.setPenColor(100, 208, 218);
        StdDraw.text(midX, (double) this.Height * 3 / 4, "CS61B: THE GAME");

        double spacing = 2;
        double startY = (double) this.Height / 2;
        Font menuFont = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(menuFont);
        StdDraw.setPenRadius(0.015);
        StdDraw.setPenColor(52, 178, 228);
        StdDraw.text(midX, startY, "New Game (N)");

        startY -= spacing;
        StdDraw.setPenColor(6, 83, 129);
        StdDraw.text(midX, startY, "Load Game (L)");

        startY -= spacing;
        StdDraw.setPenColor(139, 16, 62);
        StdDraw.text(midX, startY, "Quit (Q)");

        StdDraw.show();
    }

    private void drawSeedPrompt(String inputSeed){
        StdDraw.clear();
        double midX = (double) this.Width / 2;

        Font titleFont = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(titleFont);
        StdDraw.setPenRadius(0.02);
        StdDraw.setPenColor(100, 208, 218);
        StdDraw.text(midX, (double) this.Height * 3 / 4, "CS61B: THE GAME");

        double spacing = 2;
        double startY = (double) this.Height / 2;
        Font menuFont = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(menuFont);
        StdDraw.setPenRadius(0.015);
        StdDraw.setPenColor(52, 178, 228);
        StdDraw.text(midX, startY, "New Game (N)");

        startY -= spacing;
        StdDraw.setPenColor(52, 178, 228);
        StdDraw.text(midX, startY, "Seed: " + inputSeed);

        startY -= spacing;
        StdDraw.text(midX, startY, "Press (S) to confirm");

        startY -= spacing;
        StdDraw.setPenColor(139, 16, 62);
        StdDraw.text(midX, startY, "Load Game (L)");

        startY -= spacing;
        StdDraw.setPenColor(227, 72, 86);
        StdDraw.text(midX, startY, "Quit (Q)");

        StdDraw.show();
    }


}
