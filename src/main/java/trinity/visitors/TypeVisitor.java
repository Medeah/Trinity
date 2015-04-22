package trinity.visitors;

import org.antlr.v4.runtime.tree.ErrorNode;
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

    public TypeVisitor() {
        errorReporter = new StandardErrorReporter(true);
    }

    private ErrorReporter errorReporter;
    private SymbolTable symbolTable;

    private Type scalar = new PrimitiveType(EnumType.SCALAR);
    private Type bool = new PrimitiveType(EnumType.BOOLEAN);


    private boolean expect(Type expected, Type actual) {
        if (expected.equals(actual)) {
            return true;
        } else {
            errorReporter.reportTypeError(expected, actual);
            return false;
        }
    }

    @Override
    public Type visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        // Declared (expected) type:
        Type LHS = ctx.type().accept(this);

        // trinity.types.Type found in expr (RHS of declaration)
        Type RHS = ctx.semiExpr().accept(this);

        // Check if the two achieved types matches each other and react accordingly:
        if (LHS.equals(RHS)) {

            try {
                symbolTable.enterSymbol(ctx.ID().getText(), LHS);
            } catch (SymbolAlreadyDefinedException e) {
                errorReporter.reportError("Symbol was already defined!");
            }

            return LHS;
        } else {
            errorReporter.reportTypeError(LHS, RHS);
        }

        return null;
    }

    @Override
    public Type visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {

        symbolTable.openScope();

        Type funcType = ctx.type().accept(this);

        List<Type> formalParamterTypes = new ArrayList<Type>();
        for (int i = 0; i < ctx.formalParameters().formalParameter().size(); i++) {
            formalParamterTypes.add(ctx.formalParameters().formalParameter(i).accept(this));
        }

        FunctionType functionDecl = new FunctionType(funcType, formalParamterTypes);
        try {
            symbolTable.enterSymbol(ctx.ID().getText(), functionDecl);
        } catch (SymbolAlreadyDefinedException e) {
            errorReporter.reportError("Symbol was already defined!");
        }
        symbolTable.setfunc(functionDecl);
        ctx.block().accept(this);

        symbolTable.closeScope();

        return null;
    }

    @Override
    public Type visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        Type funT;
        try {
            funT = symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not defined!");
            return null;
        }
        if (funT instanceof FunctionType) {
            // TODO mere fuction

            return null;

        } else {
            errorReporter.reportError(ctx.ID().getText() + " is not a function");
            return null;
        }

    }

    @Override
    public Type visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        errorReporter.reportError("internal compiler error. visitFormalParameters should never be called");

        return null;
    }

    @Override
    public Type visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        Type parameterType = ctx.type().accept(this);

        try {
            symbolTable.enterSymbol(ctx.ID().getText(), parameterType);
        } catch (SymbolAlreadyDefinedException e) {
            errorReporter.reportError("Symbol was already defined!");
        }

        return parameterType;
    }

    @Override
    public Type visitBlock(TrinityParser.BlockContext ctx) {
        symbolTable.openScope();
        for (int i = 0; i < ctx.stmt().size(); i++) {
            ctx.stmt(i).accept(this);
        }

        if (ctx.semiExpr() != null) {
            ctx.semiExpr().accept(this);
        }
        symbolTable.closeScope();
        return null;
    }

    @Override
    public Type visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        return ctx.expr().accept(this);
    }

    //TODO
    @Override
    public Type visitForLoop(TrinityParser.ForLoopContext ctx) {
        return null;
    }

    //TODO
    @Override
    public Type visitIfStatement(TrinityParser.IfStatementContext ctx) {
        return null;
    }

    @Override
    public Type visitVector(TrinityParser.VectorContext ctx) {
        if (ctx.range() != null) {
            return ctx.range().accept(this);
        } else {
            List<TrinityParser.ExprContext> exprs = ctx.exprList().expr();
            for (TrinityParser.ExprContext expr : exprs) {
                expect(scalar, expr.accept(this));
            }
            return new VectorType(exprs.size());
        }
    }

    @Override
    public Type visitRange(TrinityParser.RangeContext ctx) {
        int from  = new Integer(ctx.NUMBER(0).getText());
        int to  = new Integer(ctx.NUMBER(1).getText());
        if (from > to) {
            errorReporter.reportError("report in range, from is larger than to ");
        }
        return new VectorType(to - from + 1);
    }

    @Override
    public Type visitExprList(TrinityParser.ExprListContext ctx) {
        errorReporter.reportError("internal error, visitExprList");
        return null;
    }

    @Override
    public Type visitMatrixLit(TrinityParser.MatrixLitContext ctx) {
        return ctx.matrix().accept(this);
    }

    @Override
    public Type visitMatrix(TrinityParser.MatrixContext ctx) {
        List<TrinityParser.VectorContext> vectors = ctx.vector();

        int rows = vectors.size();
        int cols = -1;

        for (TrinityParser.VectorContext vector : vectors) {
            Type ty = vector.accept(this);
            if (ty instanceof VectorType) {
                VectorType vectorty = (VectorType) ty;
                if (cols == -1) {
                    cols = vectorty.getNumElems();
                } else if (cols != vectorty.getNumElems()) {
                    errorReporter.reportError("all rows in matrix lit must be of same size");
                }
            } else {
                errorReporter.reportError("hmm error");
            }
        }
        return new MatrixType(rows, cols);
    }


    @Override
    public Type visitSingleIndexing(TrinityParser.SingleIndexingContext ctx) {
        expect(new PrimitiveType(EnumType.SCALAR), ctx.expr().accept(this));

        Type symbol;

        try {
            symbol = symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not defined!");
            return null;
        }

        if (symbol instanceof VectorType) {
            return new PrimitiveType(EnumType.SCALAR);
        } else if (symbol instanceof MatrixType) {
            MatrixType cast = (MatrixType) symbol;

            //TODO row vector vs col vector
            return new VectorType(cast.getCols());
        } else {

            errorReporter.reportError("hmm error");
            return null;
        }
    }

    @Override
    public Type visitDoubleIndexing(TrinityParser.DoubleIndexingContext ctx) {
        expect(scalar, ctx.expr(0).accept(this));
        expect(scalar, ctx.expr(1).accept(this));

        Type symbol = null;
        try {
           symbol = symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not found");
            return null;
        }

        if (symbol instanceof MatrixType) {
            return new PrimitiveType(EnumType.SCALAR);
        } else {
            errorReporter.reportError("hmm error");
            return null;
        }
    }

    @Override
    public Type visitParens(TrinityParser.ParensContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Type visitVectorLit(TrinityParser.VectorLitContext ctx) {
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
            errorReporter.reportError("hmm error");
            return null;
        }
    }

    @Override
    public Type visitRelation(TrinityParser.RelationContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(op1, op2);

        if (op1.equals(new PrimitiveType(EnumType.BOOLEAN)) || op1.equals(new PrimitiveType(EnumType.BOOLEAN))) {
            errorReporter.reportError("cannot compare booleans");
        }

        return new PrimitiveType(EnumType.BOOLEAN);
    }

    @Override
    public Type visitEquality(TrinityParser.EqualityContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(op1, op2);

        if (op1.equals(new PrimitiveType(EnumType.BOOLEAN)) || op1.equals(new PrimitiveType(EnumType.BOOLEAN))) {
            errorReporter.reportError("cannot compare booleans");
        }

        return new PrimitiveType(EnumType.BOOLEAN);
    }

    @Override
    public Type visitBoolean(TrinityParser.BooleanContext ctx) {
        return new PrimitiveType(EnumType.BOOLEAN);
    }

    @Override
    public Type visitNot(TrinityParser.NotContext ctx) {
        Type exprT = ctx.expr().accept(this);
        expect(new PrimitiveType(EnumType.BOOLEAN), exprT);
        return exprT;
    }

    @Override
    public Type visitOr(TrinityParser.OrContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(new PrimitiveType(EnumType.BOOLEAN), op1);
        expect(new PrimitiveType(EnumType.BOOLEAN), op2);

        return op1;
    }

    @Override
    public Type visitAnd(TrinityParser.AndContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(new PrimitiveType(EnumType.BOOLEAN), op1);
        expect(new PrimitiveType(EnumType.BOOLEAN), op2);

        return op1;
    }

    @Override
    public Type visitExponent(TrinityParser.ExponentContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(new PrimitiveType(EnumType.SCALAR), op1);
        expect(new PrimitiveType(EnumType.SCALAR), op2);

        return op1;
    }

    @Override
    public Type visitAddSub(TrinityParser.AddSubContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);

        expect(op1, op2);

        if (op1.equals(new PrimitiveType(EnumType.BOOLEAN))) {
            errorReporter.reportError("cannot use operator +/- on boolean values");
        } else {
            return op1;
        }

        return null;
    }

    @Override
    public Type visitMultDivMod(TrinityParser.MultDivModContext ctx) {
        Type op1 = ctx.expr(0).accept(this);
        Type op2 = ctx.expr(1).accept(this);
        String operator = ctx.op.getText(); // TODO måske ikke det rigte måde at få operator ud

        if (operator.equals("*")) {
            if (op1.equals(new PrimitiveType(EnumType.BOOLEAN)) || op2.equals(new PrimitiveType(EnumType.BOOLEAN))) {
                errorReporter.reportError("cannot mult or div boolean");
                return null;
            }

            if (op1.equals(scalar)) {
                return op2;
            }
            // Nx1
            if (op1 instanceof VectorType) {
                int size = ((VectorType)op1).getNumElems();
                if (op2.equals(scalar)) {
                    return new VectorType(size);
                } else if (op2.equals(op1) ) {
                    return scalar;
                } else if (op2 instanceof MatrixType) {
                    MatrixType mt = (MatrixType)op2;
                    if (mt.getRows() == 1) {
                        return new MatrixType(size, mt.getCols());
                    } else {
                        errorReporter.reportError("size mismatch");
                        return null;
                    }
                } else {
                    errorReporter.reportError("cannot multiply vector with " + op2);
                    return null;
                }
            } else if (op1 instanceof MatrixType) {
                if (op2.equals(scalar)) {
                    return op1;
                } else if (op2 instanceof VectorType ) {
                    // NxM * Lx1 = Nx1
                    int N = ((MatrixType) op1).getRows();
                    int M = ((MatrixType) op1).getCols();
                    int L = ((VectorType) op2).getNumElems();
                    if (M == L) {
                        return new VectorType(N);
                    } else {
                        errorReporter.reportError("size mismatch");
                        return null;
                    }
                } else if (op2 instanceof MatrixType) {
                    if (((MatrixType) op1).getCols() == ((MatrixType) op2).getRows()) {
                        return new MatrixType(((MatrixType) op1).getRows(), ((MatrixType) op2).getCols());
                    } else {
                        errorReporter.reportError("size mismatch");
                        return null;
                    }
                } else {
                    errorReporter.reportError("cannot multiply matrix with " + op2);
                    return null;
                }
            }
        } else if (operator.equals("/")) {
            expect(scalar, op1);
            expect(scalar, op2);
            return scalar;

        } else if (operator.equals("/")) {
            // TODO
        } else {
            errorReporter.reportError("what?");
        }

        return null;
    }

    @Override
    public Type visitIdentifier(TrinityParser.IdentifierContext ctx) {
        try {
            return symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not found");
            return null;
        }
    }


    @Override
    public Type visitNegate(TrinityParser.NegateContext ctx) {
        Type op = ctx.expr().accept(this);
        if (op.equals(bool)) {
            errorReporter.reportError("cannot negate bool");
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
            errorReporter.reportError("hmm");
            return null;
        }
    }

    @Override
    public Type visitVectorType(TrinityParser.VectorTypeContext ctx) {
        Type out = null;
        if (ctx.ID() != null) {
            errorReporter.reportError("IDs not supported ... yet");
        } else {
            int size = new Integer(ctx.NUMBER().getText());
            out = new VectorType(size);
        }
        return out;
    }

    @Override
    public Type visitMatrixType(TrinityParser.MatrixTypeContext ctx) {
        Type out = null;
        if (ctx.ID(1) != null || ctx.ID(0) != null) {
            errorReporter.reportError("IDs not supported ... yet");
        } else {
            int rows = new Integer(ctx.NUMBER(0).getText());
            int cols = new Integer(ctx.NUMBER(1).getText());

            out = new MatrixType(rows, cols);
        }

        return out;
    }

    // TODO find ud af hvornår dette sker
    @Override
    public Type visitErrorNode(ErrorNode node) {
        return null;
    }
}
