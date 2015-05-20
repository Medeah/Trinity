package trinity.utils;

import java.util.Stack;

/**
 * Class for emitting code into StringBuilders.
 */
public class Emitter {

    private final Stack<StringBuilder> emitStack = new Stack<>();
    private StringBuilder currentWriter;

    public Emitter(StringBuilder context) {
        setContext(context);
    }

    public void setContext(StringBuilder writer) {
        if (currentWriter != null) {
            emitStack.push(currentWriter);
        }
        currentWriter = writer;
    }

    public void restoreContext() {
        currentWriter = emitStack.pop();
    }

    public void emit(String string) {
        currentWriter.append(string);
    }

}
