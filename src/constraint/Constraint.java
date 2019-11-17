package constraint;
import variables.Variable;

public abstract class Constraint {

    public static final String AC3 = "AC3", AC4 = "AC4", AC6 = "AC6", AC2001 = "AC2001";
    public static final String LT = "<";

    protected Variable x, y;
    protected Table table;
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
}
