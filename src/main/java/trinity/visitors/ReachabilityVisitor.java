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
        for(TrinityParser.FunctionDeclContext fdecl : ctx.functionDecl()) {
            if (!fdecl.accept(this)) {
                errorReporter.reportError("Can reach end of function " + fdecl.ID().toString(), ctx);
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
        if (ctx.returnStmt() == null) {
            // no return statement

            if (ctx.stmt().size() == 0) {
                // no statement -> can always reach end
                return false;
            }

            int i = 0;
            for (; i < ctx.stmt().size() - 1; i++) {
                if (ctx.stmt(i).accept(this)) {
                    return false;
                }
            }
            return ctx.stmt(i).accept(this);
        } else {
            for (TrinityParser.StmtContext stm : ctx.stmt()) {
                if (stm.accept(this)) {
                    return false;
                }
            }
            return ctx.returnStmt().accept(this);
        }
    }



        /*boolean returnPresent = (ctx.returnStmt() != null);

        for (TrinityParser.StmtContext stm : ctx.stmt()) {
            if (stm.accept(this)) {
                return true;
            }
        }

        return returnPresent;
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
     * Visit semi expression inside single expression.
     *
     * @param ctx the parse tree
     * @return a boolean, true if return is found in all functions
     */
    @Override
    public Boolean visitSingleExpression(TrinityParser.SingleExpressionContext ctx) {
        return false;
    }

    @Override
    public Boolean visitIfStatement(TrinityParser.IfStatementContext ctx) {
        int blocks = ctx.block().size();
        int exprs = ctx.expr().size();
        boolean contentFound = true;

        if (exprs == blocks) {
            // no else block. It is posible that all expr will be false
            return false;
        }

        for (TrinityParser.BlockContext btx : ctx.block()) {
            if (!btx.accept(this)) {
                return false;
            }
        }

        return true;
    }


    /**
     * Visits the return statement, checks if the semi expression after 'return' is valid.
     *
     * @param ctx the parse tree
     * @return a boolean, true if the return is valid
     */
    @Override
    public Boolean visitReturnStmt(TrinityParser.ReturnStmtContext ctx) {
        return true;
    }

    @Override
    public Boolean visitConstDeclaration(TrinityParser.ConstDeclarationContext ctx) {
        return false;
    }




    @Override
    public Boolean visitPrintStatement(TrinityParser.PrintStatementContext ctx) {
        return false;
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
