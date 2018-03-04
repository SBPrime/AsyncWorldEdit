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
package org.primesoft.asyncworldedit.configuration;

import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.platform.api.IConfigurationSection;

/**
 *
 * @author SBPrime
 */
public class ConfigUndo {

    private final UndoBehaviour m_undoMain;
    private final UndoBehaviour m_undoAsync;
    private final UndoBehaviour m_undoLoad;
    private final boolean m_storeOnDisk;
    private final int m_keepSessionOnLogoutFor;
    private final int m_keepUndoFileFor;
    private final int m_undoFileCleanupInterval;

    public UndoBehaviour getMainBehaviour() {
        return m_undoMain;
    }

    public UndoBehaviour getAsyncBehaviour() {
        return m_undoAsync;
    }
    
    public UndoBehaviour getStorageBehaviour() {
        return m_undoLoad;
    }
        
    public boolean storeOnDisk() {
        return m_storeOnDisk;
    }
    
    public int keepSessionOnLogoutFor() {
        return m_keepSessionOnLogoutFor;
    }
    
    public int undoFileCleanupInterval() {
        return m_undoFileCleanupInterval;
    }
    
    public int keepUndoFileFor() {
        return m_keepUndoFileFor;
    }


    ConfigUndo(IConfigurationSection secUndo) {
        if (secUndo == null) {
            m_undoAsync = UndoBehaviour.Wait;
            m_undoMain = UndoBehaviour.Cancel;
            m_undoLoad = UndoBehaviour.Wait;
            m_storeOnDisk = true;
            m_keepSessionOnLogoutFor = 0;
            m_undoFileCleanupInterval = 30;
            m_keepUndoFileFor = 0;
        } else {
            m_storeOnDisk = secUndo.getBoolean("storeOnDisk", true);
            m_keepSessionOnLogoutFor = secUndo.getInt("keepSessionOnLogoutFor", 0);
            m_undoFileCleanupInterval = secUndo.getInt("undoFileCleanupInterval", 30);
            m_keepUndoFileFor = secUndo.getInt("keepUndoFileFor", 0);
            
            m_undoAsync = parse(secUndo.getString("memoryLow", null), UndoBehaviour.Wait);
            UndoBehaviour uLoad = parse(secUndo.getString("memoryLowStorage", null), UndoBehaviour.Wait);
            if (uLoad == UndoBehaviour.Cancel) {
                log("ERROR: Cancel behaviour is not allowed for loader");
                uLoad = UndoBehaviour.Cancel;
            }
            m_undoLoad = uLoad;
            
            UndoBehaviour uMain = parse(secUndo.getString("memoryLowMain", null), UndoBehaviour.Cancel);
            if (uMain == UndoBehaviour.Wait) {
                log("ERROR: Wait behaviour is not allowed for main thread undo queue");
                uMain = UndoBehaviour.Cancel;
            } else if (uMain == UndoBehaviour.Off) {
                log("WARNING: Memory monitoring is off for main thread undo queue");
            }
            if (m_undoAsync == UndoBehaviour.Off) {
                log("WARNING: Memory monitoring is off for undo queue");
            }

            m_undoMain = uMain;
        }
    }

    private UndoBehaviour parse(String value, UndoBehaviour defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        for (UndoBehaviour i : UndoBehaviour.values()) {
            if (i.name().equalsIgnoreCase(value)) {
                return i;
            }
        }
        
        return defaultValue;
    }
}
