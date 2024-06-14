class BayesBallState {
    Node node;
    String direction;
    Node previousNode;

    public BayesBallState(Node node, String direction, Node previousNode) {
        this.node = node;
        this.direction = direction;
        this.previousNode = previousNode;
    }
}
