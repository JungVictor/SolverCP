import tools.builders.ExpressionBuilder;
import tools.expressions.Expression;
import tools.expressions.ExpressionParser;
import static tools.builders.ExpressionBuilder.*;

public class Test {

    public static void main(String[] args){
        System.out.println(ExpressionBuilder.sequence("var + var", "+", 3));
        System.out.println(sum("var^2 + x", 3));

        System.out.println(ExpressionBuilder.create("%c*%c < 5", 2, 2));

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
