package problems;

import model.Model;
import model.constraint.Constraint;
import model.variables.Variable;

public class GolombRuler {

    public static void main(String[] args){
        Model model = new Model();

        //////////////////////
        // PROBLEM'S CONSTANTS
        int N = 9;
        int UB = 44;

        int F = ((N-1)*N)/2;

        ////////////
        // VARIABLES
        Variable[] variables = model.addVariables(N, 0, UB);
        Variable[] diff = model.addVariables(F, 0, UB);

        //////////////
        // CONSTRAINTS
        model.allDifferent(variables);
        model.allDifferent(diff);

        int cpt = 0;
        for(int i = 0; i < N-1; i++)
            for(int j = i+1; j < N; j++)
                model.addConstraint("diff[i][j] = var[j] - var[i]", diff[cpt++], variables[j], variables[i]);

        for(int i =0; i < N-1; i++)
            model.addConstraint("var[i] < var[j]", variables[i], variables[i+1]);

        model.addConstraint("var[0] = 0", variables[0]);

        //////////////////////
        // SOLVER'S PARAMETERS
        model.setDefaultFilter(Constraint.AC2001);
        model.lookingForSolution(1);
        model.setDebugMode(false);

        //////////////////
        // SOLVING PROBLEM
        model.solve();

        ///////////////////////
        // METRICS ABOUT SOLVER
        System.out.print(model.stats());

        /////////////////////
        // PRINT THE SOLUTION
        for(int[] sol : model.solutions()){
            for(int i = 0; i < N; i++) System.out.print(sol[i] + " ");
            System.out.println();
        }
    }

}
