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
package org.primesoft.asyncworldedit.injector;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.function.operation.Operations;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author SBPrime
 */
public class InjectorMain extends JavaPlugin {

    private static final Logger s_log = Logger.getLogger("Minecraft.AWE");
    private static String s_prefix = null;
    private static final String s_logFormat = "%s %s";

    /**
     * The WorldEdit class factory
     */
    private IClassFactory m_classFactory = new BaseClassFactory();

    /**
     * Instance of the main class
     */
    private static InjectorMain s_instance;

    /**
     * Get plugin instance
     *
     * @return
     */
    public static InjectorMain getInstance() {
        return s_instance;
    }

    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    /**
     * Set new class factory
     *
     * @param factory
     */
    public void setClassFactory(IClassFactory factory) {
        if (factory == null) {
            factory = new BaseClassFactory();
            log("New class factory set to default factory.");
        } else {
            log("New class factory set to: " + factory.getClass().getName());
        }

        m_classFactory = factory;
    }

    /**
     * Get the class factory
     *
     * @return
     */
    public IClassFactory getClassFactory() {
        return m_classFactory;
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());

        EditSession.ForceClassLoad();        
        Operations.ForceClassLoad();
        SchematicCommands.ForceClassLoad();

        s_instance = this;
        
        log("Enabled");
    }

    @Override
    public void onDisable() {

        s_instance = null;

        log("Disabled");
    }
}
