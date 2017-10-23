package de.mccityville.libmanager.bukkit.config;

import org.bukkit.configuration.ConfigurationSection;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Config {

    private boolean debug;
    private File localRepositoryDirectory;
    private List<RemoteRepository> repositories;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

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

    public void load(File dataFolder, ConfigurationSection config) {
        setDebug(config.getBoolean("debug"));

        Path localRepositoryDirectory = Paths.get(config.getString("local_repository_directory"));
        if (!localRepositoryDirectory.isAbsolute())
            localRepositoryDirectory = dataFolder.toPath().resolve(localRepositoryDirectory);
        setLocalRepositoryDirectory(localRepositoryDirectory.toFile());

        ConfigurationSection repositories = config.getConfigurationSection("repositories");
        if (repositories != null) {
            Map<String, Object> entries = repositories.getValues(false);
            List<RemoteRepository> resultRepositories = new ArrayList<>(entries.size());
            for (Map.Entry<String, Object> entry : entries.entrySet())
                resultRepositories.add(readRemoteRepository(entry.getKey(), (ConfigurationSection) entry.getValue()));
            setRepositories(resultRepositories);
        } else {
            setRepositories(Collections.emptyList());
        }
    }

    private static RemoteRepository readRemoteRepository(String id, ConfigurationSection config) {
        String url = config.getString("url");
        RemoteRepository.Builder builder = new RemoteRepository.Builder(id, "default", url);

        ConfigurationSection authConfig = config.getConfigurationSection("authentication");
        if (authConfig != null) {
            String username = authConfig.getString("username");
            String password = authConfig.getString("password");
            Authentication authentication = new AuthenticationBuilder()
                    .addUsername(username)
                    .addPassword(password)
                    .build();
            builder = builder.setAuthentication(authentication);
        }

        return builder.build();
    }
}
