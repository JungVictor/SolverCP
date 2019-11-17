package constraint;

import variables.Variable;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Integer.valueOf;

public class AC6 extends Constraint {

    private ArrayList<HashMap<Integer, ArrayList<Integer>>> supports;
    private HashMap<Integer, ArrayList<Integer>> support;

    public AC6(Variable x, Variable y, Table table) {
        super(x, y, table);

        supports = new ArrayList<>();
        support = new HashMap<>();

        computeSupports();
    }

    private void restore(){
        support = copy(supports.get(index));
    }

    @Override
    public void setIndex(int index) {
        if(this.index < index) {
            this.index = index;
            supports.add(index, copy(support));
        } else {
            this.index = index;
            this.restore();
        }
    }

    private HashMap<Integer, ArrayList<Integer>> copy(HashMap<Integer, ArrayList<Integer>> support){
        HashMap<Integer, ArrayList<Integer>> copy = new HashMap<>();
        for(int key : support.keySet()) copy.put(key, new ArrayList<>(support.get(key)));
        return copy;
    }

    private void computeSupports(){
        ArrayList<int[]> tab = table.getTable();

        for(int i = 0; i < y.getDomainSize(); i++) support.put(y.getDomainValue(i), new ArrayList<>());

        for(int i = 0; i < tab.size(); i++){
            int[] t = tab.get(i);
            support.get(t[1]).add(t[0]);
        }
        supports.add(0, copy(support));
    }

    @Override
    public boolean filterFrom(Variable v) {
        ArrayList<Integer> removed = v.getDeltaValues();
        if(v == x){
            for(int key : support.keySet()) {
                if (!support.get(key).isEmpty()) for (int rem : removed) {
                    int index = support.get(key).indexOf(rem);
                    if (index >= 0) {
                        support.get(key).remove(index);
                        if (support.get(key).isEmpty()) y.removeValue(key);
                    }
                }
            }
            return !y.isDomainEmpty();
        }
        else{
            boolean hasSupport = false;
            for(int rem : removed) {
                ArrayList<Integer> supported = support.get(rem);
                support.put(rem, new ArrayList<>());
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
