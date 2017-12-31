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
package org.primesoft.asyncworldedit.changesetSerializer;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;

/**
 *
 * @author SBPrime
 */
public class StreamProvider {

    /**
     * The stream description
     */
    private final class StreamDescription {

        /**
         * The file
         */
        private final File m_file;

        /**
         * Remove the stream when reference count reaches 0
         */
        private final boolean m_removeOnClean;

        /**
         * The number of references
         */
        private int m_referenceCount;

        /**
         * Is the stream removed
         */
        private boolean m_isRemoved = false;

        /**
         * Is the stream handler disposed
         */
        private boolean m_isDisposed = false;

        /**
         * The MTA mutex
         */
        private final Object m_mutex = new Object();

        public StreamDescription(File file) {
            this(file, false);
        }

        public StreamDescription(File file, boolean removeOnClean) {
            m_file = file;
            m_removeOnClean = removeOnClean;
            m_referenceCount = 0;
        }

        /**
         * Reserve new reference
         *
         * @return
         */
        public int reserve() {
            synchronized (m_mutex) {
                if (m_isRemoved) {
                    return -1;
                }

                m_referenceCount++;
                return m_referenceCount;
            }
        }

        /**
         * Release reference. If the reference count reaches 0 and clean is set
         * to true the file gets removed
         *
         * @return the number of references or -1 if the file is disposed
         */
        public int release() {
            synchronized (m_mutex) {
                if (m_isRemoved) {
                    return -1;
                }

                m_referenceCount--;

                if (m_referenceCount > 0) {
                    return m_referenceCount;
                }

                if (!m_isDisposed) {
                    return 0;
                }

                removeFile();

                return -1;
            }
        }

        /**
         * Remove the file
         */
        private void removeFile() {
            if (m_removeOnClean) {                
                if (m_file.exists() && !m_file.delete()) {
                    log(String.format("Error removing file %1$s", m_file));
                } else {
                    m_isRemoved = true;
                }
            } else {
                m_isRemoved = true;
            }
        }

        /**
         * Dispose the file stream
         */
        public boolean dispose() {
            synchronized (m_mutex) {
                m_isDisposed = true;

                if (m_referenceCount > 0) {
                    return false;
                }

                if (!m_isRemoved) {
                    removeFile();
                }
                return true;
            }
        }
    }

    /**
     * Maximum number of opened files
     */
    private final static int MAX_FILES = 10;

    /**
     * The instance of StreamProvider
     */
    private final static StreamProvider s_instance = new StreamProvider();

    /**
     * Get the instance of stream provider
     *
     * @return
     */
    public static StreamProvider getInstance() {
        return s_instance;
    }

    /**
     * The MTA access mutex
     */
    private final Object m_streamCountMutex = new Object();

    /**
     * Number of opened files
     */
    private int m_streamCount = 0;

    /**
     * The stream reference counter
     */
    private final HashMap<File, StreamDescription> m_streamReferences = new LinkedHashMap<File, StreamDescription>();

    /**
     * Release file
     *
     */
    public void release() {
        synchronized (m_streamCountMutex) {
            m_streamCount = Math.max(0, m_streamCount - 1);
            m_streamCountMutex.notifyAll();
        }
    }

    /**
     * Reserve file handler
     *
     */
    public void reserve() {
        synchronized (m_streamCountMutex) {
            while (m_streamCount >= MAX_FILES) {
                try {
                    m_streamCountMutex.wait(1000);
                } catch (InterruptedException ex) {
                }
            }

            m_streamCount++;
        }
    }

    /**
     * Initialize stream
     *
     * @param stream
     * @param remove Should the file be removed on dispose (after each
     * references are removed)
     * @return True if stram was initialized, False if stream reference already
     * exists
     */
    public boolean initializeStream(File stream, boolean remove) {
        boolean debug = ConfigProvider.isDebugOn();

        synchronized (m_streamReferences) {
            if (m_streamReferences.containsKey(stream)) {
                if (debug) {
                    log(String.format("StreamProvider: initialize stream failed, stream %1$s already initialized.", stream));
                }
                return false;
            }

            StreamDescription sd = new StreamDescription(stream, remove);

            m_streamReferences.put(stream, sd);

            if (debug) {
                log(String.format("StreamProvider: initialize stream %1$s.", stream));
            }
            return true;
        }
    }

    /**
     * Dispose the stream
     *
     * @param stream
     * @return True if the stream was removed or the stream was not found False
     * if the stream still has references
     */
    public boolean disposeStream(File stream) {
        boolean debug = ConfigProvider.isDebugOn();

        synchronized (m_streamReferences) {
            StreamDescription sd = m_streamReferences.get(stream);
            if (sd == null) {
                if (debug) {
                    log(String.format("StreamProvider: unable to dispose stream, stream %1$s not found.", stream));
                }
                return true;
            }

            if (!sd.dispose()) {
                if (debug) {
                    log(String.format("StreamProvider: stream %1$s disposed.", stream));
                }
                return false;
            }

            if (debug) {
                log(String.format("StreamProvider: stream %1$s disposed and removed.", stream));
            }
            m_streamReferences.remove(stream);
            return true;
        }
    }

    /**
     * Add new reference to stream
     *
     * @param stream
     * @return False if stream is disposed
     */
    public boolean addReference(File stream) {
        boolean debug = ConfigProvider.isDebugOn();
        synchronized (m_streamReferences) {
            StreamDescription sd = m_streamReferences.get(stream);
            if (sd == null) {
                if (debug) {
                    log(String.format("StreamProvider: unable to add reference to stream %1$s, stream not found.", stream));
                }
                return false;
            }

            int cnt = sd.reserve();

            if (debug) {
                log(String.format("StreamProvider: added reference to stream %1$s, number of references: %2$s.", stream, cnt));
            }
            return true;
        }
    }

    /**
     * Remove reference from stream
     *
     * @param stream
     * @return True if stream is removed
     */
    public boolean removeReference(File stream) {
        boolean debug = ConfigProvider.isDebugOn();
        synchronized (m_streamReferences) {
            StreamDescription sd = m_streamReferences.get(stream);
            if (sd == null) {
                if (debug) {
                    log(String.format("StreamProvider: unable to remove reference from stream %1$s, stream not found.", stream));
                }

                return true;
            }

            int cnt = sd.release();
            if (cnt >= 0) {
                if (debug) {
                    log(String.format("StreamProvider: removed reference from stream %1$s, number of references: %1$s.", stream, cnt));
                }
                return false;
            }

            m_streamReferences.remove(stream);

            if (debug) {
                log(String.format("StreamProvider: removed reference from stream %1$s, stream released.", stream));
            }
            return true;
        }
    }
}
