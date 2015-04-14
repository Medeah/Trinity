import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class Trinity {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            showUsage();
        }

        try {
            //byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
            byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/parsing-tests-new.tri"));

            String is = new String(encoded, Charset.defaultCharset());
            //String out = compile(is);
            prettyPrint(is);
            //System.out.println(out);
        } catch (NoSuchFileException ex) {
            System.out.println("File Not Found: " + ex.getMessage());
            showUsage();
        }
    }

    private static void showUsage() {
        System.out.println("Usage: Trinity <source file>");
        System.exit(1);
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