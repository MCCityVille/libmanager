package de.mccityville.libmanager.util.collection;

import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;

import java.util.Objects;
import java.util.logging.Logger;

public class DebuggingDependencySelector implements DependencySelector {

    private final DependencySelector delegate;
    private final Logger logger;

    public DebuggingDependencySelector(DependencySelector delegate, Logger logger) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    @Override
    public boolean selectDependency(Dependency dependency) {
        boolean result = delegate.selectDependency(dependency);
        logger.info("Result for " + debug(dependency) + ": " + result);
        return false;
    }

    @Override
    public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
        DependencySelector dependencySelector = delegate.deriveChildSelector(context);
        if (dependencySelector != null)
            logger.info("Derived child selector for artifact " + debug(context.getDependency()) + " of type " + dependencySelector.getClass().getName());
        else
            logger.info("Derived no child select for artifact " + debug(context.getDependency()));
        return null;
    }

    private static String debug(Dependency dependency) {
        return dependency.getArtifact().toString() + " (optional: " + dependency.isOptional() + ", scope: " + dependency.getScope() + ')';
    }
}
