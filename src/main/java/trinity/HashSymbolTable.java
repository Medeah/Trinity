package trinity;

import trinity.customExceptions.SymbolAlreadyDefinedException;
import trinity.customExceptions.SymbolNotFoundException;
import trinity.types.FunctionType;
import trinity.types.Type;

import java.util.*;

/**
 * Symbol table implemented with a hash table.
 */
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

    /**
     * Gets the nesting level (depth) of the current scope
     *
     * @return the scope depth
     */
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
        for (tableEntry sym : entries) {
            hashTable.remove(sym.name);

            // Restore outer scope
            tableEntry prev = sym.outerDeclaration;
            if (prev != null) {
                hashTable.put(prev.name, prev);
            }
        }
    }

    /**
     * Enters the given symbol information into the symbol table. If the given
     * symbol is already present at the current nest level an exception is thrown.
     *
     * @param id   the symbol identifier
     * @param info the symbol type information
     * @throws SymbolAlreadyDefinedException
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
     * Returns the information associated with the innermost currently valid declaration
     * of the given symbol. If there is no such valid declaration an exception is thrown.
     *
     * @param id the symbol identifier
     * @return the type of the symbol
     * @throws SymbolNotFoundException
     */
    public Type retrieveSymbol(String id) throws SymbolNotFoundException {
        if (hashTable.containsKey(id)) {
            return hashTable.get(id).type;
        }
        throw new SymbolNotFoundException();
    }

    /**
     * Returns whether id is present in the symbol table’s current (innermost) scope.
     *
     * @param id the symbol identifier
     * @return true if symbol is locally declared, false otherwise.
     */
    public boolean declaredLocally(String id) {
        return hashTable.containsKey(id) && hashTable.get(id).depth == getCurrentScopeDepth();
    }

    /**
     * Gets the type of the current function in scope
     *
     * @return the type of the current function in scope
     * @throws SymbolNotFoundException
     */
    public FunctionType getCurrentFunction() throws SymbolNotFoundException {
        return (FunctionType) retrieveSymbol("##func");
    }

    /**
     * Sets the type of the current function in scope
     *
     * @param type the type of the current function in scope
     */
    public void setCurrentFunction(FunctionType type) {
        try {
            enterSymbol("##func", type);
        } catch (SymbolAlreadyDefinedException e) {
            // TODO: should this be caught during type-check instead?
            e.printStackTrace();
        }
    }

}
