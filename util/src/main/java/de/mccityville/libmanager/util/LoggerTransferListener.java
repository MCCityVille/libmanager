package de.mccityville.libmanager.util;

import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;

import java.util.Objects;
import java.util.logging.Logger;

public class LoggerTransferListener extends AbstractTransferListener {

    private final Logger logger;

    public LoggerTransferListener(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
        logger.info("Transferring " + event.getResource() + "...");
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        logger.info(event.getResource() + " was transferred successfully");
    }
}
