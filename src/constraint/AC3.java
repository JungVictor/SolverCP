package constraint;

import variables.Variable;

import java.util.ArrayList;

public class AC3 extends Constraint {

    public AC3(Variable x, Variable y, Table table){
        super(x, y , table);
    }

    private boolean isCompatible(int xVal, int yVal){
        return table.isCompatible(xVal, yVal);
    }

    @Override
    public boolean filterFrom(Variable v) {
        ArrayList<Integer> xValues = new ArrayList<>(x.getDomainValues());
        ArrayList<Integer> yValues = new ArrayList<>(y.getDomainValues());
        boolean supported;
        if(v == y){
            for(int xVal : xValues){
                supported = false;
                for(int yVal : yValues){
                    if(isCompatible(xVal, yVal)) {
                        supported = true;
                        break;
                    }
                }
                if(!supported) x.removeValue(xVal);
            }
            return !x.isDomainEmpty();
        } else {
            for(int yVal : yValues){
                supported = false;
                for(int xVal : xValues){
                    if(isCompatible(xVal, yVal)) {
                        supported = true;
                        break;
                    }
                }
                if(!supported) y.removeValue(yVal);
            }
            return !y.isDomainEmpty();
        }
    }
}
