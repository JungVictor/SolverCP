package solver;

import solver.constraint.couples.Couple;
import solver.constraint.Constraint;
import solver.constraint.Table;
import solver.constraint.couples.NCouple;
import solver.variables.Domain;
import solver.variables.Propagation;
import solver.variables.Variable;
import tools.Logger;
import tools.builders.ConstraintBuilder;
import tools.builders.ExpressionBuilder;
import tools.expressions.Expression;
import tools.expressions.Objective;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class Model {

    // TIME
    private Clock clock;
    private long time;
    private long construction;

    // MODEL
    private Propagation propagation;
    private ArrayList<Variable> allVariables, decisionVariables;
    private int filter = Constraint.AC3;  // Default filter used
    private Objective objective;

    // CONSTRAINTS
    private HashMap<Variable, ArrayList<Expression>> unaryConstraints;
    private HashSet<Couple> binaryConstraints;
    private HashSet<NCouple> naryConstraints;
    private HashMap<String, Expression> expressions;

    // INFORMATIONS
    private boolean status = false;         // Is there a solution ?
    private ArrayList<int[]> solutions;     // All solutions
    private int nSol = 0;                   // Number of solution we're looking for
    private int BACKTRACKS, FAILS, NSOLUTIONS;
    private boolean DEBUG = false;

    public Model(){
        this.propagation = new Propagation();

        this.allVariables = new ArrayList<>();
        this.decisionVariables = new ArrayList<>();

        this.expressions = new HashMap<>();

        this.unaryConstraints = new HashMap<>();
        this.binaryConstraints = new HashSet<>();
        this.naryConstraints = new HashSet<>();

        this.solutions = new ArrayList<>();
        this.objective = new Objective();

        this.clock = Clock.systemDefaultZone();
    }


    /**
     * Set the filter used by the solver
     * @param filter
     */
    public void setFilter(int filter){
        this.filter = filter;
    }

    /**
     * Set the filter used by the solver
     * @param filter
     */
    public void setFilter(String filter){
        this.filter = Constraint.toInt(filter);
    }

    /**
     * Add a new objective function that minimize the expression given with the variables binded
     * @param expression
     * @param variables
     */
    public void minimize(Expression expression, Variable... variables){
        objective.minimize(expression, variables);
    }

    /**
     * Add a new objective function that minimize the expression given with the variables binded
     * @param expression
     * @param variables
     */
    public void minimize(String expression, Variable... variables){
        objective.minimize(ExpressionBuilder.create_arith(expression), variables);
    }

    /**
     * Add a new objective function that maximize the expression given with the variables binded
     * @param expression
     * @param variables
     */
    public void maximize(Expression expression, Variable... variables){
        objective.maximize(expression, variables);
    }

    /**
     * Add a new objective function that maximize the expression given with the variables binded
     * @param expression
     * @param variables
     */
    public void maximize(String expression, Variable... variables){
        objective.maximize(ExpressionBuilder.create_arith(expression), variables);
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
        this.allVariables.add(v);
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

    /**
     * Add a variable to the list of variables for which we decide a value during the solving.
     * @param v
     */
    public void decisionVariable(Variable v){
        this.decisionVariables.add(v);
    }

    /**
     * Add variables to the list of variables for which we decide a value during the solving.
     * @param variables
     */
    public void decisionVariables(Variable[] variables){
        Collections.addAll(decisionVariables, variables);
    }

    /**************************************************
     * ADD A CONSTRAINT TO THE MODEL (UNDER THE HOOD) *
     **************************************************/

    /**
     * Get an expression corresponding to the expression given in string.
     * Tries to search in already existing expression before creating a new one.
     * @param expression
     * @return
     */
    private Expression getExpression(final String expression){
        if(expressions.containsKey(expression)) return expressions.get(expression);
        final Expression e = ExpressionBuilder.create(expression);
        expressions.put(expression, e);
        return e;
    }

    /**
     * Add a constraint corresponding to the given expression between the set of variables
     * @param expression
     * @param variables
     */
    public void addConstraint(final String expression, Variable... variables){
        if(variables.length == 0) return;
        if(variables.length == 1) addUnaryConstraint(getExpression(expression), variables[0]);
        else if(variables.length == 2) addBinaryConstraint(getExpression(expression), variables[0], variables[1]);
        else addNaryConstraint(getExpression(expression), variables);
    }

    public void addConstraint(String expression, int[] constants, Variable... variables){
        expression = ExpressionBuilder.replace(expression, constants);
        addConstraint(expression, variables);
    }

    /**
     * Add a allDifferent constraint between the set of variables
     * @param variables
     */
    public void allDifferent(Variable... variables){
        for(int i = 0; i < variables.length; i++)
            for(int j = i+1; j < variables.length; j++)
                addConstraint("x != y", variables[i], variables[j]);
    }

    /**
     * Add an unary constraint corresponding to the expression binding the variable v
     * @param expression
     * @param v
     */
    private void addUnaryConstraint(Expression expression, Variable v){
        if(!unaryConstraints.containsKey(v)) unaryConstraints.put(v, new ArrayList<>());
        unaryConstraints.get(v).add(expression);
    }

    /**
     * Add a binary constraint corresponding to the expression binding the two variables x and y
     * @param expression
     * @param x
     * @param y
     */
    private void addBinaryConstraint(Expression expression, Variable x, Variable y){
        for(Couple couple : binaryConstraints) {
            if(couple.equals(x, y)) {
                couple.addExpression(x, y, expression);
                return;
            }
        }
        Couple couple = new Couple(x, y, expression);
        this.binaryConstraints.add(couple);
    }

    /**
     * Add a n-ary constraint between the expression and a set of variable
     * @param expression
     * @param variables
     */
    private void addNaryConstraint(Expression expression, Variable... variables){
        for(NCouple couple : naryConstraints){
            if(couple.equals(variables)){
                couple.addExpression(expression, variables);
                return;
            }
        }
        NCouple couple = new NCouple(expression, variables);
        this.naryConstraints.add(couple);
    }


    /**
     * Add the actual constraint to the model.
     * @param x Variable
     * @param y Variable
     * @param table Table of feasible tuples
     * @return
     */
    private Constraint buildConstraint(Variable x, Variable y, Table table){
        return ConstraintBuilder.constraint(x, y, table, 0);
    }

    /**
     * Evaluate the expression, building the tables and constraints
     */
    private void build(){
        // Unary constraints (=> domain filtering)
        for(Variable v : unaryConstraints.keySet())
           for(Expression expression : unaryConstraints.get(v))
               v.filter(expression);

        // Binary constraints
        for(Couple couple : binaryConstraints)
            buildConstraint(couple.getX(), couple.getY(), couple.build());

        // N-ary constraints
        for(NCouple couple : naryConstraints){
           Table[] tables = couple.build();
           Variable fake = addVariable(0, couple.getSize() - 1);
           for(int i = 0; i < tables.length; i++)
                buildConstraint(fake, couple.getVariable(i), tables[i]);
        }
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

        construction = clock.millis();
        build();

        if(decisionVariables.size() == 0) decisionVariables = allVariables;

        time = clock.millis();
        construction = time - construction;

        while(true){
            if(index == decisionVariables.size()){
                status = true;
                index--;
                solutions.add(solution());
                if(objective != null) objective.keepBest(solutions.get(NSOLUTIONS));
                decisionVariables.get(index).reset();
                NSOLUTIONS++;
                if(DEBUG) Logger.debug("solution");
                if(NSOLUTIONS == nSol) {
                    time = clock.millis() - time;
                    return true;
                }
            }

            for(Variable v : allVariables) v.setDepth(index);

            Variable v = decisionVariables.get(index);
            if(DEBUG) {
                Logger.println("\n---------------------------");
                Logger.println("DEPTH = " + index);
                Logger.println("---------------------------");
                for (int i = 0; i < allVariables.size(); i++) Logger.println(i + " " + allVariables.get(i).toString_());
            }

            if(v.setFirst()) {
                if (propagation.run()) {
                    if(DEBUG) Logger.println(index + " set to " + v.getDomain().getValue(0));

                    if(DEBUG) {
                        Logger.println("---------------------------");
                        for (int i = 0; i < allVariables.size(); i++) Logger.println(i + " " + allVariables.get(i).toString_());
                    }
                    index++;
                }
                else {
                    FAILS++;
                    for(Variable var : allVariables) var.resetDelta();
                    if(DEBUG) Logger.println(index + " set to " + v.getDomain().getValue(0) + " : fail");
                }
            } else {
                if(index == 0) {
                    time = clock.millis() - time;
                    return status;
                }
                else {
                    for(int i = index; i < decisionVariables.size(); i++) decisionVariables.get(i).resetChoices();
                    index--;
                    BACKTRACKS++;
                }
            }
        }
    }

    /****************************
     * RESULTS AND INFORMATIONS *
     ****************************/

    /**
     * Get the number of backtrack
     * @return
     */
    public int getBacktracks(){
        return BACKTRACKS;
    }

    /**
     * Get the number of fails
     * @return
     */
    public int getFails(){
        return FAILS;
    }

    /**
     * Get the number of solution
     * @return
     */
    public int getNSolutions(){
        return NSOLUTIONS;
    }

    /**
     * Get the status of the solver (there exists a solution or not)
     * @return
     */
    public boolean status(){
        return this.status;
    }

    /**
     * All the stats
     * @return
     */
    public String stats(){
        String stats = "Building (s) : " + construction/1000.0;
        stats += "\nExecution (s) : " + time/1000.0;
        stats += "\n# Fails : " + getFails();
        stats += "\n# Backtracks : " + getBacktracks();
        stats += "\n# Solutions : " + getNSolutions();
        if(nSol == 0) {
            if(NSOLUTIONS == 0) stats += " (no solution)";
            else if(NSOLUTIONS == 1) stats += " (unique solution)";
            else stats += " (all solutions)";
        }
        else if(nSol == 1) stats += " (while searching for 1 solution)";
        else stats += " (while searching for " + nSol + " solutions)";
        stats += "\nCONSTRAINT : " + Constraint.toString(filter);
        return stats+"\n";
    }

    /**
     * Add a solution to the pool of solutions
     * @return
     */
    private int[] solution(){
        int[] solution = new int[decisionVariables.size()];
        for(int i = 0; i < decisionVariables.size(); i++) solution[i] = decisionVariables.get(i).getDomain().getValue(0);
        return solution;
    }

    /**
     * Get all the solutions stored by the solver
     * @return
     */
    public int[][] solutions(){
        int[][] solutions = new int[this.solutions.size()][];
        for(int s = 0; s < this.solutions.size(); s++) solutions[s] = this.solutions.get(s);
        return solutions;
    }

    /**
     * Get the best solution, if available (there is an objective)
     * @return
     */
    public int[] best(){
        if(objective != null) return objective.getBestSolution();
        return this.solutions.get(0);
    }
}
