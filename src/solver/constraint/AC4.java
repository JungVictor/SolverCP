package solver.constraint;

import solver.variables.Delta;
import solver.variables.Variable;
import structures.set.ReversibleSet;
import structures.supports.ReversibleSupportSet;
import structures.supports.ReversibleUnorderedSupportSet;

public class AC4 extends Constraint {

    private ReversibleSupportSet xSupports, ySupports;

    public AC4(Variable x, Variable y, Table table) {
        super(x, y, table);

        xSupports = new ReversibleUnorderedSupportSet();
        ySupports = new ReversibleUnorderedSupportSet();

        computeSupports();
    }

    @Override
    public void setIndex(int index) {
        xSupports.setIndex(index);
        ySupports.setIndex(index);
    }

    private void computeSupports(){
        for(int xVal : x.getDomainValues()) xSupports.addKey(xVal);
        for(int yVal : y.getDomainValues()) ySupports.addKey(yVal);

        for (int[] t : table) {
            if (x.contains(t[0]) && y.contains(t[1])) {
                xSupports.put(t[0], t[1]);
                ySupports.put(t[1], t[0]);
            }
        }

        xSupports.build();
        ySupports.build();
    }


    @Override
    public boolean filterFrom(Variable v) {
        Variable v2;
        Delta removed = v.getDelta();

        ReversibleSupportSet vSupport, v2Support;
        if(v == x){
            v2 = y;
            vSupport = xSupports;
            v2Support = ySupports;
        }
        else{
            v2 = x;
            vSupport = ySupports;
            v2Support = xSupports;
        }
        if(removed.isEmpty()) return !v2.isDomainEmpty();


        // Pour chaque valeur retirée de v
        for(int rem : removed){
            // Toutes les valeurs supportées par rem
            ReversibleSet supported = vSupport.getSupports(rem);
            if(supported != null) {
                // Pour chaque valeur supportée
                for (int s : supported) {
                    int index = v2Support.indexOf(s, rem);
                    // S'il n'y a plus de support, on retire la valeur
                    if (index >= 0 && v2Support.remove(s, index)) v2.removeValue(s);
                }
            }
        }

        return !v2.isDomainEmpty();
    }
}
