package trinity.visitors;

import trinity.*;

public class ReachabilityVisitor extends TrinityBaseVisitor<Boolean> implements TrinityVisitor<Boolean>{

    private ErrorReporter errorReporter;

    public ReachabilityVisitor(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    @Override
    public Boolean visitProg(TrinityParser.ProgContext ctx){
        for (int i = 0; i < ctx.functionDecl().size(); i++){
            // Check every FunctionDecl for unreachable code or missing return statements.
            // If a check has returned false, then end visitor.
            if(!ctx.functionDecl(i).accept(this)){
                return false;
            }
        }

        return true;
    }

    @Override
    public Boolean visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {

        //TODO: Remember to check block for return-controls thrown too early.
        return ctx.block().accept(this);
    }

    @Override
    public Boolean visitBlock(TrinityParser.BlockContext ctx){
        System.out.println("visitBlock");

        if(ctx.getChildCount() == 0 || (ctx.getChildCount() == 1 && !ctx.stmt(0).accept(this))){
            System.out.println("visitBlock error");

            errorReporter.reportError("No return found.", ctx);
            return false;
        }

        for (int i = 0; i < ctx.stmt().size(); i++) {
            System.out.println("visitBlock stmt");
            if(!ctx.stmt(i).accept(this)) {
                errorReporter.reportError("No return found.", ctx);
                return false;
            }
        }

        return true;
    }

    /*
    @Override
    public Boolean visitConstDeclaration(TrinityParser.ConstDeclarationContext ctx){
        System.out.println("test: ConstDeclarationContext");

        return false;
    }
    */

    @Override
    public Boolean visitSingleExpression(TrinityParser.SingleExpressionContext ctx){

        return false;
    }

    /*
    @Override
    public Boolean visitForLoop(TrinityParser.ForLoopContext ctx){
        System.out.println("test: ForLoopContext");

        return false;
    }
    */

    @Override
    public Boolean visitIfStatement(TrinityParser.IfStatementContext ctx){
        for (TrinityParser.BlockContext blockCtx : ctx.block()) {
            System.out.println("omg: " + blockCtx.getText());
            blockCtx.accept(this);
        }

        return false;
    }

    @Override
    public Boolean visitBlockStatement(TrinityParser.BlockStatementContext ctx){
        // Simply return whatever the block returns.
        return ctx.block().accept(this);
    }
}
