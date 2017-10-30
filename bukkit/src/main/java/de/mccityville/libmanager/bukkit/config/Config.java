package de.mccityville.libmanager.bukkit.config;

import de.mccityville.libmanager.util.config.BaseConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Config extends BaseConfig {

    public void load(File dataFolder, ConfigurationSection config) {
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
        if (authConfig != null)
            builder = builder.setAuthentication(readAuthentication(authConfig));

        ConfigurationSection releasePolicyConfig = config.getConfigurationSection("release_policy");
        if (releasePolicyConfig != null)
            builder = builder.setReleasePolicy(readRepositoryPolicy(releasePolicyConfig));

        ConfigurationSection snapshotPolicyConfig = config.getConfigurationSection("snapshot_policy");
        if (snapshotPolicyConfig != null)
            builder = builder.setSnapshotPolicy(readRepositoryPolicy(snapshotPolicyConfig));

        return builder.build();
    }

    private static Authentication readAuthentication(ConfigurationSection config) {
        String username = config.getString("username");
        String password = config.getString("password");
        return new AuthenticationBuilder()
                .addUsername(username)
                .addPassword(password)
                .build();
    }

    private static RepositoryPolicy readRepositoryPolicy(ConfigurationSection config) {
        boolean enabled = config.getBoolean("enabled", true);
        String updatePolicy = config.getString("update", DEFAULT_POLICY.getUpdatePolicy());
        String checksumPolicy = config.getString("checksum", DEFAULT_POLICY.getChecksumPolicy());
        return new RepositoryPolicy(enabled, updatePolicy, checksumPolicy);
    }
}
