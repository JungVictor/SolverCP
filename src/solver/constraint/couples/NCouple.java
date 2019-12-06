package solver.constraint.couples;

import solver.constraint.Table;
import solver.variables.Variable;
import tools.expressions.Expression;

import java.util.ArrayList;
import java.util.HashMap;

public class NCouple {

    private Variable[] ordered;
    private HashMap<Variable, Integer> variables;
    private HashMap<Expression, int[]> expressions;
    private int size = 0;

    public NCouple(Variable... variables){
        this.variables = new HashMap<>();
        this.expressions = new HashMap<>();
        this.ordered = variables;

        for(int i = 0; i < variables.length; i++) this.variables.put(variables[i], i);
    }

    public NCouple(Expression expression, Variable... variables){
        this(variables);
        addExpression(expression, variables);
    }

    public void addExpression(Expression expression, Variable... variables){
        expressions.put(expression, ordering(variables));
    }

    private int[] ordering(Variable[] variables){
        int[] order = new int[variables.length];
        for(int i = 0; i < variables.length; i++)
            order[i] = this.variables.get(variables[i]);
        return order;
    }

    public boolean equals(Variable... variables){
        if(variables.length != this.variables.size()) return false;
        for(Variable v : variables) if(!this.variables.containsKey(v)) return false;
        return true;
    }

    private int[] reorder(int[] values, Expression expression){
        int[] order = expressions.get(expression);
        int[] reorder = new int[order.length];
        for(int i = 0; i < ordered.length; i++){
            reorder[i] = values[variables.get(ordered[order[i]])];
        }
        return reorder;
    }

    public Table[] build(){
        Table[] tables = new Table[variables.size()];

        ArrayList<int[]> tuples = new ArrayList<>();

        int[] counts = new int[variables.size()];
        int[] values = new int[variables.size()];
        int index = 0;

        while(true) {
            if (index == 0 && counts[index] == ordered[0].getDomainSize()) break;
            if (index == variables.size()) {
                boolean find = true;
                for(Expression expression : expressions.keySet()) if(find && !expression.eval(reorder(values, expression))) find = false;
                if (find) {
                    int[] new_values = new int[values.length];
                    System.arraycopy(values, 0, new_values, 0, values.length);
                    tuples.add(new_values);
                }
                index--;
            }
            if (index < 0) break;
            if (counts[index] >= ordered[index].getDomainSize()) {
                counts[index] = 0;
                index--;
            } else {
                values[index] = ordered[index].getDomainValue(counts[index]);
                counts[index] += 1;
                index++;
            }
        }

        for(int i = 0; i < tables.length; i++) tables[i] = new Table();

        for(int i = 0; i < tuples.size(); i++)
            for(int t = 0; t < tables.length; t++)
                tables[t].add(i, tuples.get(i)[t]);

        for(Table table : tables) table.computeHashTable();

        size = tuples.size();
        return tables;
    }

    public Variable getVariable(int index){
        return ordered[index];
    }

    public int getSize(){
        return size;
    }

}
