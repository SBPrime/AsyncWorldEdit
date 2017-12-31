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
package org.primesoft.asyncworldedit.worldedit.history.changeset;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SBPrime
 */
public class FileChangeSetManager {

    private static final List<FileChangeSet> s_fileSavers = new LinkedList<FileChangeSet>();
    private static boolean s_isRunning = false;
    private static final Object s_waitMutex = new Object();

    /**
     * Start the file saver thread
     *
     * @param changeSet
     */
    static void start(FileChangeSet changeSet) {
        boolean isRunning;
        synchronized (s_fileSavers) {
            s_fileSavers.add(changeSet);

            isRunning = s_isRunning;
            s_isRunning = true;
        }

        if (isRunning) {
            synchronized (s_waitMutex) {
                s_waitMutex.notifyAll();
            }
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                saveLoop();
            }
        }).start();
    }

    /**
     * Stop the file saver thread
     *
     * @param changeSet
     */
    static void stop(FileChangeSet changeSet) {
        synchronized (s_fileSavers) {
            s_fileSavers.remove(changeSet);

            if (!s_fileSavers.isEmpty()) {
                return;
            }

            s_isRunning = false;
        }

        synchronized (s_waitMutex) {
            s_waitMutex.notifyAll();
        }
    }

    /**
     * The change set save loop
     */
    private static void saveLoop() {
        while (s_isRunning) {
            boolean dataSaved;
            do {
                dataSaved = false;
                final FileChangeSet[] changeSets;
                synchronized (s_fileSavers) {
                    changeSets = s_fileSavers.toArray(new FileChangeSet[0]);
                }

                for (FileChangeSet changeSet : changeSets) {
                    boolean result = changeSet.save();
                    if (result) {
                        try {
                            /**
                             * HACK: Sleep is added to prevent IO overflow
                             */
                            Thread.sleep(5);
                        } catch (InterruptedException ex) {
                        }
                    }
                    dataSaved |= result;
                }
            } while (dataSaved);

            synchronized (s_waitMutex) {
                try {
                    s_waitMutex.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    static void notiffy() {
        synchronized (s_waitMutex) {
            s_waitMutex.notifyAll();
        }
    }

}
