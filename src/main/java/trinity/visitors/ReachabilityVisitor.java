package trinity.visitors;

import org.antlr.v4.runtime.tree.ParseTree;
import trinity.*;
import trinity.types.Type;

public class ReachabilityVisitor extends TrinityBaseVisitor<Object> implements TrinityVisitor<Object>{

    private ErrorReporter errorReporter;
    private SymbolTable symbolTable;

    public ReachabilityVisitor(ErrorReporter errorReporter, SymbolTable symbolTable) {
        this.errorReporter = errorReporter;
        this.symbolTable = symbolTable;
    }


    @Override
    public Object visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        if(ctx.block().getChildCount() == 1 || !ctx.block().getChild(ctx.block().getChildCount()-2).getText().equalsIgnoreCase("return")){
            errorReporter.reportError("ARGH!", ctx);
        }

        return null;
    }

}
