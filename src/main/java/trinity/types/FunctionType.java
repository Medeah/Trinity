package trinity.types;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class FunctionType extends Type {

    final private Type returnType;
    final private List<Type> parameterTypes;

    public FunctionType(Type returntype, List<Type> formalParameterTypes) {
        this.returnType = returntype;
        this.parameterTypes = ImmutableList.copyOf(formalParameterTypes);
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public Type getType() {
        return returnType;
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof FunctionType) {
            FunctionType that = (FunctionType) other;
            result = (this.parameterTypes.equals(that.parameterTypes) && this.returnType.equals(that.returnType));
        }
        return result;
    }

    @Override
    public int hashCode() {
        return returnType.hashCode() ^ parameterTypes.hashCode();
    }

    @Override
    public String toString() {
        String out = returnType.toString();
        for (Type t : parameterTypes) {
            out += t.toString();
        }
        return out;
    }
}
