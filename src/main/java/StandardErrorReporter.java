public class StandardErrorReporter implements ErrorReporter {
    private int errorAmount = 0;
    private boolean failOnError;

    public StandardErrorReporter(boolean fail) {
        failOnError = fail;
    }

    public StandardErrorReporter() {
        failOnError = true;
    }


    @Override
    public int getErrorAmount() {
        return errorAmount;
    }

    private void errorHandling(String message) {
        System.out.println(message);
        errorAmount++;
        if (failOnError) {
            System.exit(1);
        }
    }

    @Override
    public void reportError(String message) {
        errorHandling("ERROR: " + message);
    }

    @Override
    public void reportTypeError(Type expectedType, Type receivedType) {
        errorHandling("TYPE ERROR: " + "Expected type " + expectedType + " but got " + receivedType);
    }
}