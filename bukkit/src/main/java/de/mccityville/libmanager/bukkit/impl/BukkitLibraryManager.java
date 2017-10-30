package de.mccityville.libmanager.bukkit.impl;

import de.mccityville.libmanager.api.LibraryManager;
import de.mccityville.libmanager.api.LibraryResolver;
import de.mccityville.libmanager.util.ClassLoaderLibraryResolver;
import de.mccityville.libmanager.util.LoggerTransferListener;
import de.mccityville.libmanager.util.SessionUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;

import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
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
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader))
            throw new UnsupportedOperationException("Plugin " + plugin.getName() + " was not loaded by a URLClassLoader");
        return libraryResolvers.computeIfAbsent(plugin.getName(), name -> new ClassLoaderLibraryResolver(
                repositorySystem,
                createSession(),
                remoteRepositorySupplier,
                (URLClassLoader) classLoader,
                plugin.getLogger()
        ));
    }

    private RepositorySystemSession createSession() {
        DefaultRepositorySystemSession session = SessionUtils.createDefaultSession();
        session.setLocalRepositoryManager(createLocalRepositoryManager(session));
        session.setTransferListener(new LoggerTransferListener(logger));
        return session;
    }

    private LocalRepositoryManager createLocalRepositoryManager(RepositorySystemSession session) {
        return repositorySystem.newLocalRepositoryManager(session, localRepository);
    }
}
