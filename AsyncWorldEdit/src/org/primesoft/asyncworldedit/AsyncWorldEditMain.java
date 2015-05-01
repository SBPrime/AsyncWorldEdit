/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
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

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.commands.*;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.injector.InjectorBukkit;
import org.primesoft.asyncworldedit.injector.async.AsyncClassFactory;
import org.primesoft.asyncworldedit.injector.core.InjectorCore;
import org.primesoft.asyncworldedit.mcstats.MetricsLite;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.playerManager.PlayerManager;
import org.primesoft.asyncworldedit.plotme.IPlotMeFix;
import org.primesoft.asyncworldedit.plotme.NullFix;
import org.primesoft.asyncworldedit.strings.MessageProvider;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.taskdispatcher.TaskDispatcher;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.worldedit.WorldeditIntegrator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplayManager;
import org.primesoft.asyncworldedit.progressDisplay.ProgressDisplayManager;


/**
 *
 * @author SBPrime
 */
public class AsyncWorldEditMain extends JavaPlugin implements IAsyncWorldEdit {

    private static final Logger s_log = Logger.getLogger("Minecraft.AWE");
    private static ConsoleCommandSender s_console;
    private static String s_prefix = null;
    private static final String s_logFormat = "%s %s";
    private static AsyncWorldEditMain s_instance;
    private BlocksHubIntegration m_blocksHub;
    private Boolean m_isInitialized = false;
    private MetricsLite m_metrics;
    private final EventListener m_listener = new EventListener(this);
    private final PhysicsWatch m_physicsWatcher = new PhysicsWatch();
    private final ChunkWatch m_chunkWatch = new ChunkWatch();
    private BlockPlacer m_blockPlacer;
    private TaskDispatcher m_dispatcher;
    private WorldeditIntegrator m_weIntegrator;
    private IPlotMeFix m_plotMeFix;
    private final PlayerManager m_playerManager = new PlayerManager(this);
    private IProgressDisplayManager m_progressDisplay;
    private InjectorCore m_aweInjector;

    public PlayerManager getPlayerManager() {
        return m_playerManager;
    }

    public PhysicsWatch getPhysicsWatcher() {
        return m_physicsWatcher;
    }

    public ChunkWatch getChunkWatch() {
        return m_chunkWatch;
    }

    public IPlotMeFix getPlotMeFix() {
        return m_plotMeFix;
    }
    
    public void setPlotMeFix(IPlotMeFix plotMeFix) {
        if (plotMeFix == null){
            plotMeFix = new NullFix();
        }
        
        
        log("PlotMeFix set to " + plotMeFix.getClass());
        m_plotMeFix = plotMeFix;
    }

    public BlockPlacer getBlockPlacer() {
        return m_blockPlacer;
    }

    public TaskDispatcher getTaskDispatcher() {
        return m_dispatcher;
    }

    @Override
    public IProgressDisplayManager getProgressDisplayManager() {
        return m_progressDisplay;
    }

    public static String getPrefix() {
        return s_prefix;
    }

    public static AsyncWorldEditMain getInstance() {
        return s_instance;
    }
    
    public IAsyncWorldEdit getAPI() {
        return this;
    }

    /**
     * Send message to the log
     *
     * @param msg
     */
    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    /**
     * Send message to the console
     *
     * @param msg
     */
    public static void sayConsole(String msg) {
        s_console.sendRawMessage(msg);
    }

    public BlocksHubIntegration getBlocksHub() {
        return m_blocksHub;
    }

    @Override
    public void onEnable() {
        s_instance = this;
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        s_console = getServer().getConsoleSender();
        
        m_isInitialized = false;

        if (!ConfigProvider.load(this)) {
            log("Error loading config");
            return;
        }
        
        initialiseStrings();

        try {
            MetricsLite metrics = new MetricsLite(this);
            if (!metrics.isOptOut()) {
                m_metrics = metrics;
                m_metrics.start();
            }
        } catch (IOException e) {
            ExceptionHelper.printException(e, "Error initializing MCStats");            
        }
        
        WorldEditPlugin worldEdit = getWorldEdit(this);
        if (worldEdit == null) {
            log("World edit not found.");
            return;
        }

        m_progressDisplay = new ProgressDisplayManager();
        
        m_blocksHub = new BlocksHubIntegration(this);
        m_blockPlacer = new BlockPlacer(this);
        m_dispatcher = new TaskDispatcher(this);
        setPlotMeFix(new NullFix());

        m_aweInjector = getAWEInjector(this);
        m_aweInjector.setClassFactory(new AsyncClassFactory(this));

        if (ConfigProvider.getCheckUpdate()) {
            log(VersionChecker.CheckVersion(desc.getVersion()));
        }
        if (!ConfigProvider.isConfigUpdated()) {
            log("Please update your config file!");
        }

        m_weIntegrator = new WorldeditIntegrator(this, worldEdit);

        if (ConfigProvider.isPhysicsFreezEnabled()) {
            m_physicsWatcher.Enable();
        } else {
            m_physicsWatcher.Disable();
        }

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(m_listener, this);
        pm.registerEvents(m_physicsWatcher, this);
        pm.registerEvents(m_chunkWatch, this);

        m_isInitialized = true;
        m_playerManager.initalize();

        log("Enabled");
    }

    
    /**
     * Initialise the strings
     */
    private void initialiseStrings() {
        if (!MessageProvider.saveDefault(this)) {
            log("Unable to save english.yml to plugin folder.");
        }
        
        if (!MessageProvider.loadDefault(this)) {
            log("Error loading default strings file, no internal fallback available!.");
        }
        if (!MessageProvider.loadFile(ConfigProvider.getStringsFile())) {
            log("Error loading strings file, using internal fallback.");
        }
    }

    @Override
    public void onDisable() {
        m_blockPlacer.stop();
        m_dispatcher.stop();
        m_weIntegrator.queueStop();
        m_chunkWatch.clear();
        log("Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PlayerEntry player = m_playerManager.getPlayer((sender instanceof Player) ? (Player) sender : null);

        if (!command.getName().equalsIgnoreCase(Commands.COMMAND_MAIN)) {
            return false;
        }

        String name = (args != null && args.length > 0) ? args[0] : "";

        if (name.equalsIgnoreCase(Commands.COMMAND_RELOAD)) {
            doReloadConfig(player, args != null && args.length > 1 ? args[1] : "");
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_HELP)) {
            String arg = args != null && args.length > 1 ? args[1] : null;
            return Help.ShowHelp(player, arg);
        } else if (name.equalsIgnoreCase(Commands.COMMAND_PURGE)) {
            doPurge(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_JOBS)) {
            doJobs(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_CANCEL)) {
            doCancel(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_TOGGLE)) {
            doToggle(player, args);
            return true;
        }

        return Help.ShowHelp(player, null);
    }

    private void doReloadConfig(PlayerEntry player, String arg) {
        if (!player.isAllowed(Permission.RELOAD_CONFIG)) {
            player.say(MessageType.NO_PERMS.format());
            return;
        }

        if (arg == null || arg.length() == 0) {
            Help.ShowHelp(player, Commands.COMMAND_RELOAD);
            return;
        }

        boolean reloadConfig, flushGroups;

        if (arg.equalsIgnoreCase("all")) {
            reloadConfig = true;
            flushGroups = true;
        } else if (arg.equalsIgnoreCase("config")) {
            reloadConfig = true;
            flushGroups = false;
        } else if (arg.equalsIgnoreCase("groups")) {
            reloadConfig = false;
            flushGroups = true;
        } else {
            Help.ShowHelp(player, Commands.COMMAND_RELOAD);
            return;
        }

        log(player.getName() + " reloading config (" + arg + ")...");
        if (reloadConfig) {
            reloadConfig();
            m_isInitialized = false;

            if (!ConfigProvider.load(this)) {
                player.say(MessageType.CMD_RELOAD_ERROR.format());
                return;
            }

            if (!MessageProvider.loadFile(ConfigProvider.getStringsFile())) {
                log("Error loading strings file, using internal fallback.");
            }
        }

        if (flushGroups) {
            m_playerManager.updateGroups();
        }
        if (reloadConfig) {
            m_blockPlacer.loadConfig();

            if (ConfigProvider.isPhysicsFreezEnabled()) {
                m_physicsWatcher.Enable();
            } else {
                m_physicsWatcher.Disable();
            }
        }

        m_isInitialized = true;
        player.say(MessageType.CMD_RELOAD_DONE.format());
    }

    private void doToggle(PlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        ToggleCommand.Execte(this, player, args);
    }

    private void doPurge(PlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        PurgeCommand.Execte(this, player, args);
    }

    private void doJobs(PlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        JobsCommand.Execte(this, player, args);
    }

    private void doCancel(PlayerEntry player, String[] args) {
        if (!m_isInitialized) {
            player.say(MessageType.NOT_INITIALIZED.format());
            return;
        }

        CancelCommand.Execte(this, player, args);
    }

    /**
     * Get instance of the world edit plugin
     *
     * @param plugin
     * @return
     */
    public static WorldEditPlugin getWorldEdit(JavaPlugin plugin) {
        final Plugin wPlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");

        if ((wPlugin == null) || (!(wPlugin instanceof WorldEditPlugin))) {
            return null;
        }

        PluginDescriptionFile pd = wPlugin.getDescription();
        if (!pd.getVersion().startsWith("6.")) {
            log("Unsupported version of WorldEdit, found: " + pd.getVersion() + " required: 6.x");
            return null;
        }
        return (WorldEditPlugin) wPlugin;
    }

    /**
     * Get instance of the world edit plugin
     *
     * @param plugin
     * @return
     */
    public static InjectorCore getAWEInjector(JavaPlugin plugin) {
        final Plugin wPlugin = plugin.getServer().getPluginManager().getPlugin("AsyncWorldEditInjector");

        if ((wPlugin == null) || (!(wPlugin instanceof InjectorBukkit))) {
            log("AsyncWorldEditInjector plugin not found, trying to initialize using static instances...");
            try {
                return InjectorCore.getInstance();
            } catch (Error ex) {
                ExceptionHelper.printException(ex, "AsyncWorldEditInjector not found.");
                return null;
            }            
        }

        return ((InjectorBukkit) wPlugin).getCore();
    }
}
