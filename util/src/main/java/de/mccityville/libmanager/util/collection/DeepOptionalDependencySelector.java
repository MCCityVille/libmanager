package de.mccityville.libmanager.util.collection;

import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;

/**
 * A dependency selector that excludes optional dependencies as well as all of their dependencies from the dependency
 * graph.
 *
 * @see Dependency#isOptional()
 */
public class DeepOptionalDependencySelector implements DependencySelector {

    public static DeepOptionalDependencySelector INSTANCE = new DeepOptionalDependencySelector();

    @Override
    public boolean selectDependency(Dependency dependency) {
        return !dependency.isOptional();
    }

    @Override
    public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
        return context.getDependency().isOptional() ? DependencySelectors.OMIT_ALL_SELECTOR : this;
    }
}
