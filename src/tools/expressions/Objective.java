package tools.expressions;

import solver.variables.Variable;

import java.util.ArrayList;

public class Objective {

    private ArrayList<Expression> objectives;
    private ArrayList<Variable[]> variables;
    private ArrayList<Integer> best_values;
    private int[] best_solution, values, last_value;
    private ArrayList<Boolean> minimize;

    private static final int MINF = Integer.MIN_VALUE, PINF = Integer.MAX_VALUE;

    public Objective(){
        this.objectives = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.best_values = new ArrayList<>();
        this.minimize = new ArrayList<>();
    }

    public void minimize(final Expression expression, final Variable[] variables){
        minimize.add(true);
        best_values.add(PINF);
        objectives.add(expression);
        this.variables.add(variables);
    }

    public void maximize(final Expression expression, final Variable[] variables){
        minimize.add(false);
        best_values.add(MINF);
        objectives.add(expression);
        this.variables.add(variables);
    }

    public int[] eval(){
        last_value = new int[variables.size()];
        for(int i = 0; i < variables.size(); i++) {
            values = new int[variables.get(i).length];
            for(int j = 0; j < variables.get(i).length; j++)
                values[j] = variables.get(i)[j].getDomainValue(0);
            last_value[i] = objectives.get(i).eval_int(values);
        }
        return last_value;
    }

    private void save(int[] all_variables){
        best_values = new ArrayList<>();
        for (int value : last_value) best_values.add(value);
        best_solution = all_variables;
    }

    public void keepBest(int[] all_variables){
        eval();
        for(int i = 0; i < last_value.length; i++){
            if(minimize.get(i)){
                if(last_value[i] > best_values.get(i)) return;  // Worse
                if(last_value[i] < best_values.get(i)){         // Better
                    save(all_variables);
                    return;
                }
            } else {
                if(last_value[i] < best_values.get(i)) return; // Worse
                if(last_value[i] > best_values.get(i)){
                    save(all_variables);
                    return;
                }
            }
        }
    }

    public int[] getBestSolution(){
        return best_solution;
    }

}
