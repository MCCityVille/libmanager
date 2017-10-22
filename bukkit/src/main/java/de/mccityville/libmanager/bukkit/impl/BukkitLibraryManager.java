package de.mccityville.libmanager.bukkit.impl;

import de.mccityville.libmanager.api.LibraryManager;
import de.mccityville.libmanager.api.LibraryResolver;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitLibraryManager implements LibraryManager {

    private final Map<String, LibraryResolver> libraryResolvers = new HashMap<>();
    private final RepositorySystem repositorySystem;
    private final LocalRepository localRepository;
    private final Supplier<List<RemoteRepository>> remoteRepositorySupplier;
    private final Logger logger;

    public BukkitLibraryManager(RepositorySystem repositorySystem,
                                LocalRepository localRepository,
                                Supplier<List<RemoteRepository>> remoteRepositorySupplier,
                                Logger logger) {
        this.repositorySystem = Objects.requireNonNull(repositorySystem, "repositorySystem must not be null");
        this.localRepository = Objects.requireNonNull(localRepository, "localRepository must not be null");
        this.remoteRepositorySupplier = Objects.requireNonNull(remoteRepositorySupplier, "remoteRepositorySupplier must not be null");
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    @Override
    public LibraryResolver getLibraryResolver(Object realm) {
        if (!(realm instanceof JavaPlugin))
            throw new IllegalArgumentException("Only realms of type " + JavaPlugin.class.getName() + " are supported");
        JavaPlugin plugin = (JavaPlugin) realm;
        return libraryResolvers.computeIfAbsent(plugin.getName(), name ->
                new BukkitLibraryResolver(repositorySystem, createSession(plugin), remoteRepositorySupplier));
    }

    private RepositorySystemSession createSession(JavaPlugin plugin) {
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader))
            throw new UnsupportedOperationException("Plugin " + plugin.getName() + " was not loaded by an " + URLClassLoader.class.getName());
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
        session.setLocalRepositoryManager(createLocalRepositoryManager(session));
        session.setRepositoryListener(new RepositoryInjectionListener((URLClassLoader) classLoader, plugin.getLogger()));
        session.setTransferListener(new LoggerTransferListener(logger));
        return session;
    }

    private LocalRepositoryManager createLocalRepositoryManager(RepositorySystemSession session) {
        return repositorySystem.newLocalRepositoryManager(session, localRepository);
    }

    private static class RepositoryInjectionListener extends AbstractRepositoryListener {

        private static final Method ADD_URL_METHOD;

        static {
            try {
                ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                ADD_URL_METHOD.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        private final Set<String> injected = new HashSet<>();
        private final URLClassLoader classLoader;
        private final Logger logger;

        private RepositoryInjectionListener(URLClassLoader classLoader, Logger logger) {
            this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
            this.logger = Objects.requireNonNull(logger, "logger must not be null");
        }

        @Override
        public void artifactResolved(RepositoryEvent event) {
            Exception exception = event.getException();
            if (exception != null) {
                logger.log(Level.SEVERE, "Exception occurred while resolving dependency", exception);
                return;
            }

            if ("pom".equalsIgnoreCase(event.getArtifact().getExtension()))
                return;

            if (!injected.add(event.getArtifact().toString()))
                return;

            File file = event.getFile();
            URL url;
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, "Exception occurred while converting " + File.class.getName() + " to " + URL.class.getName());
                return;
            }
            logger.info("Injecting " + file + " into class loader...");
            try {
                ADD_URL_METHOD.invoke(classLoader, url);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.log(Level.SEVERE, "An exception occurred while trying to add file into class loader");
            }
        }
    }

    private static class LoggerTransferListener extends AbstractTransferListener {

        private final Logger logger;

        private LoggerTransferListener(Logger logger) {
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

        @Override
        public void transferFailed(TransferEvent event) {
            Exception exception = event.getException();
            if (exception == null)
                logger.severe(event.getResource() + " was not transferred normally");
            else
                logger.log(Level.SEVERE, event.getResource() + " was not transferred normally", exception);
        }
    }
}
