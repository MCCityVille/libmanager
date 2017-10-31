package de.mccityville.libmanager.bukkit;

import de.mccityville.libmanager.api.LibraryManager;
import de.mccityville.libmanager.api.LibraryResolver;
import de.mccityville.libmanager.bukkit.config.Config;
import de.mccityville.libmanager.bukkit.impl.BukkitLibraryManager;
import de.mccityville.libmanager.util.LoggerServiceLocatorErrorHandler;
import de.mccityville.libmanager.util.ServiceLocatorFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.util.List;

public class LibManagerPlugin extends JavaPlugin implements LibraryManager {

    private BukkitLibraryManager libraryManager;

    public BukkitLibraryManager getLibraryManager() {
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
        List<RemoteRepository> remoteRepositories = config.getRepositories();
        LocalRepository localRepository = createLocalRepository(config);
        RepositorySystem repositorySystem = createRepositorySystem();
        libraryManager = new BukkitLibraryManager(repositorySystem, localRepository, () -> remoteRepositories, getLogger());
        Bukkit.getServicesManager().register(LibraryManager.class, libraryManager, this, ServicePriority.Normal);
    }

    private Config loadConfig() {
        saveDefaultConfig();
        Config config = new Config();
        config.load(getDataFolder(), getConfig());
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
