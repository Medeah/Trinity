package CustomExceptions;

public class SymbolNotFoundException extends Exception {

    //Parameterless Constructor
    public SymbolNotFoundException() {
    }

    //Constructor that accepts a message
    public SymbolNotFoundException(String message) {
        super(message);
    }
}