package model.constraint;

import model.variables.Variable;

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
        boolean supported;
        if(v == y){
            for(int xVal : x.getDomainValues()){
                supported = false;
                for(int yVal : y.getDomainValues()){
                    if(isCompatible(xVal, yVal)) {
                        supported = true;
                        break;
                    }
                }
                if(!supported) x.removeValue(xVal);
            }
            return !x.isDomainEmpty();
        } else {
            for(int yVal : y.getDomainValues()){
                supported = false;
                for(int xVal : x.getDomainValues()){
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