package trinity;

import trinity.CustomExceptions.SymbolAlreadyDefinedException;
import trinity.CustomExceptions.SymbolNotFoundException;
import trinity.types.FunctionType;
import trinity.types.Type;

public interface SymbolTable {
    void openScope();

    void closeScope();

    int getCurrentScopeDepth();

    Type retrieveSymbol(String name) throws SymbolNotFoundException;

    void enterSymbol(String name, Type type) throws SymbolAlreadyDefinedException;

    boolean declaredLocally(String name);

    FunctionType getCurrentFunction() throws SymbolNotFoundException;

    void setCurrentFunction(FunctionType inp);
}
