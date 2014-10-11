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

import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
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

    public void setMask(PlayerEntry entry) {
        if (!m_isEnabled || entry == null || entry.getPlayer() == null) {
            return;
        }

        Player player = entry.getPlayer();
        if (PlotManager.isPlotWorld(player)) {
            setMask(!PlotMe.isIgnoringWELimit(player), player);
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
