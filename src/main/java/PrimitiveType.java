public class PrimitiveType extends Type {
    final private EnumType ptype;

    public PrimitiveType(EnumType ptype) {
        this.ptype = ptype;
    }

    public EnumType getPType () {
        return ptype;
    }
}
