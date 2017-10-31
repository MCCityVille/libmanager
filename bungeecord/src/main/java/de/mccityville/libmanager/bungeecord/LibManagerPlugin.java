package de.mccityville.libmanager.bungeecord;

import de.mccityville.libmanager.api.LibraryManager;
import de.mccityville.libmanager.api.LibraryResolver;
import de.mccityville.libmanager.bungeecord.config.Config;
import de.mccityville.libmanager.bungeecord.impl.BungeeCordLibraryManager;
import de.mccityville.libmanager.util.LoggerServiceLocatorErrorHandler;
import de.mccityville.libmanager.util.ServiceLocatorFactory;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

public class LibManagerPlugin extends Plugin implements LibraryManager {

    private BungeeCordLibraryManager libraryManager;

    public BungeeCordLibraryManager getLibraryManager() {
        if (libraryManager == null)
            throw new IllegalStateException("Not initialized");
        return libraryManager;
    }

    @Override
    public LibraryResolver getLibraryResolver(Object realm) {
        return getLibraryManager().getLibraryResolver(realm);
    }

    @Override
    public void onLoad() {
        Config config = loadConfig();
        if (config == null) {
            getLogger().severe("Configuration was not loaded successfully");
            return;
        }
        List<RemoteRepository> remoteRepositories = config.getRepositories();
        LocalRepository localRepository = createLocalRepository(config);
        RepositorySystem repositorySystem = createRepositorySystem();
        libraryManager = new BungeeCordLibraryManager(repositorySystem, localRepository, () -> remoteRepositories, getLogger());
    }

    private Config loadConfig() {
        File dataFolder = getDataFolder();
        if (dataFolder.mkdirs())
            getLogger().info("Data directory created");
        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            try {
                Files.copy(getResourceAsStream("config.yml"), configFile.toPath());
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Cannot copy config.yml from plugin resources to data directory", e);
                return null;
            }
        }
        Configuration configuration;
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot load config.yml from data directory", e);
            return null;
        }
        Config config = new Config();
        config.load(dataFolder, configuration);
        return config;
    }

    private LocalRepository createLocalRepository(Config config) {
        File localRepositoryFolder = config.getLocalRepositoryDirectory();
        if (localRepositoryFolder.mkdirs())
            getLogger().info("Created " + localRepositoryFolder.getPath() + " as local repository folder");
        else
            getLogger().info("Using " + localRepositoryFolder.getPath() + " as local repository folder");
        return new LocalRepository(localRepositoryFolder);
    }

    private RepositorySystem createRepositorySystem() {
        getLogger().info("Creating repository system...");
        DefaultServiceLocator locator = ServiceLocatorFactory.createDefault();
        locator.setErrorHandler(new LoggerServiceLocatorErrorHandler(getLogger()));
        RepositorySystem repositorySystem = locator.getService(RepositorySystem.class);
        getLogger().info("Repository system created");
        return repositorySystem;
    }
}
