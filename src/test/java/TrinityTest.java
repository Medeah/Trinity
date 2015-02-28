import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.InputStream;
import org.junit.Test;

import static org.junit.Assert.*;

public class TrinityTest {

    @Test
    public void testAdd() throws Exception {
        assertEquals(5, Trinity.add(2,3));
    }

    @Test
    public void testParser() throws Exception  {
        InputStream is = this.getClass().getResourceAsStream("parsing-tests.tri");
        ANTLRInputStream input = new ANTLRInputStream(is);
        LibExprLexer lexer = new LibExprLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LibExprParser parser = new LibExprParser(tokens);
        //parser.removeErrorListeners();
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

}