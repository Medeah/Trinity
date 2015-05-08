package trinity.visitors;

import com.google.common.collect.ImmutableList;
import org.antlr.v4.runtime.ParserRuleContext;
import trinity.customExceptions.SymbolAlreadyDefinedException;
import trinity.customExceptions.SymbolNotFoundException;
import trinity.*;
import trinity.types.*;

import java.util.ArrayList;
import java.util.List;

public class TypeVisitor extends TrinityBaseVisitor<Type> implements TrinityVisitor<Type> {

    private ErrorReporter errorReporter;
    private SymbolTable symbolTable;

    private final Type scalar = new PrimitiveType(EnumType.SCALAR);
    private final Type bool = new PrimitiveType(EnumType.BOOLEAN);

    public TypeVisitor(ErrorReporter errorReporter, SymbolTable symbolTable) {
        this.errorReporter = errorReporter;
        this.symbolTable = symbolTable;

        addstdlib();
    }

    private void addstdlib() {
        Type numFunc = new FunctionType(scalar, ImmutableList.of(scalar));
        List<String> funcs = ImmutableList.of("abs", "round", "floor", "ceil", "sin", "cos", "tan", "asin", "acos", "atan", "log", "log10", "sqrt");
        try {
            for (String func : funcs) {
                symbolTable.enterSymbol(func, numFunc);
            }
        } catch (SymbolAlreadyDefinedException e) {
            errorReporter.reportError("error adding standard library to symbol table");
        }
    }

    private boolean expect(Type expected, Type actual, ParserRuleContext ctx) {
        if (expected.equals(actual)) {
            return true;
        } else {
            errorReporter.reportError("Expected type " + expected + " but got " + actual, ctx);
            return false;
        }
    }

    @Override
    public Type visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        // Declared (expected) type:
        Type LHS = ctx.type().accept(this);

        // Type found in expr (RHS of declaration)
        Type RHS = ctx.semiExpr().accept(this);

        // Check if the two achieved types matches each other and react accordingly:
        if (expect(LHS, RHS, ctx.semiExpr())) {
            try {
                symbolTable.enterSymbol(ctx.ID().getText(), LHS);
            } catch (SymbolAlreadyDefinedException e) {
                errorReporter.reportError("Symbol was already defined!", ctx.ID().getSymbol());
            }
        }
        return null;
    }

    @Override
    public Type visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {

        Type returnType = ctx.type().accept(this);

        List<String> formalParameterIds = new ArrayList<>();
        List<Type> formalParameterTypes = new ArrayList<>();

        if (ctx.formalParameters() != null) {
            for (TrinityParser.FormalParameterContext formalParameter : ctx.formalParameters().formalParameter()) {
                formalParameterTypes.add(formalParameter.accept(this));
                formalParameterIds.add(formalParameter.ID().getText());
            }
        }

        FunctionType functionDecl = new FunctionType(returnType, formalParameterTypes);
        try {
            symbolTable.enterSymbol(ctx.ID().getText(), functionDecl);
        } catch (SymbolAlreadyDefinedException e) {
            errorReporter.reportError("Symbol was already defined!", ctx.ID().getSymbol());
        }

        symbolTable.openScope();
        symbolTable.setCurrentFunction(functionDecl);

        if (formalParameterIds.size() != formalParameterTypes.size()) {
            errorReporter.reportError("internel compiler error");
        }

        for (int i = 0; i < formalParameterTypes.size(); i++) {
            try {
                symbolTable.enterSymbol(formalParameterIds.get(i), formalParameterTypes.get(i));
            } catch (SymbolAlreadyDefinedException e) {
                errorReporter.reportError("Formal parameter Symbol was already defined!", ctx);
            }
        }

        ctx.block().accept(this);

        symbolTable.closeScope();

        return null;
    }

    @Override
    public Type visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        Type type;
        try {
            type = symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not defined!", ctx);
            return null;
        }
        if (type instanceof FunctionType) {
            FunctionType funcType = (FunctionType) type;

            if (ctx.exprList() != null) {
                List<TrinityParser.ExprContext> actualParams = ctx.exprList().expr();

                if (actualParams.size() != funcType.getParameterTypes().size()) {
                    errorReporter.reportError(ctx.ID().getText() + " called with wrong number of parameters", ctx);
                    return null;
                }

                for (int i = 0; i < actualParams.size(); i++) {
                    expect(funcType.getParameterTypes().get(i), actualParams.get(i).accept(this), actualParams.get(i));
                }
            } else if(funcType.getParameterTypes().size() != 0) {
                errorReporter.reportError(ctx.ID().getText() + " called with wrong number of parameters", ctx);
                return null;
            }
            return ctx.t = funcType.getType();
        } else {
            errorReporter.reportError(ctx.ID().getText() + " is not a function", ctx.getStart());
            return null;
        }

    }

    @Override
    public Type visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        errorReporter.reportError("internal compiler error. visitFormalParameters should never be called", ctx);

        return null;
    }

    @Override
    public Type visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        return ctx.type().accept(this);
    }

    @Override
    public Type visitBlock(TrinityParser.BlockContext ctx) {
        for (int i = 0; i < ctx.stmt().size(); i++) {
            ctx.stmt(i).accept(this);
        }

        if (ctx.returnStmt() != null) {
            return ctx.returnStmt().accept(this);
        }

        return null;
    }

    @Override
    public Type visitReturnStmt(TrinityParser.ReturnStmtContext ctx) {
        Type returnType = ctx.semiExpr().accept(this);
        try {
            if (!returnType.equals(symbolTable.getCurrentFunction().getType())) {
                errorReporter.reportError("Incorrect return type for function", ctx.semiExpr());
            }
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("No function to return from", ctx.semiExpr());

        }
        return returnType;
    }

    @Override
    public Type visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Type visitForLoop(TrinityParser.ForLoopContext ctx) {
        Type type = ctx.expr().accept(this);
        Type contextType = ctx.type().accept(this);

        symbolTable.openScope();

        if (type instanceof MatrixType) {
            if (((MatrixType) type).getRows() == 1) {
                expect(scalar, contextType, ctx.type());
            } else {
                expect(new MatrixType(1, ((MatrixType) type).getCols()), contextType, ctx.type());
            }
        } else {
            errorReporter.reportError("Hmm, expected a Matrix or Vector.", ctx.expr().getStart());
        }

        try {
            symbolTable.enterSymbol(ctx.ID().getText(), contextType);
        } catch (SymbolAlreadyDefinedException e) {
            errorReporter.reportError("ID already exists: " + ctx.ID().getText(), ctx.ID().getSymbol());
        }
        ctx.block().accept(this);

        symbolTable.closeScope();

        return null;
    }

    @Override
    public Type visitIfStatement(TrinityParser.IfStatementContext ctx) {
        for (TrinityParser.ExprContext expCtx : ctx.expr()) {
            expect(bool, expCtx.accept(this), expCtx);
        }

        for (TrinityParser.BlockContext blockCtx : ctx.block()) {
            symbolTable.openScope();
            blockCtx.accept(this);
            symbolTable.closeScope();
        }

        return null;
    }

    @Override
    public Type visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        symbolTable.openScope();
        ctx.block().accept(this);
        symbolTable.closeScope();
        return null;
    }

    @Override
    public Type visitVector(TrinityParser.VectorContext ctx) {
        if (ctx.range() != null) {
            return ctx.range().accept(this);
        } else {
            List<TrinityParser.ExprContext> exprs = ctx.exprList().expr();
            for (TrinityParser.ExprContext expr : exprs) {
                expect(scalar, expr.accept(this), expr);
            }
            return new MatrixType(1, exprs.size()); // Vector
        }
    }

    @Override
    public Type visitRange(TrinityParser.RangeContext ctx) {
        int from = new Integer(ctx.NUMBER(0).getText());
        int to = new Integer(ctx.NUMBER(1).getText());
        int size = Math.abs(to - from) + 1;

        return new MatrixType(1, size); // vector
    }

    @Override
    public Type visitExprList(TrinityParser.ExprListContext ctx) {
        errorReporter.reportError("internal error, visitExprList", ctx);
        return null;
    }

    @Override
    public Type visitMatrix(TrinityParser.MatrixContext ctx) {
        List<TrinityParser.VectorContext> vectors = ctx.vector();

        int rows = vectors.size();
        int cols = -1;

        for (int i = 0; i < rows; i++) {
            Type type = vectors.get(i).accept(this);
            if (type instanceof MatrixType) {
                MatrixType vectortyY = (MatrixType) type;
                if (cols == -1) {
                    cols = vectortyY.getCols();
                } else if (cols != vectortyY.getCols()) {
                    errorReporter.reportError("All rows in a Matrix must be of same size.", vectors.get(i));
                }
            } else {
                errorReporter.reportError("hmm error", ctx);
            }
        }
        return new MatrixType(rows, cols);
    }


    @Override
    public Type visitSingleIndexing(TrinityParser.SingleIndexingContext ctx) {
        expect(scalar, ctx.expr().accept(this), ctx.expr());

        Type symbol;

        Type out;

        try {
            symbol = symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not defined!", ctx.ID().getSymbol());
            return null;
        }
        if (symbol instanceof MatrixType) {
            MatrixType matrix = (MatrixType) symbol;
            if (matrix.getRows() == 1) {
                out = scalar;
            } else {
                out = new MatrixType(1, matrix.getCols()); // vector
            }
        } else {

            errorReporter.reportError("hmm error", ctx);
            out = null;
        }
        ctx.t = out;
        return out;
    }

    @Override
    public Type visitDoubleIndexing(TrinityParser.DoubleIndexingContext ctx) {
        Type out;
        expect(scalar, ctx.expr(0).accept(this), ctx.expr(0));
        expect(scalar, ctx.expr(1).accept(this), ctx.expr(1));

        Type symbol = null;
        try {
            symbol = symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not found", ctx.ID().getSymbol());
            return null;
        }

        if (symbol instanceof MatrixType) {
            out = scalar;
        } else {
            errorReporter.reportError("hmm error", ctx);
            return null;
        }

        ctx.t = out;
        return out;
    }

    @Override
    public Type visitParens(TrinityParser.ParensContext ctx) {
        return ctx.t = ctx.expr().accept(this);
    }

    @Override
    public Type visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        return ctx.t = ctx.matrix().accept(this);
    }

    @Override
    public Type visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        return ctx.t = ctx.vector().accept(this);
    }

    @Override
    public Type visitNumber(TrinityParser.NumberContext ctx) {
        return ctx.t = scalar;
    }

    @Override
    public Type visitTranspose(TrinityParser.TransposeContext ctx) {
        Type exprT = ctx.expr().accept(this);
        Type out;

        if (exprT instanceof MatrixType) {
            MatrixType matrixT = (MatrixType) exprT;
            out = new MatrixType(matrixT.getCols(), matrixT.getRows());
        } else {
            errorReporter.reportError("Only Matrix and Vectors can be transposed.", ctx);
            out = null;
        }
        ctx.t = out;
        return out;
    }

    @Override
    public Type visitRelation(TrinityParser.RelationContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(op1, op2, ctx);

        if (op1.equals(bool) && op2.equals(bool)) {
            errorReporter.reportError("Cannot compare booleans", ctx);
        }

        return ctx.t = bool;
    }

    @Override
    public Type visitEquality(TrinityParser.EqualityContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(op1, op2, ctx);

        return ctx.t = bool;
    }

    @Override
    public Type visitBoolean(TrinityParser.BooleanContext ctx) {
        return ctx.t = bool;
    }

    @Override
    public Type visitNot(TrinityParser.NotContext ctx) {
        Type exprT = ctx.expr().accept(this);
        expect(bool, exprT, ctx.expr());
        return ctx.t = exprT;
    }

    @Override
    public Type visitOr(TrinityParser.OrContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(bool, op1, ctx.expr(0));
        expect(bool, op2, ctx.expr(1));

        return ctx.t = op1;
    }

    @Override
    public Type visitAnd(TrinityParser.AndContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(bool, op1, ctx.expr(0));
        expect(bool, op2, ctx.expr(1));

        return ctx.t = op1;
    }

    @Override
    public Type visitExponent(TrinityParser.ExponentContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        if (op1.equals(bool)) {
            errorReporter.reportError("Can only use exponent operator scalars and square matrices.", ctx);
        } else if (op1 instanceof MatrixType) {
            MatrixType matrix = (MatrixType) op1;
            if (matrix.getRows() != matrix.getCols()) {
                errorReporter.reportError("Can only use exponent operator scalars and square matrices.", ctx);
            }
        }

        expect(scalar, op2, ctx.expr(1));

        return ctx.t = op1;
    }

    @Override
    public Type visitAddSubtract(TrinityParser.AddSubtractContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(op1, op2, ctx);

        if (op1.equals(bool)) {
            errorReporter.reportError("Cannot use operator +/- on boolean values.", ctx);
        } else {
            return ctx.t = op1;
        }

        return null;
    }

    @Override
    public Type visitMultiplyDivide(TrinityParser.MultiplyDivideContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);
        String operator = ctx.op.getText();

        if (op1.equals(bool) || op2.equals(bool)) {
            errorReporter.reportError("Cannot mult or div boolean", ctx);
            ctx.t = null;
        } else if (op1.equals(scalar)) {
            ctx.t = op2;
        } else if (op1 instanceof MatrixType) {
            if (operator.equals("*")) {
                // Nx1
                if (op2.equals(scalar)) {
                    ctx.t = op1;
                } else if (op2 instanceof MatrixType) {
                    MatrixType matrix1 = (MatrixType) op1;
                    MatrixType matrix2 = (MatrixType) op2;

                    // Vector dot product
                    if (matrix1.getRows() == 1 && matrix2.getRows() == 1 && matrix1.getCols() == matrix2.getCols()) {
                        ctx.t = scalar;
                    } else

                        // Matrix multiplication
                        if (matrix1.getCols() == matrix2.getRows()) {
                            ctx.t = new MatrixType(matrix1.getRows(), matrix2.getCols());
                        } else {
                            errorReporter.reportError("Size mismatch", ctx);
                            ctx.t = null;
                        }
                } else {
                    errorReporter.reportError("Cannot multiply matrix with " + op2, ctx);
                    ctx.t = null;
                }
            } else if (operator.equals("/")) {
                if (op2.equals(scalar)) {
                    ctx.t = op1;
                } else {
                    errorReporter.reportError("Cannot divde matrix with " + op2, ctx);
                    ctx.t = null;
                }
            }
        } else {
            errorReporter.reportError("what?", ctx);
            ctx.t = null;
        }

        return ctx.t;
    }

    @Override
    public Type visitIdentifier(TrinityParser.IdentifierContext ctx) {
        try {
            return ctx.t = symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not found", ctx.ID().getSymbol());
            return null;
        }
    }


    @Override
    public Type visitNegate(TrinityParser.NegateContext ctx) {
        Type out;
        Type op = ctx.expr().accept(this);
        if (op.equals(bool)) {
            errorReporter.reportError("Cannot negate bool", ctx);
            out = bool;
        } else {
            out = op;
        }
        ctx.t = out;
        return out;
    }

    @Override
    public Type visitPrimitiveType(TrinityParser.PrimitiveTypeContext ctx) {
        String prim = ctx.getChild(0).getText();
        if (prim.contentEquals("Boolean")) {
            return bool;
        } else if (prim.contentEquals("Scalar")) {
            return scalar;
        } else {
            errorReporter.reportError("hmm", ctx);
            return null;
        }
    }

    @Override
    public Type visitVectorType(TrinityParser.VectorTypeContext ctx) {
        Type out = null;
        if (ctx.ID() != null) {
            errorReporter.reportError("IDs not supported ... yet", ctx.ID().getSymbol());
        } else {
            try {
                int size = new Integer(ctx.NUMBER().getText());
                out = new MatrixType(1, size);
            } catch (NumberFormatException ex) {
                errorReporter.reportError("Unsupported dimension", ctx.NUMBER().getSymbol());
            }
        }
        return out;
    }

    @Override
    public Type visitMatrixType(TrinityParser.MatrixTypeContext ctx) {
        Type out = null;
        if (ctx.ID(1) != null || ctx.ID(0) != null) {
            errorReporter.reportError("IDs not supported ... yet", ctx.ID(0).getSymbol());
        } else {
            int rows = 0;
            int cols = 0;
            try {
                rows = new Integer(ctx.NUMBER(0).getText());
            } catch (NumberFormatException ex) {
                errorReporter.reportError("Unsupported dimension", ctx.NUMBER(0).getSymbol());
            }

            try {
                cols = new Integer(ctx.NUMBER(1).getText());
            } catch (NumberFormatException ex) {
                errorReporter.reportError("Unsupported dimension", ctx.NUMBER(0).getSymbol());
            }

            out = new MatrixType(rows, cols);

        }

        return out;
    }

}
