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
import java.util.HashSet;
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
import org.primesoft.asyncworldedit.commands.Commands;
import org.primesoft.asyncworldedit.commands.JobsCommand;
import org.primesoft.asyncworldedit.commands.PurgeCommand;
import org.primesoft.asyncworldedit.commands.ToggleCommand;
import org.primesoft.asyncworldedit.mcstats.MetricsLite;
import org.primesoft.asyncworldedit.worldedit.WorldeditIntegrator;

/**
 *
 * @author SBPrime
 */
public class PluginMain extends JavaPlugin {

    private static final Logger s_log = Logger.getLogger("Minecraft.AWE");
    private static ConsoleCommandSender s_console;
    private static String s_prefix = null;
    private static String s_logFormat = "%s %s";
    private static PluginMain s_instance;
    private static final HashSet<String> s_asyncPlayers = new HashSet<String>();
    private BlocksHubIntegration m_blocksHub;

    /**
     * Check if player has AWE enabled
     *
     * @param player
     * @return
     */
    public static boolean hasAsyncMode(String player) {
        if (player == null) {
            return true;
        }

        synchronized (s_asyncPlayers) {
            return s_asyncPlayers.contains(player.toLowerCase());
        }
    }

    /**
     * Set player AEW default mode (on or off)
     *
     * @param player
     */
    public static void setMode(Player player) {
        boolean hasOn = PermissionManager.isAllowed(player, PermissionManager.Perms.Mode_On);
        boolean hasOff = PermissionManager.isAllowed(player, PermissionManager.Perms.Mode_Off);
        boolean def = ConfigProvider.getDefaultMode();

        if (hasOn) {
            PluginMain.setMode(player.getName(), true);
        } else if (hasOff) {
            PluginMain.setMode(player.getName(), false);
        } else {
            PluginMain.setMode(player.getName(), def);
        }
    }

    /**
     * Aet the AWE player mode
     *
     * @param player
     * @param mode
     */
    public static void setMode(String player, boolean mode) {
        if (player == null) {
            return;
        }

        synchronized (s_asyncPlayers) {
            player = player.toLowerCase();
            boolean contains = s_asyncPlayers.contains(player);

            if (contains && !mode) {
                s_asyncPlayers.remove(player);
            } else if (!contains && mode) {
                s_asyncPlayers.add(player);
            }
        }
    }
    private Boolean m_isInitialized = false;
    private MetricsLite m_metrics;
    private EventListener m_listener = new EventListener(this);
    private PhysicsWatch m_physicsWatcher = new PhysicsWatch();
    private BlockPlacer m_blockPlacer;
    private WorldeditIntegrator m_weIntegrator;    
    private PlotMeFix m_plotMeFix;

    public PhysicsWatch getPhysicsWatcher()
    {
        return m_physicsWatcher;
    }
    
    public PlotMeFix getPlotMeFix() {       
        return m_plotMeFix;
    }

    public BlockPlacer getBlockPlacer() {
        return m_blockPlacer;
    }

    public static String getPrefix() {
        return s_prefix;
    }

    public static Player getPlayer(String player) {
        if (s_instance == null) {
            return null;
        }

        return s_instance.getServer().getPlayer(player);
    }
       

    public static void Log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    public static void Say(Player player, String msg) {
        if (player == null) {
            s_console.sendRawMessage(msg);
        } else {
            player.sendRawMessage(msg);
        }
    }

    public BlocksHubIntegration getBlocksHub(){
        return m_blocksHub;
    }
    
    @Override
    public void onEnable() {
        s_instance = this;
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        m_isInitialized = false;

        try {
            m_metrics = new MetricsLite(this);
            m_metrics.start();
        } catch (IOException e) {
            Log("Error initializing MCStats: " + e.getMessage());
        }

        s_console = getServer().getConsoleSender();
        WorldEditPlugin worldEdit = getWorldEdit(this);
        if (worldEdit == null) {
            Log("World edit not found.");
            return;
        }

        if (!ConfigProvider.load(this)) {
            Log("Error loading config");
            return;
        }

        m_blocksHub = new BlocksHubIntegration(this);
        m_plotMeFix = new PlotMeFix(this);
        m_blockPlacer = new BlockPlacer(this);

        if (ConfigProvider.getCheckUpdate()) {
            Log(VersionChecker.CheckVersion(desc.getVersion()));
        }
        if (!ConfigProvider.isConfigUpdated()) {
            Log("Please update your config file!");
        }
        
        m_weIntegrator = new WorldeditIntegrator(this, worldEdit.getWorldEdit());

        if (ConfigProvider.isPhysicsFreezEnabled()) {
            m_physicsWatcher.Enable();
        } else {
            m_physicsWatcher.Disable();
        }
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(m_listener, this);
        pm.registerEvents(m_physicsWatcher, this);
        
        m_isInitialized = true;

        setPlayerModes();

        Log("Enabled");
    }

    @Override
    public void onDisable() {
        m_blockPlacer.stop();
        m_weIntegrator.queueStop();
        Log("Disabled");
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
        }

        if (player == null) {
            return Help.ShowHelp(player, null);
        }

        if (name.equalsIgnoreCase(Commands.COMMAND_PURGE)) {
            doPurge(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_JOBS)) {
            doJobs(player, args);
            return true;
        } else if (name.equalsIgnoreCase(Commands.COMMAND_TOGGLE)) {
            doToggle(player, args);
            return true;
        }

        return Help.ShowHelp(player, null);
    }

    private void doReloadConfig(Player player) {
        if (player != null) {
            if (!PermissionManager.isAllowed(player, PermissionManager.Perms.ReloadConfig)) {
                Say(player, ChatColor.RED + "You have no permissions to do that.");
                return;
            }
        }

        Log(player != null ? player.getName() : "console " + " reloading config...");

        reloadConfig();
        m_isInitialized = false;

        if (!ConfigProvider.load(this)) {
            Say(player, "Error loading config");
            return;
        }

        m_blockPlacer.queueStop();
        m_blockPlacer = new BlockPlacer(this);

        if (ConfigProvider.isPhysicsFreezEnabled())
        {
            m_physicsWatcher.Enable();
        }
        else {
            m_physicsWatcher.Disable();
        }

        m_isInitialized = true;
        Say(player, "Config reloaded");
    }

    private void doToggle(Player player, String[] args) {
        if (!m_isInitialized) {
            Say(player, ChatColor.RED + "Module not initialized, contact administrator.");
            return;
        }

        ToggleCommand.Execte(this, player, args);
    }

    private void doPurge(Player player, String[] args) {
        if (!m_isInitialized) {
            Say(player, ChatColor.RED + "Module not initialized, contact administrator.");
            return;
        }

        PurgeCommand.Execte(this, player, args);
    }

    private void doJobs(Player player, String[] args) {
        if (!m_isInitialized) {
            Say(player, ChatColor.RED + "Module not initialized, contact administrator.");
            return;
        }

        JobsCommand.Execte(this, player, args);
    }

    /**
     * Get instance of the world edit plugin
     *
     * @param plugin
     * @return
     */
    public static WorldEditPlugin getWorldEdit(JavaPlugin plugin) {
        Plugin wPlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");

        if ((wPlugin == null) || (!(wPlugin instanceof WorldEditPlugin))) {
            return null;
        }

        return (WorldEditPlugin) wPlugin;
    }

    /**
     * Set all players AWE mode on/off
     */
    private void setPlayerModes() {
        Player[] players = getServer().getOnlinePlayers();
        for (Player player : players) {
            setMode(player);
        }
    }
}