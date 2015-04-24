package trinity.tests;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import trinity.*;
import org.junit.Test;
import trinity.CustomExceptions.ParseException;
import trinity.visitors.ReachabilityVisitor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class    ReachabilityVisitorTest {

    private boolean reachabilityTest(String str) throws Exception{
        ErrorReporter er = new StandardErrorReporter(false, str);
        SymbolTable tab = new HashSymbolTable();
        ReachabilityVisitor ReachabilityVisitor = new ReachabilityVisitor(er, tab);

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

    @Test
    public void testSimpleFunction() throws Exception{
        assertTrue(reachabilityTest("Boolean b () do return false; end"));
        assertTrue(reachabilityTest("Scalar b () do return 1+1; end"));

        assertFalse(reachabilityTest("Boolean b () do Scalar s = 1+1; end"));
        assertFalse(reachabilityTest("Boolean b (Scalar s) do return s; end"));
    }

    @Test
    public void testFunctionWithoutElse() throws Exception{

    }

    @Test
    public void moreFunctionTestst() throws Exception{
        assertTrue(reachabilityTest("Scalar b() do if (1+1)==2 then return false; elseif test() then return true; else return false; end end Boolean test() do  return true; end"));
        assertTrue(reachabilityTest("Boolean b () do return 2==2; end"));
        assertTrue(reachabilityTest("Boolean b () do return !(2==2); end"));

        assertFalse(reachabilityTest("Boolean b () do return; end"));
        assertFalse(reachabilityTest("Scalar b() do if false then return false; end end"));
    }
}
