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
package org.primesoft.asyncworldedit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Version checker class
 *
 * @author SBPrime
 */
public class VersionChecker {
    // Keys for extracting file information from JSON response

    private static final String API_NAME_VALUE = "name";
    /**
     * Version url
     */
    private final static String s_versionUrl = "https://api.github.com/repos/SBPrime/AsyncWorldEdit/releases";

    /**
     * Download version page from the www
     *
     * @param url Version file http page
     * @return Version page content
     */
    private static String downloadPage(String url) {
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
        } catch (Exception e) {
            AsyncWorldEditMain.log("Error downloading file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if the version is up to date
     *
     * @param version Current plugin version
     * @return Version comperation answer
     */
    public static String CheckVersion(String version) {
        String content = downloadPage(s_versionUrl);

        if (content == null || content.isEmpty()) {
            return "Unable to check latest plugin version.";
        }

        String eVersion = null;
        String vLatest = null;
        
        JSONArray array = (JSONArray) JSONValue.parse(content);
        if (array.size() > 0) {
            final int latestId = 0;
            for (int i = 0; i < array.size(); i++) {
                JSONObject jObject = (JSONObject) array.get(i);
                String versionName = (String) jObject.get(API_NAME_VALUE);
                String[] parts = versionName.split("[ \t-]");

                StringBuilder sb = new StringBuilder();
                for (int j = 1;j<parts.length;j++)
                {
                    if (j > 1)
                    {
                        sb.append("-");
                    }
                    sb.append(parts[j]);
                }
                eVersion = sb.toString();
                if (vLatest == null) {
                    vLatest = eVersion;
                }
                if (eVersion != null && eVersion.length() > 0 && version.equalsIgnoreCase(eVersion)) {
                    if (i != latestId) {
                        return "You have an old version of the plugin. Your version: " + version
                                + ", available version: " + vLatest;
                    } else {
                        return "You have the latest version of the plugin.";
                    }
                }
            }
        }

        return "Your version of the plugin was not found on the plugin page. Your version: " + version;
    }
}