package constraint;

import variables.SupportList;
import variables.Variable;

import java.util.ArrayList;
import java.util.Collection;

public class AC6 extends Constraint {

    private SupportList supports;

    public AC6(Variable x, Variable y, Table table) {
        super(x, y, table);

        supports = new SupportList();

        computeSupports();
    }

    @Override
    public void setIndex(int index) {
        supports.setIndex(index);
    }

    private void computeSupports(){
        ArrayList<int[]> tab = table.getTable();

        for(int i = 0; i < y.getDomainSize(); i++) supports.addKey(y.getDomainValue(i));

        for(int i = 0; i < tab.size(); i++){
            int[] t = tab.get(i);
            supports.put(t[1], t[0]);
        }
    }

    @Override
    public boolean filterFrom(Variable v) {
        ArrayList<Integer> removed = v.getDeltaValues();
        if(v == x){
            for(int key : supports.keySet()) {
                if (!supports.isEmpty(key)) for (int rem : removed) {
                    int index = supports.indexOf(key, rem);
                    if (index >= 0 && supports.remove(key, index)) y.removeValue(key);
                }
            }
            return !y.isDomainEmpty();
        }
        else{
            boolean hasSupport = false;
            for(int rem : removed) {
                Collection<Integer> supported = supports.getSupports(rem);
                for (int value : supported) {
                    for (int yVal : v.getDomainValues()) {
                        if (table.isCompatible(value, yVal)) {
                            hasSupport = true;
                            break;
                        }
                    }
                    if (!hasSupport) x.removeValue(value);
                    hasSupport = false;
                }

            }
            return !x.isDomainEmpty();
        }
    }
}
