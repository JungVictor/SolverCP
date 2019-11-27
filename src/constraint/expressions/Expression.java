package constraint.expressions;

import java.util.HashMap;

public class Expression {

    public static String[] comparison = {"<", "<=", ">", ">=", "==", "!="};
    public static String[] arithmetic = {"+", "-", "*", "/"};
    public static String[] unair = {"abs"};

    private HashMap<String, Integer> binding;

    private Expression left;
    private Expression right;
    private String operator;

    private String variable;
    private int constant;

    private int nVar = 0;

    public void setBinding(HashMap<String, Integer> binding){
        this.binding = new HashMap<>(binding);
    }

    public Expression(int constant){
        this.constant = constant;
    }

    public Expression(String variable){
        this.variable = variable;
        this.nVar = 1;
    }

    public Expression(Expression left, String op_unair){
        this.left = left;
        this.nVar = left.nVar;
        this.operator = op_unair;
    }

    public Expression(Expression left, String op, Expression right){
        this.left = left;
        this.operator = op;
        this.right = right;
        this.nVar = left.nVar + right.nVar;
    }

    public boolean isLeaf(){
        return left == null && right == null;
    }

    public void changeSign(){
        if(operator.equals("+")) operator = "-";
        else if(operator.equals("-")) operator = "+";
    }

    private int neutralValue(){
        if(operator.equals("*") || operator.equals("/")) return 1;
        return 0;
    }

    public int nVar(){
        return nVar;
    }

    private int localEvaluation(int left, int right){
        if(operator.equals("+")) return left + right;
        else if(operator.equals("-")) return left - right;
        else if(operator.equals("*")) return left * right;
        else if(operator.equals("/")) return left / right;

        // ABS
        if(left < 0) return -left;
        return left;
    }

    public int eval_int(HashMap<String, Integer> binding, int... values){
        if(isLeaf()){
            if(variable != null) return values[binding.get(variable)];
            else return constant;
        } else {
            int left_eval = left.eval_int(binding, values);
            int right_eval;
            if(right != null) right_eval = right.eval_int(binding, values);
            else right_eval = neutralValue();
            return localEvaluation(left_eval, right_eval);
        }
    }

    private boolean check(int left, int right){
        if(operator.equals("<")) return left < right;
        else if(operator.equals("<=")) return left <= right;
        else if(operator.equals(">")) return left > right;
        else if(operator.equals(">=")) return left >= right;
        else if(operator.equals("==")) return left == right;
        return left != right;
    }

    public boolean eval(int... values){

        int left_eval = left.eval_int(binding, values);
        int right_eval = right.eval_int(binding, values);

        return check(left_eval, right_eval);
    }

}
