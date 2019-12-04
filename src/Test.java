import model.Model;
import model.constraint.expressions.Expression;
import model.constraint.expressions.ExpressionParser;
import model.variables.Domain;
import model.variables.Variable;
import structures.UnorderedReversibleList;

import java.util.function.Predicate;

public class Test {

    public static void main(String[] args){

        String test = "(1 - 1) * 20 + 20 - 40 + 20 < x";
        ExpressionParser parser = new ExpressionParser();
        Expression expression = parser.parse(test);

        System.out.println(expression);

        /*
        Model model = new Model();

        Variable x = model.addVariable(1, 10);
        Variable y = model.addVariable(1, 10);

        ExpressionParser parser = new ExpressionParser();
        // -x - 2 + y
        // 0 - x - 2 + y
        // 0 - (x - 2 + y)
        // 0 - (x - (2 + y))
        // 0 - (x + (2 - y))
        // 0 - (x - (2 + y))
        // 0 - (x - 2 - y)
        // -x + 2 + y
        Expression expression = parser.parse("-x + 1 - 1 - y < -5");
        System.out.println(expression);

        model.addConstraint("-x - 1 - 1 - y < -5", x, y);

        model.solve();
        System.out.print(model.stats());


        /////////////////////
        // PRINT THE SOLUTION
        for(int[] sol : model.solutions()){
            //for(int s : sol) System.out.print(s + " ");
            //System.out.println();
        }

         */

    }

}
