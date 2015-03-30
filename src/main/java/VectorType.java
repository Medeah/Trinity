public class VectorType extends Type {
    final private int numElems;
    public VectorType(int numElems)
    {
        this.numElems = numElems;
    }

    public int getNumElems() {
        return numElems;
    }
    @Override public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof VectorType) {
            VectorType that = (VectorType) other;
            result = this.numElems == that.numElems;
        }
        return result;
    }

    @Override public int hashCode() {
        return new Integer(this.numElems).hashCode();
    }

}
