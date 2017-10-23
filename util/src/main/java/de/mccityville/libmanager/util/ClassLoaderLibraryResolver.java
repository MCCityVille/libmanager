package de.mccityville.libmanager.util;

import de.mccityville.libmanager.api.DependencyProvisionException;
import de.mccityville.libmanager.api.LibraryResolver;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ClassLoaderLibraryResolver implements LibraryResolver {

    private static final Method ADD_URL_METHOD;

    static {
        try {
            ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final RepositorySystem repositorySystem;
    private final RepositorySystemSession repositorySystemSession;
    private final Supplier<List<RemoteRepository>> remoteRepositoriesSupplier;
    private final URLClassLoader targetClassLoader;
    private final Logger logger;

    public ClassLoaderLibraryResolver(RepositorySystem repositorySystem,
                                      RepositorySystemSession repositorySystemSession,
                                      Supplier<List<RemoteRepository>> remoteRepositoriesSupplier,
                                      URLClassLoader targetClassLoader,
                                      Logger logger) {
        this.repositorySystem = Objects.requireNonNull(repositorySystem, "repositorySystem must not be null");
        this.repositorySystemSession = Objects.requireNonNull(repositorySystemSession, "repositorySystemSession must not be null");
        this.remoteRepositoriesSupplier = Objects.requireNonNull(remoteRepositoriesSupplier, "remoteRepositoriesSupplier must not be null");
        this.targetClassLoader = Objects.requireNonNull(targetClassLoader, "targetClassLoader must not be null");
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    @Override
    public void load(Dependency dependency) throws DependencyResolutionException, DependencyProvisionException {
        List<RemoteRepository> remoteRepositories = remoteRepositoriesSupplier.get();
        CollectRequest collectRequest = new CollectRequest(dependency, remoteRepositories);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE));

        DependencyResult dependencyResult = repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);
        for (ArtifactResult artifactResult : dependencyResult.getArtifactResults())
            inject(artifactResult);
    }

    private void inject(ArtifactResult artifactResult) throws DependencyProvisionException {
        File file = artifactResult.getArtifact().getFile();
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new DependencyProvisionException("Cannot obtain url from artifact file", e);
        }
        logger.info("Injecting " + url + " into class loader...");
        try {
            ADD_URL_METHOD.invoke(targetClassLoader, url);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new DependencyProvisionException("Cannot inject url into URLClassLoader", e);
        }
    }
}
