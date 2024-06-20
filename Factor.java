    import java.util.*;

    public class Factor {
        private String name;  // Name of the factor
        private List<String> variables;  // Variables involved in the factor (including the node itself and its parents)
        private List<Map<String, String>> assignments;  // Possible assignments of the variables
        private List<Double> probabilities;  // Corresponding probabilities for each assignment

        private static int joinOperationsCount = 0;
        private static int eliminateOperationsCount = 0;
        public Factor(String name, List<String> variables, List<Map<String, String>> assignments, List<Double> probabilities) {
            this.name = name;
            this.variables = variables;
            this.assignments = assignments;
            this.probabilities = probabilities;
        }
        public static int getAdditionCount() {
            return joinOperationsCount;
        }
    
        public static int getMultiplicationCount() {
            return eliminateOperationsCount;
        }
        public static void resetOperationCounts() {
            joinOperationsCount = 0;
            eliminateOperationsCount = 0;
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

            List<String> nodeOutcomes = node.getOutcomes();
            int numOutcomes = nodeOutcomes.size();
            List<List<String>> parentOutcomes = new ArrayList<>();
            for (Node parent : node.getParents()) {
                parentOutcomes.add(parent.getOutcomes());
            }

            // Generate all possible combinations of parent outcomes
            List<List<String>> allParentCombinations = generateCombinations(parentOutcomes);

            // Create assignments and probabilities based on CPT values
            int cptIndex = 0;
            for (List<String> parentCombination : allParentCombinations) {
                for (String outcome : nodeOutcomes) {
                    Map<String, String> assignment = new LinkedHashMap<>();
                    for (int i = 0; i < parentCombination.size(); i++) {
                        assignment.put(node.getParents().get(i).getName(), parentCombination.get(i));
                    }
                    assignment.put(node.getName(), outcome);
                    assignments.add(assignment);
                    probabilities.add(Double.parseDouble(node.getCPTValues()[cptIndex]));
                    cptIndex++;
                }
            }

            return new Factor(node.getName(), variables, assignments, probabilities);
        }

        // Helper method to generate all combinations of parent outcomes
        private static List<List<String>> generateCombinations(List<List<String>> parentOutcomes) {
            List<List<String>> result = new ArrayList<>();
            generateCombinationsRecursive(result, new ArrayList<>(), parentOutcomes, 0);
            return result;
        }

        private static void generateCombinationsRecursive(List<List<String>> result, List<String> current, List<List<String>> parentOutcomes, int depth) {
            if (depth == parentOutcomes.size()) {
                result.add(new ArrayList<>(current));
                return;
            }

            for (String outcome : parentOutcomes.get(depth)) {
                current.add(outcome);
                generateCombinationsRecursive(result, current, parentOutcomes, depth + 1);
                current.remove(current.size() - 1);
            }
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

        public void applyEvidence(Map<String, String> evidence) {
            List<Map<String, String>> newAssignments = new ArrayList<>();
            List<Double> newProbabilities = new ArrayList<>();

            List<Integer> columnsToRemove = new ArrayList<>();
            for (String evVar : evidence.keySet()) {
                if (variables.contains(evVar)) {
                    columnsToRemove.add(variables.indexOf(evVar));
                }
            }

            for (int i = 0; i < assignments.size(); i++) {
                Map<String, String> assignment = assignments.get(i);
                boolean keepAssignment = true;

                for (Map.Entry<String, String> evEntry : evidence.entrySet()) {
                    String evVar = evEntry.getKey();
                    String evVal = evEntry.getValue();

                    if (assignment.containsKey(evVar) && !assignment.get(evVar).equals(evVal)) {
                        keepAssignment = false;
                        break;
                    }
                }

                if (keepAssignment) {
                    Map<String, String> newAssignment = new HashMap<>(assignment);
                    for (int colIndex : columnsToRemove) {
                        newAssignment.remove(variables.get(colIndex));
                    }

                    newAssignments.add(newAssignment);
                    newProbabilities.add(probabilities.get(i));
                }
            }

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

        public void printFactor() {
            System.out.println("Factor: " + name);
            System.out.println("Variables: " + variables);
            System.out.println("Assignments and Probabilities:");
            for (int i = 0; i < assignments.size(); i++) {
                System.out.println(assignments.get(i) + " : " + probabilities.get(i));
            }
            System.out.println();
        }

        public Factor join(Factor factor2) {
            System.out.println("Joining factors:");
            System.out.println("Factor 1:");
            this.printFactor();
            System.out.println("Factor 2:");
            factor2.printFactor();
    
            List<String> commonVariables = new ArrayList<>(this.variables);
            commonVariables.retainAll(factor2.variables);
            Set<String> joinedVariableSet = new LinkedHashSet<>(this.variables);
            joinedVariableSet.addAll(factor2.variables);
            List<String> joinedVariables = new ArrayList<>(joinedVariableSet);
    
            List<Map<String, String>> joinedAssignments = new ArrayList<>();
            List<Double> joinedProbabilities = new ArrayList<>();
    
            for (int i = 0; i < this.assignments.size(); i++) {
                Map<String, String> assignment1 = this.assignments.get(i);
                double probability1 = this.probabilities.get(i);
    
                for (int j = 0; j < factor2.assignments.size(); j++) {
                    Map<String, String> assignment2 = factor2.assignments.get(j);
                    double probability2 = factor2.probabilities.get(j);
    
                    joinOperationsCount++;  // Increment join operation counter
    
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
                        double probability = probability1 * probability2;
                        joinedAssignments.add(joinedAssignment);
                        joinedProbabilities.add(probability);
                    }
                }
            }
    
            Factor joinedFactor = new Factor("JoinedFactor", joinedVariables, joinedAssignments, joinedProbabilities);
    
            System.out.println("Resulting Factor After Join:");
            joinedFactor.printFactor();
    
            return joinedFactor;
        }
    

        public Factor eliminate(String variable) {
            System.out.println("Eliminating variable: " + variable);
            System.out.println("Original Factor:");
            this.printFactor();
        
            // Create a new list of variables without the eliminated variable
            List<String> newVariables = new ArrayList<>(this.variables);
            newVariables.remove(variable);
        
            // Map to hold the summed probabilities for each new assignment
            Map<Map<String, String>, Double> newAssignmentsMap = new HashMap<>();
            
            // Iterate through current assignments and sum out the variable
            for (int i = 0; i < assignments.size(); i++) {
                Map<String, String> assignment = assignments.get(i);
                Map<String, String> newAssignment = new HashMap<>(assignment);
        
                eliminateOperationsCount++;  // Increment eliminate operation counter
        
                // Remove the variable from assignment
                newAssignment.remove(variable);
        
                // Get the current probability
                double currentProbability = probabilities.get(i);
        
                // Update the summed probability for the new assignment
                double updatedProbability = newAssignmentsMap.getOrDefault(newAssignment, 0.0) + currentProbability;
                newAssignmentsMap.put(newAssignment, updatedProbability);
        
                // Debug: Print the details of the elimination process
                System.out.println("Original Assignment: " + assignment + ", New Assignment: " + newAssignment + ", Probability: " + currentProbability);
            }
        
            // Convert the map to lists of assignments and probabilities for the new factor
            List<Map<String, String>> newAssignments = new ArrayList<>(newAssignmentsMap.keySet());
            List<Double> newProbabilities = new ArrayList<>(newAssignmentsMap.values());
        
            // Create the new factor after elimination
            Factor newFactor = new Factor("Eliminated" + variable, newVariables, newAssignments, newProbabilities);
        
            System.out.println("Resulting Factor After Elimination:");
            newFactor.printFactor();
        
            return newFactor;
        }
        
        
    }
