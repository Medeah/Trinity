package trinity;

import trinity.CustomExceptions.SymbolAlreadyDefinedException;
import trinity.CustomExceptions.SymbolNotFoundException;
import trinity.types.Type;

public interface SymbolTable {
    public void openScope();

    public void closeScope();

    public int getCurrentScopeDepth();

    public Type retrieveSymbol(String name) throws SymbolNotFoundException;

    public void enterSymbol(String name, Type type) throws SymbolAlreadyDefinedException;

    public boolean declaredLocally(String name);
}
