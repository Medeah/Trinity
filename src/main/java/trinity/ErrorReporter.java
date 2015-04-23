package trinity;

import org.antlr.v4.runtime.*;
import trinity.types.Type;

public interface ErrorReporter {

    int getErrorAmount();

    // Default errors:
    void reportError(String message);

    // trinity.types.Type errors;
    void reportTypeError(Type expectedType, Type receivedType);

    //TODO: lav bedre desc af denne
    void reportError(String message, Token token);

    //TODO: lav bedre desc af denne
    void reportError(String message, ParserRuleContext ctx);
}
