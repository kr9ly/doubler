package net.kr9ly.doubler.model;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.kr9ly.doubler.ExposeHelper;
import net.kr9ly.doubler.SpecHelper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Copyright 2015 kr9ly
 * <br />
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <br />
 * http://www.apache.org/licenses/LICENSE-2.0
 * <br />
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ExposeSupportInterfaceBuilder {

    private ProcessingEnvironment processingEnv;

    private TypeSpec.Builder classBuilder;

    public ExposeSupportInterfaceBuilder(ProcessingEnvironment processingEnv, Element moduleElement) {
        this.processingEnv = processingEnv;
        classBuilder = TypeSpec.interfaceBuilder(toSupportName(moduleElement))
                .addAnnotation(SpecHelper.getGeneratedAnnotation())
                .addAnnotation(ExposeHelper.class)
                .addModifiers(Modifier.PUBLIC);
    }

    public void addExposeMethod(ExecutableElement targetMethod) {
        TypeMirror returnType = targetMethod.getReturnType();
        MethodSpec exposeMethod = MethodSpec.methodBuilder(toExposeMethodName(returnType))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.get(returnType))
                .build();
        classBuilder.addMethod(exposeMethod);
    }

    public TypeSpec build() {
        return classBuilder.build();
    }

    private String toSupportName(Element module) {
        return module.getSimpleName() + "Support";
    }

    private String toExposeMethodName(TypeMirror typeMirror) {
        String typeName = SpecHelper.getTypeName(processingEnv, typeMirror);
        return typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
    }
}
