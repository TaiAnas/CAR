package cmd.Exception;

public class BadOrderException extends Exception {

    public BadOrderException(){
        System.out.println("Bad order of commands");
    }
}