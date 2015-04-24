package trinity.visitors;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.*;
import trinity.*;
import trinity.CustomExceptions.SymbolAlreadyDefinedException;
import trinity.CustomExceptions.SymbolNotFoundException;
import trinity.types.*;

import java.util.ArrayList;
import java.util.List;

public class TypeVisitor extends TrinityBaseVisitor<Type> implements TrinityVisitor<Type> {
    public TypeVisitor(ErrorReporter errorReporter, SymbolTable symbolTable) {
        this.errorReporter = errorReporter;
        this.symbolTable = symbolTable;
    }

    private ErrorReporter errorReporter;
    private SymbolTable symbolTable;

    private final Type scalar = new PrimitiveType(EnumType.SCALAR);
    private final Type bool = new PrimitiveType(EnumType.BOOLEAN);

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

        Type funcType = ctx.type().accept(this);

        List<String> formalParameterIds = new ArrayList<String>();
        List<Type> formalParameterTypes = new ArrayList<Type>();

        if (ctx.formalParameters() != null) {
            for (TrinityParser.FormalParameterContext formalParameter : ctx.formalParameters().formalParameter()) {
                formalParameterTypes.add(formalParameter.accept(this));
                formalParameterIds.add(formalParameter.ID().getText());
            }
        }

        FunctionType functionDecl = new FunctionType(funcType, formalParameterTypes);
        try {
            symbolTable.enterSymbol(ctx.ID().getText(), functionDecl);
        } catch (SymbolAlreadyDefinedException e) {
            errorReporter.reportError("Symbol was already defined!", ctx.ID().getSymbol());
        }

        symbolTable.openScope();
        symbolTable.setCurrentFunction(functionDecl);

        assert formalParameterIds.size() == formalParameterTypes.size();

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
            }

            return funcType.getType();
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

        if (ctx.semiExpr() != null) {
            Type returnType = ctx.semiExpr().accept(this);
            if (!returnType.equals(symbolTable.getCurrentFunction().getType())) {
                errorReporter.reportError("Incorrect return type for function", ctx.semiExpr().getStart());
            }
            return returnType;
        }

        return null;
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
            errorReporter.reportError("ID already exsists: " + ctx.ID().getText(), ctx.ID().getSymbol());
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
        if (from > to) {
            errorReporter.reportError("Range error, " + from + " is larger than " + to + ".", ctx.NUMBER(0).getSymbol());
        }
        return new MatrixType(1, to - from + 1); // vector
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
        expect(new PrimitiveType(EnumType.SCALAR), ctx.expr().accept(this), ctx.expr());

        Type symbol;

        try {
            symbol = symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not defined!", ctx.ID().getSymbol());
            return null;
        }

        if (symbol instanceof MatrixType) {
            MatrixType matrix = (MatrixType) symbol;
            if (matrix.getRows() == 1) {
                return new PrimitiveType(EnumType.SCALAR);
            } else {
                return new MatrixType(1, matrix.getCols()); // vector
            }
        } else {

            errorReporter.reportError("hmm error", ctx);
            return null;
        }
    }

    @Override
    public Type visitDoubleIndexing(TrinityParser.DoubleIndexingContext ctx) {
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
            return new PrimitiveType(EnumType.SCALAR);
        } else {
            errorReporter.reportError("hmm error", ctx);
            return null;
        }
    }

    @Override
    public Type visitParens(TrinityParser.ParensContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Type visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        return ctx.matrix().accept(this);
    }

    @Override
    public Type visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        return ctx.vector().accept(this);
    }

    @Override
    public Type visitNumber(TrinityParser.NumberContext ctx) {
        return new PrimitiveType(EnumType.SCALAR);
    }

    @Override
    public Type visitTranspose(TrinityParser.TransposeContext ctx) {
        Type exprT = ctx.expr().accept(this);

        if (exprT instanceof MatrixType) {
            MatrixType matrixT = (MatrixType) exprT;
            return new MatrixType(matrixT.getCols(), matrixT.getRows());
        } else {
            errorReporter.reportError("Only Matrix and Vectors can be transposed.", ctx);
            return null;
        }
    }

    @Override
    public Type visitRelation(TrinityParser.RelationContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(op1, op2, ctx);

        if (op1.equals(bool) && op2.equals(bool)) {
            errorReporter.reportError("Cannot compare booleans", ctx);
        }

        return new PrimitiveType(EnumType.BOOLEAN);
    }

    @Override
    public Type visitEquality(TrinityParser.EqualityContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(op1, op2, ctx);

        return new PrimitiveType(EnumType.BOOLEAN);
    }

    @Override
    public Type visitBoolean(TrinityParser.BooleanContext ctx) {
        return new PrimitiveType(EnumType.BOOLEAN);
    }

    @Override
    public Type visitNot(TrinityParser.NotContext ctx) {
        Type exprT = ctx.expr().accept(this);
        expect(new PrimitiveType(EnumType.BOOLEAN), exprT, ctx.expr());
        return exprT;
    }

    @Override
    public Type visitOr(TrinityParser.OrContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(new PrimitiveType(EnumType.BOOLEAN), op1, ctx.expr(0));
        expect(new PrimitiveType(EnumType.BOOLEAN), op2, ctx.expr(1));

        return op1;
    }

    @Override
    public Type visitAnd(TrinityParser.AndContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(new PrimitiveType(EnumType.BOOLEAN), op1, ctx.expr(0));
        expect(new PrimitiveType(EnumType.BOOLEAN), op2, ctx.expr(1));

        return op1;
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

        return op1;
    }

    @Override
    public Type visitAddSubtract(TrinityParser.AddSubtractContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(op1, op2, ctx);

        if (op1.equals(bool)) {
            errorReporter.reportError("Cannot use operator +/- on boolean values.", ctx);
        } else {
            return op1;
        }

        return null;
    }

    @Override
    public Type visitMultiplyDivide(TrinityParser.MultiplyDivideContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);
        String operator = ctx.op.getText();

        if (operator.equals("*")) {
            if (op1.equals(new PrimitiveType(EnumType.BOOLEAN)) || op2.equals(new PrimitiveType(EnumType.BOOLEAN))) {
                errorReporter.reportError("Cannot mult or div boolean", ctx);
                return null;
            }

            if (op1.equals(scalar)) {
                return op2;
            }
            // Nx1
            else if (op1 instanceof MatrixType) {
                if (op2.equals(scalar)) {
                    return op1;
                } else if (op2 instanceof MatrixType) {
                    MatrixType matrix1 = (MatrixType) op1;
                    MatrixType matrix2 = (MatrixType) op2;

                    // Vector dot product
                    if (matrix1.getRows() == 1 && matrix2.getRows() == 1 && matrix1.getCols() == matrix2.getCols()) {
                        return new PrimitiveType(EnumType.SCALAR);
                    }

                    // Matrix multiplication
                    if (matrix1.getCols() == matrix2.getRows()) {
                        return new MatrixType(matrix1.getRows(), matrix2.getCols());
                    } else {
                        errorReporter.reportError("Size mismatch", ctx);
                        return null;
                    }
                } else {
                    errorReporter.reportError("Cannot multiply matrix with " + op2, ctx);
                    return null;
                }
            }
        } else if (operator.equals("/")) {
            expect(scalar, op1, ctx.expr(0));
            expect(scalar, op2, ctx.expr(1));
            return scalar;

        } else {
            errorReporter.reportError("what?", ctx);
        }

        return null;
    }

    @Override
    public Type visitIdentifier(TrinityParser.IdentifierContext ctx) {
        try {
            return symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not found", ctx.ID().getSymbol());
            return null;
        }
    }


    @Override
    public Type visitNegate(TrinityParser.NegateContext ctx) {
        Type op = ctx.expr().accept(this);
        if (op.equals(bool)) {
            errorReporter.reportError("Cannot negate bool", ctx);
            return bool;
        }

        return op;
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
            int size = new Integer(ctx.NUMBER().getText());
            out = new MatrixType(1, size);
        }
        return out;
    }

    @Override
    public Type visitMatrixType(TrinityParser.MatrixTypeContext ctx) {
        Type out = null;
        if (ctx.ID(1) != null || ctx.ID(0) != null) {
            errorReporter.reportError("IDs not supported ... yet", ctx.ID(0).getSymbol());
        } else {
            int rows = new Integer(ctx.NUMBER(0).getText());
            int cols = new Integer(ctx.NUMBER(1).getText());

            out = new MatrixType(rows, cols);
        }

        return out;
    }

}
