package tools.builders;

import tools.expressions.Expression;
import tools.expressions.ExpressionParser;

public class ExpressionBuilder {

    private static final ExpressionParser parser = new ExpressionParser();
    private ExpressionBuilder(){}

    static public Expression create(String expression){
        return parser.parse(expression);
    }

    static public Expression create_arith(String expression){
        return parser.parse_arith(expression);
    }

    static public Expression sum(String pattern, int N){
        String var = "var_";
        StringBuilder sum = new StringBuilder();
        for(int i = 0; i < N-1; i++) sum.append(pattern.replace("var", var + i).replace("i", i + "")).append(" + ");
        sum.append(pattern.replace("var", var + (N - 1)).replace("i", (N - 1) + ""));
        return parser.parse_arith(sum.toString());
    }

}
