package constraint;

import variables.Variable;

import java.util.ArrayList;
import java.util.HashMap;

public class AC2001 extends Constraint {

    private HashMap<Integer, int[]> xSupport, ySupport;

    public AC2001(Variable x, Variable y, Table table) {
        super(x, y, table);
        xSupport = new HashMap<>();
        ySupport = new HashMap<>();

        computeSupports();
    }

    private void computeSupports(){
        ArrayList<int[]> tab = table.getTable();
        for(int i = 0; i < tab.size(); i++){
            int[] t = tab.get(i);
            if(!xSupport.containsKey(t[0])) xSupport.put(t[0], new int[]{t[1], i});
            if(!ySupport.containsKey(t[1])) ySupport.put(t[1], new int[]{t[0], i});
        }
    }

    private boolean searchSupport(Variable v, int value, int index){
        if(v == x) {
            for (int yVal : y.getDomainValues()) {
                if (table.isCompatible(value, yVal, index)) {
                    xSupport.put(value, new int[]{yVal, 0});
                    return true;
                }
            }
        } else {
            for (int xVal : x.getDomainValues()) {
                if (table.isCompatible(xVal, value, index)) {
                    ySupport.put(value, new int[]{xVal, 0});
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean filterFrom(Variable v) {
        Variable v2;
        HashMap<Integer, int[]> v2Support;
        if(v == x) {
            v2 = y;
            v2Support = ySupport;
        }
        else {
            v2 = x;
            v2Support = xSupport;
        }

        // Removed values from v
        ArrayList<Integer> removedValues = v.getDelta().getDelta();
        ArrayList<Integer> domain = new ArrayList<>(v2.getDomain().getValues());

        for(int removed : removedValues){
            for(int key : domain){
                int[] support = v2Support.get(key);
                // Si la valeur n'a plus de support
                if(support[0] == removed) {
                    // Si on ne trouve pas de nouveau support, on supprime
                    if(!searchSupport(v2, key, support[1])) v2.removeValue(key);
                }
            }
        }

        return !v2.isDomainEmpty();
    }
}
