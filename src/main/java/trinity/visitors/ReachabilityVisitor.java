package trinity.visitors;

import trinity.ErrorReporter;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;

/**
 * The ReachabilityVisitor goes through all function decls, to vertify a return is allways present.
 */

public class ReachabilityVisitor extends TrinityBaseVisitor<Boolean> implements TrinityVisitor<Boolean> {

    private ErrorReporter errorReporter;

    public ReachabilityVisitor(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    /**
     * Visits all function decls in the desired program.
     *
     * @param ctx the parse tree
     * @return a boolean, true if return is found in all functions
     */
    @Override
    public Boolean visitProg(TrinityParser.ProgContext ctx) {
        for (int i = 0; i < ctx.functionDecl().size(); i++) {
            // Check every FunctionDecl for unreachable code or missing return statements.
            // If a check has returned false, then end visitor.

            if (!ctx.functionDecl(i).accept(this)) {
                errorReporter.reportError("No return found in function.", ctx);
                return false;
            }
        }

        return true;
    }

    /**
     * Visit single block inside function.
     *
     * @param ctx the parse tree
     * @return a boolean, true if return is found in all functions
     */
    @Override
    public Boolean visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        return ctx.block().accept(this);
    }

    @Override
    public Boolean visitBlock(TrinityParser.BlockContext ctx) {
        boolean isReturnValid = (ctx.returnStmt() != null) ? ctx.returnStmt().accept(this) : false;

        for (TrinityParser.StmtContext csctx : ctx.stmt()) {
            if (csctx.accept(this)) {
                return true;
            }
        }

        return isReturnValid;
    }

    /**
     * Visit semi expression inside single expression.
     *
     * @param ctx the parse tree
     * @return a boolean, true if return is found in all functions
     */
    @Override
    public Boolean visitSingleExpression(TrinityParser.SingleExpressionContext ctx) {
        return ctx.semiExpr().accept(this);
    }

    /**
     * Inside the semi expression visits the expression, and returns if its not null.
     *
     * @param ctx the parse tree
     * @return a boolean, true if the expr returns not null
     */
    @Override
    public Boolean visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        return !(ctx.expr().accept(this) == null);
    }

    @Override
    public Boolean visitIfStatement(TrinityParser.IfStatementContext ctx) {
        int blocks = ctx.block().size(), exprs = ctx.expr().size();
        boolean contentFound = (blocks != 0) ? true : false;

        if (exprs == blocks) {
            return false;
        }

        for (TrinityParser.BlockContext btx : ctx.block()) {
            if (contentFound) {
                contentFound = btx.accept(this);
            }
        }

        return contentFound;
    }


    /**
     * Visits the return statement, checks if the semi expression after 'return' is valid.
     *
     * @param ctx the parse tree
     * @return a boolean, true if the return is valid
     */
    @Override
    public Boolean visitReturnStmt(TrinityParser.ReturnStmtContext ctx) {
        return (ctx.semiExpr() != null) ? !ctx.semiExpr().isEmpty() : true;
    }


    /**
     * Visits the block in a block statement
     *
     * @param ctx the parse tree
     * @return what the block returns.
     */
    @Override
    public Boolean visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        return ctx.block().accept(this);
    }

    /**
     * Visits the block in a for loop
     *
     * @param ctx the parse tree
     * @return what the for loop returns.
     */
    @Override
    public Boolean visitForLoop(TrinityParser.ForLoopContext ctx) {
        return ctx.block().accept(this);
    }
}
