package utils;

public class Log {
    
    public static void print(String title, String message){
        System.out.println(String.format("[%s] - %s", title, message));
    }
}
