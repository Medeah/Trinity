package trinity.visitors;

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


        return null;
    }

}
