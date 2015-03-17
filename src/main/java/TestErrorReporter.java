import java.util.ArrayList;
import java.util.List;

public class TestErrorReporter implements GenericErrorReporter {
    private int errors;
    private ArrayList<String> errorList;

    public TestErrorReporter() {
        errors = 0;
        errorList = new ArrayList<>();
    }

    public String getError(int i) {
        return errorList.get(i);
    }

    @Override
    public int getErrorAmount() {
        return errors;
    }

    @Override
    public void reportError(String message) {

        errorList.add(message);
        errors++;
    }
}
