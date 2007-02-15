package edu.indiana.cs.webmining.util;

public interface Disposable {
    public <E extends Exception> void close() throws E; 
}
