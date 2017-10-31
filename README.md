# LibManager

LibManager is a helper plugin (currently only for Bukkit) for downloading and injecting dependencies from an Apache
Maven repository. This allows to use dependent libraries without the need of shading them into your plugins jar file.

## Example (Bukkit)

You can find a simple example plugin in the `bukkit-example` folder. This plugin will require the
`org.eclipse.collections:eclipse-collections:9.0.0` dependency which will gets downloaded by LibManager. Additionally
`org.eclipse.collections:eclipse-collections:9.0.0` references `org.eclipse.collections:eclipse-collections-api:9.0.0`
as a dependency of itself so this will gets downloaded too.

The relevant code is:

```
// The LibraryManager is provided by the bukkit plugin of LibManager
LibraryManager librarayManager = Bukkit.getServicesManager().load(LibraryManager.class);
// Each plugin has its own LibraryResolver instance which also knowns how to inject loaded dependencies
LibraryResolver libraryResolver = libraryManager.getLibraryResolver(this);
// This will trigger the download of eclipse-collections and eclipse-collections-api
libraryResolver.load("org.eclipse.collections:eclipse-collections:9.0.0");
```

## Known limitations

 * On a PaperSpigot test server the `PluginClassLoader` tries to load all referenced classes of the plugins main class
   when the plugin is loaded (normally classes are loaded when they are required). This means you *cannot* use
   dependencies in the plugins main class without getting an `ClassNotFoundException`.
