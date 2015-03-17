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

    TypeVisitor vis;
    ParseTree tree;
    TestErrorReporter er;

    @Before
    public void initialize() {
        er = new TestErrorReporter();
        vis = new TypeVisitor(er);
    }

    @Test
    public void testConstDecl() throws Exception {
        // Check if the given ConstDecl have same types on LHS and RHS...
        tree = createParseTree("Boolean b = true;");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.Boolean);
        tree = createParseTree("Scalar s = 1;");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.Scalar);
        tree = createParseTree("Vector v = [1,2];");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.Vector);
        tree = createParseTree("Matrix m = [1,2][3,4];");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.Matrix);
    }

    @Test
    public void testSimpleConstDeclErrorReporting() throws Exception {
        // Check if the correct ErrorReport is thrown...
        tree = createParseTree("Boolean b = 1;");                               // Create invalid ConstDecl
        tree.getChild(0).getChild(0).accept(vis);                               // Type-check ConstDecl
        assertTrue(er.getError(er.getErrorAmount() - 1).equals("Type error!"));   // Expected error report

        tree = createParseTree("Scalar s = false;");                            // Create invalid ConstDecl
        tree.getChild(0).getChild(0).accept(vis);                               // Type-check ConstDecl
        assertTrue(er.getError(er.getErrorAmount() - 1).equals("Type error!"));   // Expected error report

        tree = createParseTree("Vector v = [1][2];");                           // Create invalid ConstDecl
        tree.getChild(0).getChild(0).accept(vis);                               // Type-check ConstDecl
        assertTrue(er.getError(er.getErrorAmount() - 1).equals("Type error!"));   // Expected error report

        tree = createParseTree("Matrix v = 1;");                                // Create invalid ConstDecl
        tree.getChild(0).getChild(0).accept(vis);                               // Type-check ConstDecl
        assertTrue(er.getError(er.getErrorAmount() - 1).equals("Type error!"));   // Expected error report

        // There should be 4 reported errors...
        assertTrue(er.getErrorAmount() == 4);
    }

}
