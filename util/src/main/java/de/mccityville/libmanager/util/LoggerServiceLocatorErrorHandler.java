package de.mccityville.libmanager.util;

import org.eclipse.aether.impl.DefaultServiceLocator;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerServiceLocatorErrorHandler extends DefaultServiceLocator.ErrorHandler {

    private final Logger logger;

    public LoggerServiceLocatorErrorHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception )
    {
        logger.log(Level.SEVERE, "Failed to create service fo type " + type.getName() + " with implementation ");
    }
}
