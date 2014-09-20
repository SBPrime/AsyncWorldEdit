/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.taskdispatcher.TaskDispatcher;
import org.primesoft.asyncworldedit.commands.*;
import org.primesoft.asyncworldedit.injector.InjectorMain;
import org.primesoft.asyncworldedit.injector.async.AsyncClassFactory;
import org.primesoft.asyncworldedit.mcstats.MetricsLite;
import org.primesoft.asyncworldedit.worldedit.WorldeditIntegrator;

/**
 *
 * @author SBPrime
 */
public class AsyncWorldEditMain extends JavaPlugin {

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
    private PlotMeFix m_plotMeFix;
    private final PlayerManager m_playerManager = new PlayerManager(this);
    private BarAPIntegrator m_barApi;
    private InjectorMain m_aweInjector;

    public PlayerManager getPlayerManager() {
        return m_playerManager;
    }

    public PhysicsWatch getPhysicsWatcher() {
        return m_physicsWatcher;
    }

    public ChunkWatch getChunkWatch() {
        return m_chunkWatch;
    }

    public PlotMeFix getPlotMeFix() {
        return m_plotMeFix;
    }

    public BlockPlacer getBlockPlacer() {
        return m_blockPlacer;
    }

    public TaskDispatcher getTaskDispatcher() {
        return m_dispatcher;
    }

    public BarAPIntegrator getBarAPI() {
        return m_barApi;
    }

    public static String getPrefix() {
        return s_prefix;
    }

    public static AsyncWorldEditMain getInstance() {
        return s_instance;
    }

    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    public static void say(UUID uuid, String msg) {
        say(getPlayer(uuid), msg);
    }

    /**
     * Get craft bukkit player
     *
     * @param uuid player
     * @return
     */
    public static Player getPlayer(UUID uuid) {
        if (s_instance == null) {
            return null;
        }

        PlayerManager pManager = s_instance.getPlayerManager();
        PlayerWrapper player = pManager.getPlayer(uuid);
        if (player == null) {
            return null;
        }
        Player bPlayer = player.getPlayer();
        if (bPlayer == null || !bPlayer.isOnline()) {
            return null;
        }

        return bPlayer;
    }

    public static void say(Player player, String msg) {
        if (player == null) {
            s_console.sendRawMessage(msg);
        } else {
            player.sendRawMessage(msg);
        }
    }

    public BlocksHubIntegration getBlocksHub() {
        return m_blocksHub;
    }

    @Override
    public void onEnable() {
        s_instance = this;
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        m_isInitialized = false;

        if (!ConfigProvider.load(this)) {
            log("Error loading config");
            return;
        }

        try {
            MetricsLite metrics = new MetricsLite(this);
            if (!metrics.isOptOut()) {
                m_metrics = metrics;
                m_metrics.start();
            }
        } catch (IOException e) {
            log("Error initializing MCStats: " + e.getMessage());
        }

        s_console = getServer().getConsoleSender();
        WorldEditPlugin worldEdit = getWorldEdit(this);
        if (worldEdit == null) {
            log("World edit not found.");
            return;
        }

        m_barApi = new BarAPIntegrator(this);
        m_blocksHub = new BlocksHubIntegration(this);
        m_blockPlacer = new BlockPlacer(this);
        m_dispatcher = new TaskDispatcher(this);
        m_plotMeFix = new PlotMeFix(this);

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
        Player player = (sender instanceof Player) ? (Player) sender : null;

        if (!command.getName().equalsIgnoreCase(Commands.COMMAND_MAIN)) {
            return false;
        }

        String name = (args != null && args.length > 0) ? args[0] : "";

        if (name.equalsIgnoreCase(Commands.COMMAND_RELOAD)) {
            doReloadConfig(player);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_HELP)) {
            String arg = args.length > 1 ? args[1] : null;
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

    private void doReloadConfig(Player player) {
        if (player != null) {
            if (!PermissionManager.isAllowed(player, Permission.RELOAD_CONFIG)) {
                say(player, ChatColor.RED + "You have no permissions to do that.");
                return;
            }
        }

        log(player != null ? player.getName() : "console " + " reloading config...");

        reloadConfig();
        m_isInitialized = false;

        if (!ConfigProvider.load(this)) {
            say(player, "Error loading config");
            return;
        }

        m_blockPlacer.queueStop();
        m_blockPlacer = new BlockPlacer(this);

        if (ConfigProvider.isPhysicsFreezEnabled()) {
            m_physicsWatcher.Enable();
        } else {
            m_physicsWatcher.Disable();
        }

        m_isInitialized = true;
        say(player, "Config reloaded");
    }

    private void doToggle(Player player, String[] args) {
        if (!m_isInitialized) {
            say(player, ChatColor.RED + "Module not initialized, contact administrator.");
            return;
        }

        ToggleCommand.Execte(this, player, args);
    }

    private void doPurge(Player player, String[] args) {
        if (!m_isInitialized) {
            say(player, ChatColor.RED + "Module not initialized, contact administrator.");
            return;
        }

        PurgeCommand.Execte(this, player, args);
    }

    private void doJobs(Player player, String[] args) {
        if (!m_isInitialized) {
            say(player, ChatColor.RED + "Module not initialized, contact administrator.");
            return;
        }

        JobsCommand.Execte(this, player, args);
    }

    private void doCancel(Player player, String[] args) {
        if (!m_isInitialized) {
            say(player, ChatColor.RED + "Module not initialized, contact administrator.");
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

        return (WorldEditPlugin) wPlugin;
    }

    /**
     * Get instance of the world edit plugin
     *
     * @param plugin
     * @return
     */
    public static InjectorMain getAWEInjector(JavaPlugin plugin) {
        final Plugin wPlugin = plugin.getServer().getPluginManager().getPlugin("AsyncWorldEditInjector");

        if ((wPlugin == null) || (!(wPlugin instanceof InjectorMain))) {
            return null;
        }

        return (InjectorMain) wPlugin;
    }
}
