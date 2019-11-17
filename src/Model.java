import constraint.*;
import variables.Domain;
import variables.Propagation;
import variables.Variable;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class Model {

    private Clock clock;
    private long time;
    private Propagation propagation;
    private ArrayList<Variable> variables;
    private HashMap<Variable, HashMap<Variable, Table>> constraints;
    private String defaultFilter = Constraint.AC3;
    private boolean status = false;
    private ArrayList<int[]> solutions;

    private int BACKTRACKS, FAILS, NSOLUTIONS;
    private int nSol = 0;
    private boolean DEBUG = false;

    public Model(){
        this.propagation = new Propagation();
        this.variables = new ArrayList<>();
        this.solutions = new ArrayList<>();
        this.constraints = new HashMap<>();
        this.clock = Clock.systemDefaultZone();
    }

    public void setDefaultFilter(String filter){
        defaultFilter = filter;
    }

    /*******************************
     * ADD A VARIABLE TO THE MODEL *
     *******************************/

    public Variable addVariable(Domain domain){
        Variable v = new Variable(domain, propagation);
        this.variables.add(v);
        return v;
    }

    public Variable addVariable(int lb, int ub){
        return addVariable(new Domain(lb, ub));
    }

    public Variable addVariable(int[] values){
        return addVariable(new Domain(values));
    }

    public Variable[] addVariables(int n, Domain domain){
        Variable[] variables = new Variable[n];
        for(int i = 0; i < n; i++) variables[i] = addVariable(domain);
        return variables;
    }

    public Variable[] addVariables(int n, int lb, int ub){
        return addVariables(n, new Domain(lb, ub));
    }

    public Variable[] addVariables(int n, int[] values){
        return addVariables(n, new Domain(values));
    }

    /**************************************************
     * ADD A CONSTRAINT TO THE MODEL (UNDER THE HOOD) *
     **************************************************/

    private boolean existsConstraint(Variable x, Variable y){
        return constraints.containsKey(x) && constraints.get(x).containsKey(y);
    }

    private Table getConstraintTable(Variable x, Variable y){
        if(existsConstraint(x, y)) return constraints.get(x).get(y);
        else return null;
    }

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

    /********************************************
     * ADD A CONSTRAINT TO THE MODEL (FOR USER) *
     ********************************************/

    public void addConstraint(Variable x, Variable y, Table table, String filter){
        addConstraintToMap(x, y, table);
    }

    public void addConstraint(Variable x, String op, Variable y){
        addConstraint(x, y, new Table(x, op, y), defaultFilter);
    }

    public void addConstraint(Variable x, String op, Variable y, String op2, int cons){
        addConstraint(x, y, new Table(x, op, y, op2, cons), defaultFilter);
    }

    public void addConstraint(Variable x, Variable y, Table table){
        addConstraint(x, y, table, defaultFilter);
    }

    public void addConstraint(Variable x, Variable y, int[][] tuples, String filter){
        Table table = new Table(tuples);
        addConstraint(x, y, table, filter);
    }

    public void addConstraint(Variable x, Variable y, int[][] tuples){
        Table table = new Table(tuples);
        addConstraint(x, y, table, defaultFilter);
    }

    public ArrayList<Constraint> allDifferent(Variable... variables){
        ArrayList<Constraint> constraints = new ArrayList<>();
        for(int i = 0; i < variables.length; i++){
            for(int j = i+1; j < variables.length; j++){
                addConstraint(variables[i], "!=", variables[j]);
            }
        }
        return constraints;
    }

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

    private void orderVariables(){
        Collections.sort(variables);
    }

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

    public void lookingForSolution(int number){
        this.nSol = number;
    }

    public void setDebugMode(boolean debug){
        this.DEBUG = debug;
    }

    public boolean solve(){
        status = false;
        FAILS = 0;
        BACKTRACKS = 0;
        NSOLUTIONS = 0;
        int index = 0;


        for(Variable v : variables) v.initDomains(variables.size());

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
        int[] solution = new int[variables.size()];
        for(int i = 0; i < variables.size(); i++) solution[i] = variables.get(i).getDomain().getValue(0);
        return solution;
    }

    public int[][] solutions(){
        int[][] solutions = new int[this.solutions.size()][];
        for(int s = 0; s < this.solutions.size(); s++) solutions[s] = this.solutions.get(s);
        return solutions;
    }



}
