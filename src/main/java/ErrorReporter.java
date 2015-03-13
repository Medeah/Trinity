public class ErrorReporter {
    private int errors = 0;
    private boolean failOnError;

    public ErrorReporter(boolean fail) {
        failOnError = fail;
    }

    public int getErrors() {
        return errors;
    }

    public void reportError(String message) {
        System.out.println("ERROR: " + message);
        errors++;
        if (failOnError) {
            System.exit(1);
        }
    }
}