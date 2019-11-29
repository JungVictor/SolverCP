package model.variables;

import java.util.ArrayList;

public class Delta {

    ArrayList<Integer> delta;

    public String toString(){
        if(delta.isEmpty()) return "{}";
        StringBuilder res = new StringBuilder("{");
        for(int i = 0; i < delta.size()-1; i++) res.append(delta.get(i)).append(", ");
        return res.toString() + delta.get(delta.size()-1) + "}";
    }

    public Delta copy(){
        Delta copy = new Delta();
        for(int v : delta) copy.add(v);
        return copy;
    }

    public Delta(){
        delta = new ArrayList<>();
    }

    public boolean isEmpty(){
        return delta.isEmpty();
    }

    public void reset(){
        delta = new ArrayList<>();
    }

    public ArrayList<Integer> getDelta(){
        return this.delta;
    }

    public void add(int a){
        delta.add(a);
    }

}
