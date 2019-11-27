import constraint.Constraint;
import constraint.expressions.Expression;
import variables.Variable;
import constraint.expressions.ExpressionParser;

public class Main {

    public static void main(String[] args){

        Model model = new Model();

        int N = 13;

        Variable[] queens = model.addVariables(N, 1, N);

        model.allDifferent(queens);
        for(int i = 0; i < N-1; i++){
            for(int j = i+1; j < N; j++){
                model.addConstraint("x != y + " + (j-i), queens[i], queens[j]);
                model.addConstraint("x != y - " + (j-i), queens[i], queens[j]);
                //model.addConstraint(queens[i], "!=", queens[j], "+", (j-i));
                //model.addConstraint(queens[i], "!=", queens[j], "-", (j-i));
            }
        }
        model.setDefaultFilter(Constraint.AC3);
        model.lookingForSolution(0);
        model.setDebugMode(false);

        model.solve();

        System.out.print(model.stats());

    }

}
