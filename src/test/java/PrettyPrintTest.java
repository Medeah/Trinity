import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class PrettyPrintTest {

    private TrinityParser createParser(String path) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(path);
        ANTLRInputStream input = new ANTLRInputStream(is);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new TrinityParser(tokens);
    }

    @Test
    public void testPrettyPrinter() throws Exception  {
        URL url = Resources.getResource("parsing-tests.tri");
        String pretty = Resources.toString(url, Charsets.UTF_8);

        TrinityParser parser = createParser("ugly.tri");
        ParseTree tree = parser.prog();
        PrettyPrintVisitor prettyPrinter = new PrettyPrintVisitor();

        String prettyPrinted = prettyPrinter.prettyfy(tree);

        assertEquals(pretty, prettyPrinted);
    }


}
