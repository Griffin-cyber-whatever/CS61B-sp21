package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */

// Each hexagon has a height of 2 * S, a max width of 2 * S, and a minimum width of S
public class HexWorld {
    private int S; // Side length of the hexagon
    private int Width;
    private TETile[][] world;
    private long SEED;
    private Random RANDOM;

    public HexWorld(int seed, int S) {
        this.SEED = seed;
        this.S = S;
        this.RANDOM = new Random(SEED);
        this.Width = (S * 3) - 2;
        this.world = new TETile[3 * Width + 2 * S][10 * S];
        for (int x = 0; x < 3 * Width + 2 * S; x++) {
            for (int y = 0; y < 10 * S; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    // Use tile to represent which type of tile you want to have in that hexagon
    // The starting point is the leftmost bottom point of the entire hexagon
    private void draw(TETile tile, int startX, int startY) {
        if(startX < 0 || startX + Width > world.length || startY < 0 || startY + 2 * S > world[0].length) {
            return;
        }
        // The top part of the hexagon
        for (int y = 0; y < S; y++) {
            for (int x = S - y - 1; x < Width - S + y + 1; x++) {
                int worldX = startX + x;
                int worldY = startY + y;
                if (worldX >= 0 && worldX < world.length && worldY >= 0 && worldY < world[0].length) {
                    world[worldX][worldY] = tile;
                }
            }
        }

        // The bottom part of the hexagon
        for (int y = S; y < 2 * S; y++) {
            for (int x = y - S; x < Width - y + S; x++) {
                int worldX = startX + x;
                int worldY = startY + y;
                if (worldX >= 0 && worldX < world.length && worldY >= 0 && worldY < world[0].length) {
                    world[worldX][worldY] = tile;
                }
            }
        }
    }

    // Find the possible adjacent hexagon starting point
    // tmp[0] - left x
    // tmp[1] - right x
    // tmp[2] - both y
    private int[] adjacent(int startX, int startY) {

        int length = world.length;
        // y position
        int bothY = startY + S;
        if (bothY > world[0].length) {
            return null;
        }

        int leftX = startX - 2 * S + 1;
        int rightX = startX + 2 * S - 1;

        if (leftX < 0 || leftX + Width > world.length) {
            return new int[]{rightX, bothY};
        } else if (rightX < 0 || rightX + Width > world.length + S -1) {
            return new int[]{leftX, bothY};
        } else if (leftX > 0 && leftX + Width < world.length && rightX > 0 && rightX + Width < world.length + S -1) {
            return new int[]{leftX, rightX, bothY};
        }
        return null;
    }

    private void drawRecursive(int startX, int startY) {
        if (startY + 2 * S > world[0].length) return;
        TETile tile = random();
        draw(tile, startX, startY);
        int[] tmp = adjacent(startX, startY);
        if (tmp != null) {
            if (tmp.length == 2){
                drawRecursive(tmp[0], tmp[1]);
            }
            else {
                drawRecursive(tmp[0], tmp[2]);
                drawRecursive(tmp[1], tmp[2]);
            }
        }
    }

    private TETile random() {
        int tileNum = RANDOM.nextInt(4);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.WATER;
            default: return Tileset.GRASS;
        }
    }



    public static void main(String[] args) {
        int s = 5;
        int t = 3;
        HexWorld hexWorld = new HexWorld(s, t);

        int width = hexWorld.world.length;
        int height = hexWorld.world[0].length;
        TERenderer ter = new TERenderer();
        ter.initialize(width+t-1 , height+t-1);

        int starterX = width / 2 - t + 1;
        int starterY = 0;

        hexWorld.drawRecursive(starterX, starterY);

        ter.renderFrame(hexWorld.world);
    }
}
