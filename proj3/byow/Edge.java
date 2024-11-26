package byow;

import java.io.Serializable;
import java.util.ArrayList;

public class Edge implements Comparable<Edge>, Serializable {
    private Node sourceNode;
    private Node targetNode;
    private int source;
    private int target;
    private double weight;

    public Edge(int source, int target, ArrayList<Node> nodes) {
        this.source = source;
        this.target = target;
        sourceNode = nodes.get(source);
        targetNode = nodes.get(target);
        weight = sourceNode.getDistance(targetNode);
    }

    @Override
    public int compareTo(Edge o) {
        return Double.compare(weight, o.weight);
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public String toString() {
        return "(" + source + ", " + target + ")\n" + "vertex " + source + " -> " + sourceNode.toString() + "\nvertex" + target +
        " -> " + targetNode.toString();
    }
}
