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
package org.primesoft.asyncworldedit.plotme;

import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class PlotMeIntegrator implements IPlotMeIntegrator {

    /**
     * The PlotMe instance
     */
    private PlotMe m_instance;
    private Field m_plotWorldEditField;
    private Method m_setMaskMethod;
    private Method m_removeMaskMethod;

    @Override
    public void updateMask(Player player) {
        if (PlotManager.isPlotWorld(player)) {
            setMask(!PlotMe.isIgnoringWELimit(player), player);
        }
    }

    @Override
    public boolean initialize(Plugin instance) {
        try {
            m_instance = (PlotMe) instance;
            return initialize();
        } catch (Throwable ex) {
            ExceptionHelper.printException(ex, "Error initializing PlotMe integrator");
            return false;
        }
    }

    private boolean initialize() {
        m_plotWorldEditField = getPlotWorldEditField();

        final String[] classes = new String[]{
            "com.worldcretornica.plotme.worldedit.PlotWorldEdit", //PlotMe 0.13g
            "com.worldcretornica.plotme.PlotWorldEdit", //PlotMe 0.13f and earlier            
        };

        Class<?> plotWorldEditClass = null;
        for (String className : classes) {
            try {
                plotWorldEditClass = Class.forName(className);
                AsyncWorldEditMain.log("Found PlotMe at: " + className);
                break;
            } catch (NoClassDefFoundError ex) {
                ExceptionHelper.printException(ex, "Errot initializing " + className);
            } catch (ClassNotFoundException ex) {
            } catch (Throwable ex) {
                ExceptionHelper.printException(ex, "Errot initializing " + className);
            }
        }

        if (plotWorldEditClass != null) {
            try {
                m_setMaskMethod = plotWorldEditClass.getMethod("setMask", Player.class);
                m_removeMaskMethod = plotWorldEditClass.getMethod("removeMask", Player.class);
                return true;
            } catch (NoSuchMethodException ex) {
                ExceptionHelper.printException(ex, "Errot initializing ");
            } catch (SecurityException ex) {
                ExceptionHelper.printException(ex, "Errot initializing ");
            }
        }

        AsyncWorldEditMain.log("No supported version of PlotMe was found. PlotMe fix disabled.");
        m_setMaskMethod = null;
        m_removeMaskMethod = null;

        return false;
    }

    /**
     * Try to find the plotworldedit field
     *
     * @return
     */
    private Field getPlotWorldEditField() {
        Class<?> plotMeClass = m_instance.getClass();
        try {
            return plotMeClass.getField("plotworldedit");
        } catch (NoSuchFieldException ex) {
        } catch (SecurityException ex) {
        }

        return null;
    }

    /**
     * Inject the new mask
     *
     * @param enable
     * @param p
     */
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
