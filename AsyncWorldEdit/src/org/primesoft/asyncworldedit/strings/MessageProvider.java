/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.Pair;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author SBPrime
 */
public abstract class MessageProvider {
    /**
     * Color entry pattern
     */
    private final static Pattern PATTERN_COLLOR = Pattern.compile("\\[[A-Z_]+\\]");

    /**
     * The loaded texts
     */
    public final HashMap<String, String> m_messages = new HashMap<String, String>();

    /**
     * The loaded default texts
     */
    public final HashMap<String, String> m_default = new HashMap<String, String>();

    protected MessageProvider() {
        MessageType.initializeMessageProvider(this);
    }

    /**
     * Save english.yml to plugins folder
     *
     * @return
     */
    public static boolean saveDefault() {
        File pluginFolder = ConfigProvider.getPluginFolder();

        if (!pluginFolder.canRead()) {
            return false;
        }

        File english = new File(pluginFolder, "english.yml");

        if (english.exists()) {
            return true;
        }

        if (!pluginFolder.canWrite()) {
            return false;
        }

        InputStream input = null;
        FileOutputStream output = null;

        try {
            if (!english.createNewFile()) {
                return false;
            }

            input = MessageProvider.class.getResourceAsStream("/english.yml");
            if (input == null) {
                return false;
            }

            output = new FileOutputStream(english.getAbsoluteFile());

            byte[] buf = new byte[4096];
            int readBytes = 0;

            while ((readBytes = input.read(buf)) > 0) {
                output.write(buf, 0, readBytes);
            }
        } catch (IOException ex) {
            ExceptionHelper.printException(ex, "Unable to extract default strings file.");
            return false;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    //Ignore close error
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                    //Ignore close error
                }
            }
        }
        return true;
    }

    /**
     * Initialise the default strings
     *
     * @return
     */
    public boolean loadDefault() {
        InputStream is = null;
        try {
            is = MessageProvider.class.getResourceAsStream("/english.yml");
            if (is == null) {
                return false;
            }
            return loadFile(is, m_default);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    //Ignore close error
                }
            }
        }
    }

    /**
     * Load the message file
     *
     * @param file
     * @return
     */
    public boolean loadFile(String file) {
        if (file == null || file.length() == 0) {
            return false;
        }
        File f = new File(ConfigProvider.getPluginFolder(), file);
        if (!f.exists() || !f.canRead()) {
            return false;
        }

        InputStream is = null;
        try {
            is = new FileInputStream(f);

            return loadFile(is, m_messages);
        } catch (IOException ex) {
            ExceptionHelper.printException(ex, "Unable to load strings file.");
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    //Ignore close error
                }
            }
        }
    }

    /**
     * Load the message file
     *
     * @param file
     * @param messages
     * @return
     */
    private boolean loadFile(InputStream f, HashMap<String, String> messages) {
        if (f == null || messages == null) {
            return false;
        }
        messages.clear();

        Yaml yaml = new Yaml();

        Object o = yaml.load(f);
        Map data = (Map)(o instanceof Map ? o : null);
        if (data == null) {
            return false;
        }
        for (Object key : data.keySet()) {
            Object value = data.get(key);
            
            if (value instanceof Map || value == null) {
                continue;
            }
            
            messages.put(key.toString().toLowerCase(), format(value.toString()));
        }
        
        return true;
    }

    /**
     * Format the message
     *
     * @param t
     * @return
     */
    private String format(String t) {
        if (t == null) {
            return "";
        }

        Matcher m = PATTERN_COLLOR.matcher(t);
        Stack<Pair<Integer, Integer>> entries = new Stack<Pair<Integer, Integer>>();
        while (m.find()) {
            int start = m.start();
            if (start == 0 || t.charAt(start - 1) != '\\') {
                entries.push(new Pair<Integer, Integer>(start, m.end()));
            }
        }

        String result = "";

        while (!entries.empty()) {
            Pair<Integer, Integer> entry = entries.pop();
            int from = entry.getX1();
            int to = entry.getX2();

            String s = t.substring(from, to);
            result = getColor(s) + t.substring(to) + result;
            t = from > 0 ? t.substring(0, from) : "";
        }

        return (t + result).replace("\\[", "[")
                .replaceFirst("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\", "\\");
    }

    /**
     * Get chat color code from string
     *
     * @param s
     * @return
     */
    protected abstract String getColor(String s);

    /**
     * Format the message
     *
     * @param messageType
     * @param params
     * @return
     */
    public String formatMessage(MessageType messageType, Object... params) {
        if (messageType == null) {
            return "";
        }

        String key = messageType.getKey().toLowerCase();
        String message;
        synchronized (m_messages) {
            message = m_messages.get(key);
        }
        if (message == null) {
            synchronized (m_default) {
                message = m_default.get(key);
            }
        }

        if (message == null) {
            return key;
        }

        try {
            return String.format(message, params);
        } catch (IllegalFormatException ex) {
            ExceptionHelper.printException(ex, String.format("Unable to format message: %1$s", messageType.name()));
            return "";
        } catch (NullPointerException ex) {
            return "";
        }
    }
}
