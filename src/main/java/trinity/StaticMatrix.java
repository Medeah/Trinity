package trinity;

import java.util.List;

/**
 * This class describes a Matrix or a Vector and is used to
 * initialize elements with expressions.
 * The {@code id} property identifies the matrix according to
 * the {@code ref} property for VectorLiteral and MatrixLiteral.
 *
 * @see trinity.visitors.DependencyVisitor#visitMatrixLiteral(TrinityParser.MatrixLiteralContext)
 */
public class StaticMatrix {
    public List<TrinityParser.VectorContext> rows;
    public String id;
    public int size;
}
