/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2019, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassVisitor;
import org.primesoft.asyncworldedit.injector.injected.commands.ICommandsRegistration;
import org.primesoft.asyncworldedit.injector.injected.commands.ICommandsRegistrationDelegate;
import org.primesoft.asyncworldedit.injector.utils.SimpleValidator;

/**
 *
 * @author SBPrime
 */
public class CommandsRegistrationVisitor extends BaseClassVisitor {

    private static final String METHOD_NAME_BUILD = "build";
    private static final String METHOD_NAME_BUILDER = "builder";
    private static final String METHOD_NAME_COMMAND_MANAGER = "commandManager";
    private final static String DESCRIPTOR_METHOD_BUILD = "()V";

    private final static String FIELD_NAME_NEW_BUILDER = "m_newBuilderDelegate";
    private final static String FIELD_NAME_KEYS_CACHE = "m_cahcedKeys";
    private final static String FIELD_NAME_FIELDS_CACHE = "m_cahcedFields";

    private final static String DESCRIPTOR_KEY = "Lorg/enginehub/piston/inject/Key;";
    private final static String[] DESCRIPTOR_PARAS = new String[]{
        "Lorg/enginehub/piston/part/CommandArgument;",
        "Lorg/enginehub/piston/part/ArgAcceptingCommandFlag;",
        "Lorg/enginehub/piston/part/ArgAcceptingCommandPart;",
        "Lorg/enginehub/piston/part/CommandArgument;",
        "Lorg/enginehub/piston/part/CommandFlag;",
        "Lorg/enginehub/piston/part/CommandPart;",
        "Lorg/enginehub/piston/part/NoArgCommandFlag;"
    };

    private final SimpleValidator m_vBuilder = new SimpleValidator("Builder not injected");
    private final SimpleValidator m_vBuild = new SimpleValidator("Build not injected");
    private final SimpleValidator m_vCommandManager = new SimpleValidator("CommandManager not injected");

    private final List<FieldEntry> m_fields = new LinkedList<>();

    private String m_descriptorClass;

    private String m_descriptorMethodBuilder;
    private String m_descriptorMethodCommandManager;
    private ICommandsRegistrationDelegate tmp;

    public CommandsRegistrationVisitor(ClassVisitor classVisitor) {
        super(classVisitor);
    }

    @Override
    public void validate() throws RuntimeException {
        m_vBuilder.validate();
        m_vBuild.validate();
        m_vCommandManager.validate();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        m_descriptorClass = name;
        m_descriptorMethodBuilder = "()L" + name + ";";
        m_descriptorMethodCommandManager = "(Lorg/enginehub/piston/CommandManager;)L" + name + ";";

        super.visit(version,
                changeVisibility(access, Opcodes.ACC_PUBLIC),
                name, signature, superName,
                injectInterface(interfaces, Type.getInternalName(ICommandsRegistration.class)));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        boolean isKey = isStatic(access) && DESCRIPTOR_KEY.equals(descriptor);
        boolean isParam = Stream.of(DESCRIPTOR_PARAS).anyMatch(s -> descriptor.startsWith(s));

        m_fields.add(new FieldEntry(isStatic(access), name, descriptor, isKey, isParam));

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (isPublic(access) && isStatic(access)
                && METHOD_NAME_BUILDER.equals(name) && m_descriptorMethodBuilder.equals(descriptor)) {

            m_vBuilder.set();
            return null;
        }

        if (isPublic(access) && !isStatic(access)
                && METHOD_NAME_BUILD.equals(name) && DESCRIPTOR_METHOD_BUILD.equals(descriptor)) {
            m_vBuild.set();

            return super.visitMethod(access, RANDOM_PREFIX + name, descriptor, signature, exceptions);
        }

        if (isPublic(access) && !isStatic(access)
                && METHOD_NAME_COMMAND_MANAGER.equals(name) && m_descriptorMethodCommandManager.equals(descriptor)) {
            m_vCommandManager.set();

            return null;
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        cv.visitField(Opcodes.ACC_PRIVATE, FIELD_NAME_NEW_BUILDER, Type.getDescriptor(ICommandsRegistrationDelegate.class), null, false).visitEnd();
        cv.visitField(Opcodes.ACC_PRIVATE, FIELD_NAME_FIELDS_CACHE, Type.getDescriptor(Map.class), null, false).visitEnd();
        cv.visitField(Opcodes.ACC_PRIVATE, FIELD_NAME_KEYS_CACHE, Type.getDescriptor(Map.class), null, false).visitEnd();

        visitBuilderMethod();
        visitBuildMethod();
        visitCommandManagerMethod();

        final Map<String, Method> crMethods = Stream.of(ICommandsRegistration.class.getDeclaredMethods())
                .collect(Collectors.toMap(i -> i.getName(), i -> i));

        visitGetBuilderDelegate(crMethods, "getBuilderDelegate");
        visitSimpleGetMethod(crMethods, "getCommandManager");
        visitSimpleGetMethod(crMethods, "getCommandPermissionsConditionGenerator");
        visitSimpleGetMethod(crMethods, "getListeners", "Lcom/google/common/collect/ImmutableList;");
        visitContainerGetMethod(crMethods, "getContainerInstance");
        visitMapGetMethod(crMethods, "getKeys", FIELD_NAME_KEYS_CACHE, i -> i.isKey);
        visitMapGetMethod(crMethods, "getCommandArguments", FIELD_NAME_FIELDS_CACHE, i -> i.isArgument);

        super.visitEnd();
    }

    private void visitCommandManagerMethod() {
        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, METHOD_NAME_COMMAND_MANAGER, m_descriptorMethodCommandManager, null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, CLASS_HELPERS_DESCRIPTOR, "wrapCommandManager",
                "(Ljava/lang/Object;Lorg/enginehub/piston/CommandManager;)Lorg/enginehub/piston/CommandManager;",
                false);

        mv.visitFieldInsn(Opcodes.PUTFIELD, m_descriptorClass, "commandManager", "Lorg/enginehub/piston/CommandManager;");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(2, 1);
        mv.visitEnd();
    }

    private void visitBuilderMethod() {
        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, METHOD_NAME_BUILDER, m_descriptorMethodBuilder, null, null);

        mv.visitCode();
        // var result =  new CLASS_NAME();
        mv.visitTypeInsn(Opcodes.NEW, m_descriptorClass);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, m_descriptorClass, "<init>", "()V", false);
        mv.visitVarInsn(Opcodes.ASTORE, 0);

        // result.FIELD_NAME_NEW_BUILDER = Helpers.createCommandsRegistrationDelegate(result);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, CLASS_HELPERS_DESCRIPTOR, "createCommandsRegistrationDelegate",
                "(Lorg/primesoft/asyncworldedit/injector/injected/commands/ICommandsRegistration;)Lorg/primesoft/asyncworldedit/injector/injected/commands/ICommandsRegistrationDelegate;",
                false);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_descriptorClass, FIELD_NAME_NEW_BUILDER, Type.getDescriptor(ICommandsRegistrationDelegate.class));

        // return result;
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(2, 1);
        mv.visitEnd();
    }

    private void visitBuildMethod() {
        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, METHOD_NAME_BUILD, DESCRIPTOR_METHOD_BUILD, null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                m_descriptorClass,
                RANDOM_PREFIX + METHOD_NAME_BUILD, DESCRIPTOR_METHOD_BUILD,
                false);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_descriptorClass, FIELD_NAME_NEW_BUILDER, Type.getDescriptor(ICommandsRegistrationDelegate.class));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                Type.getInternalName(ICommandsRegistrationDelegate.class),
                "build", "(Lorg/primesoft/asyncworldedit/injector/injected/commands/ICommandsRegistration;)V",
                true);

        mv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void visitSimpleGetMethod(Map<String, Method> methods, String methodName) {
        visitSimpleGetMethod(methods, methodName, null);
    }

    private void visitSimpleGetMethod(Map<String, Method> methods, String methodName, String fieldType) {
        final Method im = methods.get(methodName);
        if (im == null) {
            throw new IllegalStateException("Method '" + methodName + "' not found in " + ICommandsRegistration.class.getName() + " interface");
        }

        final String methodDescriptor = Type.getMethodDescriptor(im);
        final String resultDescriptor = fieldType == null ? Type.getDescriptor(im.getReturnType()) : fieldType;
        final FieldEntry field = m_fields.stream().filter(i -> !i.isArgument && !i.isKey
                && methodName.toLowerCase().contains(i.name.toLowerCase())
                && i.descriptor.startsWith(resultDescriptor)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find source field of type '" + resultDescriptor + "' for '" + methodName + "'"));

        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, im.getName(), methodDescriptor, null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_descriptorClass, field.name, field.descriptor);

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
    
    private void visitContainerGetMethod(Map<String, Method> methods, String methodName) {
        final Method im = methods.get(methodName);
        if (im == null) {
            throw new IllegalStateException("Method '" + methodName + "' not found in " + ICommandsRegistration.class.getName() + " interface");
        }

        final String methodDescriptor = Type.getMethodDescriptor(im);
        final FieldEntry field = m_fields.stream().filter(i -> !i.isArgument && !i.isKey
                && methodName.toLowerCase().contains(i.name.toLowerCase())).findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find source field for '" + methodName + "'"));

        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, im.getName(), methodDescriptor, null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_descriptorClass, field.name, field.descriptor);

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void visitMapGetMethod(Map<String, Method> methods, String methodName,
            String sourceField, Predicate<FieldEntry> filter) {
        final Method im = methods.get(methodName);
        if (im == null) {
            throw new IllegalStateException("Method '" + methodName + "' not found in " + ICommandsRegistration.class.getName() + " interface");
        }

        final String nameMap = Type.getInternalName(Map.class);
        final String nameHashMap = Type.getInternalName(HashMap.class);
        final String descriptorMap = Type.getDescriptor(Map.class);
        final String descriptorMethod = Type.getMethodDescriptor(im);
        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, im.getName(), descriptorMethod, null, null);
        final Label lCahceFound = new Label();

        mv.visitCode();

        // if (this.SOURCE_FIELD != null) goto lCahceFound
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_descriptorClass, sourceField, descriptorMap);
        mv.visitJumpInsn(Opcodes.IFNONNULL, lCahceFound);

        // Map result = new HashMap();
        mv.visitTypeInsn(Opcodes.NEW, nameHashMap);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, nameHashMap, "<init>", "()V", false);
        mv.visitVarInsn(Opcodes.ASTORE, 1);

        for (FieldEntry field : m_fields) {
            if (!filter.test(field)) {
                continue;
            }

            // result.put("FIELD_NAME", this.FIELD);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitLdcInsn(field.name);

            if (field.isStatic) {
                mv.visitFieldInsn(Opcodes.GETSTATIC, m_descriptorClass, field.name, field.descriptor);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, m_descriptorClass, field.name, field.descriptor);
            }
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, nameMap, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(Opcodes.POP);
        }

        // this.SOURCE_FIELD = Collections.unmodifiableMap(result);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);        
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Collections.class),                 
                "unmodifiableMap", "(" + descriptorMap + ")" + descriptorMap, true);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_descriptorClass, sourceField, descriptorMap);

        mv.visitLabel(lCahceFound);

        // return this.SOURCE_FIELD;
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_descriptorClass, sourceField, descriptorMap);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void visitGetBuilderDelegate(Map<String, Method> methods, String methodName) {
        final Method im = methods.get(methodName);
        if (im == null) {
            throw new IllegalStateException("Method '" + methodName + "' not found in " + ICommandsRegistration.class.getName() + " interface");
        }

        final String methodDescriptor = Type.getMethodDescriptor(im);        
        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, im.getName(), methodDescriptor, null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_descriptorClass, FIELD_NAME_NEW_BUILDER, Type.getDescriptor(ICommandsRegistrationDelegate.class));

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private static class FieldEntry {

        public final String name;
        public final String descriptor;

        public final boolean isKey;
        public final boolean isArgument;

        public final boolean isStatic;

        public FieldEntry(boolean isStatic, String name, String descriptor, boolean isKey, boolean isArgument) {
            this.isStatic = isStatic;
            this.name = name;
            this.descriptor = descriptor;
            this.isKey = isKey;
            this.isArgument = isArgument;
        }
    }
}
