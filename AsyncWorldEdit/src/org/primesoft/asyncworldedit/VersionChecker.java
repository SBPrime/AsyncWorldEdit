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
package org.primesoft.asyncworldedit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.primesoft.asyncworldedit.strings.MessageType;

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
            return MessageType.CHECK_VERSION_ERROR.format();
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
                        return MessageType.CHECK_VERSION_OLD.format(version, vLatest);
                    } else {
                        return MessageType.CHECK_VERSION_LATEST.format();
                    }
                }
            }
        }

        return MessageType.CHECK_VERSION_UNKNOWN.format(version);
    }
}