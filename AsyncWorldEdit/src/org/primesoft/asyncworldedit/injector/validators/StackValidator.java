/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
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
package org.primesoft.asyncworldedit.injector.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        new StackValidatorEntry(".*sk89q.*RegionCommands", new String[]{"forest", "flora"}, ""),
        new StackValidatorEntry(".*sk89q.*BiomeCommands", new String[]{"setBiome"}, ""),
        new StackValidatorEntry(".*primesoft.*ThreadSafeEditSession", "", ".*"),
        new StackValidatorEntry(".*primesoft.*AsyncEditSession.*", ".*", new String[]{
            "undo", "redo", "task", "flushQueue"
        })
    };

    /**
     * Does the stack trace allow asyncing
     *
     * @param methodName
     * @return
     */
    public static boolean isVaild(InOutParam<String> methodName) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        System.out.println("Stack trace:");
        for (int i = stackTrace.length - 1; i >= 0; i--) {
            StackTraceElement element = stackTrace[i];
            System.out.println(" * " + element.getClassName() + "\t" + element.getMethodName() + "\t" + element.getLineNumber());

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
                        System.out.println("* on blacklist");
                        return false;
                    }
                }

                for (Pattern pattern : entry.getMethodWhiteList()) {
                    m = pattern.matcher(name);
                    if (m.matches()) {
                        System.out.println("* on whitelist");

                        methodName.setValue(name);
                        return true;
                    }
                }
            }
        }

        System.out.println("* No match");
        return false;
    }
}
