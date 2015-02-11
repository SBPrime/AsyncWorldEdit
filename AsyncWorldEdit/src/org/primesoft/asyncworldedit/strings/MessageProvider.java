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
package org.primesoft.asyncworldedit.strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.Pair;

/**
 *
 * @author SBPrime
 */
public class MessageProvider {

    /**
     * Color entry pattern
     */
    private final static Pattern PATTERN_COLLOR = Pattern.compile("\\[[A-Z_]+\\]");

    /**
     * The loaded texts
     */
    public final static HashMap<String, String> s_messages = new HashMap<String, String>();

    /**
     * The loaded default texts
     */
    public final static HashMap<String, String> s_default = new HashMap<String, String>();

    /**
     * Save english.yml to plugins folder
     *
     * @param plugin
     * @return
     */
    public static boolean saveDefault(JavaPlugin plugin) {
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

            input = plugin.getClass().getResourceAsStream("/english.yml");
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
    public static boolean loadDefault(JavaPlugin plugin) {
        InputStream is = null;
        try {
            is = plugin.getClass().getResourceAsStream("/english.yml");
            if (is == null) {
                return false;
            }
            return loadFile(is, s_default);
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
    public static boolean loadFile(String file) {
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

            return loadFile(is, s_messages);
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
    private static boolean loadFile(InputStream f, HashMap<String, String> messages) {
        if (f == null || messages == null) {
            return false;
        }
        messages.clear();

        Configuration strings = YamlConfiguration.loadConfiguration(f);
        if (strings == null) {
            return false;
        }

        synchronized (messages) {
            for (String s : strings.getKeys(false)) {
                messages.put(s.toLowerCase(), format(strings.get(s).toString()));
            }
        }
        return true;
    }

    /**
     * Format the message
     *
     * @param t
     * @return
     */
    private static String format(String t) {
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
    private static String getColor(String s) {
        if (s == null || s.length() < 2) {
            return "";
        }
        try {
            return ChatColor.valueOf(s.substring(1, s.length() - 1)).toString();
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    /**
     * Format the message
     *
     * @param messageType
     * @param params
     * @return
     */
    public static String formatMessage(MessageType messageType, Object... params) {
        if (messageType == null) {
            return "";
        }

        String key = messageType.getKey().toLowerCase();
        String message;
        synchronized (s_messages) {
            message = s_messages.get(key);
        }
        if (message == null) {
            synchronized (s_default) {
                message = s_default.get(key);
            }
        }
        
        if (message == null) {
            return key;
        }

        try {
            return String.format(message, params);
        } catch (IllegalFormatException ex) {
            return "";
        } catch (NullPointerException ex) {
            return "";
        }
    }
}
