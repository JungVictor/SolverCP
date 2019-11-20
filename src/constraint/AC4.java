package constraint;

import variables.SupportList;
import variables.Variable;

import java.util.ArrayList;
import java.util.Collection;

public class AC4 extends Constraint {

    private SupportList xSupports, ySupports;

    public AC4(Variable x, Variable y, Table table) {
        super(x, y, table);

        xSupports = new SupportList();
        ySupports = new SupportList();

        computeSupports();
    }

    @Override
    public void setIndex(int index) {
        xSupports.setIndex(index);
        ySupports.setIndex(index);
    }

    private void computeSupports(){
        ArrayList<int[]> tab = table.getTable();

        for(int i = 0; i < x.getDomainSize(); i++) xSupports.addKey(x.getDomainValue(i));
        for(int i = 0; i < y.getDomainSize(); i++) ySupports.addKey(y.getDomainValue(i));

        for(int i = 0; i < tab.size(); i++){
            int[] t = tab.get(i);
            xSupports.put(t[0], t[1]);
            ySupports.put(t[1], t[0]);
        }
    }


    @Override
    public boolean filterFrom(Variable v) {
        Variable v2;
        ArrayList<Integer> removed = v.getDeltaValues();

        SupportList vSupport, v2Support;
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
            Collection<Integer> supported = vSupport.getSupports(rem);
            // Pour chaque valeur supportée
            for(int s : supported){
                int index = v2Support.indexOf(s, rem);
                // S'il n'y a plus de support, on retire la valeur
                if(index >= 0 && v2Support.remove(s, index)) v2.removeValue(s);
            }
        }

        return !v2.isDomainEmpty();
    }
}
