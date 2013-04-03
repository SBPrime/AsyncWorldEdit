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

import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.PrimeSoft.AsyncWorldedit.MCStats.MetricsLite;
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
    private Boolean m_isInitialized = false;
    private WorldEditPlugin m_worldEdit = null;
    private MetricsLite m_metrics;
    
    private BlockPlacer m_blockPlacer;

    
    public BlockPlacer getBlockPlacer() {
        return m_blockPlacer;
    }

    public static String getPrefix() {
        return s_prefix;
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
        m_worldEdit = getWorldEdit(this);
        if (m_worldEdit == null) {
            Log("World edit not found.");
        }
        
        if (!ConfigProvider.load(this)) {
            Log("Error loading config");
            return;
        }
        
        m_blockPlacer = new BlockPlacer(this);
        
        //if (ConfigProvider.getCheckUpdate()) {
        //    Log(VersionChecker.CheckVersion(desc.getVersion()));
        //}        
        if (!ConfigProvider.isConfigUpdated())
        {
            Log("Please update your config file!");
        }
        
        m_worldEdit.getWorldEdit().setEditSessionFactory(new AsyncEditSessionFactory(m_blockPlacer));
        
        m_isInitialized = true;
        Log("Enabled");
    }
    
    
    @Override
    public void onDisable() {
        m_blockPlacer.Stop();
        m_worldEdit.getWorldEdit().setEditSessionFactory(new EditSessionFactory());
        Log("Disabled");
    }
    
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        
        return super.onCommand(sender, command, label, args);
    }
    
    
    public static WorldEditPlugin getWorldEdit(JavaPlugin plugin) {
        Plugin wPlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");

        if ((wPlugin == null) || (!(wPlugin instanceof WorldEditPlugin))) {
            return null;
        }

        return (WorldEditPlugin) wPlugin;
    }
}