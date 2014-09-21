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

import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotWorldEdit;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class is used to fix PlotMe Mask seting errors
 *
 * @author SBPrime
 */
public class PlotMeFix {

    /**
     * Is PlotMe fix enabled
     */
    private boolean m_isEnabled;

    /**
     * The PlotMe instance
     */
    private PlotMe m_instance;
    private Field m_plotWorldEditField;
    private Method m_setMaskMethod;
    private Method m_removeMaskMethod;

    /**
     * New instance of PlotMe plugin
     *
     * @param plugin
     */
    public PlotMeFix(JavaPlugin plugin) {
        m_isEnabled = false;
        if (ConfigProvider.isPlotMeFixEnabled()) {
            try {
                Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("PlotMe");

                if ((cPlugin != null) && (cPlugin instanceof PlotMe)) {
                    m_isEnabled = true;

                    m_instance = (PlotMe) cPlugin;
                }
            } catch (NoClassDefFoundError ex) {
            }

            if (m_isEnabled) {
                if (Initialize()) {
                    AsyncWorldEditMain.log("PlotMe fix enabled.");
                } else {
                    AsyncWorldEditMain.log("Error initializing PlotMe fix, unsupported version?.");
                }
            }
        }
    }

    public void setMask(Player p) {
        if (!m_isEnabled || p == null) {
            return;
        }

        if (PlotManager.isPlotWorld(p)) {
            setMask(!PlotMe.isIgnoringWELimit(p), p);
        }
    }

    private boolean Initialize() {
        Class<?> plotMeClass = m_instance.getClass();
        Class<?> plotWorldEditClass;

        try {
            m_plotWorldEditField = plotMeClass.getField("plotworldedit");
        } catch (NoSuchFieldException ex) {
            m_plotWorldEditField = null;
        } catch (SecurityException ex) {
            m_plotWorldEditField = null;
        }
        
        try {
            plotWorldEditClass = Class.forName("com.worldcretornica.plotme.worldedit.PlotWorldEdit");
        } catch (ClassNotFoundException ex) {
            try {
                plotWorldEditClass = Class.forName("com.worldcretornica.plotme.PlotWorldEdit");
            } catch (ClassNotFoundException ex1) {
                return false;
            }
        }
        
        try {
            m_setMaskMethod = plotWorldEditClass.getMethod("setMask", Player.class);
            m_removeMaskMethod = plotWorldEditClass.getMethod("removeMask", Player.class);
        } catch (NoSuchMethodException ex) {
            m_setMaskMethod = null;
            m_removeMaskMethod = null;

            return false;
        } catch (SecurityException ex) {
            m_setMaskMethod = null;
            m_removeMaskMethod = null;

            return false;
        }

        return true;
    }

    private void setMask(boolean enable, Player p) {
        try {
            Object plotWorldEdit = m_plotWorldEditField != null ? m_plotWorldEditField.get(null) : null;
            if (enable && m_setMaskMethod != null) {
                m_setMaskMethod.invoke(plotWorldEdit, p);
            } else if (!enable && m_removeMaskMethod != null) {
                m_removeMaskMethod.invoke(plotWorldEdit, p);
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(PlotMeFix.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(PlotMeFix.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(PlotMeFix.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
