package trinity;

import org.antlr.v4.runtime.*;

public interface ErrorReporter {

    int getErrorAmount();

    //TODO: lav bedre desc af denne
    void reportError(String message, Token token);

    //TODO: lav bedre desc af denne
    void reportError(String message, ParserRuleContext ctx);
}
