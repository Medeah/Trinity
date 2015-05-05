package trinity;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import trinity.customExceptions.ParseException;
import trinity.customExceptions.TypeCheckException;
import trinity.visitors.CodeGenerationVisitor;
import trinity.visitors.PrettyPrintVisitor;
import trinity.visitors.TypeVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.io.Files.getNameWithoutExtension;

public class Trinity {

    private Trinity () {}
    static CommandLineOptions options = new CommandLineOptions();

    private static class CommandLineOptions {
        @Parameter(description = "filename")
        private List<String> files = new ArrayList<String>();

        @Parameter(names = {"-p", "--pretty"}, description = "Pretty Print mode")
        private boolean prettyPrint;

        @Parameter(names = {"-i", "--indent"}, description = "Indentation width")
        private int indentation = 4;

        @Parameter(names = {"-g", "--go"}, description = "Keep-on-trucking on error")
        private boolean notFailOnError;

        @Parameter(names = {"-f", "--format"}, description = "Format the generated c code using indent")
        private boolean formatc;

        @Parameter(names = {"-c", "--ccompiler"}, description = "Name of c compiler command")
        private String ccompiler = "cc";

        @Parameter(names = {"-v", "--version"}, description = "Display the version number")
        private boolean version;
    }

    public static void main(String[] args) throws Exception {
        JCommander jc = new JCommander(options, args);

        options.files.add("src/test/resources/trinity/tests/simple.tri");

        options.formatc = true;

        if (options.version) {
            System.out.println("Trinity 0.1");
            System.exit(0);
        }

        if (options.files.size() != 1) {
            jc.usage();
            System.exit(1);
        }
        String file = options.files.get(0);
        String filename = getNameWithoutExtension(file);
        try {
            if (options.prettyPrint) {
                prettyPrint(file, options.indentation);
            } else {
                String out = compile(file);
                System.out.println(out);
                PrintWriter pw = new PrintWriter(filename + ".c");
                pw.println(out);
                pw.flush();
                if (options.formatc) {
                    Process process = new ProcessBuilder("indent", filename + ".c").start();
                    if (process.waitFor() != 0) {
                        System.err.println("error running indent, do you have it installed?");
                    }
                }

                Process process = new ProcessBuilder(options.ccompiler, filename + ".c", "-lm").start();
                if (process.waitFor() != 0) {
                    System.err.println("Error compiling c code");
                }
            }

        } catch (IOException ex) {
            System.out.println("File not found: " + ex.getMessage());
            System.exit(1);
            //jc.usage();
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        } catch (TypeCheckException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }

    }

    public static String compiles(String trinityProgram) throws Exception{
        ANTLRInputStream input = new ANTLRInputStream(trinityProgram);
        return compile(input);
    }

    public static String compile(String filename) throws Exception {
        ANTLRInputStream input = new ANTLRFileStream(filename);
        return compile(input);
    }

    private static String compile(ANTLRInputStream is) throws Exception {
        Pair<ParseTree, TrinityParser> r = parse(is);
        ParseTree tree = r.a;
        TrinityParser parser = r.b;

        ErrorReporter reporter = new StandardErrorReporter(!options.notFailOnError, parser.getInputStream().getTokenSource().getInputStream().toString());
        SymbolTable table = new HashSymbolTable();

        TypeVisitor typeChecker = new TypeVisitor(reporter, table);

        typeChecker.visit(tree);
        if (reporter.getErrorAmount() > 0) {
            throw new TypeCheckException("To many type errors aborting");
        }

        CodeGenerationVisitor generator = new CodeGenerationVisitor();
        String out = generator.generate(tree);

        return out;
    }

    private static Pair<ParseTree, TrinityParser> parse(ANTLRInputStream is) throws Exception {
        trinity.TrinityLexer lexer = new TrinityLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);
        ParseTree tree = parser.prog();

        if (parser.getNumberOfSyntaxErrors() != 0) {
            throw new ParseException("Input contains syntax errors.");
        }

        return new Pair<>(tree, parser);

    }

    private static void prettyPrint(String filename, int indentation) throws Exception {
        ANTLRInputStream input = new ANTLRFileStream(filename);
        ParseTree tree = parse(input).a;
        PrettyPrintVisitor prettyPrinter = new PrettyPrintVisitor(indentation);
        prettyPrinter.visit(tree);
        System.out.print(prettyPrinter.getString());
    }
}
