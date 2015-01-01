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
package org.primesoft.asyncworldedit.injector.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.utils.InOutParam;

/**
 * Validateif operation call stack allows asyncing of the operation
 *
 * @author SBPrime
 */
public class StackValidator {

    /**
     * Operations entry
     */
    private static final StackValidatorEntry[] s_data = new StackValidatorEntry[]{
        new StackValidatorEntry(".*sk89q.*EditSession", "", ".*"),
        new StackValidatorEntry(".*sk89q.*ClipboardCommands", new String[]{"copy", "paste", "cut"}, ""),
        new StackValidatorEntry(".*sk89q.*SchematicCommands", "", ".*"),
        new StackValidatorEntry(".*sk89q.*RegionCommands", new String[]{"forest", "flora"}, ""),
        new StackValidatorEntry(".*sk89q.*BiomeCommands", new String[]{"setBiome"}, ""),
        new StackValidatorEntry(".*primesoft.*ThreadSafeEditSession", "", ".*"),
        new StackValidatorEntry(".*primesoft.*AsyncEditSession.*", ".*", new String[]{
            "undo", "redo", "task", "flushQueue"
        })
    };

    /**
     * Elements that should
     */
    private static final Pattern[] s_countPatterns = new Pattern[]{
        Pattern.compile(".*asyncworldedit.*AsyncOperationProcessor.*")
    };

    /**
     * Does the stack trace allow asyncing
     *
     * @param methodName
     * @return
     */
    public static boolean isVaild(InOutParam<String> methodName) {
        final boolean debugOn = ConfigProvider.isDebugOn();
        try {
            if (debugOn) {
                AsyncWorldEditMain.log("****************************************************************");
                AsyncWorldEditMain.log("* Validating stack trace");
                AsyncWorldEditMain.log("****************************************************************");
            }

            if (!validateStack(methodName)) {
                return false;
            }
            if (!validateCount()) {
                return false;
            }

            return true;
        } finally {
            if (debugOn) {
                AsyncWorldEditMain.log("****************************************************************");

            }
        }
    }

    /**
     * Validate the stack trace possition
     *
     * @param methodName
     * @return
     */
    private static boolean validateStack(InOutParam<String> methodName) {
        final boolean debugOn = ConfigProvider.isDebugOn();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int i = stackTrace.length - 1;
        try {
            for (; i >= 0; i--) {
                StackTraceElement element = stackTrace[i];
                if (debugOn) {
                    AsyncWorldEditMain.log("* " + element.toString());
                }

                for (StackValidatorEntry entry : s_data) {
                    Matcher m = entry.getClassPattern().matcher(element.getClassName());
                    if (!m.matches()) {
                        //No class match
                        continue;
                    }

                    String name = element.getMethodName();
                    for (Pattern pattern : entry.getMethodBlackList()) {
                        m = pattern.matcher(name);
                        if (m.matches()) {
                            if (debugOn) {
                                AsyncWorldEditMain.log("*");
                                AsyncWorldEditMain.log("* Found on blacklist");
                                AsyncWorldEditMain.log("* Class:\t\t" + element.getClassName());
                                AsyncWorldEditMain.log("* Method:\t\t" + name);
                                AsyncWorldEditMain.log("* Class pattern:\t" + entry.getClassPattern().pattern());
                                AsyncWorldEditMain.log("* Method pattern:\t" + pattern.pattern());
                            }
                            return false;
                        }
                    }

                    for (Pattern pattern : entry.getMethodWhiteList()) {
                        m = pattern.matcher(name);
                        if (m.matches()) {
                            methodName.setValue(name);
                            if (debugOn) {
                                AsyncWorldEditMain.log("*");
                                AsyncWorldEditMain.log("* Found on whitelist");
                                AsyncWorldEditMain.log("* Class:\t\t" + element.getClassName());
                                AsyncWorldEditMain.log("* Method:\t\t" + name);
                                AsyncWorldEditMain.log("* Class pattern:\t" + entry.getClassPattern().pattern());
                                AsyncWorldEditMain.log("* Method pattern:\t" + pattern.pattern());
                            }
                            return true;
                        }
                    }
                }
            }

            if (debugOn) {
                AsyncWorldEditMain.log("*");
                AsyncWorldEditMain.log("* No match found");
            }
            return false;
        } finally {
            if (debugOn) {
                AsyncWorldEditMain.log("*");
                i--;
                for (; i >= 0; i--) {
                    StackTraceElement element = stackTrace[i];
                    AsyncWorldEditMain.log("* " + element.toString());
                }
                AsyncWorldEditMain.log("*");
            }
        }
    }

    /**
     * Validate stack entry count
     *
     * @return
     */
    private static boolean validateCount() {
        final boolean debugOn = ConfigProvider.isDebugOn();
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        boolean result = true;

        for (Pattern p : s_countPatterns) {
            int cnt = 0;
            for (StackTraceElement stack : stackTrace) {
                if (p.matcher(stack.toString()).matches()) {
                    cnt++;
                }
            }

            result &= cnt < 2;
            if (debugOn) {
                AsyncWorldEditMain.log("* " + p.pattern() + " --> " + cnt);
            } else if (!result) {
                return false;
            }
        }

        return result;
    }
}
