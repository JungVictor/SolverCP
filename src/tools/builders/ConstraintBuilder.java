package tools.builders;

import solver.constraint.*;
import solver.variables.Variable;

public class ConstraintBuilder {

    public static Constraint constraint(Variable x, Variable y, Table table, int c){
        if(c == Constraint.AC3) return AC3(x, y, table);
        if(c == Constraint.AC4) return AC4(x, y, table);
        if(c == Constraint.AC6) return AC6(x, y, table);
        if(c == Constraint.AC2001) return AC2001(x, y, table);
        return null;
    }

    private ConstraintBuilder(){}

    public static AC3 AC3(Variable x, Variable y, Table table){
        return new AC3(x, y, table);
    }

    public static AC4 AC4(Variable x, Variable y, Table table){
        return new AC4(x, y, table);
    }

    public static AC6 AC6(Variable x, Variable y, Table table){
        return new AC6(x, y, table);
    }

    public static AC2001 AC2001(Variable x, Variable y, Table table){
        return new AC2001(x, y, table);
    }

}
