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
    boolean controlMode;
    World world;
    TERenderer renderer;

    public Render(int Width, int Height) {
        this.Width = Width;
        this.Height = Height;
        quit = false;
        menu = true;
        controlMode = false;

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
            String userInput = nextNTypedKey(1);

            System.out.println(userInput);
            drawMovement(userInput);

            if (controlMode && (userInput.equals("q") || userInput.equals("Q"))) {
                world.save();
                //TODO read specification
                System.out.println("dwadwa ");
                menu = true;
                controlMode = false;
                drawMenu();
                continue;
            }

            // menu
            if (menu){
                switch (userInput) {
                    case "q" -> quit = true;
                    case "n" -> {
                        try {
                            drawPrompt();
                        } catch (Exception e) {
                            drawError(e.getMessage());
                            continue;
                        }
                    }
                    case "l" -> {
                        try {
                            World latestSave = World.load();
                            initializingWorld(latestSave);
                        } catch (Exception e) {
                            drawError(e.getMessage());
                            continue;
                        }
                    }
                }
            } else {
                switch (userInput) {
                    case "w" -> {
                        controlMode = false;
                        world.moveUp();
                        RenderWorld();
                    }
                    case "s" -> {
                        controlMode = false;
                        world.moveDown();
                        RenderWorld();
                    }
                    case "a" -> {
                        controlMode = false;
                        world.moveLeft();
                        RenderWorld();
                    }
                    case "d" -> {
                        controlMode = false;
                        world.moveRight();
                        RenderWorld();
                    }
                    case ":" -> {
                        controlMode = true;
                    }
                }
            }
        }
    }

    private String nextNTypedKey(int n){
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        while (counter < n){
            if (StdDraw.hasNextKeyTyped()) {
                sb.append(StdDraw.nextKeyTyped());
                counter ++;
            }
        }
        return sb.toString();
    }
    private void initializingWorld(World world){
        StdDraw.clear(StdDraw.GRAY);
        menu = false;
        this.world = world;
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
                    // TODO need to prove, long just plain parsing
                    World worldWithTypedSeed = new World(Width, Height, Long.valueOf(sb.toString()));
                    initializingWorld(worldWithTypedSeed);
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

    // add error message at the bottom of the screen
    private void drawError(String errorMessage){
        Font Errorfont = new Font("Monaco", Font.CENTER_BASELINE, 15);
        StdDraw.setFont(Errorfont);
        StdDraw.setPenRadius(0.005);
        StdDraw.setPenColor(Color.red);
        StdDraw.text(Width/2,2, errorMessage);
        StdDraw.show();
    }

    private void drawMovement(String movement){
        Font Errorfont = new Font("Monaco", Font.CENTER_BASELINE, 15);
        StdDraw.setFont(Errorfont);
        StdDraw.setPenRadius(0.005);
        StdDraw.setPenColor(77, 170, 131);
        StdDraw.textLeft(2,2, movement);
        StdDraw.show();
    }
}
