package de.mccityville.libmanager.api;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.DependencyResolutionException;

public interface LibraryResolver {

    default void load(String coordinates) throws DependencyResolutionException {
        load(new DefaultArtifact(coordinates));
    }

    default void load(Artifact artifact) throws DependencyResolutionException {
        load(new Dependency(artifact, null));
    }

    void load(Dependency dependency) throws DependencyResolutionException;
}
