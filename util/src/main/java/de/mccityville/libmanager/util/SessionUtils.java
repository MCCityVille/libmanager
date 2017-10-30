package de.mccityville.libmanager.util;

import de.mccityville.libmanager.util.collection.CompileScopeDependencySelector;
import de.mccityville.libmanager.util.collection.DeepOptionalDependencySelector;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;

public class SessionUtils {

    private SessionUtils() {
    }

    public static DefaultRepositorySystemSession createDefaultSession() {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
        session.setSystemProperties(System.getProperties());
        session.setDependencySelector(new AndDependencySelector(
                DeepOptionalDependencySelector.INSTANCE,
                CompileScopeDependencySelector.INSTANCE
        ));
        return session;
    }
}
