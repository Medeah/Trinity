import java.util.ArrayList;

public class TestErrorReporter implements ErrorReporter {
    private int errorAmount;
    private ArrayList<String> errorList;

    public TestErrorReporter() {
        errorAmount = 0;
        errorList = new ArrayList<String>();
    }

    public String getError(int i) {
        return errorList.get(i);
    }

    @Override
    public int getErrorAmount() {
        return errorAmount;
    }

    private void errorHandling(String message) {
        errorList.add(message);
        errorAmount++;
    }

    @Override
    public void reportError(String message) {
        errorHandling("ERROR: " + message);
    }

    @Override
    public void reportTypeError(Type.TrinityType expectedType, Type.TrinityType receivedType) {
        errorHandling("TYPE ERROR: " + "Expected type " + expectedType + " but got " + receivedType);
    }
}
