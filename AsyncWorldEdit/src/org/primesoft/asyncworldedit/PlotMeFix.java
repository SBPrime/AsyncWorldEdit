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

import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotWorldEdit;
import net.coreprotect.CoreProtect;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class is used to fix PlotMe Mask seting errors
 *
 * @author SBPrime
 */
public class PlotMeFix
{
    /**
     * Is PlotMe fix enabled
     */
    private boolean m_isEnabled;

    /**
     * Instance of plotme plugin
     */
    private PlotMe m_plotMe;

    /**
     * New instance of PlotMe plugin
     *
     * @param plugin
     */
    public PlotMeFix(JavaPlugin plugin)
    {
        m_isEnabled = false;
        try
        {
            Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("PlotMe");

            if ((cPlugin != null) && (cPlugin instanceof PlotMe))
            {
                m_isEnabled = true;
                m_plotMe = (PlotMe) cPlugin;
            }
        }
        catch (NoClassDefFoundError ex)
        {
        }
    }

    
    public void setMask(Player p)
    {
        if (!m_isEnabled || p == null)
        {
            return;
        }

        if (PlotManager.isPlotWorld(p))
        {
            if (!PlotMe.isIgnoringWELimit(p))
            {
                PlotWorldEdit.setMask(p);
            } else
            {
                PlotWorldEdit.removeMask(p);
            }
        }
    }
}
