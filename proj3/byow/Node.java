package byow;

import byow.TileEngine.TETile;

import java.io.Serializable;

public class Node implements Serializable {
    private TETile tile;
    private int x;
    private int y;
    private boolean isRoom;

    public Node(TETile tile, int x, int y) {
        this.tile = tile;
        this.x = x;
        this.y = y;
        this.isRoom = false;
    }

    public TETile getTile() {
        return tile;
    }


    public int getX() {
        return x;
    }

    public int getY(){
        return y;
    }

    public void setTile(TETile tile) {
        this.tile = tile;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        System.out.println(this.x + " " + this.y + " " + this);
    }

    public boolean isRoom() {
        return isRoom;
    }

    public void setRoom() {
        this.isRoom = true;
    }

    public double getDistance(Node n) {
        int OtherX = n.getX();
        int OtherY = n.getY();
        return Math.sqrt(Math.pow(OtherX - x, 2) + Math.pow(OtherY - y, 2));
    }
}
