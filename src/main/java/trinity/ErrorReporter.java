package trinity;

import trinity.types.Type;

public interface ErrorReporter {

    public int getErrorAmount();

    // Default errors:
    public void reportError(String message);

    // trinity.types.Type errors;
    public void reportTypeError(Type expectedType, Type receivedType);
}
