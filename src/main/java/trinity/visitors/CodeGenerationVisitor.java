package trinity.visitors;

import com.google.common.base.Charsets;
import org.antlr.v4.runtime.ParserRuleContext;
import trinity.*;
import trinity.types.EnumType;
import trinity.types.MatrixType;
import trinity.types.PrimitiveType;
import trinity.types.Type;
import trinity.utils.UniqueId;

import java.io.IOException;
import java.net.URL;
import java.util.Stack;
import java.util.Vector;

import static com.google.common.io.Resources.getResource;

/**
 * Visitor for C code generation.
 */
public class CodeGenerationVisitor extends TrinityBaseVisitor<Void> implements TrinityVisitor<Void> {

    private int scopeDepth = 0;
    private static final StringBuilder output = new StringBuilder();
    private static final StringBuilder mainBody = new StringBuilder();
    private static final StringBuilder funcBody = new StringBuilder();
    private static final StringBuilder globals = new StringBuilder();

    private final DependencyVisitor dependencyVisitor = new DependencyVisitor();

    /**
     * Get C code output after calling visit.
     * @return C code
     */
    public String getOutput() {
        if(output == null) {
            return "";
        }

        output.append(stdlib());
        output.append("/* ENTRY POINT */\n"); //TODO: remove
        output.append(globals.toString());
        output.append(funcBody.toString());

        // main body
        output.append("\nint main(void){");
        output.append(mainBody.toString());
        output.append("return 0;};"); // TODO: end-semi is just for indent.

        return output.toString();
    }

    private String stdlib() {
        URL test = getResource("stdtrinity.c");
        try {
            return com.google.common.io.Resources.toString(test, Charsets.UTF_8);
        } catch (IOException e) {
            System.err.println("error loading stdlib");
        }
        return "";
    }

    // TODO: make emit class or organize code
    private Stack<StringBuilder> emitStack = new Stack<>();

    private void setEmitterContext(StringBuilder writer) {
        if (currentWriter != null) {
            emitStack.push(currentWriter);
        }
        currentWriter = writer;
    }

    private void restoreEmitterContext() {
        currentWriter = emitStack.pop();
    }

    //TODO: dynamically set input stream, to avoid bad restoration code.
    private static StringBuilder currentWriter; //= new StringBuilder();

    private static void emit(String string) {
        currentWriter.append(string);
    }

    /**
     * Visit expression and emit initialization code for all vector and matrix literals.
     * @param ctx parse tree rule to search
     */
    private void emitDependencies(ParserRuleContext ctx) {
        Iterable<StaticMatrix> matrices = ctx.accept(dependencyVisitor);

        // Initialize matrix arrays
        if (matrices != null) {
            for (StaticMatrix staticMatrix : matrices) {
                // Declare array
                //emit("float " + staticMatrix.id + "[" + staticMatrix.items.size() + "];");
                emit("float* " + staticMatrix.id + " = malloc(" + staticMatrix.size + "*sizeof(float));");

                // TODO: this could be implemented as a init visitor.
                // Init array elements from expressions or ranges
                int i = 0;
                for (TrinityParser.VectorContext vector : staticMatrix.rows) {
                    if(vector.exprList() != null) {
                        for (TrinityParser.ExprContext expr : vector.exprList().expr()) {
                            emit(staticMatrix.id + "[" + i++ + "]=");
                            expr.accept(this);
                            emit(";");
                        }
                    } else if (vector.range() != null) {
                        int from = new Integer(vector.range().NUMBER(0).getText());
                        int to = new Integer(vector.range().NUMBER(1).getText());
                        int step = from > to ? -1 : 1;

                        // TODO: refactor
                        for (int r = from; true; r += step, i++) {
                            emit(staticMatrix.id + "[" + i + "]=" + r + ";");
                            if (r == to) {
                                i++;
                                break;
                            }
                        }
                    }
                }

                assert i == staticMatrix.size;

            }
        }
    }

    @Override
    public Void visitProg(TrinityParser.ProgContext ctx) {
        // Reset variables and StringBuilders
        scopeDepth = 0;
        output.setLength(0);
        mainBody.setLength(0);
        funcBody.setLength(0);
        globals.setLength(0);

        setEmitterContext(mainBody);

        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        emitDependencies(ctx.semiExpr());
        if (scopeDepth == 0) {
            setEmitterContext(globals);

            ctx.type().accept(this);
            emit(ctx.ID().getText());
            emit(";");

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
        setEmitterContext(funcBody);

        ctx.type().accept(this);
        emit("_" + ctx.ID().getText());
        emit("(");
        if (ctx.formalParameters() != null) {
            ctx.formalParameters().accept(this);
        }
        emit("){");
        ctx.block().accept(this);
        emit("}");

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
        return null;
    }

    @Override
    public Void visitBlock(TrinityParser.BlockContext ctx) {
        scopeDepth++;
        for (TrinityParser.StmtContext stmt : ctx.stmt()) {
            stmt.accept(this);
        }
        if (ctx.returnStmt() != null) {
            ctx.returnStmt().accept(this);
        }
        scopeDepth--;
        return null;
    }

    @Override
    public Void visitReturnStmt(TrinityParser.ReturnStmtContext ctx) {
        emitDependencies(ctx.semiExpr());
        emit("return ");
        ctx.semiExpr().accept(this);
        return null;
    }

    @Override
    public Void visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        visitChildren(ctx);
        emit(";");
        return null;
    }

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
        if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof PrimitiveType) {
            emit("pow(");
            ctx.expr(0).accept(this);
            emit(",");
            ctx.expr(1).accept(this);
            emit(")");
        }
        else if (ctx.expr(0).t instanceof MatrixType && ctx.expr(1).t instanceof PrimitiveType) {
            emit("mfexpo(");
            emit(")");
        }

        return null;
    }// TODO: Throw error when exponent is <0


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
        if (ctx.op.getText().equals("*")) {
            if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof PrimitiveType) {
                ctx.expr(0).accept(this);
                emit("*");
                ctx.expr(1).accept(this);
            } else if (ctx.expr(0).t instanceof MatrixType && ctx.expr(1).t instanceof MatrixType) {
                if (((MatrixType) ctx.expr(0).t).getRows() == 1 && ((MatrixType) ctx.expr(1).t).getRows() == 1) {
                    emit("dotProduct(");
                    ctx.expr(0).accept(this);
                    emit(",");
                    ctx.expr(1).accept(this);
                    emit("," + ((MatrixType) ctx.expr(0).t).getCols());
                    emit(")");
                } else {
                    emit("mmmult(");
                    ctx.expr(0).accept(this);
                    emit("," + ((MatrixType) ctx.expr(0).t).getRows());
                    emit("," + ((MatrixType) ctx.expr(0).t).getCols());
                    emit(",");
                    ctx.expr(1).accept(this);
                    emit("," + ((MatrixType) ctx.expr(1).t).getRows());
                    emit("," + ((MatrixType) ctx.expr(1).t).getCols());
                    emit(")");
                }
            }else if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof MatrixType) {
                emit("fmmult(");
                ctx.expr(0).accept(this);
                emit(",");
                ctx.expr(1).accept(this);
                emit("," + ((MatrixType) ctx.expr(1).t).getRows());
                emit("," + ((MatrixType) ctx.expr(1).t).getCols());
                emit(")");
            } else if (ctx.expr(0).t instanceof MatrixType && ctx.expr(1).t instanceof PrimitiveType) {
                emit("fmmult(");
                ctx.expr(1).accept(this);
                emit(",");
                ctx.expr(0).accept(this);
                emit("," + ((MatrixType) ctx.expr(0).t).getRows());
                emit("," + ((MatrixType) ctx.expr(0).t).getCols());
                emit(")");
            } // TODO: smarter way to destinguish between Matrix*Primitive and Primitive*Matrix
        }

        else {
            // Op can only be "*" which we have done or "/" hence the else
            if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof PrimitiveType) {
                ctx.expr(0).accept(this);
                emit("/");
                ctx.expr(1).accept(this);
            } else if (ctx.expr(0).t instanceof MatrixType && ctx.expr(1).t instanceof PrimitiveType) {
                emit("mfdiv(");
                ctx.expr(1).accept(this);
                emit(",");
                ctx.expr(0).accept(this);
                emit("," + ((MatrixType) ctx.expr(0).t).getRows());
                emit("," + ((MatrixType) ctx.expr(0).t).getCols());
                emit(")");
            }
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
        } else if (ctx.expr().t instanceof MatrixType) {
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
        emit("_" + ctx.ID().getText());
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
            emit("mmeq(");
            ctx.expr(0).accept(this);
            emit(",");
            ctx.expr(1).accept(this);
            emit("," + ((MatrixType) ctx.expr(1).t).getRows());
            emit("," + ((MatrixType) ctx.expr(1).t).getCols());
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
        if (ctx.op.getText().equals("+")){
            if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof PrimitiveType) {
                ctx.expr(0).accept(this);
                emit("+");
                ctx.expr(1).accept(this);
            } else {
                emit("mmadd(");
                ctx.expr(0).accept(this);
                emit(",");
                ctx.expr(1).accept(this);
                emit("," + ((MatrixType) ctx.expr(0).t).getRows());
                emit("," + ((MatrixType) ctx.expr(0).t).getCols());
                emit(")");
            }
        } else {
            // Op can only be "+" which we have done or "-" hence the else
            if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof PrimitiveType) {
                ctx.expr(0).accept(this);
                emit("-");
                ctx.expr(1).accept(this);
            } else {
                emit("mmsubt(");
                ctx.expr(0).accept(this);
                emit(",");
                ctx.expr(1).accept(this);
                emit("," + ((MatrixType) ctx.expr(0).t).getRows());
                emit("," + ((MatrixType) ctx.expr(0).t).getCols());
                emit(")");
            }
        }
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
       /* int start = new Integer(ctx.NUMBER(0).getText());
        int end = new Integer(ctx.NUMBER(1).getText());
        int step = start > end ? -1 : 1;

        emit(Integer.toString(start));
        for (int i = start + 1; i < end; i += step) {
            emit("," + i);
        }*/
        emit("{range}");
        return null;
    }

    @Override
    public Void visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        // Emit identifier to matrix array initialized with the dependency visitor.
        emit(ctx.ref);
        return null;
    }


    @Override
    public Void visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        // Emit identifier to vector array initialized with the dependency visitor.
        emit(ctx.ref);
        return null;
    }

    @Override
    public Void visitVector(TrinityParser.VectorContext ctx) {
        // TODO: This should never be called
        System.out.println("ERROR: visitVector should not be called.");
        return null;
    }

    @Override
    public Void visitMatrix(TrinityParser.MatrixContext ctx) {
        // TODO: This should never be called
        System.out.println("ERROR: visitMatrix should not be called.");
        return null;
    }

    @Override
    public Void visitSingleExpression(TrinityParser.SingleExpressionContext ctx) {
        emitDependencies(ctx.semiExpr());
        return null;
    }

    // TODO: these will be visited by default implementation (TrinityBaseVisitor)
    /*@Override
    public Void visitConstDeclaration(TrinityParser.ConstDeclarationContext ctx) {

        return super.visitConstDeclaration(ctx);
    }

    */

}