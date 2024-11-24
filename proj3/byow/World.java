package byow;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;

import java.util.*;

/* my solution:
*   using seed to generate node for world, each node would be a room
*   using MST to connect all of them with hall
*   wrap room and hall with wall in the end*/
public class World {
    private Node[][] world;
    private Random rand;
    private int Width;
    private int Height;
    private ArrayList<Node> vertex;

    public World(int width, int height, long seed) {
        this.Width = width;
        this.Height = height;
        this.rand = new Random(seed);
        world = new Node[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = new Node(Tileset.NOTHING, x, y);
            }
        }
        vertex = new ArrayList<>();

        // total Room Area = CoverageFactor * width * height
        double CoverageFactor = 0.7;

        int MaxWidth = Math.max(1, width / 3);
        int MaxHeight = Math.max(1, height/3);
        int MinWidth = Math.max(1, width/5);
        int MinHeight = Math.max(1, height/5);
        int numberOfNodes = (int) Math.max(1, CoverageFactor * width * height * 4 / ((MaxWidth + MinWidth) * (MaxHeight + MinHeight)));

        // generate the world array
        NodeGenerator(numberOfNodes);
        RoomGenerator();
        generatePaths(getMST());
        wrapFloorWithWalls();
    }

    private boolean IsValidForRoom(int x, int y) {
        return x > 0 && x < world.length -1  && y > 0 && y < world[0].length -1 && world[x][y].getTile() == Tileset.NOTHING;
    }

    private boolean IsWallValid(int x, int y) {
        if (x < 0 || x >= world.length || y < 0 || y >= world[0].length || world[x][y].getTile() != Tileset.NOTHING){
            return false;
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int currentX = x + i;
                int currentY = y + j;
                if (currentX > 0 && currentX < world.length && currentY > 0 && currentY < world[0].length
                        && world[currentX][currentY].getTile() != Tileset.WALL && world[currentX][currentY].getTile() != Tileset.NOTHING) {
                    return true;
                }
            }
        }
        return false;
    }

    // Check if room placement is valid based on boundaries and room size
    private boolean IsRoomValid(int leftBoundary, int bottomBoundary, int width, int height) {
        int rightBoundary = leftBoundary + width;
        int topBoundary = bottomBoundary + height;

        // Ensure the room is within world boundaries and does not overlap other rooms
        if (rightBoundary > world.length || topBoundary > world[0].length) {
            return false;
        }

        for (int x = leftBoundary; x < rightBoundary; x++) {
            for (int y = bottomBoundary; y < topBoundary; y++) {
                if (!IsValidForRoom(x, y)) {
                    return false; // Found overlap or out of bounds
                }
            }
        }

        return true; // Room placement is valid
    }

    private void CreateRoom(int leftBoundary, int bottomBoundary, int width, int height) {
        for (int x = leftBoundary; x < leftBoundary + width; x++) {
            for (int y = bottomBoundary; y < bottomBoundary + height; y++) {
                if (IsValidForRoom(x, y)) {
                    world[x][y].setTile(Tileset.FLOOR);
                    world[x][y].setRoom();
                }
            }
        }
    }

    private void NodeGenerator(int numberOfNodes) {
        for (int x = 0; x < numberOfNodes; x++) {
            int tmpX = rand.nextInt(Width - 3) + 2;
            int tmpY = rand.nextInt(Height - 3) + 2;
            world[tmpX][tmpY].setTile(Tileset.FLOWER);
            vertex.add(world[tmpX][tmpY]);
        }
    }

    // generate room for different nodes in MST
    private void RoomGenerator() {
        for (Node n : vertex) {
            boolean roomPlaced = false;
            int attemptCount = 0; // To avoid infinite loop
            while (!roomPlaced && attemptCount < 100) {
                // Gradually decrease the room size range after each failed attempt
                int width = rand.nextInt(Width / 3 - Width / 5) + Width / 5;
                int height = rand.nextInt(Height / 3 - Height / 5) + Height / 5;

                // Gradually adjust the range of room sizes
                if (attemptCount > 50) {
                    width = rand.nextInt(Width / 4 - Width / 6) + Width / 6;
                    height = rand.nextInt(Height / 4 - Height / 6) + Height / 6;
                }

                int xOffset = n.getX();
                int yOffset = n.getY();

                // Try placing the room within adjusted bounds
                int leftBoundary = Math.max(0, xOffset - width / 2);
                int bottomBoundary = Math.min(Height - 1, yOffset - height / 2);

                // Check if room placement is valid
                if (IsRoomValid(leftBoundary, bottomBoundary, width, height)) {
                    CreateRoom(leftBoundary, bottomBoundary, width, height);
                    roomPlaced = true;
                }

                attemptCount++;
            }
        }
    }

    /*
    * This part will start connecting rooms with paths by using Prim's algorithm
    * */
    private class NodeGraph {
        public PriorityQueue<Edge> edges;
        public NodeGraph(ArrayList<Node> vertex) {
            edges = new PriorityQueue<>();
            for (int i = 0; i < vertex.size(); i++) {
                for (int j = i + 1; j < vertex.size(); j++) { // Avoid duplicate edges
                    Edge edge = new Edge(i, j, vertex);
                    edges.add(edge);
                }
            }
        }
        
        public PriorityQueue<Edge> getEdges() {
            return edges;
        }
        
    }

    private ArrayList<Edge> getMST() {
        ArrayList<Edge> mst = new ArrayList<>();
        NodeGraph graph = new NodeGraph(vertex);
        WeightedQuickUnionUF union = new WeightedQuickUnionUF(vertex.size());
        
        PriorityQueue<Edge> edges = graph.getEdges();
        while(!edges.isEmpty() && mst.size() < vertex.size() - 1) {
            Edge edge = edges.poll();
            int source = edge.getSource();
            int target = edge.getTarget();
            if (!union.connected(source, target)) {
                union.union(source, target);
                mst.add(edge);
            }
        }

        return mst;
    }

    // generate paths to connect all the rooms
    private void generatePaths(ArrayList<Edge> edges) {
        for (Edge edge : edges) {
            Node sourceNode = vertex.get(edge.getSource());
            Node targetNode = vertex.get(edge.getTarget());
            connectRooms(sourceNode, targetNode);
        }
    }

    private void connectRooms(Node source, Node target) {
        int sourceX = source.getX();
        int sourceY = source.getY();
        int targetX = target.getX();
        int targetY = target.getY();

        int smallerX = Math.min(sourceX, targetX);
        int smallerY = Math.min(sourceY, targetY);
        int biggerX = Math.max(sourceX, targetX);
        int biggerY = Math.max(sourceY, targetY);

        // two vertexes in the same horizontal line
        if (sourceX == targetX) {
            connectHorizontally(smallerX, sourceY, biggerX);
        } else if (sourceY == targetY) {
            // two vertexes in the same vertical line
            connectVertically(sourceX, smallerY, biggerY);
        } else {
            connectHorizontally(smallerX, sourceY, biggerX);
            connectVertically(targetX, smallerY, biggerY);
        }
    }

    // we have already ensured that there is no overlap room in the previous method, so we can just connect them directly
    private void connectHorizontally(int sourceX, int sourceY, int targetX) {
        for (int x = sourceX; x <= targetX; x++) {
            if (IsValidForRoom(x, sourceY)) {
                world[x][sourceY].setTile(Tileset.FLOOR);
                world[x][sourceY].setRoom();
            }
        }
    }

    private void connectVertically(int sourceX, int sourceY, int targetY) {
        for (int y = sourceY; y <= targetY; y++) {
            if (IsValidForRoom(sourceX, y)) {
                world[sourceX][y].setTile(Tileset.FLOOR);
                world[sourceX][y].setRoom();
            }
        }
    }

    private void wrapFloorWithWalls(){
        for (int x = 0; x < Width; x++) {
            for (int y = 0; y < Height; y++) {
                if (IsWallValid(x, y)) {
                    world[x][y].setTile(Tileset.WALL);
                }
            }
        }
    }

    public TETile[][] getWorld() {
        TETile[][] tmp = new TETile[Width][Height];
        for (int x = 0; x < Width; x++) {
            for (int y = 0; y < Height; y++) {
                tmp[x][y] = world[x][y].getTile();
            }
        }
        return tmp;
    }

}
