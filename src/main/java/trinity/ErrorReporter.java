package trinity;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public interface ErrorReporter {

    int getErrorAmount();

    //TODO: lav bedre desc af denne
    void reportError(String message, Token token);

    //TODO: lav bedre desc af denne
    void reportError(String message, ParserRuleContext ctx);
}
