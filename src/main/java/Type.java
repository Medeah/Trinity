// Class for representing and handling types in Trinity

public class Type {
    public Type(TrinityType input) {
        setType(input);
    }

    public Type() {
        setType(null);
    }

    public TrinityType getType() {
        return type;
    }

    public void setType(TrinityType type) {
        this.type = type;
    }

    public enum TrinityType {Boolean, Scalar, Vector, Matrix}

    private TrinityType type;
}
