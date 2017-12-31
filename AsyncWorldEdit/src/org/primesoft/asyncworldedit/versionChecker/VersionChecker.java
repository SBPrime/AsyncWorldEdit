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
package org.primesoft.asyncworldedit.versionChecker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.IntUtils;

/**
 * Version checker class (Spigot)
 *
 * @author SBPrime
 */
public class VersionChecker {
    /**
     * Spigot API key
     */
    private static final String API_KEY = "98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4";

    /**
     * AWE plugin ID
     */
    private static final int PLUGIN_ID = 9661;

    /**
     * Spigo API url
     */
    private static final String URL = "http://www.spigotmc.org/api/general.php";

    /**
     * The API function
     */
    private static final String DATA = "key=%s&resource=%s";

    /**
     * Get the latest plugin version
     * @return 
     */
    private static String getVersion() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.getOutputStream().write(String.format(DATA, API_KEY, PLUGIN_ID).getBytes("UTF-8"));
            return new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        } catch (Exception ex) {
            ExceptionHelper.printException(ex, "Unable to check AWE version");
        }

        return null;
    }

    /**
     * Check if the version is up to date
     *
     * @param localVersion Current plugin version
     * @return Version comperation answer
     */
    public static VersionCheckResult CheckVersion(String localVersion) {
        String remoteVersion = getVersion();

        if (remoteVersion == null || remoteVersion.isEmpty()) {
            return VersionCheckResult.error();
        }

        Integer[] iLocalVersion = parseVersion(localVersion);
        Integer[] iRemoteVersion = parseVersion(remoteVersion);

        int local, remote;
        for (int i = 0; i < Math.max(iLocalVersion.length, iRemoteVersion.length); i++) {
            local = i < iLocalVersion.length ? iLocalVersion[i] : -1;
            remote = i < iRemoteVersion.length ? iRemoteVersion[i] : -1;

            if (local < remote) {
                return VersionCheckResult.old(localVersion, remoteVersion);
            } else if (local > remote) {
                return VersionCheckResult.unknown(localVersion);
            }
        }

        return VersionCheckResult.latest();
    }

    /**
     * Try to parse the plugin version to parts
     * @param localVersion
     * @return 
     */
    private static Integer[] parseVersion(String localVersion) {
        if (localVersion == null || localVersion.isEmpty()) {
            return new Integer[0];
        }

        final Pattern pattern = Pattern.compile("([0-9.]+)");
        Matcher m = pattern.matcher(localVersion);

        if (!m.find()) {
            return new Integer[0];
        }

        String[] parts = m.group(0).split("\\.");
        List<Integer> result = new ArrayList<Integer>();
        
        for (String p : parts) {
            InOutParam<Integer> out = InOutParam.Out();
            if (!IntUtils.TryParseInteger(p, out)) {
                break;
            }
            
            result.add(out.getValue());
        }
        
        return result.toArray(new Integer[0]);
    }
}
