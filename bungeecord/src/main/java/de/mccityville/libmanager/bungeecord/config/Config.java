package de.mccityville.libmanager.bungeecord.config;

import de.mccityville.libmanager.util.config.BaseConfig;
import net.md_5.bungee.config.Configuration;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Config extends BaseConfig {

    public void load(File dataFolder, Configuration config) {
        Path localRepositoryDirectory = Paths.get(config.getString("local_repository_directory"));
        if (!localRepositoryDirectory.isAbsolute())
            localRepositoryDirectory = dataFolder.toPath().resolve(localRepositoryDirectory);
        setLocalRepositoryDirectory(localRepositoryDirectory.toFile());

        Configuration repositories = (Configuration) config.get("repositories");
        if (repositories != null) {
            Collection<String> keys = repositories.getKeys();
            List<RemoteRepository> resultRepositories = new ArrayList<>(keys.size());
            for (String key : keys) {
                Configuration value = (Configuration) repositories.get(key);
                resultRepositories.add(readRemoteRepository(key, value));
            }
            setRepositories(resultRepositories);
        } else {
            setRepositories(Collections.emptyList());
        }
    }

    private static RemoteRepository readRemoteRepository(String id, Configuration config) {
        String url = config.getString("url");
        RemoteRepository.Builder builder = new RemoteRepository.Builder(id, "default", url);

        Configuration authConfig = (Configuration) config.get("authentication");
        if (authConfig != null)
            builder = builder.setAuthentication(readAuthentication(authConfig));

        Configuration releasePolicyConfig = (Configuration) config.get("release_policy");
        if (releasePolicyConfig != null)
            builder = builder.setReleasePolicy(readRepositoryPolicy(releasePolicyConfig));

        Configuration snapshotPolicyConfig = (Configuration) config.get("snapshot_policy");
        if (snapshotPolicyConfig != null)
            builder = builder.setSnapshotPolicy(readRepositoryPolicy(snapshotPolicyConfig));

        return builder.build();
    }

    private static Authentication readAuthentication(Configuration config) {
        String username = config.getString("username");
        String password = config.getString("password");
        return new AuthenticationBuilder()
                .addUsername(username)
                .addPassword(password)
                .build();
    }

    private static RepositoryPolicy readRepositoryPolicy(Configuration config) {
        boolean enabled = config.getBoolean("enabled", true);
        String updatePolicy = config.getString("update", DEFAULT_POLICY.getUpdatePolicy());
        String checksumPolicy = config.getString("checksum", DEFAULT_POLICY.getChecksumPolicy());
        return new RepositoryPolicy(enabled, updatePolicy, checksumPolicy);
    }
}
