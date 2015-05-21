package trinity.visitors;

import trinity.ErrorReporter;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;

/**
 * This visitor goes through all function decls to verify that the end is not reachable. And that there are
 * no unreachable statements. This ensures that every function will return a value.
 * Each visit method returns a boolean. It returns true if there are no errors.
 */
public class ReachabilityVisitor extends TrinityBaseVisitor<Boolean> implements TrinityVisitor<Boolean> {

    private ErrorReporter errorReporter;

    public ReachabilityVisitor(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    @Override
    public Boolean visitProg(TrinityParser.ProgContext ctx) {
        for (TrinityParser.FunctionDeclContext fdecl : ctx.functionDecl()) {
            if (!fdecl.accept(this)) {
                errorReporter.reportError("Reachability error in function " + fdecl.ID().toString(), fdecl);
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        return ctx.block().accept(this);
    }

    @Override
    public Boolean visitBlock(TrinityParser.BlockContext ctx) {
        if (ctx.returnStmt() == null) {
            // no return statement

            if (ctx.stmt().size() == 0) {
                // no statements -> can always reach end
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

    @Override
    public Boolean visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        return ctx.block().accept(this);
    }

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
            // no else block. It is possible that all expr will be false
            return false;
        }

        for (TrinityParser.BlockContext btx : ctx.block()) {
            if (!btx.accept(this)) {
                return false;
            }
        }

        return true;
    }

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

    @Override
    public Boolean visitForLoop(TrinityParser.ForLoopContext ctx) {
        return ctx.block().accept(this);
    }
}
