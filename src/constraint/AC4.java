package constraint;

import variables.Variable;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Integer.valueOf;

public class AC4 extends Constraint {

    private ArrayList<HashMap<Integer, ArrayList<Integer>>> xSupports, ySupports;
    private HashMap<Integer, ArrayList<Integer>> xSupport, ySupport;

    public AC4(Variable x, Variable y, Table table) {
        super(x, y, table);

        xSupports = new ArrayList<>();
        ySupports = new ArrayList<>();

        xSupport = new HashMap<>();
        ySupport = new HashMap<>();

        computeSupports();
    }

    private void restore(){
        xSupport = copy(xSupports.get(index));
        ySupport = copy(ySupports.get(index));
    }

    @Override
    public void setIndex(int index) {
        if(this.index < index) {
            this.index = index;
            xSupports.add(index, copy(xSupport));
            ySupports.add(index, copy(ySupport));
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

        for(int i = 0; i < x.getDomainSize(); i++) xSupport.put(x.getDomainValue(i), new ArrayList<>());
        for(int i = 0; i < y.getDomainSize(); i++) ySupport.put(y.getDomainValue(i), new ArrayList<>());

        for(int i = 0; i < tab.size(); i++){
            int[] t = tab.get(i);
            xSupport.get(t[0]).add(t[1]);
            ySupport.get(t[1]).add(t[0]);
        }

        xSupports.add(0, copy(xSupport));
        ySupports.add(0, copy(ySupport));
    }


    @Override
    public boolean filterFrom(Variable v) {
        Variable v2;
        ArrayList<Integer> removed = v.getDeltaValues();

        HashMap<Integer, ArrayList<Integer>> vSupport, v2Support;
        if(v == x){
            v2 = y;
            vSupport = xSupport;
            v2Support = ySupport;
        }
        else{
            v2 = x;
            vSupport = ySupport;
            v2Support = xSupport;
        }
        if(removed.isEmpty()) return !v2.isDomainEmpty();


        // Pour chaque valeur retirée de v
        for(int rem : removed){
            // Toutes les valeurs supportées par rem
            ArrayList<Integer> supported = vSupport.get(rem);
            vSupport.put(rem, new ArrayList<>());
            // Pour chaque valeur supportée
            for(int s : supported){
                // On retire rem de la liste des supports
                v2Support.get(s).remove(valueOf(rem));
                // S'il n'y a plus de support, on retire la valeur
                if(v2Support.get(s).isEmpty()) v2.removeValue(s);
            }
        }

        return !v2.isDomainEmpty();
    }
}
