import constraint.Constraint;
import variables.Variable;

public class Main {

    public static void main(String[] args){
        Model model = new Model();

        int N = 12;

        Variable[] queens = model.addVariables(N, 1, N);

        model.allDifferent(queens);
        for(int i = 0; i < N-1; i++){
            for(int j = i+1; j < N; j++){
                model.addConstraint(queens[i], "!=", queens[j], "+", (j-i));
                model.addConstraint(queens[i], "!=", queens[j], "-", (j-i));
            }
        }

        model.setDefaultFilter(Constraint.AC4);
        model.lookingForSolution(0);
        model.setDebugMode(false);

        model.solve();

        System.out.print(model.stats());

    }

}
