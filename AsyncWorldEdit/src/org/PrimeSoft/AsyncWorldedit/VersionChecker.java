/*
 * The MIT License
 *
 * Copyright 2012 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.PrimeSoft.AsyncWorldedit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Version checker class
 * @author SBPrime
 */
public class VersionChecker {
    /**
     * Version url
     */
    private final static String s_versionUrl = "http://dev.bukkit.org/server-mods/async_worldedit/pages/version/";

    
    /**
     * Download version page from the www
     * @param url Version file http page
     * @return Version page content
     */
    private static String downlaodPage(String url) {
        try {
            InputStreamReader is = new InputStreamReader(new URL(url).openStream());
            BufferedReader br = new BufferedReader(is);
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            return sb.toString();
        } catch (IOException e) {
            PluginMain.Log("Error downloading file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if the version is up to date
     * @param version Current plugin version
     * @return Version comperation answer
     */
    public static String CheckVersion(String version) {
        String content = downlaodPage(s_versionUrl);

        if (content == null || content.isEmpty()) {
            return "Unable to check latest plugin version.";
        }

        String eVersion = content.replaceAll("(.*>VERSION:[\t ]*)([^<]+)(<.*)", "$2");

        int cmp = version.compareToIgnoreCase(eVersion);
        if (cmp < 0) {
            return "You have an old version of the plugin. Your version: " + version
                    + ", available version: " + eVersion;
        } else if (cmp > 0) {
            return "You have a newer version of the plugin then available! Your version: " + version
                    + ", available version: " + eVersion;
        } else {
            return "You have the latest version of the plugin.";
        }
    }
}