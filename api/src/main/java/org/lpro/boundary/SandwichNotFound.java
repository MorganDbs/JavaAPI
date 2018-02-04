package org.lpro.boundary;

public class SandwichNotFound extends RuntimeException {
    public SandwichNotFound(String s) {
        super(s);
    }
}