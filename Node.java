import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node {
    private String name;
    private List<String> outcomes;
    private List<Node> parents;
    private List<Node> children;
    private String[] cptValues;
    private boolean shaded;
    private boolean fromChild;

    public Node(String name) {
        this.name = name;
        this.outcomes = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addOutcome(String outcome) {
        outcomes.add(outcome);
    }

    public void addParent(Node parent) {
        parents.add(parent);
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public List<Node> getParents() {
        return parents;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setCPTValues(String[] cptValues) {
        this.cptValues = cptValues;
    }

    public boolean isShaded() {
        return shaded;
    }

    public void setShaded(boolean shaded) {
        this.shaded = shaded;
    }

    public boolean isFromChild() {
        return fromChild;
    }

    public void setFromChild(boolean fromChild) {
        this.fromChild = fromChild;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", outcomes=" + outcomes +
                ", parents=" + getNodeNames(parents) +
                ", children=" + getNodeNames(children) +
                ", cptValues=" + Arrays.toString(cptValues) +
                '}';
    }

    private List<String> getNodeNames(List<Node> nodes) {
        List<String> names = new ArrayList<>();
        for (Node node : nodes) {
            names.add(node.getName());
        }
        return names;
    }

    public List<String> getOutcomes() {
        return outcomes;
    }

    public String[] getCPTValues() {
        return cptValues;
    }
    public int getOutcomeCount() {
        return outcomes.size();
    }
}
