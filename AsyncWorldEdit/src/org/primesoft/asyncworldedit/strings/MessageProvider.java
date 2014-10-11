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
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
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
     * Load the message file
     *
     * @param file
     * @return
     */
    public static boolean loadFile(String file) {
        s_messages.clear();
        if (file == null || file.length() == 0) {
            return false;
        }

        File f = new File(ConfigProvider.getPluginFolder(), file);
        if (!f.exists() || !f.canRead()) {
            return false;
        }

        Configuration strings = YamlConfiguration.loadConfiguration(f);
        if (strings == null) {
            return false;
        }

        synchronized (s_messages) {
            for (String s : strings.getKeys(false)) {
                s_messages.put(s.toLowerCase(), format(strings.get(s).toString()));
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
            if (start > 0 && t.charAt(start - 1) != '\\') {
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
     * @param s
     * @return 
     */
    private static String getColor(String s) {
        try {
            return ChatColor.valueOf(s).toString();
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
            message = messageType.getDefault();
        }

        if (message == null) {
            return "";
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
