package solver.constraint;
import solver.variables.Variable;

public abstract class Constraint {

    public static final int AC3 = 0, AC4 = 1, AC6 = 2, AC2001 = 3, UNDEFINED = -1;

    Variable x, y;
    Table table;
    protected int index;

    public Table getTable(){
        return table;
    }

    public Constraint(Variable x, Variable y, Table table){
        this.x = x;
        this.y = y;

        this.table = table;

        this.x.addConstraint(this);
        this.y.addConstraint(this);
    }

    public void setIndex(int index){
        this.index = index;
    }

    public boolean filterFrom(Variable v){
        return false;
    }

    public static int toInt(String constraint){
        if(constraint.equals("AC3")) return AC3;
        if(constraint.equals("AC4")) return AC4;
        if(constraint.equals("AC6")) return AC6;
        if(constraint.equals("AC2001")) return AC2001;
        return UNDEFINED;
    }

    public static String toString(int constraint){
        if(constraint == AC3) return "AC3";
        if(constraint == AC4) return "AC4";
        if(constraint == AC6) return "AC6";
        if(constraint == AC2001) return "AC2001";
        return "UNDEFINED";
    }
}
