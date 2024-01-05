package io.github.qudtlib.tools.contribute.support;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class IndentedOutputStream extends OutputStream {
    private OutputStream delegate;
    private String indentString;

    private boolean isNewLine = true;

    public IndentedOutputStream(PrintStream delegate, String indentString) {
        this.delegate = delegate;
        this.indentString = indentString;
    }

    public PrintStream printStream() {
        return new PrintStream(this);
    }

    public static OutputStream nullOutputStream() {
        return OutputStream.nullOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        if (isNewLine) {
            for (int i = 0; i < indentString.getBytes().length; i++) {
                delegate.write(indentString.getBytes()[i]);
            }
            isNewLine = false;
        }
        if (b == '\n') {
            isNewLine = true;
        }
        delegate.write(b);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
