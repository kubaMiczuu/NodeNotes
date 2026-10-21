package org.jakubmiczek.nodenotes.exception;

public class SubItemDoesNotExistException extends SubItemException {
    public SubItemDoesNotExistException(Long id) {
        super("Sub Item with ID: " + id + " does not exist");
    }
}
