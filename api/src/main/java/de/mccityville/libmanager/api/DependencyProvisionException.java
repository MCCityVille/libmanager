package de.mccityville.libmanager.api;

public class DependencyProvisionException extends Exception {

    public DependencyProvisionException() {
    }

    public DependencyProvisionException(String message) {
        super(message);
    }

    public DependencyProvisionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DependencyProvisionException(Throwable cause) {
        super(cause);
    }
}
