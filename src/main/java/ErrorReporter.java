public interface ErrorReporter {

    public int getErrorAmount();

    // Default errors:
    public void reportError(String message);

    // Type errors;
    public void reportTypeError(Type expectedType, Type receivedType);
}
