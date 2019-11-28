import constraint.*;
import constraint.expressions.Expression;
import constraint.expressions.ExpressionParser;
import variables.Domain;
import variables.Propagation;
import variables.Variable;

import java.time.Clock;
import java.util.*;

public class Model {

    // TIME
    private Clock clock;
    private long time;

    // UTIL
    private ExpressionParser parser = new ExpressionParser();   // Parser of expressions

    // MODEL
    private Propagation propagation;                // The propagation object
    private ArrayList<Variable> variables;          // All the variables + the one generated (n-ary variables)
    private ArrayList<Variable> staticVariables;    // All the "base" variables (for which to output a solution)
    private String defaultFilter = Constraint.AC3;  // Default filter used

    // CONSTRAINTS
    private HashMap<Variable, HashMap<Variable, Table>> constraints;    // All constraint's tables (binary)
    private HashMap<Variable, ArrayList<Variable>> addedConstraints;    // All new variables (n-ary)
    private HashMap<Variable, ArrayList<Expression>> addedExpressions;  // All n-ary expressions

    // INFORMATIONS
    private boolean status = false;         // Is there a solution ?
    private ArrayList<int[]> solutions;     // All solutions
    private int nSol = 0;                   // Number of solution we're looking for
    private int BACKTRACKS, FAILS, NSOLUTIONS;
    private boolean DEBUG = false;

    public Model(){
        this.propagation = new Propagation();
        this.variables = new ArrayList<>();
        this.staticVariables = new ArrayList<>();
        this.solutions = new ArrayList<>();
        this.constraints = new HashMap<>();
        this.addedConstraints = new HashMap<>();
        this.addedExpressions = new HashMap<>();
        this.clock = Clock.systemDefaultZone();
    }

    public void setDefaultFilter(String filter){
        defaultFilter = filter;
    }

    /*******************************
     * ADD A VARIABLE TO THE MODEL *
     *******************************/

    /**
     * Add a variable to the model using the given domain.
     * @param domain Domain of the variable
     * @return The created variable
     */
    public Variable addVariable(Domain domain){
        Variable v = new Variable(domain, propagation);
        this.variables.add(v);
        this.staticVariables.add(v);
        return v;
    }

    /**
     * Add a variable to the model using the domain [lb, ub]
     * @param lb Lower bound of the domain
     * @param ub Upper bound of the domain
     * @return The created variable
     */
    public Variable addVariable(int lb, int ub){
        return addVariable(new Domain(lb, ub));
    }

    /**
     * Add a variable to the model using the domain given by values
     * @param values Possible values of the variable
     * @return The created variable
     */
    public Variable addVariable(int[] values){
        return addVariable(new Domain(values));
    }

    /**
     * Add n variables to the model using the given domain.
     * @param n Number of variables to add
     * @param domain Domain of the variables
     * @return All the created variables
     */
    public Variable[] addVariables(int n, Domain domain){
        Variable[] variables = new Variable[n];
        for(int i = 0; i < n; i++) variables[i] = addVariable(domain);
        return variables;
    }

    /**
     * Add n variables to the model using the domain [lb, ub]
     * @param n Number of variables to add
     * @param lb Lower bound of the domain
     * @param ub Upper bound of the domain
     * @return All the created variables
     */
    public Variable[] addVariables(int n, int lb, int ub){
        return addVariables(n, new Domain(lb, ub));
    }

    /**
     * Add n variables to the model using the domain given by values
     * @param n Number of variables to add
     * @param values Possible values for the variables
     * @return All the created variables
     */
    public Variable[] addVariables(int n, int[] values){
        return addVariables(n, new Domain(values));
    }

    /**************************************************
     * ADD A CONSTRAINT TO THE MODEL (UNDER THE HOOD) *
     **************************************************/

    /**
     * Check if there exists a constraint between two variables
     * @param x Variable
     * @param y Variable
     * @return true if there exists a constraint between x and y, false otherwise.
     */
    private boolean existsConstraint(Variable x, Variable y){
        return constraints.containsKey(x) && constraints.get(x).containsKey(y);
    }

    /**
     * Get the table of the constraint between two variables.
     * @param x Variable
     * @param y Variable
     * @return Table of feasible tuples
     */
    private Table getConstraintTable(Variable x, Variable y){
        if(existsConstraint(x, y)) return constraints.get(x).get(y);
        else return null;
    }

    /**
     * Add a constraint to the map of constraints between two variables
     * @param x Variable
     * @param y Variable
     * @param table Table of feasible tuples
     */
    private void addConstraintToMap(Variable x, Variable y, Table table){
        if(constraints.containsKey(x)){
            Table t = getConstraintTable(x, y);
            if(t == null) {
                if(constraints.containsKey(y)) t = getConstraintTable(y, x);
                if(t == null) constraints.get(x).put(y, table);
                else constraints.get(y).put(x, table);
            }
            else constraints.get(x).put(y, table.intersection(t));
        } else {
            if(constraints.containsKey(y)){
                Table t = getConstraintTable(y, x);
                if(t == null) constraints.get(y).put(x, table);
                else constraints.get(y).put(x, table.intersection(t));
            } else {
                HashMap<Variable, Table> hashmap = new HashMap<>();
                hashmap.put(y, table);
                constraints.put(x, hashmap);
            }
        }
    }

    /************************************
     * N-ARITY CONSTRAINTS CONSTRUCTION *
     ************************************/

    /**
     * Check if there already exists a constraint for a given set of variables.
     * @param variables
     * @return Variable v if it exists, null otherwise.
     */
    private Variable getFakeVariable(Variable... variables){
        boolean found = true;
        for(Variable key : this.addedConstraints.keySet()){
            for(Variable v : variables)
                if(!this.addedConstraints.get(key).contains(v)) found = false;
            if(found && this.addedConstraints.get(key).size() == variables.length) return key;
            found = true;
        }
        return null;
    }

    /**
     * Build the table for each "fake" variables and add them to the model.
     */
    private void buildTables() {
        for(Variable fake : addedExpressions.keySet()) {
            ArrayList<Variable> variables = addedConstraints.get(fake);
            ArrayList<int[]> tuples = computeTuples(addedExpressions.get(fake), variables);

            int size = tuples.size();
            Table[] tables = new Table[variables.size()];

            for (int i = 0; i < variables.size(); i++) tables[i] = new Table();
            fake = new Variable(new Domain(0, size - 1), propagation);

            for (int i = 0; i < size; i++)
                for (int j = 0; j < variables.size(); j++) tables[j].add(i, tuples.get(i)[j]);

            for (int i = 0; i < variables.size(); i++) {
                tables[i].computeHashTable();
                addConstraint(fake, variables.get(i), tables[i]);
            }
            this.variables.add(fake);
        }
    }

    /**
     * Compute every feasible tuples for a given set of expressions and a given set of variables.
     * @param expressions List of expressions to check
     * @param variables List of variables
     * @return The list of feasibles tuples.
     */
    private ArrayList<int[]> computeTuples(ArrayList<Expression> expressions, ArrayList<Variable> variables){
        ArrayList<int[]> tuples = new ArrayList<>();

        int[] counts = new int[variables.size()];
        int[] values = new int[variables.size()];
        int index = 0;

        while(true) {
            if (index == 0 && counts[index] == variables.get(0).getDomainSize()) return tuples;
            if (index == variables.size()) {
                boolean find = true;
                for(Expression expression : expressions) if(find && !expression.eval(values)) find = false;
                if (find) {
                    int[] new_values = new int[values.length];
                    System.arraycopy(values, 0, new_values, 0, values.length);
                    tuples.add(new_values);
                }
                index--;
            }
            if (index < 0) return tuples;
            if (counts[index] >= variables.get(index).getDomainSize()) {
                counts[index] = 0;
                index--;
            } else {
                values[index] = variables.get(index).getDomainValue(counts[index]);
                counts[index] += 1;
                index++;
            }
        }
    }

    /********************************************
     * ADD A CONSTRAINT TO THE MODEL (FOR USER) *
     ********************************************/

    /**
     * Add a constraint between two variables to the model
     * @param x Variable
     * @param y Variable
     * @param table Table of feasible tuples
     */
    public void addConstraint(Variable x, Variable y, Table table){
        addConstraintToMap(x, y, table);
    }

    /**
     * Add a constraint between any number of variables according to the given expression.
     * @param expression String representing a mathematical constraint
     * @param variables Variables binded to the formula
     */
    public void addConstraint(String expression, Variable... variables){
        if(variables.length == 2) addConstraint(variables[0], variables[1], new Table(expression, variables[0], variables[1]));
        else{
            Variable fake = getFakeVariable(variables);
            if(fake == null) {
                fake = new Variable(new Domain(0, 0), propagation);

                ArrayList<Variable> variableArrayList = new ArrayList<>(Arrays.asList(variables));

                this.addedConstraints.put(fake, variableArrayList);
                this.addedExpressions.put(fake, new ArrayList<>());
                this.variables.add(fake);
            }
            this.addedExpressions.get(fake).add(parser.parse(expression));
        }
    }

    /**
     * Add the All Different constraint to each variable.
     * @param variables
     * @return
     */
    public ArrayList<Constraint> allDifferent(Variable... variables){
        ArrayList<Constraint> constraints = new ArrayList<>();
        for(int i = 0; i < variables.length; i++)
            for(int j = i+1; j < variables.length; j++)
                addConstraint("x != y", variables[i], variables[j]);
        return constraints;
    }

    /**
     * Add the actual constraint to the model.
     * @param x Variable
     * @param y Variable
     * @param table Table of feasible tuples
     * @return
     */
    private Constraint buildConstraint(Variable x, Variable y, Table table){
        if(defaultFilter.equals(Constraint.AC3)) return new AC3(x, y, table);
        if(defaultFilter.equals(Constraint.AC4)) return new AC4(x, y, table);
        if(defaultFilter.equals(Constraint.AC6)) return new AC6(x, y, table);
        if(defaultFilter.equals(Constraint.AC2001)) return new AC2001(x, y, table);

        return null;
    }


    /**************************
     * ORDERING THE VARIABLES *
     **************************/

    /**
     * Sort all variables
     */
    private void orderVariables(){
        Collections.sort(variables);
    }

    /**
     * Sort all variables from index to the end.
     * @param index
     */
    private void orderVariables(int index){
        ArrayList<Variable> v1 = new ArrayList<>(variables);
        ArrayList<Variable> v2 = new ArrayList<>();
        for(int i = 0; i < index; i++) {
            v1.remove(0);
            v2.add(variables.get(i));
        }
        Collections.sort(v1);
        v2.addAll(v1);
        variables = v2;
    }

    /****************************
     * SEARCHING FOR A SOLUTION *
     ****************************/

    /**
     * Set a number of solution to find in the model.
     * @param number Number of solution. If number <= 0, search for all solutions
     */
    public void lookingForSolution(int number){
        this.nSol = number;
    }

    /**
     * Set the debug mode. If ON, print all the variables for every step in the tree.
     * @param debug
     */
    public void setDebugMode(boolean debug){
        this.DEBUG = debug;
    }

    /**
     * Solve the model.
     * @return true if there is at least one solution, false otherwise.
     */
    public boolean solve(){
        status = false;
        FAILS = 0;
        BACKTRACKS = 0;
        NSOLUTIONS = 0;
        int index = 0;

        buildTables();

        for(Variable x : constraints.keySet())
            for(Variable y : constraints.get(x).keySet())
                buildConstraint(x, y, constraints.get(x).get(y));

        time = clock.millis();

        while(true){
            if(index == variables.size()){
                status = true;
                index--;
                solutions.add(solution());
                variables.get(index).reset();
                NSOLUTIONS++;
                if(DEBUG) System.out.println("solution");
                if(NSOLUTIONS == nSol) return true;
            }

            for(Variable v : variables) v.setDepth(index);

            orderVariables(index);

            Variable v = variables.get(index);
            if(DEBUG) {
                System.out.println("\n---------------------------");
                System.out.println("DEPTH = " + index);
                System.out.println("---------------------------");
                for (int i = 0; i < variables.size(); i++) System.out.println(i + " " + variables.get(i).toString_());
            }

            if(v.setFirst()) {
                if (propagation.run()) {
                    if(DEBUG) System.out.println(index + " set to " + v.getDomain().getValue(0));

                    if(DEBUG) {
                        System.out.println("---------------------------");
                        for (int i = 0; i < variables.size(); i++) System.out.println(i + " " + variables.get(i).toString_());
                    }
                    index++;
                }
                else {
                    FAILS++;
                    for(Variable var : variables) var.resetDelta();
                    if(DEBUG) System.out.println(index + " set to " + v.getDomain().getValue(0) + " : fail");
                }
            } else {
                if(index == 0) {
                    time = clock.millis() - time;
                    return status;
                }
                else {
                    for(int i = index; i < variables.size(); i++) variables.get(i).resetChoices();
                    index--;
                    BACKTRACKS++;
                }
            }
        }
    }

    /****************************
     * RESULTS AND INFORMATIONS *
     ****************************/

    public int getBacktracks(){
        return BACKTRACKS;
    }

    public int getFails(){
        return FAILS;
    }

    public int getNSolutions(){
        return NSOLUTIONS;
    }

    public boolean status(){
        return this.status;
    }

    public String stats(){
        String stats = "Time (s) : " + time/1000.0;
        stats += "\n# Fails : " + getFails();
        stats += "\n# Backtracks : " + getBacktracks();
        stats += "\n# Solutions : " + getNSolutions();
        stats += "\nCONSTRAINT : " + defaultFilter;
        return stats+"\n";
    }

    private int[] solution(){
        int[] solution = new int[staticVariables.size()];
        for(int i = 0; i < staticVariables.size(); i++) solution[i] = staticVariables.get(i).getDomain().getValue(0);
        return solution;
    }

    public int[][] solutions(){
        int[][] solutions = new int[this.solutions.size()][];
        for(int s = 0; s < this.solutions.size(); s++) solutions[s] = this.solutions.get(s);
        return solutions;
    }



}
