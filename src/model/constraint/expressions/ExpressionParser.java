package model.constraint.expressions;

import java.util.HashMap;
import static model.constraint.expressions.Expression.*;

public class ExpressionParser {

    // Binding between variables' name and their index.
    private HashMap<String, Integer> binders;
    private int bind;

    // Last operator parsed
    private String lastOperator;
    // Comparison operator (unique)
    private String comp;

    public ExpressionParser(){}

    /**
     * Bind a variable's name to the index, if it's not already binded.
     * @param name Variable's name
     */
    private void bind(String name){
        if(this.binders.containsKey(name)) return;
        this.binders.put(name, this.bind++);
    }

    /**
     * Split the expression in two part according to the comparison operator.
     * @param expr Mathematical expression
     * @return Two sub-expression (left and right)
     */
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

    /**
     * Split the expression according to the most important operator in it.
     * @param expr Mathematical expression
     * @return Two sub-expressions (left and right) or one sub-expression (left --- unary operator case)
     */
    private String[] splitArith(String expr) {
        if (expr.contains(ABS)) {
            String[] abs = expr.split("\\"+ABS, 3);
            if (abs[0].isEmpty() && abs[2].isEmpty()) {
                lastOperator = ABS;
                return new String[]{abs[1]};
            }

            String not_null = abs[0];
            String[] split;
            if (not_null.isEmpty()) not_null = abs[2];

            if (not_null.contains(MODULO)){
                lastOperator = MODULO;
                split = not_null.split("\\"+MODULO, 2);
            } else if (not_null.contains(PLUS)) {
                lastOperator = PLUS;
                split = not_null.split("\\"+PLUS, 2);
            } else if (not_null.contains(MINUS)) {
                lastOperator = MINUS;
                split = not_null.split("\\"+MINUS, 2);
            } else if (not_null.contains(MULT)) {
                lastOperator = MULT;
                split = not_null.split("\\"+MULT, 2);
            } else {
                lastOperator = DIV;
                split = not_null.split("\\"+DIV, 2);
            }
            if(abs[0].isEmpty()) return new String[]{ABS+abs[1]+ABS, split[1]};
            else return new String[]{split[0], ABS+abs[1]+ABS};
        }
        if (expr.contains(MODULO)){
            lastOperator = MODULO;
            return expr.split("\\"+MODULO, 2);
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

    /**
     * Given a mathematical expression, output the real Expression (arithmetical).
     * @param expr Mathematical expression
     * @return Expression
     */
    private Expression createExpr(String expr){
        String[] split = splitArith(expr);
        String current_op = lastOperator;
        if(split.length == 1) {
            if(current_op != null && current_op.equals(ABS)) return new Expression(createExpr(split[0]), current_op);
            try {
                return new Expression(Integer.parseInt(split[0]));
            } catch (NumberFormatException e){
                bind(split[0]);
                return new Expression(split[0]);
            }
        } else {
            Expression left = createExpr(split[0]);
            Expression right = createExpr(split[1]);
            if(current_op.equals(MINUS)) right.changeSign();
            return new Expression(left, current_op, right);
        }
    }

    /**
     * Given a mathematical expression (comparison), output the real Expression.
     * @param expr Mathematical expression
     * @return Expression
     */
    public Expression parse(String expr){
        // Init
        this.binders = new HashMap<>();
        this.bind = 0;

        // Delete useless spaces
        expr = expr.replace(" ", "");

        // Split in two subexpression to compare
        String[] split = splitCompare(expr);
        String left = split[0];
        String right = split[1];

        // Create the expression for the two subexpressions.
        Expression lExpr = createExpr(left);
        Expression rExpr = createExpr(right);
        lExpr.simple(); rExpr.simple();
        Expression expression = new Expression(lExpr, comp, rExpr);
        expression.setBinding(this.binders);
        return expression;
    }

}
