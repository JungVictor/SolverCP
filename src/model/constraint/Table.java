package model.constraint;
import model.constraint.expressions.Expression;
import model.constraint.expressions.ExpressionParser;
import model.variables.Variable;
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

    public Table(Expression expr, Variable x, Variable y){
        this();
        computeTuples(expr, x, y);
    }

    private ExpressionParser parser = new ExpressionParser();
    public Table(String expr, Variable x, Variable y){
        this();
        computeTuples(parser.parse(expr), x, y);
    }

    public Table(Variable x, String op, Variable y){
        this();
        compare(x, "+", 0, op, y, "+", 0);
    }

    public Table(Variable x, String op, Variable y, String op2, int cons){
        this();
        compare(x, "+", 0, op, y, op2, cons);
    }

    public Table(Variable x, String op, int cons1, String op2, Variable y, String op3, int cons2){
        this();
        compare(x, op, cons1, op2, y, op3, cons2);
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

    private boolean in(String symbol, String[] symbol_table){
        for(String s : symbol_table) if(s.equals(symbol)) return true;
        return false;
    }

    private int eval(int v1, String op, int v2){
        if (op.equals("*")) return v1 * v2;
        else if (op.equals("/")) return v1 / v2;
        else if (op.equals("+")) return v1 + v2;
        else return v1 - v2;
    }



    private boolean check(int xVal, String op, int cons1, String op2, int yVal, String op3, int cons2){
        int left, right;
        String compare;

        if(in(op, COMP_OP)){
            compare = op;
            left = xVal;
            if(op3.equals("+") || op3.equals("-")) right = eval(eval(cons1, op2, yVal), op3, cons2);
            else right = eval(cons1, op2, eval(yVal, op3, cons2));
        } else if(in(op2, COMP_OP)){
            compare = op2;
            left = eval(xVal, op, cons1);
            right = eval(yVal, op3, cons2);
        } else {
            compare = op3;
            if(op2.equals("+") || op2.equals("-")) left = eval(eval(xVal, op, cons1), op2, yVal);
            else left = eval(xVal, op, eval(cons1, op2, yVal));
            right = cons2;
        }

        if(compare.equals("<")) return left < right;
        if(compare.equals("<=")) return left <= right;
        if(compare.equals(">")) return left > right;
        if(compare.equals(">=")) return left >= right;
        if(compare.equals("==")) return left == right;
        if(compare.equals("!=")) return left != right;

        return false;
    }

    private void compare(Variable x, String op, int cons1, String op2, Variable y, String op3, int cons2){

        for(int xVal : x.getDomainValues()) for(int yVal : y.getDomainValues()) if(check(xVal, op, cons1, op2, yVal, op3, cons2)) addToTable(xVal, yVal);

        computeHashTable();
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