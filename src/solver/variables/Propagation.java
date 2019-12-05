package solver.variables;

import solver.constraint.Constraint;

import java.util.LinkedList;

public class Propagation {

    private LinkedList<Variable> variables;

    public Propagation(){
        this.variables = new LinkedList<>();
    }

    public void add(Variable v){
        variables.add(v);
    }

    public Variable pick(){
        return variables.pop();
    }

    public boolean run(){
        while(!variables.isEmpty()){
            Variable v = pick();
            for(Constraint c : v.getConstraints()){
                boolean empty = !c.filterFrom(v);
                if(empty) {
                    variables = new LinkedList<>();
                    return false;
                }
            }
            v.resetDelta();
        }
        return true;
    }

}
