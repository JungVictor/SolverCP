package problems;

import solver.Model;
import solver.variables.Variable;
import tools.ArgumentReader;
import tools.builders.ExpressionBuilder;

public class GolombRuler {

    public static void main(String[] args) {

        ArgumentReader reader = new ArgumentReader("-n", "9", "-ub", "44", "-c", "AC2001", "-sol", "1", "-print", "true", "-debug", "false", "-opti", "true");
        reader.read(args);

        Model model = new Model();

        //////////////////////
        // PROBLEM'S CONSTANTS
        int N = Integer.parseInt(reader.get("-n"));
        int UB = Integer.parseInt(reader.get("-ub"));

        int F = ((N - 1) * N) / 2;

        ////////////
        // VARIABLES
        Variable[] variables = model.addVariables(N, 0, UB);
        Variable[] diff = model.addVariables(F, 0, UB);

        model.decisionVariables(variables);
        //////////////
        // CONSTRAINTS

        model.allDifferent(variables);
        model.allDifferent(diff);

        model.addConstraint("var[0] = 0", variables[0]);

        int cpt = 0;
        for (int i = 0; i < N - 1; i++){
            for (int j = i + 1; j < N; j++) {
                model.addConstraint("diff[i][j] = var[j] - var[i]", diff[cpt], variables[j], variables[i]);
                model.addConstraint("diff[i][j] >= " + ((j - i) * (j - i + 1) / 2), diff[cpt]);
                model.addConstraint("diff[i][j] <= var[m] - " + ((N-1-j+1)*(N-j+i)/2), diff[cpt++], variables[N-1]);
            }
        }

        model.addConstraint("diff[0][0] < diff[n][n]", diff[0], diff[F-1]);

        for(int i =0; i < N-1; i++)
            model.addConstraint("var[i] < var[j]", variables[i], variables[i+1]);

        //////////////////////
        // SOLVER'S PARAMETERS
        model.setFilter(reader.get("-c"));
        model.lookingForSolution(Integer.parseInt(reader.get("-sol")));
        model.setDebugMode(Boolean.parseBoolean(reader.get("-debug")));

        //////////////////
        // SOLVING PROBLEM
        if(Boolean.parseBoolean(reader.get("-opti"))) {
            // Minimize all variables, priority from end to start
            for(int i = N-1; i >= 0; i--) model.minimize("var[i]", variables[i]);
        }
        model.solve();

        ///////////////////////
        // METRICS ABOUT SOLVER
        System.out.print(model.stats());

        /////////////////////
        // PRINT THE SOLUTION
        if(Boolean.parseBoolean(reader.get("-print"))) {
            for (int[] sol : model.solutions()) {
                for (int s : sol) System.out.print(s + " ");
                System.out.println();
            }
        }

        if(Boolean.parseBoolean(reader.get("-opti"))){
            if(model.status()) {
                System.out.print("OPTIMAL SOLUTION : ");
                for (int s : model.best()) System.out.print(s + " ");
                System.out.println();
            }
        }

    }

}
