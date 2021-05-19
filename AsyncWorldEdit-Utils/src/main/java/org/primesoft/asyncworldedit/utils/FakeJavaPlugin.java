package org.primesoft.asyncworldedit.utils;

import java.io.File;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public class FakeJavaPlugin extends JavaPlugin {
    public FakeJavaPlugin(
            final JavaPluginLoader loader) {

        super(loader,
                new PluginDescriptionFile("stub", "0.0.0", "stub"),
                new File(""), new File(""));
    }
}
