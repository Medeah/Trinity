import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class HashSymbolTable implements SymbolTable {

    private static class tableEntry {
        private String Name;
        private Type Type;
        private int scopeLevel;
        public tableEntry Var;
        public tableEntry Level;

        public tableEntry(String name, Type type, int lev) {
            Name = name;
            Type = type;
            scopeLevel = lev;
            Var = nullentry;
            Level = nullentry;
        }
    }

    private Map<String, tableEntry> hashTable = new HashMap<String, tableEntry>();
    private Deque<tableEntry> scopeDisplay = new ArrayDeque<tableEntry>();
    private static final tableEntry nullentry = new tableEntry("null", null, -1);

    public HashSymbolTable() {
        openScope();
    }

    public int getCurrentScopeLevel() {
        return scopeDisplay.size();
    }

    /**
     * Opens a new scope, retaining outer ones
     */
    public void openScope() {
        scopeDisplay.push(nullentry);
    }

    /**
     * Closes the innermost scope
     */
    public void closeScope() {
        for (tableEntry sym = scopeDisplay.pop(); sym != nullentry; sym = sym.Level) {
            hashTable.remove(sym.Name);

            tableEntry prev = sym.Var;
            if (prev != nullentry) {
                hashTable.put(prev.Name, prev);
            }
        }
    }

    /**
     * Enter the given symbol information into the symbol table. If the given
     * symbol is already present at the current nest level, do whatever is most
     * efficient, but do NOT throw any exceptions from this method.
     */
    public void enterSymbol(String s, Type info) {
        tableEntry newSym = new tableEntry(s, info, getCurrentScopeLevel());

        newSym.Level = scopeDisplay.peek();
        scopeDisplay.pop();
        scopeDisplay.push(newSym);

        tableEntry oldSym = hashTable.get(s);
        if (oldSym != null) {
            hashTable.remove(s);
            newSym.Var = oldSym;
        }
        hashTable.put(s, newSym);
    }

    /**
     * Returns the information associated with the innermost currently valid
     * declaration of the given symbol. If there is no such valid declaration,
     * return null. Do NOT throw any exceptions from this method.
     */
    public Type retrieveSymbol(String s) {
        if (hashTable.containsKey(s)) {
            return hashTable.get(s).Type;
        }
        return null;
    }

    /**
     * tests whether name is present in the symbol table’s
     * current (innermost) scope. If it is, true is returned. If name is in an outer
     * scope, or is not in the symbol table at all, false is returned.
     */
    public boolean declaredLocally(String id) {
        if (hashTable.containsKey(id)) {
            return hashTable.get(id).scopeLevel == getCurrentScopeLevel();
        }
        return false;
    }
}
