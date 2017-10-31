package de.mccityville.libmanager.bungeecord.impl;

import de.mccityville.libmanager.api.LibraryManager;
import de.mccityville.libmanager.api.LibraryResolver;
import de.mccityville.libmanager.util.ClassLoaderLibraryResolver;
import de.mccityville.libmanager.util.LoggerTransferListener;
import de.mccityville.libmanager.util.SessionUtils;
import net.md_5.bungee.api.plugin.Plugin;
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

public class BungeeCordLibraryManager implements LibraryManager {

    private final Map<String, LibraryResolver> libraryResolvers = new HashMap<>();
    private final RepositorySystem repositorySystem;
    private final LocalRepository localRepository;
    private final Supplier<List<RemoteRepository>> remoteRepositorySupplier;
    private final Logger logger;

    public BungeeCordLibraryManager(RepositorySystem repositorySystem,
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
        if (!(realm instanceof Plugin))
            throw new IllegalArgumentException("Only realms of type " + Plugin.class.getName() + " are supported");
        Plugin plugin = (Plugin) realm;
        String name = plugin.getDescription().getName();
        ClassLoader classLoader = realm.getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader))
            throw new UnsupportedOperationException("Plugin " + name + " was not loaded by a URLClassLoader");
        return libraryResolvers.computeIfAbsent(name, n -> new ClassLoaderLibraryResolver(
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
