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

    //@AfterClass
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

    @Test
    public void ifStatements() throws Exception {
        assertEquals("1.000000\n", getOutput("if true then print 1; else print 2; end"));
        assertEquals("3.000000\n", getOutput("if 3 < 2 then print 2; else print 3; end"));
        assertEquals("12.000000\n", getOutput("if [1,2] == [1,3] then print 21; elseif 1 == 1 then print 12; else print 3; end"));
        assertEquals("13.000000\n", getOutput("if [1,2] == [1,3] then print 21; elseif 1 != 1 then print 12; else print 13; end"));

        assertEquals("1.000000\n", getOutput("Scalar s = 2; Scalar d = 2; Scalar a = 33; Boolean i = (s == d); Boolean y = (d < a); if i and y then print 1; else print 2; end"));
        assertEquals("3.000000\n", getOutput("Boolean b = false; Boolean c = true; if b and c then print 2; else print 3; end"));
        assertEquals("2.000000\n", getOutput("if true or false then print 2; else print 3; end"));
        assertEquals("3.000000\n", getOutput("if false or false then print 2; else print 3; end"));

    }

    @Test
    public void testRange() throws Exception {
        assertEquals("true\n", getOutput("print [1,2,3,4] == [1..4];"));
        assertEquals("[1.000000, 2.000000, 3.000000, 4.000000]\n", getOutput("Vector[4] v = [1..4]; print v;"));
        assertEquals("[8.000000, 7.000000, 6.000000]\n", getOutput("Vector[3] v = [8..6]; print v;"));
        assertEquals("true\n", getOutput("print [1,2,3][3,4,5] == [1..3][3..5];"));
        assertEquals("[1.000000, 2.000000, 3.000000]\n[3.000000, 4.000000, 5.000000]\n", getOutput("Matrix[2,3] v = [1..3][3..5]; print v;"));
        assertEquals("[1.000000, 2.000000, 3.000000]\n[3.000000, 4.000000, 5.000000]\n", getOutput("Matrix[2,3] v = [1,2,3][3..5]; print v;"));
        assertEquals("[1.000000, 2.000000, 3.000000]\n[3.000000, 4.000000, 5.000000]\n", getOutput("Matrix[2,3] v = [1..3][3,4,5]; print v;"));
    }

    @Test
    public void matrixDependency() throws Exception {
        assertEquals("[7.000000, 8.000000, 9.000000]\n[3.000000, 4.000000, 6.000000]\n[187.000000, 24.000000, 5.000000]\n",
                getOutput("Scalar x(Vector[2] v) do return v[1]; end Vector[2] w = [3,4]; Matrix[3,3] nest = [7..9][3,x(w),6][[1,[3,4]*[6,7]]*[3,4],24,5]; print nest;"));
    }

    @Test
    public void declarations() throws Exception {
        assertEquals("4.000000\n", getOutput("Scalar s = 4; print s;"));
        assertEquals("[4.000000, 6.000000]\n", getOutput("Vector[2] v = [4,6]; print v;"));
        assertEquals("[1.000000, 2.000000, 3.000000]\n[4.000000, 5.000000, 6.000000]\n", getOutput("Matrix[2,3] m = [1,2,3][4,5,6]; print m;"));
        assertEquals("true\n", getOutput("Boolean b = true; print b;"));
    }

    @Test
    public void functions() throws Exception {
        assertEquals("18.000000\n", getOutput("Scalar mulle(Scalar a, Scalar b) do return a*b; end print mulle(3,6);"));
        assertEquals("32.000000\n", getOutput("Scalar dotp(Vector[3] a, Vector[3] b) do Scalar x = 0; return a*b; end Vector[3] v1 = [1,2,3]; print dotp(v1,[4,5,6]);"));
        assertEquals("[28.000000]\n", getOutput("Matrix[1,1] crazy(Vector[2] a, Vector[2] b) do return a*b'; end Vector[2] dave = [2,3]; print crazy(dave,[5,6]);"));
        assertEquals("[4.000000, 5.000000, 6.000000]\n", getOutput("Vector[3] vectosaurus(Scalar a, Scalar b, Scalar c) do return [a,b,c]; end print vectosaurus(4,5,6);"));
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
        assertEquals("[7.000000, 10.000000]\n[15.000000, 22.000000]\n", getOutput("Matrix[2,2] m = [1, 2][3, 4]; Matrix[2,2] n = [1, 2][3, 4]; print m * n;"));
        assertEquals("221.000000\n", getOutput("Vector[4] v = [50, 2, 3, 4]; Vector[4] d = [4, 5, 1, 2]; print v * d;"));
    }

    @Test
    public void division() throws Exception {
        assertEquals("4.000000\n", getOutput("Scalar a = 100; Scalar t = 25; print a / t;"));
        assertEquals("[1.000000, 2.000000, 4.000000, 8.000000]\n", getOutput("Vector[4] v = [2, 4, 8, 16]; Scalar n = 2; print v / n;"));
        assertEquals("[8.000000, 7.000000]\n[6.000000, 5.000000]\n[4.000000, 3.000000]\n[2.000000, 1.000000]\n", getOutput("Matrix[4,2] m = [16, 14][12, 10][8, 6][4, 2]; Scalar i = 2; print m / i;"));
        assertEquals("[-8.000000, -7.000000, -6.000000, -5.000000]\n", getOutput("Vector[4] v = [16, 14, 12, 10]; Scalar i = -2; print v / i;"));
    }

    @Test
    public void not() throws Exception {
        assertEquals("true\n", getOutput("Boolean b = 4 == 3; print !b;"));
        assertEquals("false\n", getOutput("Boolean b = 4 != 3; print !b;"));
    }

    @Test
    public void transpose() throws Exception {
        assertEquals("[1.000000, 7.000000]\n[5.000000, -5.000000]\n[6.000000, -1.000000]\n", getOutput("Matrix[2,3] m = [1, 5, 6][7, -5, -1]; Matrix[3,2] t = m'; print t;"));
        assertEquals("[1.000000, 4.000000, 7.000000]\n[2.000000, -5.000000, 8.000000]\n[3.000000, -6.000000, 9.000000]\n", getOutput("Matrix[3,3] m = [1, 2, 3][4, -5, -6][7, 8, 9]; print m';"));
        assertEquals("[1.000000]\n[2.000000]\n[3.000000]\n[4.000000]\n[5.000000]\n", getOutput("Vector[5] v = [1, 2, 3, 4, 5]; Matrix[5,1] m = v'; print m;"));
        assertEquals("[1.000000, 2.000000, 3.000000, 4.000000, 5.000000]\n", getOutput("Matrix[5,1] m = [1][2][3][4][5]; Vector[5] v = m'; print v;"));
    }

    @Test
    public void addition() throws Exception {
        assertEquals("7.000000\n", getOutput("Scalar a = 5; Scalar b = 2; print a + b;"));
        assertEquals("-5.000000\n", getOutput("Scalar a = -35; Scalar b = 30; print a + b;"));
        assertEquals("[-11.000000, -9.000000, -7.000000, 13.000000]\n[13.000000, 13.000000, 13.000000, 3.000000]\n[5.000000, 13.000000, 13.000000, 13.000000]\n", getOutput("Matrix[3,4] m = [1, 2, 3, 4][5, 6, 7, 8][9, 10, 11, 12]; Matrix[3,4] n = [-12, -11, -10, 9][8, 7, 6, -5][-4, 3, 2, 1]; print m + n;"));
        assertEquals("[5.000000, 7.000000, 9.000000]\n", getOutput("Vector[3] v = [4, 5, 6]; Vector[3] l = [1, 2, 3]; print v + l;"));
    }

    @Test
    public void subtraction() throws Exception {
        assertEquals("3.000000\n", getOutput("Scalar a = 5; Scalar b = 2; print a - b;"));
        assertEquals("-5.000000\n", getOutput("Scalar a = -35; Scalar b = -30; print a - b;"));
        assertEquals("[3.000000, 3.000000, 9.000000]\n", getOutput("Vector[3] v = [4, 5, 6]; Vector[3] l = [1, 2, -3]; print v - l;"));
        assertEquals("[-3.000000, -13.000000]\n[3.000000, 8.000000]\n[4.000000, -3.000000]\n", getOutput("Matrix[3,2] m = [4, -5][-6, 3][-2, 1]; Matrix[3,2] n = [7, 8][-9, -5][-6, 4]; print m - n;"));
    }

    @Test
    public void equality() throws Exception {
        assertEquals("true\n", getOutput("Boolean t = true; Boolean o = true; print t == o;"));
        assertEquals("true\n", getOutput("Boolean t = false; Boolean o = false; print t == o;"));
        assertEquals("false\n", getOutput("Boolean t = true; Boolean o = false; print t == o;"));
        assertEquals("false\n", getOutput("Boolean t = true; Boolean o = true; print t != o;"));
        assertEquals("false\n", getOutput("Boolean t = false; Boolean o = false; print t != o;"));
        assertEquals("true\n", getOutput("Boolean t = true; Boolean o = false; print t != o;"));
        assertEquals("true\n", getOutput("Scalar s = 12; Scalar c = 12; print s == c;"));
        assertEquals("false\n", getOutput("Scalar s = -12; Scalar c = 12; print s == c;"));
        assertEquals("false\n", getOutput("Scalar s = 12; Scalar c = 12; print s != c;"));
        assertEquals("true\n", getOutput("Scalar s = -12; Scalar c = 12; print s != c;"));
        assertEquals("true\n", getOutput("Vector[5] d = [1, 2, 3, 4, 5]; Vector[5] q = [1, 2, 3, 4, 5]; print d == q;"));
        assertEquals("false\n", getOutput("Vector[5] d = [1, 2, 3, 4, 5]; Vector[5] q = [100, 2, 3, 4, 5]; print d == q;"));
        assertEquals("false\n", getOutput("Vector[5] d = [1, 2, 3, 4, 5]; Vector[5] q = [1, 2, 3, 4, 5]; print d != q;"));
        assertEquals("true\n", getOutput("Vector[5] d = [1, 2, 3, 4, 5]; Vector[5] q = [100, 2, 3, 4, 5]; print d != q;"));
        assertEquals("true\n", getOutput("Matrix[2,2] m = [1, 2][3, 4]; Matrix[2,2] n = [1, 2][3, 4]; print m == n;"));
        assertEquals("false\n", getOutput("Matrix[2,3] m = [1, 2, 3][4, 5, 6]; Matrix[2,3] n = [6, 5, 4][3, 2, 1]; print m == n;"));
        assertEquals("false\n", getOutput("Matrix[2,2] m = [1, 2][3, 4]; Matrix[2,2] n = [1, 2][3, 4]; print m != n;"));
        assertEquals("true\n", getOutput("Matrix[2,3] m = [1, 2, 3][4, 5, 6]; Matrix[2,3] n = [6, 5, 4][3, 2, 1]; print m != n;"));
    }

    @Test
    public void relation() throws Exception {
        assertEquals("true\n", getOutput("Scalar a = 22.5; Scalar t = 22; Boolean b = a > t; print b;"));
        assertEquals("false\n", getOutput("Scalar a = 22.5; Scalar t = 22; Boolean b = a < t; print b;"));
        assertEquals("false\n", getOutput("Scalar a = 22; Scalar t = 22; Boolean b = a < t; print b;"));
        assertEquals("true\n", getOutput("Scalar e = 45; Scalar r = 45; print e <= r;"));
        assertEquals("false\n", getOutput("Scalar e = 93; Scalar r = 45; print e <= r;"));
        assertEquals("true\n", getOutput("Scalar s = 93; Scalar d = 45; print s >= d;"));
        assertEquals("false\n", getOutput("Scalar s = 2; Scalar d = 45; print s >= d;"));
    }

}