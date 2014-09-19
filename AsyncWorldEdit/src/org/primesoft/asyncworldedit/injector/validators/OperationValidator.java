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

import com.sk89q.worldedit.function.operation.Operation;
import java.util.regex.Pattern;

/**
 * Validate if operation should by asynced
 *
 * @author SBPrime
 */
public class OperationValidator {

    /**
     * The blacklisted operations regexp
     */
    private static final Pattern[] s_blackList;

    /**
     * The whitelisted operations regexp
     */
    private static final Pattern[] s_whiteList;

    static {
        //No operations are on the black list (for now!)
        s_blackList = new Pattern[]{};

        s_whiteList = new Pattern[]{
            Pattern.compile(".*") //All operations are on the whitelist
        };
    }

    /**
     * Is the operation enabled for asyncing
     *
     * @param op
     * @return
     */
    public static boolean isValid(Operation op) {
        Class c = op.getClass();
        String className = c.getCanonicalName();

        for (Pattern p : s_blackList) {
            if (p.matcher(className).matches()) {
                return false;
            }
        }

        for (Pattern p : s_whiteList) {
            if (p.matcher(className).matches()) {
                return true;
            }
        }

        return false;
    }
}
