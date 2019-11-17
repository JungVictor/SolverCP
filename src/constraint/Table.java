package constraint;
import variables.Variable;
import java.util.ArrayList;
import java.util.HashMap;

public class Table {

    private ArrayList<int[]> table;
    private HashMap<Integer, ArrayList<Integer>> xTable;

    private static String[] COMP_OP = {">", ">=", "<", "<=", "==", "!="},
                            ARITH_OP = {"+", "-", "*", "/"};

    /********************
     * CREATING A TABLE *
     ********************/

    public Table(Variable x, String op, Variable y){
        this();
        compare(x, op, y, "+", 0);
    }

    public Table(Variable x, String op, Variable y, String op2, int cons){
        this();
        compare(x, op, y, op2, cons);
    }

    public Table(){
        this.table = new ArrayList<>();
    }

    public Table(ArrayList<int[]> table){
        this();
        add(table);
    }

    public Table(int[][] tuples){
        this();
        add(tuples);
    }

    private void computeHashTable(){
        this.xTable = new HashMap<>();
        for(int[] t : table){
            if(!xTable.containsKey(t[0])) xTable.put(t[0], new ArrayList<>());
            xTable.get(t[0]).add(t[1]);
        }
    }

    private void addToTable(int xVal, int yVal){
        table.add(new int[]{xVal, yVal});
    }

    private boolean in(String symbol, String[] symbol_table){
        for(String s : symbol_table) if(s.equals(symbol)) return true;
        return false;
    }

    private boolean check(int xVal, String op, int yVal, String op2, int cons){
        int left = 0, right = 0;
        String compare;

        if(in(op, ARITH_OP)) {
            right = cons;
            compare = op2;
            if (op.equals("*")) left = xVal * yVal;
            else if (op.equals("/")) left = xVal / yVal;
            else if (op.equals("+")) left = xVal + yVal;
            else if (op.equals("-")) left = xVal - yVal;
        } else {
            left = xVal;
            compare = op;
            if (op2.equals("*")) right = yVal * cons;
            else if (op2.equals("/")) right = yVal / cons;
            else if (op2.equals("+")) right = yVal + cons;
            else if (op2.equals("-")) right = yVal - cons;
        }

        if(compare.equals("<")) return left < right;
        if(compare.equals("<=")) return left <= right;
        if(compare.equals(">")) return left > right;
        if(compare.equals(">=")) return left >= right;
        if(compare.equals("==")) return left == right;
        if(compare.equals("!=")) return left != right;

        return false;
    }

    private void compare(Variable x, String op, Variable y, String op2, int cons){
        ArrayList<Integer> xValues = x.getDomain().getValues();
        ArrayList<Integer> yValues = y.getDomain().getValues();

        for(int xVal : xValues) for(int yVal : yValues) if(check(xVal, op, yVal, op2, cons)) addToTable(xVal, yVal);

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


    /*******************************
     * ADD AN ELEMENT TO THE TABLE *
     *******************************/

    public void add(ArrayList<int[]> tables){
        this.table.addAll(tables);
    }

    public void add(int[] tuple){
        this.table.add(tuple);
    }

    public void add(int[][] table){
        for(int[] tuple : table) add(tuple);
    }

    public void add(int x, int y){
        add(new int[]{x, y});
    }


    /***********
     * GETTERS *
     ***********/

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
}
