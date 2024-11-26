package byow;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* my solution:
*   using seed to generate node for world, each node would be a room
*   using MST to connect all of them with hall
*   wrap room and hall with wall in the end*/
public class World implements Serializable {
    // saves location
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File savesAddress = new File(CWD, "saves");
    private final File saveFile;
    private String fileName;

    private final Node[][] world;
    private final long seed;
    private final Random rand;
    private final int Width;
    private final int Height;
    private final ArrayList<Node> vertex;
    // set avatar position with one of vertex
    private final Node avatar;

    // set this would change the num of rooms
    private final double CoverageFactor = 0.5;
    // set this would change the ratio between room size and total map
    private final int RoomSizeFactor = 3;
    // the minimum gap between each rooms
    private final int MinimumRoomGap = 3;


    // use this constructor when u already find out the seed
    public World(int width, int height, long seed) {
        this.Width = width;
        this.Height = height;
        this.seed = seed;
        this.rand = new Random(seed);
        world = new Node[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = new Node(Tileset.NOTHING, x, y);
            }
        }
        vertex = new ArrayList<>();

        int MaxWidth = Math.max(1, width /RoomSizeFactor);
        int MaxHeight = Math.max(1, height/RoomSizeFactor);
        int MinWidth = Math.max(1, width/(RoomSizeFactor + 2));
        int MinHeight = Math.max(1, height/(RoomSizeFactor + 2));
        int numberOfNodes = (int) Math.max(1, CoverageFactor * width * height * 4 / ((MaxWidth + MinWidth) * (MaxHeight + MinHeight)));

        // generate the world array
        VertexGenerator(numberOfNodes);
        RoomGenerator();
        generatePaths(getMST());
        wrapFloorWithWalls();

        // initializing avatar
        avatar = vertex.get(rand.nextInt(vertex.size()));
        avatar.setTile(Tileset.AVATAR);

        // initializing save of current object
        this.fileName = currentTime();
        this.saveFile = new File(savesAddress, fileName);
    }

    private String currentTime(){
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter.format(now);
    }

    // ensure that directory have been initialized
    private void FileSetupChecking(){
        try {
            savesAddress.mkdir();
            saveFile.createNewFile();
        } catch (FileAlreadyExistsException e){
            // ignore
        } catch (IOException e){
            System.out.println("IO Exception");
            throw new IllegalArgumentException("setup failed" + e);
        }
    }

    // save current object in that
    public void save(){
        FileSetupChecking();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // load the latest object
    public static World load() {
        if (!savesAddress.exists() || !savesAddress.isDirectory()) {
            throw new IllegalStateException("Saves directory does not exist or is not a directory.");
        }

        File[] files = savesAddress.listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalStateException("No save files found.");
        }

        // Find the latest file by name (assumes names are timestamps)
        File latestFile = Arrays.stream(files)
                .filter(File::isFile)
                .max(Comparator.comparing(File::getName))
                .orElseThrow(() -> new IllegalStateException("No valid save files found."));

        // Load the object from the latest file
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(latestFile))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof World) {
                World loadedData = (World) loadedObject;
                return loadedData;
            } else {
                throw new IllegalArgumentException("The file does not contain a valid object.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to load the save file.", e);
        }
    }

    public static World seedProcessor(int Width, int Height, String seed) {
            seed = seed.toLowerCase();

            String number = "n+(\\d+)s";
            String movement = "([swad]*)";
            String save = "(:q)?$";
            String regex = "^" + number + movement + save + "|(l)" + movement + save ;
            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(seed);

            if (matcher.matches()) {
                String first = null;
                String movementCommands = null;
                String quitCommands = null;

                if (matcher.group(1) != null) {
                    // Matched the first alternative
                    first = matcher.group(1); // Group 1: number
                    movementCommands = matcher.group(2); // Group 2: movement commands
                    quitCommands = matcher.group(3); // Group 3: save command
                } else if (matcher.group(4) != null) {
                    // Matched the second alternative
                    first = matcher.group(4); // Group 4: "l"
                    movementCommands = matcher.group(5); // Group 5: movement commands
                    quitCommands = matcher.group(6); // Group 6: save command
                }

                return seedProcessorHelper(first, movementCommands, quitCommands, Width, Height);
            } else {
                throw new IllegalArgumentException("Seed does not match regex.");
            }
    }


    private static World seedProcessorHelper(String first, String movementCommands, String quitCommands, int Width, int Height) {
            try {
            World world;
            if (first.equals("l")){
                world = load();
            } else {
                long seed = Long.parseLong(first);
                world = new World(Width, Height, seed);
            }

            for (char c : movementCommands.toCharArray()) {
                switch (c){
                    case 'w' -> world.moveUp();
                    case 's' -> world.moveDown();
                    case 'a' -> world.moveLeft();
                    case 'd' -> world.moveRight();
                }
            }

            if (quitCommands != null &&  quitCommands.equals(":q")){
                world.save();
            }

            return world;
            } catch (Exception e){
           throw new IllegalArgumentException("invalid seed");
        }
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

        for (int x = leftBoundary - MinimumRoomGap; x < rightBoundary + MinimumRoomGap; x++) {
            for (int y = bottomBoundary - MinimumRoomGap; y < topBoundary + MinimumRoomGap; y++) {
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
                    world[x][y].setTile(Tileset.FLOOR);
                    world[x][y].setRoom();
            }
        }
    }

    private void VertexGenerator(int numberOfNodes) {
        for (int x = 0; x < numberOfNodes; x++) {
            int tmpX = rand.nextInt(Width - 3) + 2;
            int tmpY = rand.nextInt(Height - 3) + 2;
            vertex.add(world[tmpX][tmpY]);
        }
    }

    // Generate rooms using a spiral/random walk approach to maximize space utilization
    private void RoomGenerator() {
        for (int i = 0; i < vertex.size(); i++) {
            Node n = vertex.get(i);
            boolean roomPlaced = false;
            int attemptCount = 0;  // To avoid infinite loops
            while (!roomPlaced && attemptCount < 100) {
                // Starting coordinates (n.getX(), n.getY()) can be dynamically adjusted or randomized
                int xOffset = n.getX();
                int yOffset = n.getY();
                int width = rand.nextInt(Width / RoomSizeFactor - Width / (RoomSizeFactor + 2)) + Width / (RoomSizeFactor + 2);
                int height = rand.nextInt(Height / RoomSizeFactor - Height / (RoomSizeFactor + 2)) + Height / (RoomSizeFactor + 2);

                // Gradually adjust room size after failed attempts
                if (attemptCount > 50) {
                    width = width / 2;
                    height = height / 2;
                }

                // Try spiral approach to place rooms
                roomPlaced = placeRoomSpirally(xOffset, yOffset, width, height, i);
                attemptCount++;
            }
        }
    }

    // Spiral approach to place room, expanding outward from the center
    private boolean placeRoomSpirally(int centerX, int centerY, int roomWidth, int roomHeight, int vertexNumber) {

        int xOffset = centerX;
        int yOffset = centerY;
        int leftBoundary = xOffset - roomWidth / 2;
        int bottomBoundary = yOffset - roomHeight / 2;

        // Attempt placing the room with a spiral walk
        for (int radius = 1; radius < Math.max(Width, Height); radius++) {
            for (int i = 0; i < 4; i++) {
                // Depending on the direction of the spiral, move in the grid
                switch (i) {
                    case 0: // Move right
                        xOffset = centerX + radius;
                        break;
                    case 1: // Move down
                        yOffset = centerY + radius;
                        break;
                    case 2: // Move left
                        xOffset = centerX - radius;
                        break;
                    case 3: // Move up
                        yOffset = centerY - radius;
                        break;
                }

                // Check if this area is valid for room placement
                if (IsRoomValid(xOffset, yOffset, roomWidth, roomHeight)) {
                    CreateRoom(xOffset, yOffset, roomWidth, roomHeight);
                    vertex.set(vertexNumber, world[xOffset + roomWidth/2][yOffset + roomHeight/2]);
                    world[xOffset + roomWidth/2][yOffset + roomHeight/2].setRoom();
                    return true;
                }
            }
        }

        return false;  // If room can't be placed within the spiral walk
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
            System.out.println(edge);
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

        // two vertexes in the same vertical line
        if (sourceX == targetX) {
            connectVertically(sourceX, sourceY, targetY);
        } else if (sourceY == targetY) {
            // two vertexes in the same vertical line
            connectHorizontally(sourceX, sourceY, targetX);
        } else {
            connectHorizontally(sourceX, sourceY, targetX);
            connectVertically(targetX, sourceY, targetY);
        }
    }

    // we have already ensured that there is no overlap room in the previous method, so we can just connect them directly
    private void connectHorizontally(int sourceX, int sourceY, int targetX) {
        int tmp = sourceX;
        sourceX = Math.min(sourceX, targetX);
        targetX = Math.max(targetX, tmp);
        System.out.println(String.format("connect Horizontally from %d to %d in x and %d in y", sourceX, targetX, sourceY));
        for (int x = sourceX ; x <= targetX; x++) {
            world[x][sourceY].setTile(Tileset.FLOOR); // Overwrite any tile in the path
            world[x][sourceY].setRoom(); // Mark it as a room
        }
    }


    private void connectVertically(int sourceX, int sourceY, int targetY) {
        int tmp = sourceY;
        sourceY = Math.min(sourceY, targetY);
        targetY = Math.max(targetY, tmp);
        System.out.println(String.format("connect Vertically from %d to %d in y and %d in x", sourceY, targetY, sourceX));
        for (int y = sourceY; y <= targetY; y++) {
                world[sourceX][y].setTile(Tileset.FLOOR);
                world[sourceX][y].setRoom();
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

    public void moveUp(){
        int avatarX = avatar.getX();
        int avatarY = avatar.getY();
        if (world[avatarX][avatarY + 1].isRoom()) {
            swapNode(avatar, world[avatarX][avatarY + 1]);
        }
    }

    public void moveDown(){
        int avatarX = avatar.getX();
        int avatarY = avatar.getY();
        if (world[avatarX][avatarY - 1].isRoom()) {
            swapNode(avatar, world[avatarX][avatarY - 1]);
        }
    }

    public void moveLeft(){
        int avatarX = avatar.getX();
        int avatarY = avatar.getY();
        if (world[avatarX - 1][avatarY].isRoom()) {
            swapNode(avatar, world[avatarX - 1][avatarY]);
        }
    }

    public void moveRight(){
        int avatarX = avatar.getX();
        int avatarY = avatar.getY();
        if (world[avatarX + 1][avatarY].isRoom()) {
            swapNode(avatar, world[avatarX + 1][avatarY]);
        }
    }

    private void swapNode(Node source, Node target) {
        int sourceX = source.getX();
        int sourceY = source.getY();
        int targetX = target.getX();
        int targetY = target.getY();
        // swap Node in each coordinate only affect its current coordinate in array but not node object
        Node tmp = world[sourceX][sourceY];
        world[sourceX][sourceY] = world[targetX][targetY];
        world[targetX][targetY] = tmp;

        target.setPosition(sourceX, sourceY);
        source.setPosition(targetX, targetY);
    }

}
