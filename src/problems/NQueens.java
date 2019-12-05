package problems;

import solver.Model;
import solver.variables.Variable;
import tools.ArgumentReader;
import tools.builders.ExpressionBuilder;

public class NQueens {

    public static void main(String[] args){

        ArgumentReader reader = new ArgumentReader("-n", "12", "-c", "AC2001", "-sol", "0", "-debug", "false", "-print", "false");
        reader.read(args);

        Model model = new Model();

        int N = Integer.parseInt(reader.get("-n"));

        Variable[] queens = model.addVariables(N, 1, N);
        model.decisionVariables(queens);

        model.allDifferent(queens);
        for(int i = 0; i < N-1; i++){
            for(int j = i+1; j < N; j++){
                model.addConstraint("queen[i] != queen[j] + " + (j-i), queens[i], queens[j]);
                model.addConstraint("queen[i] != queen[j] - " + (j-i), queens[i], queens[j]);
            }
        }

        model.setFilter(reader.get("-c"));
        model.lookingForSolution(Integer.parseInt(reader.get("-sol")));
        model.setDebugMode(Boolean.parseBoolean(reader.get("-debug")));
        model.solve();

        System.out.print(model.stats());

        if(Boolean.parseBoolean(reader.get("-print"))) {
            for (int[] sol : model.solutions()) {
                for (int i = 0; i < N; i++) System.out.print(sol[i] + " ");
                System.out.println();
            }
        }

        for(int s : model.best()) System.out.print(s + " ");
    }

}
