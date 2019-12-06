package solver.constraint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Table implements Iterable<int[]>{

    private TableIterator iterator;
    private ArrayList<int[]> table;
    private HashMap<Integer, ArrayList<Integer>> xTable;

    /********************
     * CREATING A TABLE *
     ********************/

    public Table(){
        this.table = new ArrayList<>();
        this.iterator = new TableIterator();
    }

    public void computeHashTable(){
        this.xTable = new HashMap<>();
        for(int[] t : table){
            if(!xTable.containsKey(t[0])) xTable.put(t[0], new ArrayList<>());
            xTable.get(t[0]).add(t[1]);
        }
    }

    /*******************************
     * ADD AN ELEMENT TO THE TABLE *
     *******************************/

    public void add(int[] tuple){
        this.table.add(tuple);
    }

    public void add(int x, int y){
        add(new int[]{x, y});
    }


    /***********
     * GETTERS *
     ***********/

    public int size(){
        return this.table.size();
    }

    public ArrayList<int[]> getTable() {
        return table;
    }

    public int[] getTuple(int index){
        return table.get(index);
    }

    public boolean isCompatible(int xVal, int yVal){
        return isCompatible(xVal, yVal, 0);
    }

    public boolean isCompatible(int xVal, int yVal, int index){
        if(!xTable.containsKey(xVal)) return false;
        for(int s : xTable.get(xVal)) if(s == yVal) return true;

        /*

        for(int i = index; i < table.size(); i++){
            int[] t = table.get(i);
            if(t[0] == xVal && t[1] == yVal) return true;
        }

        for(int i = 0; i < index; i++){
            int[] t = table.get(i);
            if(t[0] == xVal && t[1] == yVal) return true;
        }

         */


        return false;
    }

    @Override
    public Iterator<int[]> iterator(){
        iterator.index = 0;
        return iterator;
    }

    private class TableIterator implements Iterator<int[]>{

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public int[] next() {
            return getTuple(index++);
        }
    }
}