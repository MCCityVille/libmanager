package de.mccityville.libmanager.util.config;

import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;

import java.io.File;
import java.util.List;

public class BaseConfig {

    protected static final RepositoryPolicy DEFAULT_POLICY = new RepositoryPolicy();

    private File localRepositoryDirectory;
    private List<RemoteRepository> repositories;

    public File getLocalRepositoryDirectory() {
        return localRepositoryDirectory;
    }

    public void setLocalRepositoryDirectory(File localRepositoryDirectory) {
        this.localRepositoryDirectory = localRepositoryDirectory;
    }

    public List<RemoteRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RemoteRepository> repositories) {
        this.repositories = repositories;
    }
}
