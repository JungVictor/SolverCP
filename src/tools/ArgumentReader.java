package tools;

import java.util.HashMap;

public class ArgumentReader {

    private HashMap<String, String> arguments;

    public ArgumentReader(String... default_args){
        this.arguments = new HashMap<>();
        for(int i = 0; i < default_args.length; i++) arguments.put(default_args[i], default_args[++i]);
    }

    public void read(String[] args){
        for(int i = 0; i < args.length; i++)
            if(args[i].charAt(0) == '-') arguments.replace(args[i], args[++i]);
    }

    public String get(String key){
        return arguments.get(key);
    }

}
