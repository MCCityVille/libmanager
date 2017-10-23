package de.mccityville.libmanager.util.collection;

import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.util.graph.selector.StaticDependencySelector;

class DependencySelectors {

    static DependencySelector OMIT_ALL_SELECTOR = new StaticDependencySelector(false);

    private DependencySelectors() {
    }
}
