package cmd.Exception;

public class NotDirException extends Exception{

    public NotDirException(){
        System.out.println("Not a directory");
    }
}
