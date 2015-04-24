package trinity.visitors;

import org.antlr.v4.runtime.tree.ParseTree;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerationVisitor extends TrinityBaseVisitor<Void> implements TrinityVisitor<Void> {


    private static void emit(String string) {
        // TODO: replace this function with the good stuff
        System.out.print(string);
    }

    // TODO: find out what is always needed and what is not? (windows)
    private static List<String> includes = new ArrayList<String>() {{
        add("cuda_runtime.h");
        add("curand.h");
        add("cublas_v2.h");
        add("time.h");
        add("windows.h");
        add("stdio.h");
        add("stdlib.h");
    }};

    private static List<String> prototypes = new ArrayList<String>();

    private static void generateIncludes() {
        for(String path : includes) {
            emit("#include <" + path + ">");
        }
    }

    @Override
    public Void visitProg(TrinityParser.ProgContext ctx) {
        generateIncludes();

        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        return super.visitConstDecl(ctx);
    }

    @Override
    public Void visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        return super.visitFunctionDecl(ctx);
    }

    @Override
    public Void visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        return super.visitFormalParameters(ctx);
    }

    @Override
    public Void visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        return super.visitFormalParameter(ctx);
    }

    @Override
    public Void visitPrimitiveType(TrinityParser.PrimitiveTypeContext ctx) {
        return super.visitPrimitiveType(ctx);
    }

    @Override
    public Void visitVectorType(TrinityParser.VectorTypeContext ctx) {
        return super.visitVectorType(ctx);
    }

    @Override
    public Void visitMatrixType(TrinityParser.MatrixTypeContext ctx) {
        return super.visitMatrixType(ctx);
    }

    @Override
    public Void visitBlock(TrinityParser.BlockContext ctx) {
        return super.visitBlock(ctx);
    }

    @Override
    public Void visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        return super.visitSemiExpr(ctx);
    }

    @Override
    public Void visitConstDeclaration(TrinityParser.ConstDeclarationContext ctx) {
        return super.visitConstDeclaration(ctx);
    }

    @Override
    public Void visitSingleExpression(TrinityParser.SingleExpressionContext ctx) {
        return super.visitSingleExpression(ctx);
    }

    @Override
    public Void visitForLoop(TrinityParser.ForLoopContext ctx) {
        return super.visitForLoop(ctx);
    }

    @Override
    public Void visitIfStatement(TrinityParser.IfStatementContext ctx) {
        return super.visitIfStatement(ctx);
    }

    @Override
    public Void visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        return super.visitBlockStatement(ctx);
    }

    @Override
    public Void visitDoubleIndexing(TrinityParser.DoubleIndexingContext ctx) {
        return super.visitDoubleIndexing(ctx);
    }

    @Override
    public Void visitOr(TrinityParser.OrContext ctx) {
        return super.visitOr(ctx);
    }

    @Override
    public Void visitExponent(TrinityParser.ExponentContext ctx) {
        return super.visitExponent(ctx);
    }

    @Override
    public Void visitParens(TrinityParser.ParensContext ctx) {
        return super.visitParens(ctx);
    }

    @Override
    public Void visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        return super.visitMatrixLiteral(ctx);
    }

    @Override
    public Void visitTranspose(TrinityParser.TransposeContext ctx) {
        return super.visitTranspose(ctx);
    }

    @Override
    public Void visitMultiplyDivide(TrinityParser.MultiplyDivideContext ctx) {
        return super.visitMultiplyDivide(ctx);
    }

    @Override
    public Void visitSingleIndexing(TrinityParser.SingleIndexingContext ctx) {
        return super.visitSingleIndexing(ctx);
    }

    @Override
    public Void visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        return super.visitVectorLiteral(ctx);
    }

    @Override
    public Void visitNot(TrinityParser.NotContext ctx) {
        return super.visitNot(ctx);
    }

    @Override
    public Void visitRelation(TrinityParser.RelationContext ctx) {
        return super.visitRelation(ctx);
    }

    @Override
    public Void visitIdentifier(TrinityParser.IdentifierContext ctx) {
        return super.visitIdentifier(ctx);
    }

    @Override
    public Void visitNumber(TrinityParser.NumberContext ctx) {
        return super.visitNumber(ctx);
    }

    @Override
    public Void visitAnd(TrinityParser.AndContext ctx) {
        return super.visitAnd(ctx);
    }

    @Override
    public Void visitNegate(TrinityParser.NegateContext ctx) {
        return super.visitNegate(ctx);
    }

    @Override
    public Void visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        return super.visitFunctionCall(ctx);
    }

    @Override
    public Void visitEquality(TrinityParser.EqualityContext ctx) {
        return super.visitEquality(ctx);
    }

    @Override
    public Void visitBoolean(TrinityParser.BooleanContext ctx) {
        return super.visitBoolean(ctx);
    }

    @Override
    public Void visitAddSubtract(TrinityParser.AddSubtractContext ctx) {
        return super.visitAddSubtract(ctx);
    }

    @Override
    public Void visitExprList(TrinityParser.ExprListContext ctx) {
        return super.visitExprList(ctx);
    }

    @Override
    public Void visitVector(TrinityParser.VectorContext ctx) {
        return super.visitVector(ctx);
    }

    @Override
    public Void visitMatrix(TrinityParser.MatrixContext ctx) {
        return super.visitMatrix(ctx);
    }

    @Override
    public Void visitRange(TrinityParser.RangeContext ctx) {
        return super.visitRange(ctx);
    }
}