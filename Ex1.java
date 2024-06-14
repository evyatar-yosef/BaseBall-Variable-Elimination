import java.util.*;

public class Ex1 {
    public static void main(String[] args) {
        try {
            // Create an instance of InputParser and read the input file
            InputParser ip = new InputParser("input.txt");

            // Create an instance of BayesianNetwork and construct the network
            BayesianNetwork bn = new BayesianNetwork(ip.getXmlFile());

            // Get the nodes from the Bayesian network
            Map<String, Node> nodes = bn.getNodes();

            // Process queries and perform d-separation tests
            for (String query : ip.getQueries()) {
                // Perform d-separation test for each query
                boolean isDConnected = processQuery(query, nodes);
                System.out.println("For query: " + query + ", Nodes are d-connected: " + isDConnected);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean processQuery(String query, Map<String, Node> nodes) {
        // Check if the query starts with "P" for variable elimination
        if (query.startsWith("P")) {
            // Implement variable elimination here
            System.out.println("Performing Variable Elimination for query: " + query);
            return processVariableEliminationQuery(query, nodes);
        } else {
            // Split the query into relevant components for BayesBall
            String[] queryParts = query.split("\\|");
            if (queryParts.length < 1) {
                System.out.println("Invalid query format: " + query);
                return false;
            }
            String[] nodeNames = queryParts[0].trim().split("-");
            String[] evidence = new String[0]; // Default to no evidence
            if (queryParts.length > 1) {
                evidence = queryParts[1].trim().split(",");
            }
            
            // Get the nodes for the query
            Node node1 = nodes.get(nodeNames[0].trim());
            Node node2 = nodes.get(nodeNames[1].trim());

            List<Node> evidenceNodes = new ArrayList<>();
            for (String evidenceNode : evidence) {
                // Split the evidence variable name to extract the variable name
                String[] parts = evidenceNode.split("=");
                if (parts.length >= 1) {
                    String variableName = parts[0].trim();
                    Node node = nodes.get(variableName);
                    if (node != null) {
                        evidenceNodes.add(node);
                    } else {
                        System.out.println("Node not found for evidence variable name: " + variableName);
                    }
                } else {
                    System.out.println("Invalid evidence format: " + evidenceNode);
                }
            }
            
                       
            return BayesBall.isIndependent(node1, node2, evidenceNodes);
           
        }
    }

    private static boolean processVariableEliminationQuery(String query, Map<String, Node> nodes) {
        // Example query: P(Q=q|E1=e1, E2=e2, …, Ek=ek) H1-H2-…-Hj

        // Extract the part before the closing parenthesis
        String[] queryParts = query.split("\\)");
        String queryPart = queryParts[0].substring(2);  // Remove "P(" at the start

        // Extract the elimination order if present
        String eliminationOrderPart = "";
        if (queryParts.length > 1) {
            eliminationOrderPart = queryParts[1].trim();
        }

        // Split the query part into the query variable and evidence
        String[] queryAndEvidence = queryPart.split("\\|");
        String queryVariable = queryAndEvidence[0].trim();
        String[] evidence = new String[0];
        if (queryAndEvidence.length > 1) {
            evidence = queryAndEvidence[1].trim().split(",");
        }

        // Extract query variable name and value
        String[] queryVarAndValue = queryVariable.split("=");
        String queryVarName = queryVarAndValue[0].trim();
        String queryVarValue = queryVarAndValue[1].trim();

        // Extract evidence variables and their values
        Map<String, String> evidenceMap = new HashMap<>();
        for (String ev : evidence) {
            String[] evParts = ev.split("=");
            String evVarName = evParts[0].trim();
            String evVarValue = evParts[1].trim();
            evidenceMap.put(evVarName, evVarValue);
        }

        // Extract the elimination order
        List<String> eliminationOrder = new ArrayList<>();
        if (!eliminationOrderPart.isEmpty()) {
            eliminationOrder.addAll(Arrays.asList(eliminationOrderPart.split("-")));
        }

        // Print the parsed query for debugging
        System.out.println("Query Variable: " + queryVarName + " = " + queryVarValue);
        System.out.println("Evidence:");
        for (Map.Entry<String, String> entry : evidenceMap.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        System.out.println("Elimination Order: " + eliminationOrder);

        // Call the variable elimination algorithm
        double result = variableElimination(queryVarName, queryVarValue, evidenceMap, eliminationOrder, nodes);
        System.out.println("Result: " + result);

        return true; // Placeholder return value for variable elimination
    }

    public static double variableElimination(String queryVarName, String queryVarValue, Map<String, String> evidenceMap, List<String> eliminationOrder, Map<String, Node> nodes) {
        // Step 1: Initialize factors
        List<Factor> factors = new ArrayList<>();
        for (Node node : nodes.values()) {
            factors.add(Factor.fromNode(node));
        }

        // System.out.println("print cpt");
        // for (Factor factor : factors) {
        //     factor.printFactor();
        // }
        // Step 2: Apply evidence to each factor
        
        for (Factor factor : factors) {
            factor.applyEvidence(evidenceMap);
            
            // Remove columns corresponding to evidence variables
            // for (String evVar : evidenceMap.keySet()) {
            //     if (nodes.containsKey(evVar)) {
            //         factor.removeVariable(evVar);
            //     }
            // }
        }
    
        int additionCount = 0;
        int multiplicationCount = 0;
    
        // Step 3: Process each hidden variable
        for (String hiddenVar : eliminationOrder) {
            List<Factor> relevantFactors = new ArrayList<>();
    
            // Find all factors that involve the hidden variable
            for (Factor factor : factors) {
                if (factor.getVariables().contains(hiddenVar)) {
                    relevantFactors.add(factor);
                }
            }
    
            // Remove relevant factors from the list of all factors
            factors.removeAll(relevantFactors);
    
            // Join all relevant factors
            Factor jointFactor = relevantFactors.get(0);
            for (int i = 1; i < relevantFactors.size(); i++) {
                jointFactor = jointFactor.join(relevantFactors.get(i));
                multiplicationCount += jointFactor.getAssignments().size();  // Track multiplication operations
            }
    
            // Eliminate the hidden variable
            Factor newFactor = jointFactor.eliminate(hiddenVar);
            additionCount += newFactor.getAssignments().size();  // Track addition operations
    
            // Add the new factor to the list of factors
            factors.add(newFactor);
        }
        
        // Print all remaining factors before stage 4
System.out.println("Remaining Factors Before Join:");
for (Factor factor : factors) {
    factor.printFactor();
}

        // Step 4: Join remaining factors
        Factor resultFactor = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            resultFactor = resultFactor.join(factors.get(i));
            multiplicationCount += resultFactor.getAssignments().size();  // Track multiplication operations
        }
        System.out.println("Final Resulting Factor After Joining Remaining Factors:");
resultFactor.printFactor();
    
        // Normalize the result factor
        double total = 0.0;
        for (double prob : resultFactor.getProbabilities()) {
            total += prob;
            additionCount++;  // Track addition operations during normalization
        }
    
        // Find the probability for the query
        double queryProbability = 0.0;
        for (int i = 0; i < resultFactor.getAssignments().size(); i++) {
            Map<String, String> assignment = resultFactor.getAssignments().get(i);
            if (assignment.get(queryVarName).equals(queryVarValue)) {
                queryProbability = resultFactor.getProbabilities().get(i) / total;
            }
        }
    
        // Output the result
        System.out.printf("%.5f,%d,%d\n", queryProbability, additionCount, multiplicationCount);
    
        return queryProbability;
    }
    
    
}
