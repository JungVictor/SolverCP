package solver.constraint;

import solver.variables.Delta;
import solver.variables.Variable;
import structures.set.ReversibleSet;
import structures.supports.ReversibleSupportSet;
import structures.supports.ReversibleUnorderedSupportSet;

public class AC6 extends Constraint {

    private ReversibleSupportSet supports;

    public AC6(Variable x, Variable y, Table table) {
        super(x, y, table);

        supports = new ReversibleUnorderedSupportSet();

        computeSupports();
    }

    @Override
    public void setIndex(int index) {
        supports.setIndex(index);
    }

    private void computeSupports(){
        for(int i = 0; i < y.getDomainSize(); i++) supports.addKey(y.getDomainValue(i));

        for(int[] t : table) supports.put(t[1], t[0]);

        supports.build();
    }

    @Override
    public boolean filterFrom(Variable v) {
        Delta removed = v.getDelta();
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
                ReversibleSet supported = supports.getSupports(rem);
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