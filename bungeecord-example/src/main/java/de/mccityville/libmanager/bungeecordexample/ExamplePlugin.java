package de.mccityville.libmanager.bungeecordexample;

import de.mccityville.libmanager.api.DependencyProvisionException;
import de.mccityville.libmanager.api.LibraryManager;
import de.mccityville.libmanager.api.LibraryResolver;
import net.md_5.bungee.api.plugin.Plugin;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.util.logging.Level;

public class ExamplePlugin extends Plugin {

    @Override
    public void onEnable() {
        LibraryResolver libraryResolver = ((LibraryManager) getProxy().getPluginManager().getPlugin("MCCV-LibManager")).getLibraryResolver(this);
        try {
            libraryResolver.load("org.eclipse.collections:eclipse-collections:9.0.0");
        } catch (DependencyResolutionException | DependencyProvisionException e) {
            getLogger().log(Level.SEVERE, "Exception occurred while resolving dependencies", e);
            return;
        }

        ExampleClass.execute(getLogger());
    }
}
