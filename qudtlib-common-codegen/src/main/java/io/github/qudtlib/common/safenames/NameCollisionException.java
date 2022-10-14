package io.github.qudtlib.common.safenames;

public class NameCollisionException extends RuntimeException {
    public NameCollisionException(String collidingInput, String input, String mappedOutput) {
        super(
                String.format(
                        "IdentifierCollision detected! Input String '%s' clashes with previously established Mapping '%s' => '%s'",
                        collidingInput, input, mappedOutput));
    }
}
