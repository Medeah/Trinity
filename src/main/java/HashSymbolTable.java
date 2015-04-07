import CustomExceptions.*;

import java.util.*;

public class HashSymbolTable implements SymbolTable {

    private static class tableEntry {
        private String name;
        private Type type;
        private int depth;
        public tableEntry outerDeclaration;

        public tableEntry(String name, Type type, int depth) {
            this.name = name;
            this.type = type;
            this.depth = depth;
            this.outerDeclaration = null;
        }
    }

    private Map<String, tableEntry> hashTable = new HashMap<String, tableEntry>();
    private Deque<List<tableEntry>> scopeDisplay = new ArrayDeque<List<tableEntry>>();

    public HashSymbolTable() {
        openScope();
    }

    public int getCurrentScopeDepth() {
        return scopeDisplay.size();
    }

    /**
     * Opens a new scope, retaining outer ones
     */
    public void openScope() {
        List<tableEntry> entries = new ArrayList<tableEntry>();
        scopeDisplay.push(entries);
    }

    /**
     * Closes the innermost scope
     */
    public void closeScope() {
        List<tableEntry> entries = scopeDisplay.pop();
        for(tableEntry sym : entries) {
            hashTable.remove(sym.name);

            // Restore outer scope
            tableEntry prev = sym.outerDeclaration;
            if (prev != null) {
                hashTable.put(prev.name, prev);
            }
        }
    }

    /**
     * Enter the given symbol information into the symbol table. If the given
     * symbol is already present at the current nest level, Throw an exception.
     */
    public void enterSymbol(String id, Type info) throws SymbolAlreadyDefinedException {
        if (declaredLocally(id)) {
            throw new SymbolAlreadyDefinedException();
        }

        tableEntry newSym = new tableEntry(id, info, getCurrentScopeDepth());

        List<tableEntry> entries = scopeDisplay.peek();
        entries.add(newSym);

        tableEntry oldSym = hashTable.get(id);
        if (oldSym != null) {
            hashTable.remove(id);
            newSym.outerDeclaration = oldSym;
        }
        hashTable.put(id, newSym);
    }

    /**
     * Returns the information associated with the innermost currently valid
     * declaration of the given symbol. If there is no such valid declaration,
     * return null. Do NOT throw any exceptions from this method.
     */
    public Type retrieveSymbol(String id) throws SymbolNotFoundException {
        if ( hashTable.containsKey(id))  {
            return hashTable.get(id).type;
        }
        throw new SymbolNotFoundException();
    }

    /**
     * tests whether name is present in the symbol table’s
     * current (innermost) scope. If it is, true is returned. If name is in an outer
     * scope, or is not in the symbol table at all, false is returned.
     */
    public boolean declaredLocally(String id) {
        return hashTable.containsKey(id) && hashTable.get(id).depth == getCurrentScopeDepth();
    }
}
