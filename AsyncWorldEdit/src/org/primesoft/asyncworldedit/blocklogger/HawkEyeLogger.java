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
package org.primesoft.asyncworldedit.blocklogger;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.PluginMain;

import uk.co.oliwali.HawkEye.DataType;
import uk.co.oliwali.HawkEye.HawkEye;
import uk.co.oliwali.HawkEye.database.DataManager;
import uk.co.oliwali.HawkEye.entry.BlockChangeEntry;
import uk.co.oliwali.HawkEye.entry.BlockEntry;

/**
 *
 * @author SBPrime
 */
public class HawkEyeLogger implements IBlockLogger {
    /**
     * HawkEye protect
     */
    private HawkEye m_hawkEye;
    
    
    /**
     * Is the logger enabled
     */
    private boolean m_isEnabled;
    
    
    /**
     * Get instance of the core protect plugin
     * @param plugin
     * @return
     */
    public static HawkEye getHawkEye(JavaPlugin plugin) {
        try {
            Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("HawkEye");

            if ((cPlugin == null) || (!(cPlugin instanceof HawkEye))) {
                return null;
            }           
            
            return (HawkEye) cPlugin;
        } catch (NoClassDefFoundError ex) {
            return null;
        }
    }
    
    public HawkEyeLogger(PluginMain plugin)
    {
        m_hawkEye = getHawkEye(plugin);
        if (m_hawkEye == null)
        {
            PluginMain.Log("Error initializing HawkEye logger.");
            m_isEnabled = false;
        } else {
            PluginMain.Log("HawkEye logger initialized");
            m_isEnabled = true;
        }
    }
    
    @Override
    public void LogBlock(Vector location, BaseBlock oldBlock, BaseBlock newBlock, 
        String player, World world) {
        
       if (!m_isEnabled)
        {
            return;
        }
        
        Location l = new Location(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
        
        if (newBlock.getType() == Material.AIR.getId())
        {
            DataManager.addEntry(new BlockEntry(player, DataType.WORLDEDIT_BREAK, oldBlock.getType(), oldBlock.getData(), l));
        } else 
        {
            DataManager.addEntry(new BlockChangeEntry(player, DataType.WORLDEDIT_PLACE, l, 
                    oldBlock.getType(), oldBlock.getData(), 
                    newBlock.getType(), newBlock.getData()));
        }
    }    
}