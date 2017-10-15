package de.mccityville.libmanager.bukkit;

import de.mccityville.libmanager.api.LibraryManager;
import de.mccityville.libmanager.bukkit.config.Config;
import de.mccityville.libmanager.bukkit.impl.BukkitLibraryManager;
import de.mccityville.libmanager.util.RepositoryUtils;
import de.mccityville.libmanager.util.ServiceLocatorFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class LibManagerPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        Config config = loadConfig();
        List<RemoteRepository> remoteRepositories = Collections.singletonList(RepositoryUtils.createCentral());
        LocalRepository localRepository = createLocalRepository(config);
        RepositorySystem repositorySystem = createRepositorySystem();
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(repositorySystem, localRepository, () -> remoteRepositories, getLogger());
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
        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception )
            {
                getLogger().log(Level.SEVERE, "Failed to create service fo type " + type.getName() + " with implementation ");
            }
        });
        RepositorySystem repositorySystem = locator.getService(RepositorySystem.class);
        getLogger().info("Repository system created");
        return repositorySystem;
    }
}
