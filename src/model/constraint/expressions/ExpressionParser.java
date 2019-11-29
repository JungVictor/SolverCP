package model.constraint.expressions;

import java.util.HashMap;
import static model.constraint.expressions.Expression.*;

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
        if(expr.contains(LEQ)) {
            comp = LEQ;
            return expr.split("\\"+LEQ);
        }
        if(expr.contains(GEQ)) {
            comp = GEQ;
            return expr.split("\\"+GEQ);
        }
        if(expr.contains(LT)) {
            comp = LT;
            return expr.split("\\"+LT);
        }
        if(expr.contains(GT)) {
            comp = GT;
            return expr.split("\\"+GT);
        }
        if(expr.contains(NEQ)){
            comp = NEQ;
            return expr.split("\\"+NEQ);
        }
        comp = EQ;
        return expr.split("\\"+EQ);
    }

    private String[] splitArith(String expr) {
        if (expr.contains(ABS)) {
            String[] abs = expr.split("\\"+ABS, 3);
            if (abs[0].isEmpty() && abs[2].isEmpty()) {
                lastOperator = ABS;
                return new String[]{abs[1]};
            }

            String not_null = abs[0];
            String[] splited;
            if (not_null.isEmpty()) not_null = abs[2];

            if (not_null.contains(PLUS)) {
                lastOperator = PLUS;
                splited = not_null.split("\\"+PLUS, 2);
            } else if (not_null.contains(MINUS)) {
                lastOperator = MINUS;
                splited = not_null.split("\\"+MINUS, 2);
            } else if (not_null.contains(MULT)) {
                lastOperator = MULT;
                splited = not_null.split("\\"+MULT, 2);
            } else {
                lastOperator = DIV;
                splited = not_null.split("\\"+DIV, 2);
            }
            if(abs[0].isEmpty()) return new String[]{ABS+abs[1]+ABS, splited[1]};
            else return new String[]{splited[0], ABS+abs[1]+ABS};
        }
        if (expr.contains(PLUS)) {
            lastOperator = PLUS;
            return expr.split("\\"+PLUS, 2);
        }
        if (expr.contains(MINUS)) {
            lastOperator = MINUS;
            String[] splited = expr.split("\\"+MINUS, 2);
            // -x
            if(splited[0].isEmpty()) return new String[]{"0", splited[1]};
            return splited;
        }
        if (expr.contains(MULT)) {
            lastOperator = MULT;
            return expr.split("\\"+MULT, 2);
        }
        if (expr.contains(DIV)) {
            lastOperator = DIV;
            return expr.split("\\"+DIV, 2);
        }
        lastOperator = null;
        return new String[]{expr};
    }

    private Expression createExpr(String expr){
        String[] splited = splitArith(expr);
        String current_op = lastOperator;
        if(splited.length == 1) {
            if(current_op != null && current_op.equals(ABS)) return new Expression(createExpr(splited[0]), current_op);
            try {
                return new Expression(Integer.parseInt(splited[0]));
            } catch (NumberFormatException e){
                bind(splited[0]);
                return new Expression(splited[0]);
            }
        } else return new Expression(createExpr(splited[0]), current_op, createExpr(splited[1]));
    }

    public Expression parse(String expr){
        this.binders = new HashMap<>();
        this.bind = 0;

        expr = expr.replace(" ", "");

        String[] splited = splitCompare(expr);
        String left = splited[0];
        String right = splited[1];


        Expression leftExpr = createExpr(left);
        Expression rightExpr = createExpr(right);
        if(comp.equals(MINUS)) rightExpr.changeSign();
        Expression expression = new Expression(leftExpr, comp, rightExpr);
        expression.setBinding(this.binders);
        return expression;
    }

}
