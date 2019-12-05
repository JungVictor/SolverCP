package tools.expressions;

import solver.variables.Variable;

import java.util.ArrayList;

public class Objective {

    private Expression objective;
    private Variable[] variables;
    private int[] best_solution, values;
    private int best_value, last_value;
    private boolean minimize;

    private static final int MINF = Integer.MIN_VALUE, PINF = Integer.MAX_VALUE;

    public Objective(){}

    public void minimize(final Expression expression, final Variable[] variables){
        minimize = true;
        best_value = PINF;
        objective = expression;
        this.variables = variables;
        this.values = new int[variables.length];
    }

    public void maximize(final Expression expression, final Variable[] variables){
        minimize = false;
        best_value = MINF;
        objective = expression;
        this.variables = variables;
        this.values = new int[variables.length];
    }

    public int eval(){
        for(int i = 0; i < variables.length; i++) values[i] = variables[i].getDomainValue(0);
        last_value = objective.eval_int(values);
        return last_value;
    }

    public void keepBest(int ... values){
        if(eval() > best_value){
            if(!minimize){
                best_value = last_value;
                best_solution = values;
            }
        } else if(minimize){
            best_value = last_value;
            best_solution = values;
        }
    }

    public int getBestValue(){
        return best_value;
    }

    public int[] getBestSolution(){
        return best_solution;
    }

}
