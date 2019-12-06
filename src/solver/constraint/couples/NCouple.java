package solver.constraint.couples;

import solver.constraint.Table;
import solver.variables.Variable;
import tools.expressions.Expression;

import java.util.ArrayList;
import java.util.HashMap;

public class NCouple {

    private Variable[] variables;
    private HashMap<Variable, Integer> indexes;
    private HashMap<Expression, int[]> expressions;
    private int size = 0;

    public NCouple(Variable... variables){
        this.indexes = new HashMap<>();
        this.expressions = new HashMap<>();
        this.variables = variables;

        for(int i = 0; i < variables.length; i++) this.indexes.put(variables[i], i);
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
            order[i] = this.indexes.get(variables[i]);
        return order;
    }

    public boolean equals(Variable... variables){
        if(variables.length != this.indexes.size()) return false;
        for(Variable v : variables) if(!this.indexes.containsKey(v)) return false;
        return true;
    }

    private int[] reorder(int[] values, Expression expression){
        int[] order = expressions.get(expression);
        int[] reorder = new int[order.length];
        for(int i = 0; i < variables.length; i++){
            reorder[i] = values[indexes.get(variables[order[i]])];
        }
        return reorder;
    }

    public Table[] build(){
        Table[] tables = new Table[indexes.size()];

        ArrayList<int[]> tuples = new ArrayList<>();

        int[] counts = new int[indexes.size()];
        int[] values = new int[indexes.size()];
        int index = 0;

        while(true) {
            if (index == 0 && counts[index] == variables[0].getDomainSize()) break;
            if (index == indexes.size()) {
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
            if (counts[index] >= variables[index].getDomainSize()) {
                counts[index] = 0;
                index--;
            } else {
                values[index] = variables[index].getDomainValue(counts[index]);
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
        return variables[index];
    }

    public int getSize(){
        return size;
    }

}
