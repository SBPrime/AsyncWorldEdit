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
package org.PrimeSoft.AsyncWorldedit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.PrimeSoft.AsyncWorldedit.BlockLogger.*;
import org.PrimeSoft.AsyncWorldedit.Commands.Commands;
import org.PrimeSoft.AsyncWorldedit.Commands.JobsCommand;
import org.PrimeSoft.AsyncWorldedit.Commands.PurgeCommand;
import org.PrimeSoft.AsyncWorldedit.MCStats.MetricsLite;
import org.PrimeSoft.AsyncWorldedit.Worldedit.WorldeditIntegrator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author SBPrime
 */
public class PluginMain extends JavaPlugin {

    private static final Logger s_log = Logger.getLogger("Minecraft.MCPainter");
    private static ConsoleCommandSender s_console;
    private static String s_prefix = null;
    private static String s_logFormat = "%s %s";
    private static PluginMain s_instance;
    private Boolean m_isInitialized = false;
    private MetricsLite m_metrics;
    private EventListener m_listener = new EventListener(this);
    private BlockPlacer m_blockPlacer;
    private WorldeditIntegrator m_weIntegrator;
    private IBlockLogger m_logger;

    public BlockPlacer getBlockPlacer() {
        return m_blockPlacer;
    }

    public static String getPrefix() {
        return s_prefix;
    }

    public static Player getPlayer(String player) {
        if (s_instance == null)
        {
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
        m_weIntegrator = new WorldeditIntegrator(this, worldEdit.getWorldEdit());

        if (!ConfigProvider.load(this)) {
            Log("Error loading config");
            return;
        }

        m_blockPlacer = new BlockPlacer(this);

        if (ConfigProvider.getCheckUpdate()) {
            Log(VersionChecker.CheckVersion(desc.getVersion()));
        }
        if (!ConfigProvider.isConfigUpdated()) {
            Log("Please update your config file!");
        }        

        m_logger = getLogger(ConfigProvider.getLogger());
        m_blockPlacer.setLogger(m_logger);
        
        getServer().getPluginManager().registerEvents(m_listener, this);
        m_isInitialized = true;
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

        m_logger = getLogger(ConfigProvider.getLogger());
        m_blockPlacer.setLogger(m_logger);
        
        m_isInitialized = true;
        Say(player, "Config reloaded");
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
     * Create the block logger
     * @param logger
     * @return 
     */
    private IBlockLogger getLogger(String logger) {
        if (logger.equalsIgnoreCase(Loggers.LOG_BLOCK))
        {
            return new LogBlockLogger(this);
        }
        if (logger.equalsIgnoreCase(Loggers.CORE_PROTECT))
        {
            return new CoreProtectLogger(this);
        }
        if (logger.equalsIgnoreCase(Loggers.PRISM))
        {
            return new PrismLogger(this);
        }
        if (logger.equalsIgnoreCase(Loggers.NONE))
        {
            return new NoneLogger();
        }
        
        Log("Unknown logger: "+ logger + ". Logger disabled.");
        return new NoneLogger();
    }
}