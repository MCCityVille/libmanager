package de.mccityville.libmanager.bukkit.impl;

import de.mccityville.libmanager.api.LibraryManager;
import de.mccityville.libmanager.api.LibraryResolver;
import de.mccityville.libmanager.util.ClassLoaderLibraryResolver;
import de.mccityville.libmanager.util.collection.DebuggingDependencySelector;
import de.mccityville.libmanager.util.collection.DeepOptionalDependencySelector;
import de.mccityville.libmanager.util.collection.ScopeExclusiveDependencySelector;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;

import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitLibraryManager implements LibraryManager {

    private final Map<String, LibraryResolver> libraryResolvers = new HashMap<>();
    private final RepositorySystem repositorySystem;
    private final LocalRepository localRepository;
    private final Supplier<List<RemoteRepository>> remoteRepositorySupplier;
    private final Logger logger;
    private final boolean debug;

    public BukkitLibraryManager(RepositorySystem repositorySystem,
                                LocalRepository localRepository,
                                Supplier<List<RemoteRepository>> remoteRepositorySupplier,
                                Logger logger,
                                boolean debug) {
        this.repositorySystem = Objects.requireNonNull(repositorySystem, "repositorySystem must not be null");
        this.localRepository = Objects.requireNonNull(localRepository, "localRepository must not be null");
        this.remoteRepositorySupplier = Objects.requireNonNull(remoteRepositorySupplier, "remoteRepositorySupplier must not be null");
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
        this.debug = debug;
    }

    @Override
    public LibraryResolver getLibraryResolver(Object realm) {
        if (!(realm instanceof JavaPlugin))
            throw new IllegalArgumentException("Only realms of type " + JavaPlugin.class.getName() + " are supported");
        JavaPlugin plugin = (JavaPlugin) realm;
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader))
            throw new UnsupportedOperationException("Plugin " + plugin.getName() + " was not loaded by a URLClassLoader");
        return libraryResolvers.computeIfAbsent(plugin.getName(), name -> new ClassLoaderLibraryResolver(
                repositorySystem,
                createSession(plugin),
                remoteRepositorySupplier,
                (URLClassLoader) classLoader
        ));
    }

    private RepositorySystemSession createSession(JavaPlugin plugin) {
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader))
            throw new UnsupportedOperationException("Plugin " + plugin.getName() + " was not loaded by an " + URLClassLoader.class.getName());
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
        session.setLocalRepositoryManager(createLocalRepositoryManager(session));
        session.setTransferListener(new LoggerTransferListener(logger));
        DependencySelector dependencySelector = new AndDependencySelector(
                DeepOptionalDependencySelector.INSTANCE,
                ScopeExclusiveDependencySelector.COMPILE_SCOPE_EXCLUSIVE_INSTANCE
        );
        session.setDependencySelector(debug ? new DebuggingDependencySelector(dependencySelector, plugin.getLogger()) : dependencySelector);
        return session;
    }

    private LocalRepositoryManager createLocalRepositoryManager(RepositorySystemSession session) {
        return repositorySystem.newLocalRepositoryManager(session, localRepository);
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
