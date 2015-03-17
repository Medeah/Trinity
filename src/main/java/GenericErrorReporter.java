public interface GenericErrorReporter {

    public int getErrorAmount();

    // Default errors:
    public void reportError(String message);

    // Type errors;
    public void reportTypeError(Type.TrinityType expectedType, Type.TrinityType receivedType);
}
