import java.util.*;

public class Factor {
    private String name;  // Name of the factor
    private List<String> variables;  // Variables involved in the factor (including the node itself and its parents)
    private List<Map<String, String>> assignments;  // Possible assignments of the variables
    private List<Double> probabilities;  // Corresponding probabilities for each assignment

    public Factor(String name, List<String> variables, List<Map<String, String>> assignments, List<Double> probabilities) {
        this.name = name;
        this.variables = variables;
        this.assignments = assignments;
        this.probabilities = probabilities;
    }

    // Method to initialize a factor from a node's CPT
    public static Factor fromNode(Node node) {
        List<String> variables = new ArrayList<>();
        variables.add(node.getName());
        for (Node parent : node.getParents()) {
            variables.add(parent.getName());
        }

        List<Map<String, String>> assignments = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();

        String[] cptValues = node.getCPTValues();
        int numParents = node.getParents().size();
        int numCombinations = (int) Math.pow(2, numParents + 1);

        for (int i = 0; i < numCombinations; i++) {
            Map<String, String> assignment = new HashMap<>();
            String binaryString = String.format("%" + (numParents + 1) + "s", Integer.toBinaryString(i)).replace(' ', '0');
            for (int j = 0; j < numParents; j++) {
                assignment.put(variables.get(j + 1), binaryString.charAt(j) == '0' ? "T" : "F");
            }
            assignment.put(variables.get(0), binaryString.charAt(numParents) == '0' ? "T" : "F");
            assignments.add(assignment);
            probabilities.add(Double.parseDouble(cptValues[i]));
        }

        return new Factor(node.getName(), variables, assignments, probabilities);
    }

    public String getName() {
        return name;
    }

    public List<String> getVariables() {
        return variables;
    }

    public List<Map<String, String>> getAssignments() {
        return assignments;
    }

    public List<Double> getProbabilities() {
        return probabilities;
    }

// Method to apply evidence to the factor
public void applyEvidence(Map<String, String> evidence) {
    List<Map<String, String>> newAssignments = new ArrayList<>();
    List<Double> newProbabilities = new ArrayList<>();

    // Identify columns to remove
    List<Integer> columnsToRemove = new ArrayList<>();
    for (String evVar : evidence.keySet()) {
        if (variables.contains(evVar)) {
            columnsToRemove.add(variables.indexOf(evVar));
        }
    }

    for (int i = 0; i < assignments.size(); i++) {
        Map<String, String> assignment = assignments.get(i);
        boolean keepAssignment = true;

        // Check if assignment matches evidence
        for (Map.Entry<String, String> evEntry : evidence.entrySet()) {
            String evVar = evEntry.getKey();
            String evVal = evEntry.getValue();

            if (assignment.containsKey(evVar) && !assignment.get(evVar).equals(evVal)) {
                keepAssignment = false;
                break;
            }
        }

        if (keepAssignment) {
            // Remove columns for evidence variables
            Map<String, String> newAssignment = new HashMap<>(assignment);
            for (int colIndex : columnsToRemove) {
                newAssignment.remove(variables.get(colIndex));
            }

            newAssignments.add(newAssignment);
            newProbabilities.add(probabilities.get(i));
        }
    }

    // Update variables and assignments
    List<String> newVariables = new ArrayList<>(variables);
    for (int colIndex : columnsToRemove) {
        newVariables.remove(colIndex);
    }

    variables = newVariables;
    assignments = newAssignments;
    probabilities = newProbabilities;
}


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Factor: ").append(name).append("\n");
        sb.append("Variables: ").append(variables).append("\n");
        sb.append("Assignments and Probabilities:\n");
        for (int i = 0; i < assignments.size(); i++) {
            sb.append(assignments.get(i)).append(" : ").append(probabilities.get(i)).append("\n");
        }
        return sb.toString();
    }

    // Method to print factor assignments and probabilities for debugging
    public void printFactor() {
        System.out.println("Factor: " + name);
        System.out.println("Variables: " + variables);
        System.out.println("Assignments and Probabilities:");
        for (int i = 0; i < assignments.size(); i++) {
            System.out.println(assignments.get(i) + " : " + probabilities.get(i));
        }
        System.out.println();
    }

    // Method to join two factors
    public Factor join(Factor factor2) {
        System.out.println("Joining factors:");
        System.out.println("Factor 1:");
        this.printFactor();
        System.out.println("Factor 2:");
        factor2.printFactor();

        // Find common variables
        List<String> commonVariables = new ArrayList<>(this.variables);
        commonVariables.retainAll(factor2.variables);

        // Combine variables
        Set<String> joinedVariableSet = new LinkedHashSet<>(this.variables);
        joinedVariableSet.addAll(factor2.variables);
        List<String> joinedVariables = new ArrayList<>(joinedVariableSet);

        // Combine assignments
        List<Map<String, String>> joinedAssignments = new ArrayList<>();
        List<Double> joinedProbabilities = new ArrayList<>();

        for (Map<String, String> assignment1 : this.assignments) {
            for (Map<String, String> assignment2 : factor2.assignments) {
                boolean compatible = true;
                for (String var : commonVariables) {
                    if (!assignment1.get(var).equals(assignment2.get(var))) {
                        compatible = false;
                        break;
                    }
                }

                if (compatible) {
                    Map<String, String> joinedAssignment = new HashMap<>(assignment1);
                    joinedAssignment.putAll(assignment2);
                    double probability = this.probabilities.get(this.assignments.indexOf(assignment1)) * factor2.probabilities.get(factor2.assignments.indexOf(assignment2));
                    joinedAssignments.add(joinedAssignment);
                    joinedProbabilities.add(probability);
                }
            }
        }

        Factor joinedFactor = new Factor("JoinedFactor", joinedVariables, joinedAssignments, joinedProbabilities);

        // Print the resulting factor after join
        System.out.println("Resulting Factor After Join:");
        joinedFactor.printFactor();

        return joinedFactor;
    }

// Method to eliminate a variable from the factor by summing over it
public Factor eliminate(String variable) {
    System.out.println("Eliminating variable: " + variable);
    System.out.println("Original Factor:");
    this.printFactor();

    List<String> newVariables = new ArrayList<>(this.variables);
    newVariables.remove(variable);

    Map<Map<String, String>, Double> newAssignmentsMap = new HashMap<>();

    for (int i = 0; i < assignments.size(); i++) {
        Map<String, String> assignment = assignments.get(i);
        Map<String, String> newAssignment = new HashMap<>(assignment);
        newAssignment.remove(variable);

        double currentProbability = probabilities.get(i);
        double updatedProbability = newAssignmentsMap.getOrDefault(newAssignment, 0.0) + currentProbability;
        newAssignmentsMap.put(newAssignment, updatedProbability);

        System.out.println("Original Assignment: " + assignment + ", New Assignment: " + newAssignment + ", Probability: " + currentProbability);
    }

    List<Map<String, String>> newAssignments = new ArrayList<>(newAssignmentsMap.keySet());
    List<Double> newProbabilities = new ArrayList<>(newAssignmentsMap.values());

    Factor newFactor = new Factor("Eliminated" + variable, newVariables, newAssignments, newProbabilities);

    // Print the resulting factor after elimination
    System.out.println("Resulting Factor After Elimination of " + variable + ":");
    newFactor.printFactor();

    return newFactor;
}

}
