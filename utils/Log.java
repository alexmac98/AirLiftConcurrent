package utils;

public class Log {
    /**
     * Method used for terminal logging.
     * @param tag The tag of the message.
     * @param message The content of the message.
     */
    public static void print(String tag, String message){
        System.out.println(String.format("[%s] - %s", tag, message));
    }
}
