/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit;

import java.io.File;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.inner.IAwePlugin;
import org.primesoft.asyncworldedit.injector.InjectorBukkit;
import org.primesoft.asyncworldedit.injector.core.InjectorCore;

/**
 *
 * @author SBPrime
 */
class LoaderBukkit extends Loader {

    private final static String PLUGIN_ACTIONBAR = "AsyncWorldEdit-ActionBarAPI.jar";
    private final static String PLUGIN_ACTIONMESSAGER = "AsyncWorldEdit-ActionMessager.jar";
    private final static String PLUGIN_BARAPI = "AsyncWorldEdit-BarAPI.jar";
    private final static String PLUGIN_BOUNTIFULAPI = "AsyncWorldEdit-BountifulAPI.jar";
    private final static String PLUGIN_PLOTME13 = "AsyncWorldEdit-PlotMe013g.jar";
    private final static String PLUGIN_PLOTMR15 = "AsyncWorldEdit-PlotMeCore015.jar";
    private final static String PLUGIN_PLOTME16 = "AsyncWorldEdit-PlotMeCore016.jar";
    private final static String PLUGIN_TITLEMANAGER = "AsyncWorldEdit-TitleManager.jar";
    private final static String PLUGIN_WELCOMETITLE = "AsyncWorldEdit-WelcomeTitle.jar";

    /**
     * The plugin main file
     */
    private final Plugin m_plugin;

    /**
     * The bukkit server
     */
    private final Server m_server;

    /**
     * The bukkit plugin manager
     */
    private final PluginManager m_pluginManager;

    LoaderBukkit(Plugin plugin) {
        super(plugin.getClass().getClassLoader());

        m_plugin = plugin;
        m_server = m_plugin.getServer();
        m_pluginManager = m_server.getPluginManager();
    }

    @Override
    protected double getInjectorVersion() {
        Plugin plugin = m_pluginManager.getPlugin(PLUGIN_INJECTOR);

        try {
            InjectorCore injector = null;

            if (plugin != null) {
                InjectorBukkit platform = plugin instanceof InjectorBukkit ? (InjectorBukkit) plugin : null;

                if (platform != null) {
                    injector = platform.getCore();
                }
            }

            if (injector == null) {
                injector = InjectorCore.getInstance();
            }

            if (injector == null) {
                return -1;
            }

            return injector.getVersion();
        } catch (Error ex) {
            return -1;
        }
    }

    @Override
    protected File getDataFolder() {
        return m_plugin.getDataFolder();
    }

    @Override
    protected File getPluginFolder() {
        return new File(getDataFolder(), "..");
    }

    @Override
    protected boolean installPlugins() {
        File awePluginFolder = new File(getDataFolder(), PLUGINS);

        if (!awePluginFolder.exists()) {
            if (!awePluginFolder.mkdirs()) {
                return false;
            }
        }

        extract(PLUGIN_ACTIONBAR, awePluginFolder, String.format("%1$s%2$s", PLUGIN_ACTIONBAR, DISABLED));
        extract(PLUGIN_ACTIONMESSAGER, awePluginFolder, String.format("%1$s%2$s", PLUGIN_ACTIONMESSAGER, DISABLED));
        extract(PLUGIN_BARAPI, awePluginFolder, String.format("%1$s%2$s", PLUGIN_BARAPI, DISABLED));
        extract(PLUGIN_BOUNTIFULAPI, awePluginFolder, String.format("%1$s%2$s", PLUGIN_BOUNTIFULAPI, DISABLED));
        extract(PLUGIN_PLOTME13, awePluginFolder, String.format("%1$s%2$s", PLUGIN_PLOTME13, DISABLED));
        extract(PLUGIN_PLOTMR15, awePluginFolder, String.format("%1$s%2$s", PLUGIN_PLOTMR15, DISABLED));
        extract(PLUGIN_PLOTME16, awePluginFolder, String.format("%1$s%2$s", PLUGIN_PLOTME16, DISABLED));
        extract(PLUGIN_TITLEMANAGER, awePluginFolder, String.format("%1$s%2$s", PLUGIN_TITLEMANAGER, DISABLED));
        extract(PLUGIN_WELCOMETITLE, awePluginFolder, String.format("%1$s%2$s", PLUGIN_WELCOMETITLE, DISABLED));

        return true;
    }

    @Override
    boolean checkDependencies() {
        if (m_pluginManager.getPlugin("WorldEdit") == null) {
            log("ERROR: WorldEdit not found.");
            return false;
        }

        return true;
    }

    @Override
    protected IAwePlugin loadPlugin(File pluginFile) {
        try {
            Plugin plugin = m_pluginManager.loadPlugin(pluginFile);
            
            if (!(plugin instanceof IAwePlugin)) {
                log(String.format("Unable to load plugin: %1$s. Plugin needs to implement IAwePlugin.", pluginFile.getName()));
                return null;
            }
            
            
            m_pluginManager.enablePlugin(plugin);

            log(String.format("Plugin %1$s loaded.", plugin.getName()));

            return (IAwePlugin)plugin;
        } catch (Exception ex) {
            log(String.format("Unable to load plugin: %1$s, check dependencies.", pluginFile.getName()));
            return null;
        }
    }

    @Override
    protected void unloadPlugin(IAwePlugin plugin) {
        if (plugin == null){
            return;
        }
        
        if (!(plugin instanceof Plugin)) {
            log(String.format("Unable to unload %1$s this is not a Bukkit plugin.", plugin.getClass().getName()));
            return;
        }
        
        Plugin bPlugin = (Plugin)plugin;
        
        try {
            m_pluginManager.disablePlugin(bPlugin);
            log(String.format("Plugin %1$s unloaded.", bPlugin.getName()));
        } catch (Exception ex) {
            log(String.format("Unable to unload plugin %1$s.", bPlugin.getName()));
        }
    }
}
