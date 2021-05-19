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
package org.primesoft.asyncworldedit.platform.bukkit;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.blockshub.platform.bukkit.BlocksHubV3Factory;
import org.primesoft.asyncworldedit.platform.api.Constants;
import org.primesoft.asyncworldedit.platform.api.IConfiguration;
import org.primesoft.asyncworldedit.platform.api.IPlatform;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.platform.base.BasePlatform;
import org.primesoft.asyncworldedit.platform.bukkit.mcstats.MetricsLite;
import org.primesoft.asyncworldedit.strings.MessageProvider;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

import static org.primesoft.asyncworldedit.LoggerProvider.log;

/**
 *
 * @author SBPrime
 */
public class BukkitPlatform extends BasePlatform implements IPlatform, Listener {

    private static final String WORLDEDIT_CLASS = "com.sk89q.worldedit.bukkit.WorldEditPlugin";
    private static final String BLOCKSHUB = "BlocksHub";

    private final Plugin m_plugin;

    private MetricsLite m_metrics;

    public BukkitPlatform(Plugin plugin) {
        super(
            new BukkitClassScanner().initialize(),
            new BukkitMapUtils(),
            coreSupplier -> new CommandManager(plugin.getServer(), Constants.PluginName, new CommandConsumer(coreSupplier)),
            new BukkitPlayerProvider(plugin, plugin.getServer()),
            new SchedulerBukkit(plugin),
            scheduler -> new BukkitPhysicsWatch(plugin, scheduler),
            new BukkitChunkWatcher(plugin),
            new BukkitMaterialLibrary()
        );

        m_plugin = plugin;
    }

    @Override
    public String getName() {
        return "Bukkit API";
    }

    @Override
    public String getVersion() {
        PluginDescriptionFile desc = m_plugin.getDescription();
        return desc.getVersion();
    }

    @Override
    public String getServerAPI() {
        Class<?> cls = m_plugin.getServer().getClass();
        String className = cls.getName();
        className = className.replace("org.bukkit.craftbukkit.", "");
        int idx = className.indexOf(".");

        if (idx >= 0) {
            className = className.substring(0, idx);
        }

        return String.format("Bukkit.%1$s", className);
    }
    
    /**
     * Gets the server api version
     */
    @Override
    public String getServerAPILong() {
        return Bukkit.getBukkitVersion();
    }

    @Override
    public void initialize(IAsyncWorldEditCore core) {
        super.initialize(core, BlocksHubV3Factory::new);
    }
    
    @Override
    public void onEnable() {
        try {
            MetricsLite metrics = new MetricsLite(m_plugin);
            if (!metrics.isOptOut()) {
                m_metrics = metrics;
                m_metrics.start();
            }
        } catch (IOException e) {
            ExceptionHelper.printException(e, "Error initializing MCStats");
        }

        TileEntityUtils.isTileEntity(Material.AIR);

        PluginManager pm = m_plugin.getServer().getPluginManager();
        pm.registerEvents(this, m_plugin);
        if (pm.isPluginEnabled("WorldEdit")) {
            onPluginEnabled(new PluginEnableEvent(pm.getPlugin("WorldEdit")));
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (m_metrics != null) {
            try {
                m_metrics.disable();
            } catch (IOException e) {
                ExceptionHelper.printException(e, "Error disabling MCStats");
            }
        }
    }

    /**
     * Get instance of the world edit plugin
     *
     */
    private WorldEditPlugin getWorldEdit() {
        final Plugin wPlugin = getTypedPlugin("WorldEdit");

        if (!(wPlugin instanceof WorldEditPlugin)) {
            return null;
        }

        PluginDescriptionFile pd = wPlugin.getDescription();
        if (!pd.getVersion().startsWith("7.")) {
            log(String.format("Unsupported version of WorldEdit, found: %1$s required: 7.x", pd.getVersion()));
            return null;
        }
        return (WorldEditPlugin) wPlugin;
    }

    @Override
    public IWorld getWorld(UUID worldUUID) {
        World world = m_plugin.getServer().getWorld(worldUUID);

        if (world == null) {
            return null;
        }

        return new BukkitWorld(world);
    }

    @Override
    public IWorld getWorld(String worldName) {
        World world = m_plugin.getServer().getWorld(worldName);

        if (world == null) {
            return null;
        }

        return new BukkitWorld(world);
    }

    @Override
    public IConfiguration getConfig() {
        m_plugin.saveDefaultConfig();
               
        YamlConfiguration configuration = loadConfig();
        if (configuration == null) {
            return null;
        }

        configuration.setDefaults(new MemoryConfiguration());

        return new BukkitConfiguration(m_plugin, configuration);
    }

    @EventHandler
    public void onPluginEnabled(PluginEnableEvent e) {
        Plugin plugin = e.getPlugin();
        String pluginClassName = plugin.getClass().getName();
        String pluginName = plugin.getName();

        if (pluginClassName.equalsIgnoreCase(WORLDEDIT_CLASS)) {
            initializeWorldEdit();
            return;
        }

        if (pluginName.equalsIgnoreCase(BLOCKSHUB)) {
            initializeBlocksHub(plugin);
        }
    }

    /**
     * Initialize the WorldEdit
     */
    private void initializeWorldEdit() {
        log("Initializing WorldEdit.");

        WorldEditPlugin worldEdit = getWorldEdit();
        if (worldEdit == null) {
            log("World edit not found.");
            initializeWorldEdit(core -> null);

        } else {
            initializeWorldEdit(core -> new BukkitWorldeditIntegrator(core, worldEdit));
        }
    }

    @Override
    public MessageProvider createMessageProvider() {
        return new BukkitMessageProvider();
    }

    /**
     * Get plugin
     *
     */
    private Plugin getTypedPlugin(String pluginName) {
        if (pluginName == null) {
            return null;
        }

        PluginManager pm = m_plugin.getServer().getPluginManager();

        return pm.getPlugin(pluginName);
    }

    @Override
    public Object getPlugin(String pluginName) {
        return getTypedPlugin(pluginName);
    }

    @Override
    public void reloadConfig() {
        m_plugin.reloadConfig();

        super.reloadConfig();
    }

    private YamlConfiguration loadConfig() {        
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.options().pathSeparator('•');
            File configFile = new File(m_plugin.getDataFolder(), "config.yml");
            config.load(configFile);
            return config;
        } catch (IOException | InvalidConfigurationException ex) {
            ExceptionHelper.printException(ex, "Unable to load configuration,");
        }

        return null;
    }

    private static class CommandConsumer implements CommandExecutor {
        private final Supplier<IAsyncWorldEditCore> getCore;

        private CommandConsumer(final Supplier<IAsyncWorldEditCore> getCore) {
            this.getCore = getCore;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            IAsyncWorldEditCore aweCore = getCore.get();
            IPlayerManager pm = aweCore != null ? aweCore.getPlayerManager() : null;

            if (aweCore == null || pm == null) {
                return false;
            }

            IPlayerEntry player;
            if (sender instanceof Player) {
                player = pm.getPlayer(((Player) sender).getUniqueId());

                if (player == null) {
                    pm.getConsolePlayer();
                }
            } else {
                player = pm.getConsolePlayer();
            }

            return aweCore.onCommand(player, command.getName(), args);
        }

    }
}
