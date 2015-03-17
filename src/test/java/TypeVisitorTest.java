import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypeVisitorTest {

    private ParseTree createParseTree(String str) {
        ANTLRInputStream input = new ANTLRInputStream(str);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);

        return parser.prog();
    }

    TypeVisitor typeVisitor;
    ParseTree parseTree;

    @Before
    public void initialize() {
        typeVisitor = new TypeVisitor(new TestErrorReporter());
    }

    @Test
    public void testConstDecl() throws Exception {
        // Check if the given ConstDecl have same types on LHS and RHS...
        parseTree = createParseTree("Boolean b = true;");
        assertEquals(parseTree.getChild(0).getChild(0).accept(typeVisitor).getType(), Type.TrinityType.Boolean);
        parseTree = createParseTree("Scalar s = 1;");
        assertEquals(parseTree.getChild(0).getChild(0).accept(typeVisitor).getType(), Type.TrinityType.Scalar);
        parseTree = createParseTree("Vector v = [1,2];");
        assertEquals(parseTree.getChild(0).getChild(0).accept(typeVisitor).getType(), Type.TrinityType.Vector);
        parseTree = createParseTree("Matrix m = [1,2][3,4];");
        assertEquals(parseTree.getChild(0).getChild(0).accept(typeVisitor).getType(), Type.TrinityType.Matrix);

        // Check if the given ConstDecl have same types on LHS and RHS...
        parseTree = createParseTree("Boolean b = 1;");
        assertNull(parseTree.getChild(0).getChild(0).accept(typeVisitor).getType());
    }

}
