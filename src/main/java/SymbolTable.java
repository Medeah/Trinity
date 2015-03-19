public interface SymbolTable {
    public void openScope();

    public void closeScope();

    public int getCurrentScopeDepth();

    public Type retrieveSymbol(String name);

    public void enterSymbol(String name, Type type);

    public boolean declaredLocally(String name);
}
