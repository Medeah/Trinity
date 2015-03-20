package CustomExceptions;

public class SymbolAlreadyDefinedException extends Exception {

    //Parameterless Constructor
    public SymbolAlreadyDefinedException() {
    }

    //Constructor that accepts a message
    public SymbolAlreadyDefinedException(String message) {
        super(message);
    }
}