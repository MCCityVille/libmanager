package de.mccityville.libmanager.util.collection;

import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.util.artifact.JavaScopes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ScopeExclusiveDependencySelector implements DependencySelector {

    public static ScopeExclusiveDependencySelector COMPILE_SCOPE_EXCLUSIVE_INSTANCE = new ScopeExclusiveDependencySelector(Collections.singleton(JavaScopes.COMPILE));

    private final Set<String> allowedScopes;

    public ScopeExclusiveDependencySelector(Set<String> allowedScopes) {
        this.allowedScopes = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(allowedScopes, "allowedScopes must not be null")));
    }

    @Override
    public boolean selectDependency(Dependency dependency) {
        return allowedScopes.contains(dependency.getScope());
    }

    @Override
    public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
        return allowedScopes.contains(context.getDependency().getScope()) ? this : DependencySelectors.OMIT_ALL_SELECTOR;
    }
}
