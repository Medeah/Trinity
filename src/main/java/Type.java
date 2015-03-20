// Class for representing and handling types in Trinity

import java.util.List;

public class Type {
    private boolean isFunction;
    private List<Type> parameterTypes;

    public Type(Type input, List<Type> formalParameterTypes) {
        setType(input.getType());
        this.isFunction = true;
        this.parameterTypes = formalParameterTypes;
    }

    public Type(TrinityType input) {
        setType(input);
        this.isFunction = false;
    }

    public Type() {
        setType(null);
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public TrinityType getType() {
        return type;
    }

    public void setType(TrinityType type) {
        this.type = type;
    }

    public enum TrinityType {BOOLEAN, SCALAR, VECTOR, MATRIX}

    private TrinityType type;
}
