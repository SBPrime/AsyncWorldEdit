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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author SBPrime
 * @param <TFactory>
 */
public abstract class BaseFactoryCreator<TFactory> extends BaseClassCreator {

    private final Class<? extends TFactory> m_factoryClass;
    private final String m_targetName;
    private final String m_targetNameClass;

    public BaseFactoryCreator(ICreateClass createClass,
            Class<? extends TFactory> factoryClass) {
        super(createClass);

        m_factoryClass = factoryClass;

        m_targetName = Type.getInternalName(m_factoryClass) + "Impl" + RANDOM_PREFIX;
        m_targetNameClass = m_targetName.replace("/", ".");
    }

    @Override
    public String getName() {
        return Type.getInternalName(m_factoryClass);
    }

    @Override
    public void run() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, m_targetName,
                null, "java/lang/Object",
                new String[]{
                    Type.getInternalName(m_factoryClass)
                });

        emitEmptyCtor(cw);

        processMethods((String name, String descriptor, String clsName, Method m) -> defineMethod(cw, name, descriptor, m),
                m_factoryClass);

        cw.visitEnd();
        try {
            createClass(m_targetNameClass, cw);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create " + m_targetName + ".", ex);
        }

        try {
            initializeFactory((TFactory) Class.forName(m_targetNameClass).getConstructor().newInstance());
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
                | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            throw new IllegalStateException("Unable to create " + m_targetName + " instance.", ex);
        }

    }

    private void defineMethod(ClassWriter cw, String name, String descriptor, Method m) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, name, descriptor, null, null);
        mv.visitCode();

        Class<?>[] params = m.getParameterTypes();
        String resultClass = getClassForMethod(name, descriptor);
        
        mv.visitTypeInsn(Opcodes.NEW, resultClass);
        mv.visitInsn(Opcodes.DUP);
        
        for (int i = 0; i < params.length; i++) {
            visitArgumemt(mv, params[i], i + 1);
        }
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, resultClass,
                "<init>",
                "(" + Stream.of(params).map(Type::getDescriptor).collect(Collectors.joining()) + ")V",
                false);


        visitReturn(mv, m.getReturnType());

        mv.visitMaxs(2, 1);
        mv.visitEnd();

        cw.visitEnd();
    }

    protected abstract void initializeFactory(TFactory instance);

    protected abstract String getClassForMethod(String name, String descriptor);
}
