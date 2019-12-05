package solver.constraint;
import tools.expressions.Expression;
import tools.builders.ExpressionBuilder;
import solver.variables.Variable;
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

    public Table(Expression expr, Variable x, Variable y){
        this();
        computeTuples(expr, x, y);
    }

    public Table(String expr, Variable x, Variable y){
        this();
        computeTuples(ExpressionBuilder.create(expr), x, y);
    }

    public Table(){
        this.table = new ArrayList<>();
        this.iterator = new TableIterator();
    }

    public Table(ArrayList<int[]> table){
        this();
        add(table);
    }

    public Table(int[][] tuples){
        this();
        add(tuples);
    }

    public void computeHashTable(){
        this.xTable = new HashMap<>();
        for(int[] t : table){
            if(!xTable.containsKey(t[0])) xTable.put(t[0], new ArrayList<>());
            xTable.get(t[0]).add(t[1]);
        }
    }

    private void addToTable(int xVal, int yVal){
        table.add(new int[]{xVal, yVal});
    }

    private void computeTuples(Expression expr, Variable x, Variable y){

        for(int xVal : x.getDomainValues()) for(int yVal : y.getDomainValues()) if(expr.eval(xVal, yVal)) addToTable(xVal, yVal);
        computeHashTable();
    }

    /***********************
     * OPERATION ON TABLES *
     ***********************/

    public Table intersection(Table t){
        Table res = new Table();
        for(int i = 0; i < table.size(); i++){
            int[] tuple1 = table.get(i);
            for(int j = 0; j < t.getTable().size(); j++){
                int[] tuple2 = t.getTuple(j);
                if(tuple1[0] == tuple2[0] && tuple1[1] == tuple2[1]) res.add(tuple1);
            }
        }
        res.computeHashTable();
        return res;
    }

    public Table union(Table t){
        Table res = new Table();
        res.add(this.getTable());
        res.add(t.getTable());
        res.computeHashTable();
        return res;
    }

    /*******************************
     * ADD AN ELEMENT TO THE TABLE *
     *******************************/

    public void add(ArrayList<int[]> tables){
        this.table.addAll(tables);
        computeHashTable();
    }

    public void add(int[] tuple){
        this.table.add(tuple);
    }

    public void add(int[][] table){
        for(int[] tuple : table) add(tuple);
        computeHashTable();
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