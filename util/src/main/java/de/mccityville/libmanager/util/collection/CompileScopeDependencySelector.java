package de.mccityville.libmanager.util.collection;

import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.util.artifact.JavaScopes;

public class CompileScopeDependencySelector implements DependencySelector {

    public static CompileScopeDependencySelector INSTANCE = new CompileScopeDependencySelector();

    @Override
    public boolean selectDependency(Dependency dependency) {
        return isCompileScope(dependency.getScope());
    }

    @Override
    public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
        return isCompileScope(context.getDependency().getScope()) ? this : DependencySelectors.OMIT_ALL_SELECTOR;
    }

    private boolean isCompileScope(String scope) {
        return scope == null || scope.isEmpty() || JavaScopes.COMPILE.equals(scope);
    }
}
