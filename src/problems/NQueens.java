package problems;

import model.constraint.Constraint;
import model.Model;
import model.variables.Variable;

public class NQueens {

    public static void main(String[] args){

        Model model = new Model();

        int N = 12;

        Variable[] queens = model.addVariables(N, 1, N);

        model.allDifferent(queens);
        for(int i = 0; i < N-1; i++){
            for(int j = i+1; j < N; j++){
                model.addConstraint("queen[i] != queen[j] + " + (j-i), queens[i], queens[j]);
                model.addConstraint("queen[i] != queen[j] - " + (j-i), queens[i], queens[j]);
            }
        }
        model.setDefaultFilter(Constraint.AC3);
        model.lookingForSolution(0);
        model.setDebugMode(false);
        model.solve();

        System.out.print(model.stats());
    }

}
