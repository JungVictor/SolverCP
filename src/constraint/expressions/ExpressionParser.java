package constraint.expressions;

import variables.Variable;

import java.util.HashMap;

public class ExpressionParser {

    private HashMap<String, Integer> binders;
    private int bind;
    private String lastOperator;
    private String comp;

    public ExpressionParser(){}

    private void bind(String name){
        if(this.binders.containsKey(name)) return;
        this.binders.put(name, this.bind++);
    }

    private String[] splitCompare(String expr){
        if(expr.contains("<=")) {
            comp = "<=";
            return expr.split("<=");
        }
        if(expr.contains(">=")) {
            comp = ">=";
            return expr.split(">=");
        }
        if(expr.contains("<")) {
            comp = "<";
            return expr.split("<");
        }
        if(expr.contains(">")) {
            comp = ">";
            return expr.split(">");
        }
        if(expr.contains("==")){
            comp = "==";
            return expr.split("==");
        }
        comp = "!=";
        return expr.split("!=");
    }

    private String[] splitArith(String expr) {
        if (expr.contains("|")) {
            String[] abs = expr.split("\\|", 3);
            if (abs[0].isEmpty() && abs[2].isEmpty()) {
                lastOperator = "abs";
                return new String[]{abs[1]};
            }

            String not_null = abs[0];
            String[] splitted;
            if (not_null.isEmpty()) not_null = abs[2];

            String op = "";
            if (not_null.contains("+")) {
                lastOperator = "+";
                splitted = not_null.split("\\+", 2);
            } else if (not_null.contains("-")) {
                lastOperator = "-";
                splitted = not_null.split("-", 2);
            } else if (not_null.contains("*")) {
                lastOperator = "*";
                splitted = not_null.split("\\*", 2);
            } else {
                lastOperator = "/";
                splitted = not_null.split("/", 2);
            }
            if(abs[0].isEmpty()) return new String[]{"|"+abs[1]+"|", splitted[1]};
            else return new String[]{splitted[0], "|"+abs[1]+"|"};
        }
        if (expr.contains("+")) {
            lastOperator = "+";
            return expr.split("\\+", 2);
        }
        if (expr.contains("-")) {
            lastOperator = "-";
            return expr.split("-", 2);
        }
        if (expr.contains("*")) {
            lastOperator = "*";
            return expr.split("\\*", 2);
        }
        if (expr.contains("/")) {
            lastOperator = "/";
            return expr.split("/", 2);
        }
        lastOperator = null;
        return new String[]{expr};
    }

    private Expression createExpr(String expr){
        String[] splitted = splitArith(expr);
        String current_op = lastOperator;
        if(splitted.length == 1) {
            if(current_op != null && current_op.equals("abs")) return new Expression(createExpr(splitted[0]), current_op);
            try {
                return new Expression(Integer.parseInt(splitted[0]));
            } catch (NumberFormatException e){
                bind(splitted[0]);
                return new Expression(splitted[0]);
            }
        } else return new Expression(createExpr(splitted[0]), current_op, createExpr(splitted[1]));
    }

    public Expression parse(String expr){
        this.binders = new HashMap<>();
        this.bind = 0;

        expr = expr.replace(" ", "");

        String[] splitted = splitCompare(expr);
        String left = splitted[0];
        String right = splitted[1];


        Expression leftExpr = createExpr(left);
        Expression rightExpr = createExpr(right);
        if(comp.equals("-")) rightExpr.changeSign();
        Expression expression = new Expression(leftExpr, comp, rightExpr);
        expression.setBinding(this.binders);
        return expression;
    }

}
