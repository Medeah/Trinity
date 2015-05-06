package trinity.tests;

import com.google.common.io.CharStreams;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import trinity.Trinity;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.FileSystems;

import static java.nio.file.Files.delete;
import static org.junit.Assert.assertEquals;

public class CodeGenerationVisitorTest {

    private static String getOutput(String input) throws Exception {
        String out = Trinity.compile(input);
        PrintWriter pw = new PrintWriter("test.c");
        pw.println(out);
        pw.flush();

        Process process = new ProcessBuilder("gcc", "test.c", "-lm").start();
        if (process.waitFor() != 0) {
            throw new Exception("Error compiling c code");
        }

        Process prog = new ProcessBuilder("./a.out").start();
        if (prog.waitFor() != 0) {
            throw new Exception("program exited with non zero exit code");
        }

        InputStreamReader inr = new InputStreamReader(prog.getInputStream());
        return CharStreams.toString(inr);
    }

    @AfterClass
    public static void removeFiles() throws Exception {
        delete(FileSystems.getDefault().getPath("test.c"));
        delete(FileSystems.getDefault().getPath("a.out"));
    }

    @Test
    public void parseReturn() throws Exception {
        assertEquals("1.000000\n", getOutput("print 1;"));
    }

    @Test
    public void stdlib() throws Exception {
        assertEquals("1.000000\n", getOutput("print abs(-1);"));
        assertEquals("1.000000\n", getOutput("print abs(1);"));
        assertEquals("1.000000\n", getOutput("print round(1.23);"));
        assertEquals("1.000000\n", getOutput("print floor(1.23);"));
        assertEquals("2.000000\n", getOutput("print ceil(1.23);"));
        assertEquals("0.841471\n", getOutput("print sin(1);"));
        assertEquals("0.540302\n", getOutput("print cos(1);"));
        assertEquals("1.557408\n", getOutput("print tan(1);"));
        assertEquals("0.523599\n", getOutput("print asin(0.5);"));
        assertEquals("1.047198\n", getOutput("print acos(0.5);"));
        assertEquals("0.785398\n", getOutput("print atan(1);"));
        assertEquals("2.302585\n", getOutput("print log(10);"));
        assertEquals("2.000000\n", getOutput("print log10(100);"));
        assertEquals("10.000000\n", getOutput("print sqrt(100);"));
        assertEquals("1.414214\n", getOutput("print sqrt(2);"));
    }

    @Test
    public void forLoop() throws Exception {
        assertEquals("1.000000\n2.000000\n3.000000\n4.000000\n", getOutput("for Scalar s in [1,2,3,4] do print s; end"));
        assertEquals("[1.000000, 2.000000]\n[3.000000, 4.000000]\n", getOutput("for Vector[2] v in [1,2][3,4] do print v; end"));
        assertEquals("1.000000\n2.000000\n3.000000\n4.000000\n", getOutput("for Vector[2] v in [1,2][3,4] do for Scalar s in v do print s; end end"));

        assertEquals("4.000000\n6.000000\n", getOutput("Vector[2] v = [4,6]; for Scalar s in v do print s; end"));
    }

    @Ignore
    public void ifStatements() throws Exception {
        assertEquals("1.000000\n", getOutput("if true then print 1; else print 2; end"));
        assertEquals("3.000000\n", getOutput("if 3 < 2 then print 2; else print 3; end"));
        assertEquals("12.000000\n", getOutput("if [1,2] == [1,3] then print 21; elseif 1 == 1 then print 12; else print 3; end"));
        assertEquals("13.000000\n", getOutput("if [1,2] == [1,3] then print 21; elseif 1 != 1 then print 12; else print 13; end"));
    }

    @Test
    public void testBooleans() throws Exception {
        assertEquals("false\n", getOutput("Boolean b = 4 == 3; print b;"));
        assertEquals("true\n", getOutput("Boolean b = 4 != 3; print b;"));
       // assertEquals("false\n", getOutput("Boolean b = 4 == 3; print b;"));
   }

    @Test
    public void declarations() throws Exception {
        assertEquals("4.000000\n", getOutput("Scalar s = 4; print s;"));
        assertEquals("[4.000000, 6.000000]\n", getOutput("Vector[2] v = [4,6]; print v;"));
        assertEquals("[1.000000, 2.000000, 3.000000]\n[4.000000, 5.000000, 6.000000]\n", getOutput("Matrix[2,3] m = [1,2,3][4,5,6]; print m;"));
        assertEquals("true\n", getOutput("Boolean b = true; print b;"));
    }

    @Test
    public void negation() throws Exception {
        assertEquals("-1.000000\n", getOutput("Scalar m = 1; Scalar n = -m; print n;"));
        assertEquals("3.000000\n", getOutput("Scalar m= -3; print -m;"));
        assertEquals("[-1.000000, -2.000000, -3.000000, -4.000000]\n", getOutput("Vector[4] v = [1, 2, 3, 4]; Vector[4] o = -v; print o;"));
        assertEquals("[-1.000000, -2.000000]\n[-3.000000, -4.000000]\n[-5.000000, -1.000000]\n[-6.000000, -2.000000]\n", getOutput("Matrix[4,2] M = [1, 2][3, 4][5, 1][6, 2]; print -M;"));
    }

    @Test
    public void multiplication() throws Exception {
        assertEquals("125.000000\n", getOutput("Scalar a = 5; Scalar b = 25; Scalar r = a * b; print r;"));
        assertEquals("-125.000000\n", getOutput("Scalar a = 5; Scalar b = -25; Scalar r = a * b; print r;"));
        assertEquals("125.000000\n", getOutput("Scalar a = -5; Scalar b = -25; Scalar r = a * b; print r;"));
        assertEquals("8.000000\n", getOutput("Scalar a = 2; Scalar b = 2; Scalar r = a * b * b; print r;"));
        assertEquals("[2.000000, 4.000000, 6.000000, 8.000000, 10.000000]\n", getOutput("Vector[5] v = [1, 2, 3, 4, 5]; Scalar j = 2; Vector[5] k = v*j; print k;"));
        assertEquals("[2.000000, 4.000000]\n[6.000000, 8.000000]\n[10.000000, 12.000000]\n", getOutput("Matrix[3,2] m = [1, 2][ 3, 4][5, 6]; Scalar j = 2; Matrix[3,2] k = m*j; print k;"));
        assertEquals("[2.000000, 4.000000]\n[6.000000, 8.000000]\n[10.000000, 12.000000]\n", getOutput("Matrix[3,2] m = [1, 2][ 3, 4][5, 6]; Scalar j = 2; Matrix[3,2] k = j*m; print k;"));
        assertEquals("[5.000000, -10.000000]\n[15.000000, -10.000000]\n[23.000000, -14.000000]\n", getOutput("Matrix[3,2] a = [-1, 2][3, 4][5, 6]; Matrix[2,2] b = [1, 2][3, -4]; Matrix[3,2] c = a * b; print c;"));
    }

    @Ignore
    public void matrixTranspose() throws Exception {

    }



}