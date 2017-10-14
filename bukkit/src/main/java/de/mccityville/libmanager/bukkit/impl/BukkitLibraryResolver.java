package de.mccityville.libmanager.bukkit.impl;

import de.mccityville.libmanager.api.LibraryResolver;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class BukkitLibraryResolver implements LibraryResolver {

    private final RepositorySystem repositorySystem;
    private final RepositorySystemSession repositorySystemSession;
    private final Supplier<List<RemoteRepository>> remoteRepositoriesSupplier;

    public BukkitLibraryResolver(RepositorySystem repositorySystem,
                                 RepositorySystemSession repositorySystemSession,
                                 Supplier<List<RemoteRepository>> remoteRepositoriesSupplier) {
        this.repositorySystem = Objects.requireNonNull(repositorySystem, "repositorySystem must not be null");
        this.repositorySystemSession = Objects.requireNonNull(repositorySystemSession, "repositorySystemSession must not be null");
        this.remoteRepositoriesSupplier = Objects.requireNonNull(remoteRepositoriesSupplier, "remoteRepositoriesSupplier must not be null");
    }

    @Override
    public void load(Dependency dependency) throws DependencyResolutionException {
        List<RemoteRepository> remoteRepositories = remoteRepositoriesSupplier.get();
        CollectRequest collectRequest = new CollectRequest(dependency, remoteRepositories);
        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setCollectRequest(collectRequest);
        repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);
    }
}
