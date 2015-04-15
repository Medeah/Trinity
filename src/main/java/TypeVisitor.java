import CustomExceptions.SymbolAlreadyDefinedException;
import CustomExceptions.SymbolNotFoundException;
import org.antlr.v4.runtime.tree.ErrorNode;

import java.util.ArrayList;
import java.util.List;

public class TypeVisitor extends TrinityBaseVisitor<Type> implements TrinityVisitor<Type> {
    TypeVisitor(ErrorReporter errorReporter, SymbolTable symbolTable) {
        this.errorReporter = errorReporter;
        this.symbolTable = symbolTable;
    }

    TypeVisitor() {
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

    /*@Override
    public Type visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        // Declared (expected) type:
        Type LHS = ctx.TYPE().accept(this);

        // Type found in expr (RHS of declaration)
        Type RHS = ctx.expr().accept(this);

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
    }*/

    @Override
    public Type visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {

        symbolTable.openScope();

        Type funcType = ctx.type().accept(this);

        List<Type> formalParamterTypes = new ArrayList<Type>();
        for (int i = 0; i < ctx.formalParameters().formalParameter().size(); i++) {
            formalParamterTypes.add(ctx.formalParameters().formalParameter(i).accept(this));
        }

        Type blockType = ctx.block().accept(this);

        if (expect(funcType, blockType)) {
            Type functionDecl = new FunctionType(funcType, formalParamterTypes);
            try {
                symbolTable.enterSymbol(ctx.ID().getText(), functionDecl);
            } catch (SymbolAlreadyDefinedException e) {
                errorReporter.reportError("Symbol was already defined!");
            }
        }

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

    /*@Override
    public Type visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        System.out.println("Err");
        System.out.println("and err");
        System.out.println("and err again,");
        System.out.println("but less");
        System.out.println("and less");
        System.out.println("and less.");

        return nullllllllllll;
    }

    @Override
    public Type visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        Type parameterType = ctx.TYPE().accept(this);

        try {
            symbolTable.enterSymbol(ctx.ID().getText(), parameterType);
        } catch (SymbolAlreadyDefinedException e) {
            errorReporter.reportError("Symbol was already defined!");
        }

        return parameterType;
    }
/*

    @Override
    public Type visitBlock(TrinityParser.BlockContext ctx) {
        Type stmtType = new Type();
        List<Type> allStmtTypes = new ArrayList<Type>();

        // Add all stmt types which are not null...
        for (int i = 0; i < ctx.stmt().size(); i++) {
            stmtType = ctx.stmt(i).accept(this);
            if (stmtType.getType() != null) {
                allStmtTypes.add(stmtType);
            }
        }

        // Check if all stmtTypes are compatible...
        Type decidingStmtType = allStmtTypes.get(0);
        for (int i = 0; i < allStmtTypes.size(); i++) {
            if (allStmtTypes.get(i) != decidingStmtType) {
                errorReporter.reportError("");
            }
        }

        return null;
    }
*/
   /* @Override
    public Type visitIfBlock(TrinityParser.IfBlockContext ctx) {
        return null;
    }

    @Override
    public Type visitIfStmt(TrinityParser.IfStmtContext ctx) {
        return null;
    }

    @Override
    public Type visitElseIfStmt(TrinityParser.ElseIfStmtContext ctx) {
        return null;
    }

    @Override
    public Type visitElseStmt(TrinityParser.ElseStmtContext ctx) {
        return null;
    }*/


    @Override
    public Type visitVectorLit(TrinityParser.VectorLitContext ctx) {
        return ctx.vector().accept(this);
    }

    @Override
    public Type visitVector(TrinityParser.VectorContext ctx) {
        // TODO check range
        List<TrinityParser.ExprContext> exprs = ctx.exprList().expr();
        for (TrinityParser.ExprContext expr : exprs) {
            expect(scalar, expr.accept(this));
        }
        return new VectorType(exprs.size());
    }

    @Override
    public Type visitExprList(TrinityParser.ExprListContext ctx) {
        errorReporter.reportError("what?");
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
    public Type visitVectorIndexing(TrinityParser.VectorIndexingContext ctx) {
        expect(new PrimitiveType(EnumType.SCALAR), ctx.expr().accept(this));

        Type idtype;

        try {
            idtype = symbolTable.retrieveSymbol(ctx.ID().getText());
        } catch (SymbolNotFoundException e) {
            errorReporter.reportError("Symbol not defined!");
            return null;
        }

        // hack??
        if (idtype instanceof VectorType) {
            return new PrimitiveType(EnumType.SCALAR);
        } else if (idtype instanceof MatrixType) {
            MatrixType cast = (MatrixType) idtype;

            //TODO row vector vs col vector
            return new VectorType(cast.getCols());
        } else {

            errorReporter.reportError("hmm error");
            return null;
        }
    }

    @Override
    public Type visitMatrixIndexing(TrinityParser.MatrixIndexingContext ctx) {
        expect(new PrimitiveType(EnumType.SCALAR), ctx.expr(0).accept(this));
        expect(new PrimitiveType(EnumType.SCALAR), ctx.expr(1).accept(this));
        Type id = ctx.ID().accept(this);
        if (id instanceof MatrixType) {
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
        String operator = ctx.getChild(1).getText(); // TODO måske ikke det rigte måde at få operator ud

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
    public Type visitType(TrinityParser.TypeContext ctx) {

        String prim = ctx.TYPE().getSymbol().getText(); // meh

        // Check and return type of node
        if (prim.contentEquals("Boolean")) {
            if (ctx.size() != null) {
                errorReporter.reportError("size not allowed for Boolean");
                return null;
            }
            return new PrimitiveType(EnumType.BOOLEAN);
        } else if (prim.contentEquals("Scalar")) {
            if (ctx.size() != null) {
                errorReporter.reportError("size not allowed for Scalar");
                return null;
            }
            return new PrimitiveType(EnumType.SCALAR);
        } else if (prim.contentEquals("Vector")) {
            TrinityParser.SizeContext size = ctx.size();
            if (size == null) {
                errorReporter.reportError("Vector must have size");
                return null;

            }
            if (size.getRuleIndex() == 0) {
                errorReporter.reportError("Vector needs size of one dim");
                return null;
            }
            size.getChildCount();
            return new VectorType(3);
        } else if (prim.contentEquals("Matrix")) {
            // TODO
            return new MatrixType(3, 3);
        } else {
            errorReporter.reportError("Type not know, must be one of Matrix, Scalar, Vector or Boolean");
            return null;
        }
    }

    // TODO find ud af hvornår dette sker
    @Override
    public Type visitErrorNode(ErrorNode node) {
        return null;
    }
}
