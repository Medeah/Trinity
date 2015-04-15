package trinity.types;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ListType extends Type {

    final private List<Type> types;

    public ListType(List<Type> formalParameterTypes) {
        this.types = ImmutableList.copyOf(formalParameterTypes);
    }

    public List<Type> getTypes() {
        return types;
    }

    @Override public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof ListType) {
            ListType that = (ListType) other;
            result = (this.types.equals(that.types));
        }
        return result;
    }

    @Override public int hashCode() {
        return types.hashCode();
    }
}
