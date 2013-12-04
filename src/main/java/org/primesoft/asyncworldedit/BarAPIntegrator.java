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

import me.confuser.barapi.BarAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author SBPrime
 */
public class BarAPIntegrator {

    private boolean m_isInitialized;

    /**
     * Get instance of the core blocks hub plugin
     *
     * @param plugin
     * @return
     */
    public static BarAPI getBarAPI(JavaPlugin plugin) {
        try {
            Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("BarAPI");

            if ((cPlugin == null) || (!(cPlugin instanceof BarAPI))) {
                return null;
            }

            return (BarAPI) cPlugin;
        } catch (NoClassDefFoundError ex) {
            return null;
        }
    }

    public BarAPIntegrator(JavaPlugin plugin) {
        BarAPI ba = getBarAPI(plugin);
        m_isInitialized = ba != null;
    }

    public void setMessage(Player player, String message, double percent) {
        if (!m_isInitialized || player == null) {
            return;
        }

        if (message == null) {
            message = "";
        }
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
        
        BarAPI.setMessage(player, message, (float)percent);
    }

    public void disableMessage(Player player) {
        if (!m_isInitialized || player == null) {
            return;
        }
        
        BarAPI.removeBar(player);
    }
}
