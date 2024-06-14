import java.util.*;

public class BayesBall {

    public static boolean isIndependent(Node startNode, Node endNode, List<Node> evidenceNodes) {
        if (startNode == null || endNode == null) return false;
        if (startNode.equals(endNode)) return true;

        for (Node node : getAllNodes(startNode)) {
            node.setShaded(evidenceNodes.contains(node));
            node.setFromChild(false);
        }

        // System.out.println("Evidence nodes:");
        // for (Node evidenceNode : evidenceNodes) {
        //     System.out.println("- " + evidenceNode.getName());
        // }

        Queue<Node> queue = new LinkedList<>();
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
           
            if (currentNode.getName().equals(endNode.getName())) {
                return false;
            }         

            List<Node> neighbors;
            if (currentNode.isShaded() && currentNode.isFromChild()) {
                continue;  // Can't go anywhere
            } else if (currentNode.isShaded() && !currentNode.isFromChild()) {
                neighbors = currentNode.getParents();  // Go to other parents

            } else if (!currentNode.isShaded() && currentNode.isFromChild()) {
                neighbors = getNeighbors(currentNode);  // Go to all neighbors
            } else {
                neighbors = currentNode.getChildren();  // Go to children only
            }

            for (Node neighbor : neighbors) {
                if (currentNode.getName().equals(neighbor.getName())) {
                    continue; // Skip adding the current node as its own neighbor
                }
                
                    neighbor.setFromChild(currentNode.getParents().contains(neighbor));
                    queue.add(neighbor);
                 //   System.out.println("Node entered the queue: " + neighbor.getName());
                
            }
            
        }

        return true;
    }

    private static List<Node> getAllNodes(Node startNode) {
        Set<Node> nodes = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            if (!nodes.contains(currentNode)) {
                nodes.add(currentNode);
                queue.addAll(currentNode.getChildren());
                queue.addAll(currentNode.getParents());
            }
        }

        return new ArrayList<>(nodes);
    }

    private static List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        neighbors.addAll(node.getChildren());
        neighbors.addAll(node.getParents());
        return neighbors;
    }

}
