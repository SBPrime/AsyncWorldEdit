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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author SBPrime
 */
public class WorldGuardIntegrator {
    /**
     * The world guard
     */
    private WorldGuardPlugin m_worldGuard;
    
    
    /**
     * Craftbukkit server
     */
    private Server m_server;    
    
    
    /**
     * Is world guard integration enabed
     */
    private boolean m_isEnabled;
    
    
    public WorldGuardIntegrator(PluginMain plugin)
    {
        m_isEnabled = false;
        try
        {
            Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

            if ((cPlugin != null) && (cPlugin instanceof WorldGuardPlugin))
            {
                m_isEnabled = true;
                m_worldGuard = (WorldGuardPlugin)cPlugin;
                m_server = plugin.getServer();
            }
        }
        catch (NoClassDefFoundError ex)
        {
        }
        
        if (m_isEnabled)
        {
            PluginMain.Log("World guard found, " + 
                    (ConfigProvider.isWorldGuardEnabled() ? "integration enabled" : "integration disabled in config"));
        } else {
            PluginMain.Log("World guard found, integration disabled.");
        }
    }
    
    
    /**
     * Check if a player is allowed to place a block
     * @param player
     * @param location
     * @param world
     * @return 
     */
    public boolean canPlace(String player, Vector pos, World world)
    {
        if (!m_isEnabled || !ConfigProvider.isWorldGuardEnabled())
        {
            return false;
        }        
        
        Player p = m_server.getPlayer(player);
        Location location = new Location(world, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
        return m_worldGuard.canBuild(p, location);
    }
}
