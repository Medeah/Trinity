package trinity.types;

public class PrimitiveType extends Type {
    final private EnumType ptype;

    public PrimitiveType(EnumType ptype) {
        this.ptype = ptype;
    }

    public EnumType getPType () {
        return ptype;
    }

    @Override public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof PrimitiveType) {
            result = this.ptype == ((PrimitiveType) other).ptype;
        }
        return result;
    }

    @Override public int hashCode() {
        return ptype.hashCode();
    }

    @Override public String toString() {
        if (ptype == EnumType.SCALAR) {
            return "Scalar";
        } else {
            return "Boolean";
        }

    }
}
