package de.mccityville.libmanager.bukkit.config;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

    private File localRepositoryDirectory;

    public File getLocalRepositoryDirectory() {
        return localRepositoryDirectory;
    }

    public void setLocalRepositoryDirectory(File localRepositoryDirectory) {
        this.localRepositoryDirectory = localRepositoryDirectory;
    }

    public void load(File dataFolder, ConfigurationSection config) {
        Path localRepositoryDirectory = Paths.get(config.getString("local_repository_directory"));
        if (!localRepositoryDirectory.isAbsolute())
            localRepositoryDirectory = dataFolder.toPath().resolve(localRepositoryDirectory);
        setLocalRepositoryDirectory(localRepositoryDirectory.toFile());
    }
}
