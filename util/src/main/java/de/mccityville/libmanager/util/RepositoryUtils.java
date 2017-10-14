package de.mccityville.libmanager.util;

import org.eclipse.aether.repository.RemoteRepository;

public class RepositoryUtils {

    private RepositoryUtils() {
    }

    public static RemoteRepository createCentral() {
        return new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/")
                .build();
    }
}
