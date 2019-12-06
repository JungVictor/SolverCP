package solver.constraint.couples;

import solver.constraint.Table;
import solver.variables.Variable;
import tools.expressions.Expression;

import java.util.ArrayList;

public class Couple {

    private Variable x, y;
    private ArrayList<Expression> expressionsXY, expressionsYX;

    public Couple(Variable x, Variable y){
        this.x = x;
        this.y = y;
        this.expressionsXY = new ArrayList<>();
        this.expressionsYX = new ArrayList<>();
    }

    public Couple(Variable x, Variable y, Expression expression){
        this(x, y);
        addExpression(x, y, expression);
    }

    public void addExpression(Variable a, Variable b, Expression expression){
        if(a == x) expressionsXY.add(expression);
        else expressionsYX.add(expression);
    }

    public boolean equals(Variable a, Variable b){
        return (a == x && b == y) || (b == x && a == y);
    }

    public Table build(){
        Table table = new Table();
        boolean check;
        for(int xVal : x.getDomainValues()){
            for(int yVal : y.getDomainValues()){
                check = true;
                for(Expression xy : expressionsXY) if(!xy.eval(xVal, yVal)) check = false;
                if(check) for(Expression yx : expressionsYX) if(!yx.eval(yVal, xVal)) check = false;
                if(check) table.add(xVal, yVal);
            }
        }
        table.computeHashTable();
        return table;
    }

    public Variable getX(){
        return x;
    }

    public Variable getY(){
        return y;
    }

}
