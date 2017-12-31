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

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.IWorldeditIntegratorInner;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.core.ChunkWatch;
import org.primesoft.asyncworldedit.core.PhysicsWatch;
import org.primesoft.asyncworldedit.platform.api.Constants;
import org.primesoft.asyncworldedit.platform.api.ICommandManager;
import org.primesoft.asyncworldedit.platform.api.IConfiguration;
import org.primesoft.asyncworldedit.platform.api.IPlatform;
import org.primesoft.asyncworldedit.platform.api.IPlayerProvider;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.events.IEvent;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubBridge;
import org.primesoft.asyncworldedit.api.inner.IChunkWatch;
import org.primesoft.asyncworldedit.api.map.IMapUtils;
import org.primesoft.asyncworldedit.injector.scanner.ClassScanner;
import org.primesoft.asyncworldedit.platform.api.IMaterialLibrary;
import org.primesoft.asyncworldedit.platform.bukkit.blockshub.BlocksHubV1Factory;
import org.primesoft.asyncworldedit.platform.bukkit.mcstats.MetricsLite;
import org.primesoft.asyncworldedit.strings.MessageProvider;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class BukkitPlatform implements IPlatform, CommandExecutor, Listener {

    private static final String WORLDEDIT_CLASS = "com.sk89q.worldedit.bukkit.WorldEditPlugin";
    private static final String BLOCKSHUB = "BlocksHub";

    private final IMapUtils m_mapUtils;

    private final ICommandManager m_commandManager;

    private final IPlayerProvider m_playerProvider;

    private IAsyncWorldEditCore m_aweCore;

    private final IScheduler m_scheduler;

    private final Plugin m_plugin;

    private MetricsLite m_metrics;

    private final PhysicsWatch m_physicsWatcher;

    private final ChunkWatch m_chunkWatcher;

    private final IMaterialLibrary m_materialLibrary;

    private IBlocksHubBridge m_blocksHub;

    private final ClassScanner m_classScanner = (new BukkitClassScanner()).initialize();

    private IWorldeditIntegratorInner m_weIntegrator;

    public BukkitPlatform(Plugin plugin) {
        m_mapUtils = new BukkitMapUtils();
        m_commandManager = new CommandManager(plugin.getServer(), Constants.PluginName, this);
        m_playerProvider = new BukkitPlayerProvider(plugin, plugin.getServer());
        m_scheduler = new SchedulerBukkit(plugin);
        m_physicsWatcher = new BykkitPhysicsWatch(plugin);
        m_chunkWatcher = new BukkitChunkWatcher(plugin);
        m_materialLibrary = new BukkitMaterialLibrary();
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
     * @return 
     */
    @Override
    public String getServerAPILong() {
        return Bukkit.getBukkitVersion();
    }

    @Override
    public void initialize(IAsyncWorldEditCore core) {
        m_aweCore = core;
        m_blocksHub = core.getBlocksHubBridge();
        m_blocksHub.addFacroty(new  BlocksHubV1Factory());
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

        IPlayerProvider playerProvider = getPlayerProvider();

        playerProvider.registerEvents();
        //playerProvider.initialize(m_aweCore.getPlayerStorage());

        m_physicsWatcher.registerEvents();
        m_chunkWatcher.registerEvents();

        m_plugin.getServer().getPluginManager().registerEvents(this, m_plugin);
    }

    @Override
    public void onDisable() {
        getChunkWatcher().clear();

        IWorldeditIntegratorInner weIntegrator = m_weIntegrator;
        if (weIntegrator != null) {
            weIntegrator.queueStop();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        IAsyncWorldEditCore aweCore = m_aweCore;
        IPlayerManager pm = aweCore != null ? aweCore.getPlayerManager() : null;

        if (aweCore == null || pm == null) {
            return false;
        }

        IPlayerEntry player = null;
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

    @Override
    public ICommandManager getCommandManager() {
        return m_commandManager;
    }

    @Override
    public IPlayerProvider getPlayerProvider() {
        return m_playerProvider;
    }

    /**
     * Get instance of the world edit plugin
     *
     * @param plugin
     * @return
     */
    private WorldEditPlugin getWorldEdit() {
        final Plugin wPlugin = getTypedPlugin("WorldEdit");

        if ((wPlugin == null) || (!(wPlugin instanceof WorldEditPlugin))) {
            return null;
        }

        PluginDescriptionFile pd = wPlugin.getDescription();
        if (!pd.getVersion().startsWith("6.")) {
            log(String.format("Unsupported version of WorldEdit, found: %1$s required: 6.x", pd.getVersion()));
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
    public IScheduler getScheduler() {
        return m_scheduler;
    }

    @Override
    public IPhysicsWatch getPhysicsWatcher() {
        return m_physicsWatcher;
    }

    @Override
    public IChunkWatch getChunkWatcher() {
        return m_chunkWatcher;
    }

    @Override
    public IMapUtils getMapUtils() {
        return m_mapUtils;
    }

    @Override
    public IConfiguration getConfig() {
        m_plugin.saveDefaultConfig();

        Configuration configuration = m_plugin.getConfig();
        if (configuration == null) {
            return null;
        }

        configuration.setDefaults(new MemoryConfiguration());

        return new BukkitConfiguration(m_plugin, configuration);
    }

    @Override
    public IMaterialLibrary getMaterialLibrary() {
        return m_materialLibrary;
    }

    @Override
    public IWorldeditIntegratorInner getWorldEditIntegrator() {
        return m_weIntegrator;
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
            m_blocksHub.initialize(plugin);
        }
    }
        
    /*private class EventTest {
        @Subscribe
        public void EventTest(IEvent event) {
            System.out.println("Event "+ event.getClass().getCanonicalName());
        }
    }
    
    private EventTest m_eventTest;*/
    
    /**
     * Initialize the WorldEdit
     */
    private void initializeWorldEdit() {
        log("Initializing WorldEdit.");

        WorldEditPlugin worldEdit = getWorldEdit();
        if (worldEdit == null) {
            log("World edit not found.");
            m_weIntegrator = null;
        } else {
            m_weIntegrator = new BukkitWorldeditIntegrator(m_aweCore, worldEdit);
            
            /*m_eventTest = new EventTest();
            
            m_weIntegrator.getEventBus().register(m_eventTest);*/
        }
    }

    @Override
    public MessageProvider createMessageProvider() {
        return new BukkitMessageProvider();
    }

    @Override
    public ClassScanner getClasScanner() {
        return m_classScanner;
    }

    /**
     * Get plugin
     *
     * @param pluginName
     * @return
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
    }
}
