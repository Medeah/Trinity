package trinity.visitors;

import com.sun.tracing.dtrace.DependencyClass;
import org.antlr.v4.runtime.tree.ParseTree;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;
import trinity.types.EnumType;
import trinity.types.MatrixType;
import trinity.types.PrimitiveType;
import trinity.types.Type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CodeGenerationVisitor extends TrinityBaseVisitor<Void> implements TrinityVisitor<Void> {

    //TODO: everything
    private static String output = "";
    private static String body = "";
    public String generate(ParseTree tree)  {
        this.visit(tree);

        output += includes();
        output += "typedef struct Matrix{float *data; int rows; int cols;} Matrix;";

        output += generateStatic();

        //TODO: fix
        output += ("int main(void) {");
        output += body;
        output += ("return 0;};");

        return output;
    }

    private static void emit(String string) {
        // TODO: replace this function with the good stuff
        //System.out.print(string);
        body += string;
    }

    // TODO: find out what is always needed and what is not? (windows)
    private static List<String> includes = new ArrayList<String>() {{
        //add("cuda_runtime.h");
        //add("curand.h");
        //add("cublas_v2.h");
        //add("time.h");
       // add("windows.h");
        add("stdio.h");
        //add("stdlib.h");
    }};

    private static List<String> prototypes = new ArrayList<String>();

    //private Map<String, MatrixType> staticInit = new HashMap<String, MatrixType>();
    private List<String> staticInit = new ArrayList<String>();

    private int idc = 0;
    private String getUniqueId() {
        return "_u" + idc;
    }

    private static String includes() {
        String out = "";
        for(String path : includes) {
            out += "#include <" + path + ">\n";
        }
        return out;
    }

    private String generateStatic() {
        String out = "";
      for(String str :  staticInit){
          out += str;
      }
        return out;
    }

    @Override
    public Void visitProg(TrinityParser.ProgContext ctx) {

        visitChildren(ctx);


        return null;
    }

    @Override
    public Void visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        DependencyVisitor dep = new DependencyVisitor();
        ctx.semiExpr().accept(dep);
        ctx.semiExpr().


        ctx.type().accept(this);
        emit(ctx.ID().getText());
        emit("=");
        ctx.semiExpr().accept(this);
        return null;
    }

    @Override
    public Void visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        ctx.type().accept(this);
        emit(ctx.ID().getText());
        emit("(");
        if(ctx.formalParameters() != null) {
            ctx.formalParameters().accept(this);
        }
        emit("){");
        ctx.block().accept(this);
        emit("}");
        return null;
    }

    @Override
    public Void visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        ctx.formalParameter(0).accept(this);
        for(int i = 1; i < ctx.formalParameter().size(); i++) {
            emit(",");
            ctx.formalParameter(i).accept(this);
        }
        return null;
    }

    @Override
    public Void visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        ctx.type().accept(this);
        emit(ctx.ID().getText());
        return null;
    }

    @Override
    public Void visitPrimitiveType(TrinityParser.PrimitiveTypeContext ctx) {
        if (ctx.getText().equals("Boolean")) {
            emit("bool ");
        } else /* Scalar */ {
            emit("float ");
        }
        return null;
    }

    @Override
    public Void visitVectorType(TrinityParser.VectorTypeContext ctx) {
        emit("Matrix ");
        return null;
    }

    @Override
    public Void visitMatrixType(TrinityParser.MatrixTypeContext ctx) {
        emit("Matrix");
        //emit(ctx.NUMBER(0).getText());
        //emit("][");
        //emit(ctx.NUMBER(1).getText());
        //emit("] ");
        return null;
    }

    @Override
    public Void visitBlock(TrinityParser.BlockContext ctx) {
        for(TrinityParser.StmtContext stmt : ctx.stmt()) {
            stmt.accept(this);
        }

        if(ctx.semiExpr() != null) {
            emit("return ");
            ctx.semiExpr().accept(this);
        }

        return null;
    }

    @Override
    public Void visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        visitChildren(ctx);
        emit(";\n"); // TODO: don't print newline?
        return null;
    }


    //TODO: move or something..
    private final Type scalar = new PrimitiveType(EnumType.SCALAR);
    private final Type bool = new PrimitiveType(EnumType.BOOLEAN);

    @Override
    public Void visitForLoop(TrinityParser.ForLoopContext ctx) {
        //TODO: make

        if(ctx.expr().t instanceof MatrixType) {
            MatrixType matrix = (MatrixType)ctx.expr().t;

            if(matrix.getRows() != 1) {
                //TODO!
                emit("matrix-forloop-error;");
                return null;
            }

            //TODO: unique id + type
            emit("float[" + matrix.getCols() + "] _sa = ");
            ctx.expr().accept(this);
            emit(";");
            emit("int i;");
            emit("for(i=;i<" + matrix.getCols() + "; i++){");
            emit("float ");
            emit(ctx.ID().getText());
            emit("=_sa[i];");
            ctx.block().accept(this);
            emit("}");

        } else {
            emit("forloop-unimpl;");

        }

        return null;
    }

    @Override
    public Void visitIfStatement(TrinityParser.IfStatementContext ctx) {
        //TODO: initialize dependencies

        /*DependencyVisitor dep = new DependencyVisitor();


        Iterable<TrinityParser.ExprContext> s = ctx.accept(dep);

        emit("float uniq[" + "s.size()" + "];\n");
        int lol = 0;
        for(TrinityParser.ExprContext expr : s) {
           // System.out.println( expr.getText());

            emit("float uniq[" + lol++ + "] = ");
            expr.accept(this);
            emit(";\n");
        }

        System.out.println("      .");*/


        emit("if(");
        ctx.expr(0).accept(this);
        emit("){");
        ctx.block(0).accept(this);

        int i;
        for(i = 1; i < ctx.expr().size(); i++) {
            emit("}else if(");
            ctx.expr(i).accept(this);
            emit("){");
            ctx.block(i).accept(this);
        }

        if(ctx.block(i) != null) {
            emit("}else{");
            ctx.block(i).accept(this);
        }

        emit("}");
        return null;
    }

    @Override
    public Void visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        emit("{");
        ctx.block().accept(this);
        emit("}");
        return null;
    }

    @Override
    public Void visitDoubleIndexing(TrinityParser.DoubleIndexingContext ctx) {
        // TODO: zero indexing
        emit(ctx.ID().getText());
        emit("[");
        ctx.expr(0).accept(this);
        emit("][");
        ctx.expr(1).accept(this);
        emit("]");
        return null;
    }

    @Override
    public Void visitOr(TrinityParser.OrContext ctx) {
        ctx.expr(0).accept(this);
        emit("||");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Void visitExponent(TrinityParser.ExponentContext ctx) {
        // TODO: exponent on types???
        ctx.expr(0).accept(this);

        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Void visitParens(TrinityParser.ParensContext ctx) {
        emit("(");
        ctx.expr().accept(this);
        emit(")");
        return null;
    }

    @Override
    public Void visitTranspose(TrinityParser.TransposeContext ctx) {
        // TODO: transpose function (unique name...)
        emit("traspose(");
        ctx.expr().accept(this);
        emit(")");
        return null;
    }

    @Override
    public Void visitMultiplyDivide(TrinityParser.MultiplyDivideContext ctx) {
        // TODO: types and shit.
        return null;
    }

    @Override
    public Void visitSingleIndexing(TrinityParser.SingleIndexingContext ctx) {
        emit(ctx.ID().getText());
        emit("[");
        ctx.expr().accept(this);
        emit("]");
        return null;
    }

    @Override
    public Void visitNot(TrinityParser.NotContext ctx) {
        emit("!");
        ctx.expr().accept(this);
        return null;
    }

    @Override
    public Void visitRelation(TrinityParser.RelationContext ctx) {
        ctx.expr(0).accept(this);
        emit(ctx.op.getText());
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Void visitIdentifier(TrinityParser.IdentifierContext ctx) {
        emit(ctx.ID().getText());
        return null;
    }

    @Override
    public Void visitNumber(TrinityParser.NumberContext ctx) {
        emit(ctx.NUMBER().getText());
        return null;
    }

    @Override
    public Void visitAnd(TrinityParser.AndContext ctx) {
        ctx.expr(0).accept(this);
        emit("&&");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Void visitNegate(TrinityParser.NegateContext ctx) {
        emit("-");
        ctx.expr().accept(this);
        return null;
    }

    @Override
    public Void visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        emit(ctx.ID().getText());
        emit("(");
        if (ctx.exprList() != null) {
            ctx.exprList().accept(this);
        }
        emit(")");
        return null;
    }

    @Override
    public Void visitEquality(TrinityParser.EqualityContext ctx) {
        ctx.expr(0).accept(this);
        emit(ctx.op.getText());
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Void visitBoolean(TrinityParser.BooleanContext ctx) {
        emit(ctx.BOOL().getText());
        return null;
    }

    @Override
    public Void visitAddSubtract(TrinityParser.AddSubtractContext ctx) {
        ctx.expr(0).accept(this);
        emit(ctx.op.getText());
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Void visitExprList(TrinityParser.ExprListContext ctx) {
        ctx.expr(0).accept(this);
        for(int i = 1; i < ctx.expr().size(); i++) {
            emit(",");
            ctx.expr(i).accept(this);
        }
        return null;
    }

    @Override
    public Void visitVector(TrinityParser.VectorContext ctx) {
        // TODO: implement properly

        return null;
    }

    @Override
    public Void visitRange(TrinityParser.RangeContext ctx) {
        int start = new Integer(ctx.NUMBER(0).getText());
        int end = new Integer(ctx.NUMBER(1).getText());
        int step = start > end ? -1 : 1;

        emit(start + ""); //TODO: string syntaxify this
        for(int i = start + 1; i < end; i += step) {
            emit("," + i);
        }
        return null;
    }

    // TODO: these will be visited by default implementation (TrinityBaseVisitor)
    /*@Override
    public Void visitMatrix(TrinityParser.MatrixContext ctx) {
        return super.visitMatrix(ctx);
    }*/

    /*@Override
    public Void visitConstDeclaration(TrinityParser.ConstDeclarationContext ctx) {

        return super.visitConstDeclaration(ctx);
    }

    @Override
    public Void visitSingleExpression(TrinityParser.SingleExpressionContext ctx) {
        return super.visitSingleExpression(ctx);
    }*/

    /*@Override
    public Void visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {

        return super.visitMatrixLiteral(ctx);
    }*/

    @Override
    public Void visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        MatrixType vector = (MatrixType)ctx.t;
        if(ctx.vector().exprList() != null) {
            String id = getUniqueId();
            //TODO: change this (remove getText())
            staticInit.add("static float " + id + "[" + vector.getCols() + "]={" +
            ctx.vector().exprList().getText() + "};");
            emit("(Matrix){.data=" + id + ",.rows=" + vector.getRows() + ",.cols=" + vector.getCols() + "}");
        } else {
            //TODO:
            emit("{nope");
            //ctx.vector().range().accept(this);
            emit("}");
        }
        return null;
    }

}