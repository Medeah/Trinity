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
import trinity.visitors.ReachabilityVisitor;
import trinity.visitors.TypeVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.io.Files.getNameWithoutExtension;

public class Trinity {

    private static CommandLineOptions options = new CommandLineOptions();

    private Trinity() {
    }

    public static void main(String[] args) throws Exception {
        JCommander jc = new JCommander(options, args);

        // TODO: hardcoded options for testing
        //options.files.add("src/test/resources/trinity/tests/simple.tri");
        //options.formatc = true;

        if (options.version) {
            System.out.println("Trinity 0.1");
            System.exit(0);
        }

        if (options.help) {
            jc.usage();
            System.exit(0);
        }
        if (options.files.size() == 0) {
            System.out.println("No file specified use -h to see usage");
            System.exit(1);
        } else if (options.files.size() > 1) {
            System.out.println("Too manny files specified");
            System.exit(1);
        }

        Path triFile = Paths.get(options.files.get(0));
        String filename = getNameWithoutExtension(triFile.toString());
        Path cFile = Paths.get(filename + ".c");

        if (options.output == null) {
            options.output = filename;
        }

        try {
            if (options.prettyPrint) {
                prettyPrint(triFile, options.indentation);
            } else {
                String out = compile(triFile, options.gpuenabled);
                Files.write(cFile, out.getBytes());

                if (options.ccompiler == null) {
                    if (options.gpuenabled) {
                        options.ccompiler = "nvcc";
                    } else {
                        options.ccompiler = "cc";
                    }
                }

                Process ccProcess = new ProcessBuilder(options.ccompiler, cFile.toString(), "-lm", "-o", options.output).start();

                if (ccProcess.waitFor() != 0) {
                    System.err.println("Error compiling c code");
                }

                if (options.formatc) {
                    Process indentProcess = new ProcessBuilder("indent", cFile.toString()).start();
                    if (indentProcess.waitFor() != 0) {
                        System.err.println("error running indent, do you have it installed?");
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("File not found: " + ex.getMessage());
            System.exit(1);
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        } catch (TypeCheckException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }

    }

    public static String compile(String trinityProgram, boolean compilemode) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(trinityProgram);
        return compile(input, compilemode);
    }

    public static String compile(Path filePath, boolean compilemode) throws Exception {
        ANTLRInputStream input = new ANTLRFileStream(filePath.toString());
        return compile(input, compilemode);
    }

    private static String compile(ANTLRInputStream is, boolean compilemode) throws Exception {
        Pair<ParseTree, TrinityParser> r = parse(is);
        ParseTree tree = r.a;
        TrinityParser parser = r.b;

        ErrorReporter reporter = new StandardErrorReporter(!options.notFailOnError, parser.getInputStream().getTokenSource().getInputStream().toString());
        SymbolTable table = new HashSymbolTable();

        TypeVisitor typeChecker = new TypeVisitor(reporter, table);

        typeChecker.visit(tree);
        if (reporter.getErrorAmount() > 0) {
            throw new TypeCheckException("Too many type errors aborting");
        }

        ReachabilityVisitor reachabilityChecker = new ReachabilityVisitor(reporter);
        tree.accept(reachabilityChecker);
        if (parser.getNumberOfSyntaxErrors() != 0) {
            throw new ParseException("Invalid reachability test.");
        }

        CodeGenerationVisitor generator = new CodeGenerationVisitor(compilemode);
        generator.visit(tree);
        String out = generator.getOutput();

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

    private static void prettyPrint(Path filename, int indentation) throws Exception {
        ANTLRInputStream input = new ANTLRFileStream(filename.toString());
        ParseTree tree = parse(input).a;
        PrettyPrintVisitor prettyPrinter = new PrettyPrintVisitor(indentation);
        prettyPrinter.visit(tree);
        System.out.print(prettyPrinter.getOutput());
    }

    private static class CommandLineOptions {
        @Parameter(description = "filename")
        private List<String> files = new ArrayList<>();

        @Parameter(names = {"-p", "--pretty"}, description = "Pretty Print mode")
        private boolean prettyPrint;

        @Parameter(names = {"-i", "--indent"}, description = "Indentation width")
        private int indentation = 4;

        @Parameter(names = {"-g", "--go"}, description = "Keep-on-trucking on error")
        private boolean notFailOnError;

        @Parameter(names = {"-f", "--format"}, description = "Format the generated c code using indent")
        private boolean formatc;

        @Parameter(names = {"-gpu", "--gpuenabled"}, description = "Enabled some functions to be performed on a gpu")
        private boolean gpuenabled;

        @Parameter(names = {"-c", "--ccompiler"}, description = "Name of c compiler command to use. If nothing is specified a default value will be chosen depending on the value of gpuenabled")
        private String ccompiler;

        @Parameter(names = {"-h", "--help"}, description = "Display this information")
        private boolean help;

        @Parameter(names = {"-o"}, description = "Write output to file")
        private String output;

        @Parameter(names = {"-v", "--version"}, description = "Display the version number")
        private boolean version;
    }
}
