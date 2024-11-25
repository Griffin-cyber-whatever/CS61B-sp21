package byow;

import java.io.Serializable;
import java.util.ArrayList;

public class Edge implements Comparable<Edge>, Serializable {
    private int source;
    private int target;
    private double weight;

    public Edge(int source, int target, ArrayList<Node> nodes) {
        this.source = source;
        this.target = target;

        weight = nodes.get(source).getDistance(nodes.get(target));
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
}
