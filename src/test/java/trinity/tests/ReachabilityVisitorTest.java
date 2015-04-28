package trinity.tests;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Ignore;
import trinity.*;
import org.junit.Test;
import trinity.CustomExceptions.ParseException;
import trinity.visitors.ReachabilityVisitor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class    ReachabilityVisitorTest {

    private boolean reachabilityTest(String str) throws Exception{
        ErrorReporter er = new StandardErrorReporter(false, str);
        ReachabilityVisitor ReachabilityVisitor = new ReachabilityVisitor(er);

        ANTLRInputStream input = new ANTLRInputStream(str);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);

        ParseTree tree = parser.prog();

        if (parser.getNumberOfSyntaxErrors() != 0) {
            throw new ParseException("Invalid reachability test.");
        }

        tree.accept(ReachabilityVisitor);

        return er.getErrorAmount() == 0;
    }

    @Ignore
    public void testSimpleFunctionFalseOnly() throws Exception{
        assertFalse(reachabilityTest("Scalar s () do\n" +
                "end"));
        assertFalse(reachabilityTest("Scalar k () do\n" +
                "    1 + 1;\n" +
                "end"));
        assertTrue(reachabilityTest("Scalar s () do\n" +
                "    return 1;\n" +
                "end"));
    }

    @Ignore
    public void testBlockHellNestedFalseOnly() throws Exception{
        assertFalse(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "    end\n" +
                "end"));
        assertFalse(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "        do\n" +
                "        end\n" +
                "    end\n" +
                "end"));
        assertFalse(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "        return 1;\n" +
                "        do\n" +
                "        end\n" +
                "    end\n" +
                "end"));
        assertFalse(reachabilityTest("Scalar s () do\n" +
                "    return 1;\n" +
                "    do\n" +
                "        do\n" +
                "        end\n" +
                "    end\n" +
                "end"));

    }

    @Ignore
    public void testBlockHellNestedTrueOnly() throws Exception{
        assertTrue(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "        do\n" +
                "           return 1;" +
                "        end\n" +
                "    end\n" +
                "end"));
        assertTrue(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "        do\n" +
                "        end\n" +
                "        return 1;" +
                "    end\n" +
                "end"));
        assertTrue(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "        do\n" +
                "        end\n" +
                "    end\n" +
                "    return 1;" +
                "end"));
    }

    @Ignore
    public void testBlockHellContinuedFalseOnly() throws Exception{
        assertFalse(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "    end\n" +
                "    do\n" +
                "    end\n" +
                "end"));
        assertFalse(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "        do\n" +
                "        end\n" +
                "    end\n" +
                "    do\n" +
                "    end\n" +
                "end"));
        assertFalse(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "        do\n" +
                "            return 1;\n" +
                "        end\n" +
                "    end\n" +
                "    do\n" +
                "    end\n" +
                "end"));
    }

    @Ignore
    public void testBlockHellContinuedTrueOnly() throws Exception{
        assertTrue(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "    end\n" +
                "    do\n" +
                "        return 1;\n" +
                "    end\n" +
                "end"));
        assertTrue(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "        do\n" +
                "        end\n" +
                "    end\n" +
                "    do\n" +
                "        return 1;\n" +
                "    end\n" +
                "end"));

    }

    @Ignore
    public void testFunctionsWithIfStatementFalseOnly() throws Exception{
        assertFalse(reachabilityTest("Boolean b () do\n" +
                "    if true then\n" +
                "        return true;\n" +
                "    else\n" +
                "        1 + 1;\n" +
                "    end\n" +
                "end"));
        assertFalse(reachabilityTest("Boolean b () do\n" +
                "    if true then\n" +
                "        1 + 1;\n" +
                "    else\n" +
                "        return false;\n" +
                "    end\n" +
                "end"));
        assertFalse(reachabilityTest("Boolean b () do\n" +
                "    if true then\n" +
                "        1 + 1;\n" +
                "    else\n" +
                "        1 + 1;\n" +
                "    end\n" +
                "end"));
    }

    @Ignore
    public void testFunctionsWithIfStatementTrueOnly() throws Exception{
        assertTrue(reachabilityTest("Boolean b () do\n" +
                "    if true then\n" +
                "        return true;\n" +
                "    else\n" +
                "        return false;\n" +
                "    end\n" +
                "end"));
        assertTrue(reachabilityTest("Scalar s () do\n" +
                "    do\n" +
                "        if true then\n" +
                "            return true;\n" +
                "        else\n" +
                "            return false;\n" +
                "        end\n" +
                "    end\n" +
                "end"));
    }
}
