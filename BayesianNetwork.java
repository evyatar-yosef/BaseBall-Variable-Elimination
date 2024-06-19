import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class BayesianNetwork {
    private Map<String, Node> nodes;

    public BayesianNetwork(String filename) throws Exception {
        nodes = new HashMap<>();
        parseXML(filename);
    }

    private void parseXML(String filename) throws Exception {
        File file = new File(filename);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList variableList = doc.getElementsByTagName("VARIABLE");
        for (int i = 0; i < variableList.getLength(); i++) {
            Element variableElement = (Element) variableList.item(i);
            String name = variableElement.getElementsByTagName("NAME").item(0).getTextContent();
            Node node = new Node(name);
            NodeList outcomeList = variableElement.getElementsByTagName("OUTCOME");
            for (int j = 0; j < outcomeList.getLength(); j++) {
                node.addOutcome(outcomeList.item(j).getTextContent());
            }
            nodes.put(name, node);
        }

        NodeList definitionList = doc.getElementsByTagName("DEFINITION");
        for (int i = 0; i < definitionList.getLength(); i++) {
            Element definitionElement = (Element) definitionList.item(i);
            String forNode = definitionElement.getElementsByTagName("FOR").item(0).getTextContent();
            Node node = nodes.get(forNode);
            NodeList givenList = definitionElement.getElementsByTagName("GIVEN");
            List<Node> parents = new ArrayList<>();
            for (int j = 0; j < givenList.getLength(); j++) {
                String parentName = givenList.item(j).getTextContent();
                Node parentNode = nodes.get(parentName);
                parents.add(parentNode);
                parentNode.addChild(node);
            }
            node.getParents().addAll(parents);

            String[] tableValues = definitionElement.getElementsByTagName("TABLE").item(0).getTextContent().split(" ");
            node.setCPTValues(tableValues);
        }

        // Print the parsed Bayesian Network structure
        System.out.println("Bayesian Network:");
        for (Node node : nodes.values()) {
            System.out.println(node);
        }
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "BayesianNetwork{" +
                "nodes=" + nodes +
                '}';
    }
}
