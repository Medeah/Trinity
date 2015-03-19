import java.util.*;

public class HashSymbolTable implements SymbolTable {

    private static class tableEntry {
        private String Name;
        private Type Type;
        private int Depth;
        public tableEntry Var;

        public tableEntry(String name, Type type, int depth) {
            Name = name;
            Type = type;
            Depth = depth;
            Var = null;
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
            hashTable.remove(sym.Name);

            // Restore outer scope
            tableEntry prev = sym.Var;
            if (prev != null) {
                hashTable.put(prev.Name, prev);
            }
        }
    }

    /**
     * Enter the given symbol information into the symbol table. If the given
     * symbol is already present at the current nest level, do whatever is most
     * efficient, but do NOT throw any exceptions from this method.
     */
    public void enterSymbol(String id, Type info) {
        tableEntry newSym = new tableEntry(id, info, getCurrentScopeDepth());

        List<tableEntry> entries = scopeDisplay.peek();
        entries.add(newSym);

        tableEntry oldSym = hashTable.get(id);
        if (oldSym != null) {
            hashTable.remove(id);
            newSym.Var = oldSym;
        }
        hashTable.put(id, newSym);
    }

    /**
     * Returns the information associated with the innermost currently valid
     * declaration of the given symbol. If there is no such valid declaration,
     * return null. Do NOT throw any exceptions from this method.
     */
    public Type retrieveSymbol(String id) {
        return hashTable.containsKey(id) ? hashTable.get(id).Type : null;
    }

    /**
     * tests whether name is present in the symbol table’s
     * current (innermost) scope. If it is, true is returned. If name is in an outer
     * scope, or is not in the symbol table at all, false is returned.
     */
    public boolean declaredLocally(String id) {
        return hashTable.containsKey(id) && hashTable.get(id).Depth == getCurrentScopeDepth();
    }
}
