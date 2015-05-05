package trinity;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public interface ErrorReporter {

    int getErrorAmount();

    void reportError(String message);

    void reportError(String message, Token token);

    void reportError(String message, ParserRuleContext ctx);
}
