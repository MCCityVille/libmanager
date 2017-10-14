package de.mccityville.libmanager.bukkitexample;

import de.mccityville.libmanager.api.LibraryManager;
import de.mccityville.libmanager.api.LibraryResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.util.logging.Level;

public class ExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        LibraryResolver libraryResolver = Bukkit.getServicesManager().load(LibraryManager.class).getLibraryResolver(this);
        try {
            libraryResolver.load("org.eclipse.collections:eclipse-collections:9.0.0");
        } catch (DependencyResolutionException e) {
            getLogger().log(Level.SEVERE, "Exception occurred while resolving dependencies", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        ExampleClass.execute(getLogger());
    }
}
