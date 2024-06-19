import java.util.*;
import javax.naming.spi.DirStateFactory;

public class Ex1 {
    static List<String> results = new ArrayList<>();
    public static void main(String[] args) {
        // List<String> results = new ArrayList<>(); // List to collect the results

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
                 processQuery(query, nodes);
              
            }

            // Write the results to the output file
            OutputWriter.writeOutput(results);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    private static void  processQuery(String query, Map<String, Node> nodes) {
        // Check if the query starts with "P" for variable elimination
        if (query.startsWith("P")) {
            // Implement variable elimination here
            System.out.println("Performing Variable Elimination for query: " + query);
             processVariableEliminationQuery(query, nodes);
        } else {
            // Split the query into relevant components for BayesBall
            String[] queryParts = query.split("\\|");
            if (queryParts.length < 1) {
                System.out.println("Invalid query format: " + query);
                return ;
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
            boolean resu = BayesBall.isIndependent(node1, node2, evidenceNodes);
            String result = resu ? "yes" : "no";
            results.add(result);

          //  return BayesBall.isIndependent(node1, node2, evidenceNodes);

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
        // Step 1: Identify relevant nodes (ancestors of query and evidence variables)
        Set<String> relevantNodes = new HashSet<>();
        relevantNodes.add(queryVarName); // Add query variable
        evidenceMap.keySet().forEach(evVar -> relevantNodes.add(evVar)); // Add all evidence variables
    
        // Step 2: Find ancestors of relevant nodes
        Set<String> ancestors = findAncestors(queryVarName, evidenceMap.keySet(), nodes);
    
        
        // Step 3: Initialize factors for relevant nodes only
        List<Factor> factors = initializeFactors(nodes, ancestors);
        

        // Step 4: Apply evidence to each factor
        applyEvidenceToFactors(factors, evidenceMap,ancestors);
        
    
        // Print factors after applying evidence for debugging
        System.out.println("Factors after applying evidence:");
        printFactors(factors);
    
        // Step 5: Process each hidden variable in the elimination order
        int additionCount = 0;
        int multiplicationCount = 0;
    
        for (String hiddenVar : eliminationOrder) {
            if (!ancestors.contains(hiddenVar)){
                factors.remove(hiddenVar);
                continue;
            }

            List<Factor> relevantFactors = findRelevantFactors(factors, hiddenVar);
    
            // If no relevant factors are found, continue to the next variable
            if (relevantFactors.isEmpty()) {
                continue;
            }
    
            // Remove relevant factors from the list of all factors
            factors.removeAll(relevantFactors);
    
            // Debug output for factors being joined
            System.out.println("Joining factors containing variable: " + hiddenVar);
            printFactors(relevantFactors);
    
            // Use a priority queue to join factors with the smallest intermediate size first
            PriorityQueue<Factor> factorQueue = new PriorityQueue<>(Comparator.comparingInt(f -> f.getAssignments().size()));
            factorQueue.addAll(relevantFactors);
    
            // Join all relevant factors
            while (factorQueue.size() > 1) {
                Factor f1 = factorQueue.poll();
                Factor f2 = factorQueue.poll();
                Factor joinedFactor = f1.join(f2);
                multiplicationCount += joinedFactor.getAssignments().size(); // Track multiplication operations
                factorQueue.add(joinedFactor);
            }
    
            Factor jointFactor = factorQueue.poll();
    
            // Eliminate the hidden variable
            Factor newFactor = jointFactor.eliminate(hiddenVar);
            additionCount += newFactor.getAssignments().size() ;  // Track addition operations
    
            // Debug output for new factor after elimination
            System.out.println("New factor after eliminating variable: " + hiddenVar);
            newFactor.printFactor();
    
            // Add the new factor to the list of factors
            factors.add(newFactor);
        }
    
        // Print all remaining factors before final join
        System.out.println("Remaining Factors Before Final Join:");
        printFactors(factors);
    
        // Step 6: Join remaining factors
        Factor resultFactor = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            resultFactor = resultFactor.join(factors.get(i));
            multiplicationCount += resultFactor.getAssignments().size();  // Track multiplication operations
        }    
        System.out.println("Final Resulting Factor After Joining Remaining Factors:");
        resultFactor.printFactor();
    
        // Step 7: Eliminate evidence variables after final join
        resultFactor = eliminateEvidenceVariables(resultFactor, evidenceMap, queryVarName);
    
        System.out.println("Final Resulting Factor After Eliminating Evidence Variables:");
        resultFactor.printFactor();
    
        // Normalize the result factor
        double total = 0.0;
        for (double prob : resultFactor.getProbabilities()) {
            total += prob;
            additionCount++;  // Track addition operations during normalization
        }    
        additionCount --;
        // Find the probability for the query
        double queryProbability = 0.0;
        for (int i = 0; i < resultFactor.getAssignments().size(); i++) {
            Map<String, String> assignment = resultFactor.getAssignments().get(i);
            if (assignment.get(queryVarName).equals(queryVarValue)) {
                queryProbability += resultFactor.getProbabilities().get(i) / total;
            }
        }
        // Output the result
      //  System.out.printf("%.5f,%d,%d\n", queryProbability, additionCount, multiplicationCount);
        String probabilty = String.format("%.5f", queryProbability); 
        String count =String.valueOf(additionCount);  
        String mult =String.valueOf(multiplicationCount);  
        String res = probabilty+","+count+","+mult;

        results.add(res);
    
        return queryProbability;
    }
    
    private static Set<String> findAncestors(String queryVarName, Set<String> evidenceVars, Map<String, Node> nodes) {
        Set<String> relevantNodes = new HashSet<>();
        relevantNodes.add(queryVarName); // Add query variable
        evidenceVars.forEach(evVar -> relevantNodes.add(evVar)); // Add all evidence variables
    
        Set<String> ancestors = new HashSet<>();
        relevantNodes.forEach(var -> gatherAncestors(nodes.get(var), ancestors));
    
        return ancestors;
    }
    
    private static List<Factor> initializeFactors(Map<String, Node> nodes, Set<String> ancestors) {
        List<Factor> factors = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (ancestors.contains(node.getName())) {
                factors.add(Factor.fromNode(node));
            }
        }
        return factors;
    }
    
    private static void applyEvidenceToFactors(List<Factor> factors, Map<String, String> evidenceMap, Set<String>ancestors) {
        for (Factor factor : factors) {
            if(!ancestors.contains(factor.getName()))
            {
                factors.remove(factor);
                continue;
            }
            factor.applyEvidence(evidenceMap);
        }
    }
    
    private static void printFactors(List<Factor> factors) {
        for (Factor factor : factors) {
            factor.printFactor();
        }
    }
    
    private static List<Factor> findRelevantFactors(List<Factor> factors, String hiddenVar) {
        List<Factor> relevantFactors = new ArrayList<>();
        for (Factor factor : factors) {
            if (factor.getVariables().contains(hiddenVar)) {
                relevantFactors.add(factor);
            }
        }
        return relevantFactors;
    }
    
   
    
    private static Factor eliminateEvidenceVariables(Factor resultFactor, Map<String, String> evidenceMap, String queryVarName) {
        for (String evVar : evidenceMap.keySet()) {
            if (resultFactor.getVariables().contains(evVar) && !evVar.equals(queryVarName)) {
                resultFactor = resultFactor.eliminate(evVar);
            }
        }
        return resultFactor;
    }
    
     
    
    
    private static void gatherAncestors(Node node, Set<String> ancestors) {
        // Add the current node to ancestors set
        ancestors.add(node.getName());
    
        // Recursively add ancestors of all parent nodes
        for (Node parent : node.getParents()) {
            if (!ancestors.contains(parent.getName())) {
                gatherAncestors(parent, ancestors);
            }
        }
    }
    
    }
