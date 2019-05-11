/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2018, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.injector.core.visitors;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author SBPrime
 */
public abstract class BaseClassVisitor extends InjectorClassVisitor {

    @FunctionalInterface
    protected interface MethodFactory {

        void define(String name, String descriptor, String clsName, Method m);
    }

    protected static final Handle BOOTSTRAP_LAMBDA = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
            false);

    protected static final String RANDOM_PREFIX;

    protected static final String METHOD_CTOR = "<init>";
    protected static final int ACC_VISIBILITY_MASK
            = Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED;

    static {
        UUID uuid = UUID.randomUUID();
        RANDOM_PREFIX = String.format("__%016x%016x__", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    private final static Pattern ARGS_MATCHER = Pattern.compile("\\([^)]+\\)");

    protected static int getArgsCount(String desc) {
        Matcher m = ARGS_MATCHER.matcher(desc);
        if (!m.find()) {
            return 0;
        }

        return (int) m.group(0).chars()
                .filter(i -> i == ';')
                .count();
    }

    protected static String[] getArgs(String desc) {
        Matcher m = ARGS_MATCHER.matcher(desc);
        if (!m.find()) {
            return new String[0];
        }

        String args = m.group(0);
        args = args.substring(1, args.length() - 1);

        return args.split(";");
    }

    /**
     * Check if the method name is a constructor
     *
     * @param methodName
     * @return
     */
    protected static boolean isCtor(String methodName) {
        return METHOD_CTOR.equals(methodName);
    }

    protected static boolean isInternal(int access) {
        return (access & ACC_VISIBILITY_MASK) == 0;
    }

    protected static boolean isStatic(int access) {
        return checkFlag(access, Opcodes.ACC_STATIC);
    }

    protected static boolean isPublic(int access) {
        return checkFlag(access, Opcodes.ACC_PUBLIC);
    }

    protected static boolean checkFlag(int access, int flag) {
        return (access & flag) == flag;
    }

    protected static int changeVisibility(int access, int toAdd) {
        return (access & (~ACC_VISIBILITY_MASK)) | toAdd;
    }

    private final ICreateClass m_createClass;

    protected BaseClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor);

        m_createClass = null;
    }

    protected BaseClassVisitor(ClassVisitor classVisitor, ICreateClass createClass) {
        super(Opcodes.ASM6, classVisitor);

        m_createClass = createClass;
    }

    protected final void createClass(String name, ClassWriter cw) throws IOException {
        m_createClass.create(name, cw);
    }

    protected final void emitEmptyCtor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0); // push `this` to the operand stack
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    protected final void processMethods(MethodFactory mf, Class<?>... cls) throws SecurityException {
        Queue<Class<?>> clsInterfaces = new ArrayDeque<>();
        for (Class<?> c : cls) {
            clsInterfaces.add(c);
        }
        Set<String> definedMethods = new HashSet<>();
        while (!clsInterfaces.isEmpty()) {
            Class<?> clsInterface = clsInterfaces.poll();
            String clsName = Type.getInternalName(clsInterface);

            for (Method m : clsInterface.getDeclaredMethods()) {
                String descriptor = Type.getMethodDescriptor(m);
                String name = m.getName();
                if (definedMethods.contains(name + descriptor)) {
                    continue;
                }

                definedMethods.add(name + descriptor);
                mf.define(name, descriptor, clsName, m);
            }

            Class<?>[] newInterfaces = clsInterface.getInterfaces();
            if (newInterfaces != null) {
                for (Class<?> c : newInterfaces) {
                    clsInterfaces.add(c);
                }
            }
        }
    }

    protected final void visitReturn(MethodVisitor mv, Class<?> resultType) {
        if (void.class.equals(resultType)) {
            mv.visitInsn(Opcodes.RETURN);
        } else if (double.class.equals(resultType)) {
            mv.visitInsn(Opcodes.DRETURN);
        } else if (float.class.equals(resultType)) {
            mv.visitInsn(Opcodes.FRETURN);
        } else if (int.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (long.class.equals(resultType)) {
            mv.visitInsn(Opcodes.LRETURN);
        } else if (boolean.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (byte.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (char.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (short.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (resultType.isPrimitive()) {
            mv.visitInsn(Opcodes.IRETURN);
        } else {
            mv.visitInsn(Opcodes.ARETURN);
        }
    }

    protected final void visitArgumemt(MethodVisitor mv, Class<?> type, int id) {
        if (double.class.equals(type)) {
            mv.visitVarInsn(Opcodes.DLOAD, id);
        } else if (float.class.equals(type)) {
            mv.visitVarInsn(Opcodes.FLOAD, id);
        } else if (int.class.equals(type)) {
            mv.visitVarInsn(Opcodes.ILOAD, id);
        } else if (long.class.equals(type)) {
            mv.visitVarInsn(Opcodes.LLOAD, id);
        } else if (boolean.class.equals(type)) {
            mv.visitVarInsn(Opcodes.ILOAD, id);
        } else if (byte.class.equals(type)) {
            mv.visitVarInsn(Opcodes.ILOAD, id);
        } else if (char.class.equals(type)) {
            mv.visitVarInsn(Opcodes.ILOAD, id);
        } else if (short.class.equals(type)) {
            mv.visitVarInsn(Opcodes.ILOAD, id);
        } else if (type.isPrimitive()) {
            mv.visitVarInsn(Opcodes.ILOAD, id);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, id);
        }
    }

    protected final void checkCast(MethodVisitor mv, String type) {
        final String newType;
        final String metchod;
        final String descriptor;

        if ("Z".equals(type)) {
            newType = "java/lang/Boolean";
            metchod = "booleanValue";
            descriptor = "()Z";
        } else if ("B".equals(type)) {
            newType = "java/lang/Byte";
            metchod = "byteValue";
            descriptor = "()B";
        } else if ("C".equals(type)) {
            newType = "java/lang/Character";
            metchod = "charValue";
            descriptor = "()C";
        } else if ("D".equals(type)) {
            newType = "java/lang/Double";
            metchod = "doubleValue";
            descriptor = "()D";
        } else if ("F".equals(type)) {
            newType = "java/lang/Float";
            metchod = "floatValue";
            descriptor = "()F";
        } else if ("I".equals(type)) {
            newType = "java/lang/Integer";
            metchod = "intValue";
            descriptor = "()I";
        } else if ("J".equals(type)) {
            newType = "java/lang/Long";
            metchod = "longValue";
            descriptor = "()J";
        } else if ("S".equals(type)) {
            newType = "java/lang/Short";
            metchod = "shortValue";
            descriptor = "()S";
        } else {
            newType = type.substring(1);
            metchod = null;
            descriptor = null;
        }

        mv.visitTypeInsn(Opcodes.CHECKCAST, newType);
        if (metchod != null) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, newType, metchod, descriptor, false);
        }
    }
}
