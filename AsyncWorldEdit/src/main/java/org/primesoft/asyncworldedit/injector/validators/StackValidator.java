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
package org.primesoft.asyncworldedit.injector.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
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
        new StackValidatorEntry(".*sk89q.*BrushTool", new String[]{"actPrimary"}, "") {
            @Override
            public String getOperationName(String name) {
                return "brush";
            }
        },
        new StackValidatorEntry(".*sk89q.*SelectionCommand", new String[]{"call"}, "") {
            @Override
            public String getOperationName(String name) {
                return "setBlocks";
            }
        },
        new StackValidatorEntry(".*primesoft.*excommands.*ClipboardCommands", new String[]{"copy", "paste", "cut"}, ""),
        new StackValidatorEntry(".*primesoft.*excommands.*SchematicCommands", "", ".*"),
        new StackValidatorEntry(".*primesoft.*excommands.*RegionCommands", new String[]{"replaceBlocks", "stack", "move"}, "") {
            @Override
            public String getOperationName(String name) {
                if (name.equalsIgnoreCase("stack")) {
                    return "stackCuboidRegion";
                }
                else if (name.equalsIgnoreCase("move")) {
                    return "moveRegion";
                }
                
                return super.getOperationName(name);
            }           
        },
        new StackValidatorEntry(".*primesoft.*ThreadSafeEditSession", "", ".*"),
        new StackValidatorEntry(".*primesoft.*AsyncEditSessionFactory.*", "", ".*"),
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
                log("****************************************************************");
                log("* Validating stack trace");
                log("****************************************************************");
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
                log("****************************************************************");

            }
        }
    }

    /**
     * Validate the stack trace position
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
                    log(String.format("* %1$s", element.toString()));
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
                                log("*");
                                log("* Found on blacklist");
                                log(String.format("* Class:\t\t%1$s", element.getClassName()));
                                log(String.format("* Method:\t\t%1$s", name));
                                log(String.format("* Class pattern:\t%1$s", entry.getClassPattern().pattern()));
                                log(String.format("* Method pattern:\t%1$s", pattern.pattern()));
                            }
                            return false;
                        }
                    }

                    for (Pattern pattern : entry.getMethodWhiteList()) {
                        m = pattern.matcher(name);
                        if (m.matches()) {
                            methodName.setValue(entry.getOperationName(name));
                            if (debugOn) {
                                log("*");
                                log("* Found on whitelist");
                                log(String.format("* Class:\t\t%1$s", element.getClassName()));
                                log(String.format("* Method:\t\t%1$s", name));
                                log(String.format("* Class pattern:\t%1$s", entry.getClassPattern().pattern()));
                                log(String.format("* Method pattern:\t%1$s", pattern.pattern()));
                            }
                            return true;
                        }
                    }
                }
            }

            if (debugOn) {
                log("*");
                log("* No match found");
            }
            return false;
        } finally {
            if (debugOn) {
                log("*");
                i--;
                for (; i >= 0; i--) {
                    StackTraceElement element = stackTrace[i];
                    log(String.format("* %1$s", element.toString()));
                }
                log("*");
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
                log(String.format("* %1$s --> %2$s", p.pattern(), cnt));
            } else if (!result) {
                return false;
            }
        }

        return result;
    }
}
