package org.lpro.boundary;

public class CommandeNotFound extends RuntimeException {
    public CommandeNotFound(String s) {
        super(s);
    }
}