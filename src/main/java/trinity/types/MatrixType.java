package trinity.types;

public class MatrixType extends Type {
    final private int rows;
    final private int cols;
    public MatrixType(int rows, int cols)
    {
        this.rows = rows;
        this.cols = cols;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    @Override public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof MatrixType) {
            MatrixType that = (MatrixType) other;
            result = (this.cols == that.cols && this.rows == that.rows);
        }
        return result;
    }

    @Override public int hashCode() {
        return new Integer(this.rows).hashCode() ^ new Integer(this.cols).hashCode();
    }

    @Override public String toString() {
        return "Matrix[" + rows + "," + cols + "]";
    }
}
