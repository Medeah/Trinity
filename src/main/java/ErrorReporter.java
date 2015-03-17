public class ErrorReporter implements GenericErrorReporter {
    private int errors = 0;
    private boolean failOnError;

    public ErrorReporter(boolean fail) {
        failOnError = fail;
    }

    public ErrorReporter() {
        failOnError = true;
    }

    @Override
    public int getErrors() {
        return errors;
    }

    @Override
    public void reportError(String message) {
        System.out.println("ERROR: " + message);
        errors++;
        if (failOnError) {
            System.exit(1);
        }
    }
}