import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Trinity {

    private static class CommandLineOptions {
        @Parameter(description = "filename")
        private List<String> files = new ArrayList<String>();

        @Parameter(names = {"-p", "--pretty"}, description = "Pretty Print mode")
        private boolean prettyPrint;
    }

    public static void main(String[] args) throws Exception {
        CommandLineOptions options = new CommandLineOptions();
        JCommander jc = new JCommander(options, args);

        /*if (options.files.size() == 0) {
            jc.usage();
            System.exit(1);
        }*/ options.prettyPrint = true;

        try {
            //byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
            byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/ugly-semi.tri"));

            String is = new String(encoded, Charset.defaultCharset());

            if(options.prettyPrint) {
                prettyPrint(is);
            } else {
                String out = compile(is);
                System.out.println(out);
            }

        } catch (NoSuchFileException ex) {
            System.out.println("File not found: " + ex.getMessage());
            System.exit(1);
            //jc.usage();
        }

    }

    private static String compile(String is) {
        ANTLRInputStream input = new ANTLRInputStream(is);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);
        ParseTree tree = parser.prog();

        ErrorReporter reporter = new StandardErrorReporter();
        SymbolTable table = new HashSymbolTable();

        TypeVisitor typeChecker = new TypeVisitor(reporter, table);

        typeChecker.visit(tree);
        if (reporter.getErrorAmount() > 0) {
            System.out.println("To many type errors aborting");
            System.exit(1);
        }
        return tree.toStringTree(parser);
    }

    private static void prettyPrint(String is) {
        ANTLRInputStream input = new ANTLRInputStream(is);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);
        ParseTree tree = parser.prog();

        PrettyPrintVisitor prettyPrinter = new PrettyPrintVisitor();

        prettyPrinter.visit(tree);
    }
}