package trinity.visitors;

import com.google.common.collect.ImmutableList;
import trinity.StaticMatrix;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;
import trinity.types.MatrixType;
import trinity.utils.UniqueId;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Visits vector and matrix literals, creating a list of all element expressions,
 * and store a unique id for future reference. This information is used to initialize
 * matrix and vector literals during code generation.
 */
public class DependencyVisitor extends TrinityBaseVisitor<Void> implements TrinityVisitor<Void> {
    //TODO: move.
    //The ref variable stores the generated ids for pre-initialized matrices and vectors
    // so they can be referenced later on.

    private List<StaticMatrix> matrixList = new ArrayList<>();

    public ImmutableList<StaticMatrix> getResult() {
        return ImmutableList.copyOf(matrixList);
    }

    /**
     * Creates a new instance of StaticMatrix and adds it to the list.
     *
     * @param ctx the parse tree
     */
    @Override
    public Void visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        if (ctx.matrix() == null) {
            return null;
        }

        ctx.ref = UniqueId.next();
        StaticMatrix staticMatrix = new StaticMatrix();
        staticMatrix.id = ctx.ref;
        staticMatrix.rows = ctx.matrix().vector();
        staticMatrix.size = ((MatrixType) ctx.t).getCols() * ((MatrixType) ctx.t).getRows();

        visitChildren(ctx);
        matrixList.add(staticMatrix);

        return null;
    }

    /**
     * Creates a new instance of StaticMatrix and adds it to the list.
     * A vector is a matrix with a single row {@code singletonList}
     *
     * @param ctx the parse tree
     */
    @Override
    public Void visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        ctx.ref = UniqueId.next();
        StaticMatrix staticMatrix = new StaticMatrix();
        staticMatrix.id = ctx.ref;
        staticMatrix.rows = singletonList(ctx.vector());
        staticMatrix.size = ((MatrixType) ctx.t).getCols() * ((MatrixType) ctx.t).getRows();

        visitChildren(ctx);
        matrixList.add(staticMatrix);

        return null;
    }

}
