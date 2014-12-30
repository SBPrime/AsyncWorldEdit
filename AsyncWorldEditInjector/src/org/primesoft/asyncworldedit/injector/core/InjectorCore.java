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
package org.primesoft.asyncworldedit.injector.core;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.command.ScriptingCommands;
import com.sk89q.worldedit.function.operation.Operations;
import org.primesoft.asyncworldedit.injector.classfactory.IClassFactory;
import org.primesoft.asyncworldedit.injector.classfactory.base.BaseClassFactory;

/**
 *
 * @author SBPrime
 */
public class InjectorCore {

    /**
     * Injector core instance
     */
    private static InjectorCore s_instance = null;

    /**
     * Instance MTA mutex
     */
    private final static Object s_mutex = new Object();

    /**
     * Get the static instance
     *
     * @return
     */
    public static InjectorCore getInstance() {
        if (s_instance == null) {
            synchronized (s_mutex) {
                if (s_instance == null) {
                    s_instance = new InjectorCore();
                }
            }
        }

        return s_instance;
    }

    /**
     * The platform specific API
     */
    private IInjectorPlatform m_platform;

    /**
     * The WorldEdit class factory
     */
    private IClassFactory m_classFactory = new BaseClassFactory();

    /**
     * The MTA access mutex
     */
    private final Object m_mutex = new Object();

    /**
     * Log a console message
     *
     * @param message
     */
    private void log(String message) {
        IInjectorPlatform platform = m_platform;
        if (platform == null) {
            return;
        }

        m_platform.log(message);
    }

    /**
     * Initialize the injector core
     *
     * @param platform
     */
    public void initialize(IInjectorPlatform platform) {
        synchronized (m_mutex) {
            if (m_platform != null) {
                log("Injector platform is already set to "
                        + m_platform.getPlatformName() + "."
                        + "Ignoring new platform " + platform.getPlatformName());
                return;
            }

            m_platform = platform;
            log("Injector platform set to: " + platform.getPlatformName());
        }

        log("Injecting WorldEdit classes...");
        try {
            log("Class " + EditSession.ForceClassLoad().getCanonicalName() + " injected.");
            log("Class " + Operations.ForceClassLoad().getCanonicalName() + " injected.");
            log("Class " + SchematicCommands.ForceClassLoad().getCanonicalName() + " injected.");
            log("Class " + ScriptingCommands.ForceClassLoad().getCanonicalName() + " injected.");
        } catch (Error ex) {
            log("****************************");
            log("* CLASS INJECTION FAILED!! *");
            log("****************************");
            log("* AsyncWorldEdit won't work properly.");
            log("* Exception: " + ex.getClass().getCanonicalName());
            log("* Error message: " +ex.getLocalizedMessage());
            log("* Stack:");
            for (StackTraceElement element : ex.getStackTrace())
            {
                log("* " + element.toString());
            }
            log("****************************");
        }
    }

    /**
     * Set new class factory
     *
     * @param factory
     */
    public void setClassFactory(IClassFactory factory) {
        synchronized (m_mutex) {
            if (factory == null) {
                factory = new BaseClassFactory();
                log("New class factory set to default factory.");
            } else {
                log("New class factory set to: " + factory.getClass().getName());
            }

            m_classFactory = factory;
        }
    }

    /**
     * Get the class factory
     *
     * @return
     */
    public IClassFactory getClassFactory() {
        return m_classFactory;
    }

}
