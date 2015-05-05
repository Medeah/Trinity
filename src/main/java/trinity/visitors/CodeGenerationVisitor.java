package trinity.visitors;

import com.google.common.base.Charsets;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import trinity.*;
import trinity.types.EnumType;
import trinity.types.MatrixType;
import trinity.types.PrimitiveType;
import trinity.types.Type;

import java.io.IOException;
import java.net.URL;

import java.io.StringWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.google.common.io.Resources.asByteSource;
import static com.google.common.io.Resources.getResource;

public class CodeGenerationVisitor extends TrinityBaseVisitor<Void> implements TrinityVisitor<Void> {

    private int scopeDepth = 0;
    private List<String> globals = new ArrayList<String>();
    private List<String> funcs = new ArrayList<String>();

    public String generate(ParseTree tree) {

        String output = "";
        //StringWriter output = new StringWriter();
        StringWriter mainBody = new StringWriter();
        setEmitterContext(mainBody);

        this.visit(tree);

        output += stdlib();

        output += "/* ENTRY POINT */\n";

        for(String g :  globals) {
            output += g + ";";
        }

        output += generateFunctions();

        //TODO: fix
        output += ("\nint main(void){");
        output += mainBody.toString();
        output += ("return 0;};"); //TODO: end-semi is just for indent.

        return output;
    }

    private String stdlib() {
        //return "hej";
        URL test = getResource("stdtrinity.c");
        try {
            return com.google.common.io.Resources.toString(test, Charsets.UTF_8);
        } catch (IOException e) {
            System.err.println("error loading stdlib");
        }
        return "";
    }

    private Stack<StringWriter> emitStack = new Stack<StringWriter>();
    private void setEmitterContext(StringWriter writer) {
        if(currentWriter != null) {
            emitStack.push(currentWriter);
        }
        currentWriter = writer;
    }

    private void restoreEmitterContext() {
        currentWriter = emitStack.pop();
    }

    //TODO: dynamically set input stream, to avoid bad restoration code.
    private static StringWriter currentWriter; //= new StringWriter();
    //private static PrintWriter printWriter = new PrintWriter(body);
    private static void emit(String string) {
        //final OutputStream os = new FileOutputStream("/tmp/out");
        //final PrintStream printStream = new PrintStream(os);

        //printWriter.close();

        currentWriter.append(string);
        //currentWriter.print(string);

    }

    // TODO: find out what is always needed and what is not? (windows)
    /*private static List<String> includes = new ArrayList<String>() {{
        //add("cuda_runtime.h");
        //add("curand.h");
        //add("cublas_v2.h");
        //add("time.h");
        // add("windows.h");
        add("stdio.h");
        add("math.h");
        add("stdbool.h");
        //add("stdlib.h");
    }};*/

    private static List<String> prototypes = new ArrayList<String>();

    /*private static String includes() {
        String out = "";
        for (String path : includes) {
            out += "#include <" + path + ">\n";
        }
        return out;
    }*/

    private String generateFunctions() {
        String out = "";
        for (String str : funcs) {
            out += str;
        }
        return out;
    }

    // TODO: who even know at this point...
    private DependencyVisitor dependencyVisitor = new DependencyVisitor();
    private void emitDependencies(ParserRuleContext ctx) {
        Iterable<NeedInit> nis = ctx.accept(dependencyVisitor);

        // Init routine
        if (nis != null) {
            for (NeedInit ni : nis) {
                //TODO: it might be possible to get cgid merged into item expr, but we should probably redo all this s*** instead
                emit("float " + ni.id + "[" + ni.items.size() + "];");

                for (int i = 0; i < ni.items.size(); i++) {
                    emit(ni.id + "[" + i + "]=");
                    ni.items.get(i).accept(this);
                    emit(";");
                }
            }
        }
    }

    /*@Override
    public Void visitProg(TrinityParser.ProgContext ctx) {
        visitChildren(ctx);
        return null;
    }*/

    @Override
    public Void visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        emitDependencies(ctx.semiExpr());
        if(scopeDepth == 0) {
            StringWriter global = new StringWriter();
            setEmitterContext(global);

            ctx.type().accept(this);
            emit(ctx.ID().getText());

            globals.add(global.toString());
            restoreEmitterContext();

        } else {
            ctx.type().accept(this);
        }
        emit(ctx.ID().getText());
        emit("=");
        ctx.semiExpr().accept(this);
        return null;
    }

    @Override
    public Void visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        StringWriter funcbody = new StringWriter();
        setEmitterContext(funcbody);

        ctx.type().accept(this);
        emit(ctx.ID().getText());
        emit("(");
        if (ctx.formalParameters() != null) {
            ctx.formalParameters().accept(this);
        }
        emit("){");
        ctx.block().accept(this);
        emit("}");

        funcs.add(funcbody.toString());
        restoreEmitterContext();
        return null;
    }

    @Override
    public Void visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        ctx.formalParameter(0).accept(this);
        for (int i = 1; i < ctx.formalParameter().size(); i++) {
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
        emit("float* ");
        return null;
    }

    @Override
    public Void visitMatrixType(TrinityParser.MatrixTypeContext ctx) {
        emit("float* ");
        //emit(ctx.NUMBER(0).getText());
        //emit("][");
        //emit(ctx.NUMBER(1).getText());
        //emit("] ");
        return null;
    }

    @Override
    public Void visitBlock(TrinityParser.BlockContext ctx) {
        scopeDepth++;
        for (TrinityParser.StmtContext stmt : ctx.stmt()) {
            stmt.accept(this);
        }

        if (ctx.semiExpr() != null) {
            emit("return ");
            ctx.semiExpr().accept(this);
        }
        scopeDepth--;
        return null;
    }

    @Override
    public Void visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        visitChildren(ctx);
        emit(";");
        return null;
    }


    //TODO: move or something..
    ///private final Type scalar = new PrimitiveType(EnumType.SCALAR);
    //private final Type bool = new PrimitiveType(EnumType.BOOLEAN);

    @Override
    public Void visitForLoop(TrinityParser.ForLoopContext ctx) {
        // TODO: row-major / column-major
        emitDependencies(ctx.expr());

        if (ctx.expr().t instanceof MatrixType) {
            MatrixType matrix = (MatrixType) ctx.expr().t;

            //TODO: refactor much.
            // vector in matrix
            Boolean vinm = false;
            if (matrix.getRows() != 1) {
                vinm = true;
            }

            String incId = UniqueId.next();
            int size = vinm ? matrix.getRows() : matrix.getCols();

            emit("int " + incId + ";");
            emit("for(" + incId + "=0;" + incId + "<" + size + "; " + incId + "++){");

            // current scalar/vector being iterated
            emit(vinm ? "float* " : "float ");
            emit(ctx.ID().getText());
            emit("=");
            ctx.expr().accept(this); //TODO: this should always emit the pre-initialized vector id;

            if (vinm) {
                // Get pointer to vector in matrix
                emit("+" + incId + "*");
                emit(matrix.getCols() + "");
                emit(";");
            } else {
                // Get scalar element
                emit("[" + incId + "];");
            }

            ctx.block().accept(this);

            emit("}");
        } else {
            // TODO: is this safe to remove.
            emit("INTERNAL-ERROR;");
        }

        return null;
    }

    @Override
    public Void visitIfStatement(TrinityParser.IfStatementContext ctx) {
        //TODO: initialize dependencies
        for (int i = 0; i < ctx.expr().size(); i++) {
            emitDependencies(ctx.expr(i));
        }

        emit("if(");
        ctx.expr(0).accept(this);
        emit("){");
        ctx.block(0).accept(this);

        int i;
        for (i = 1; i < ctx.expr().size(); i++) {
            emit("}else if(");
            ctx.expr(i).accept(this);
            emit("){");
            ctx.block(i).accept(this);
        }

        if (ctx.block(i) != null) {
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
    public Void visitPrintStatement(TrinityParser.PrintStatementContext ctx) {
        emitDependencies(ctx.semiExpr().expr());
        Type expType = ctx.semiExpr().expr().t;
        if (expType instanceof PrimitiveType) {
            if (((PrimitiveType) expType).getPType() == EnumType.SCALAR) {
                emit("print_s(");
                ctx.semiExpr().expr().accept(this);
                emit(");");
            } else {
                emit("print_b(");
                ctx.semiExpr().expr().accept(this);
                emit(");");
            }
        } else if (expType instanceof MatrixType) {
            emit("print_m(");
            ctx.semiExpr().expr().accept(this);
            emit("," + ((MatrixType) expType).getRows());
            emit("," + ((MatrixType) expType).getCols());
            emit(");");
        } else {
            // TODO: functioncall should pass expType !!!!!!!
            emit("printf(" + expType.toString() + ");");
        }
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
        emit("transpose(");
        ctx.expr().accept(this);
        emit("," + ((MatrixType) ctx.expr().t).getRows());
        emit("," + ((MatrixType) ctx.expr().t).getCols());
        emit(")");
        return null;
    }

    @Override
    public Void visitMultiplyDivide(TrinityParser.MultiplyDivideContext ctx) {
        // TODO: types and shit.
        //TODO: equals scalar
        if (ctx.t instanceof PrimitiveType) {
            ctx.expr(0).accept(this);
            emit("*");
            ctx.expr(1).accept(this);
        } else {
            emit("_mult(");
            ctx.expr(0).accept(this);
            emit(",");
            ctx.expr(1).accept(this);
            emit(")");
        }
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
        if (ctx.expr().t instanceof PrimitiveType) {
            emit("-");
            ctx.expr().accept(this);
        }
        else if (ctx.expr().t instanceof MatrixType) {
            emit("fmmult(-1,");
            ctx.expr().accept(this);
            emit("," + ((MatrixType) ctx.expr().t).getRows());
            emit("," + ((MatrixType) ctx.expr().t).getCols());
            emit(")");
        }
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
        // Expect both operands to have same type
        if (ctx.expr(0).t instanceof PrimitiveType) {
            // Both a Scalar or Boolean
            ctx.expr(0).accept(this);
            // Use same operator as trinity (== or !=)
            emit(ctx.op.getText());
            ctx.expr(1).accept(this);
        } else if (ctx.expr(0).t instanceof MatrixType) {
            if (ctx.op.getText().equals("!=")) {
                emit("!");
            }
            emit("meq(");
            ctx.expr(0).accept(this);
            emit(",");
            ctx.expr(1).accept(this);
            emit(")");
        }

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
        for (int i = 1; i < ctx.expr().size(); i++) {
            emit(",");
            ctx.expr(i).accept(this);
        }
        return null;
    }

    @Override
    public Void visitRange(TrinityParser.RangeContext ctx) {
        int start = new Integer(ctx.NUMBER(0).getText());
        int end = new Integer(ctx.NUMBER(1).getText());
        int step = start > end ? -1 : 1;

        emit(Integer.toString(start));
        for (int i = start + 1; i < end; i += step) {
            emit("," + i);
        }
        return null;
    }

    @Override
    public Void visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        // TODO: reconsider
        emit(ctx.cgid);
        return null;
    }


    @Override
    public Void visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        // TODO: reconsider
        emit(ctx.cgid);
        return null;
    }

    @Override
    public Void visitVector(TrinityParser.VectorContext ctx) {
        // TODO: This should neber be called
        System.out.println("ERROR: visitVector should not be called.");
        return null;
    }

    @Override
    public Void visitMatrix(TrinityParser.MatrixContext ctx) {
        // TODO: This should neber be called
        System.out.println("ERROR: visitMatrix should not be called.");
        return null;
    }

    @Override
    public Void visitSingleExpression(TrinityParser.SingleExpressionContext ctx) {
        emitDependencies(ctx.semiExpr());
        return super.visitSingleExpression(ctx);
    }

    // TODO: these will be visited by default implementation (TrinityBaseVisitor)
    /*@Override
    public Void visitConstDeclaration(TrinityParser.ConstDeclarationContext ctx) {

        return super.visitConstDeclaration(ctx);
    }

    */


}