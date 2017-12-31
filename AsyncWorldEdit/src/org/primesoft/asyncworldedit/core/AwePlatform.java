/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
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
package org.primesoft.asyncworldedit.core;

import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.platform.api.IPlatform;

/**
 *
 * @author SBPrime
 */
public class AwePlatform {

    /**
     * The platform instance
     */
    private final static AwePlatform s_instance = new AwePlatform();

    /**
     * Get the platform instance
     *
     * @return
     */
    public static AwePlatform getInstance() {
        return s_instance;
    }

    /**
     * The MTA mutex
     */
    private final Object m_mutex = new Object();

    /**
     * The platform
     */
    private IPlatform m_platform = null;
    
    /**
     * The awe core
     */
    private IAsyncWorldEditCore m_aweCore = null;

    /**
     * Hide the constructor
     */
    private AwePlatform() {
    }

    public void initialize(IAsyncWorldEditCore aweCore) {
        setCore(aweCore);
        setPlatform(aweCore != null ? aweCore.getPlatform() : null);
    }
    
    /**
     * Set current platform
     *
     * @param platform
     */
    private void setPlatform(IPlatform platform) {
        if (platform == null) {
            return;
        }
        
        synchronized (m_mutex) {
            if (m_platform != null) {
                log(String.format("Platform is already set to %1$s. Ignoring platform %2$s", m_platform.getName(), platform.getName()));
                return;
            }
            m_platform = platform;
        }
    }   
    
    /**
     * Get the current platform
     * @return 
     */
    public IPlatform getPlatform() {
        return m_platform;
    }
    
    
    /**
     * Set current core
     *
     * @param core
     */
    private void setCore(IAsyncWorldEditCore core) {
        if (core == null) {
            return;
        }
        
        synchronized (m_mutex) {
            if (m_aweCore != null) {
                log(String.format("Core is already set to %1$s. Ignoring new core %2$s", m_aweCore.getClass().getName(), core.getClass().getName()));
                return;
            }
            m_aweCore = core;
        }
    }   
    
    /**
     * Get the current core
     * @return 
     */
    public IAsyncWorldEditCore getCore() {
        return m_aweCore;
    }
}
