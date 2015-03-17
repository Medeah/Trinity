public class ErrorReporter implements GenericErrorReporter {
    private int errorAmount = 0;
    private boolean failOnError;

    public ErrorReporter(boolean fail) {
        failOnError = fail;
    }

    public ErrorReporter() {
        failOnError = true;
    }

    @Override
    public int getErrorAmount() {
        return errorAmount;
    }

    @Override
    public void reportError(String message) {
        System.out.println("ERROR: " + message);
        errorAmount++;
        if (failOnError) {
            System.exit(1);
        }
    }
}