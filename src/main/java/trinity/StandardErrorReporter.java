package trinity;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class StandardErrorReporter implements ErrorReporter {
    private int errorAmount = 0;
    private boolean failOnError;
    private String input = null;

    public StandardErrorReporter(boolean fail, String input) {
        failOnError = fail;
        this.input = input;
    }

    @Override
    public int getErrorAmount() {
        return errorAmount;
    }

    private void errorHandling(int line, int schar, String message) {
        errorAmount++;

        System.err.println(line + ":" + schar + " -> " + message);
    }

    @Override
    public void reportError(String message) {
        errorHandling(0, 0, message);

        testFailOnError();
    }

    @Override
    public void reportError(String message, Token token) {
        int line = token.getLine();
        int schar = token.getCharPositionInLine();
        errorHandling(line, schar, message);

        underlineError(token, token, line, schar);

        testFailOnError();
    }

    @Override
    public void reportError(String message, ParserRuleContext ctx) {
        int line = ctx.getStart().getLine();
        int schar = ctx.getStart().getCharPositionInLine();
        errorHandling(line, schar, message);

        if (line == ctx.getStop().getLine()) {
            underlineError(ctx.getStart(), ctx.getStop(), line, schar);
        }

        testFailOnError();
    }

    private void testFailOnError() {
        if (failOnError) {
            System.exit(1);
        }
    }

    private void underlineError(Token startToken, Token endToken, int line, int charPositionInLine) {
        String[] lines = input.split("\n");
        String errorLine = lines[line - 1];
        String pointToError = "";
        int start = startToken.getStartIndex();
        int stop = endToken.getStopIndex();

        System.err.println(errorLine);
        for (int i = 0; i < charPositionInLine; i++) {
            pointToError += (String.valueOf(" "));
        }

        if (start >= 0 && stop >= 0) {
            for (int i = start; i <= stop; i++) {
                pointToError += (String.valueOf("^"));
            }
        }

        System.err.println(pointToError);
    }
}