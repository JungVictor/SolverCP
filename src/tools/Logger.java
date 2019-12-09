package tools;

public class Logger {

    private static boolean PRINT = true;

    private static final String INFO  = "[INFO ] ",
                                DEBUG = "[DEBUG] ",
                                ERROR = "[ERROR] ";

    private Logger(){}

    public static void enable(){
        PRINT = true;
    }

    public static void disable(){
        PRINT = false;
    }

    public static void print(final String s){
        if(PRINT) System.out.print(s);
    }

    public static void lnprint(final String s){
        print("\n"+s);
    }

    public static void println(final String s){
        print(s+"\n");
    }

    public static void info(final String s){
        print(INFO+s);
    }

    public static void debug(final String s){
        print(DEBUG+s);
    }

    public static void error(final String s){
        print(ERROR+s);
    }

}
