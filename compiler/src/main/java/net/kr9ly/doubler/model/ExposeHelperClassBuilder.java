package net.kr9ly.doubler.model;

import com.squareup.javapoet.*;
import net.kr9ly.doubler.SpecHelper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

/**
 * Copyright 2016 kr9ly
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
public class ExposeHelperClassBuilder {

    private ProcessingEnvironment processingEnv;

    private ClassName className;

    private TypeSpec.Builder classBuilder;

    private MethodSpec.Builder getMethodBuilder;

    private CodeBlock.Builder getMethodCodeBlockBuilder;

    private boolean isFirst = true;

    public ExposeHelperClassBuilder(ProcessingEnvironment processingEnv, Element exposeElement) {
        this.processingEnv = processingEnv;

        className = ClassName.bestGuess(SpecHelper.getPackageName(processingEnv, exposeElement) + "." + exposeElement.getSimpleName());

        classBuilder = TypeSpec.classBuilder(toHelperName(exposeElement))
                .addAnnotation(SpecHelper.getGeneratedAnnotation())
                .addModifiers(Modifier.PUBLIC);

        getMethodBuilder = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        getMethodCodeBlockBuilder = CodeBlock.builder();
    }

    public void addExposeCode(ExecutableElement exposeMethod) {
        if (isFirst) {
            getMethodCodeBlockBuilder
                    .beginControlFlow("if (clazz == $T.class)", exposeMethod.getReturnType());
            isFirst = false;
        } else {
            getMethodCodeBlockBuilder
                    .nextControlFlow("else if (clazz == $T.class)", exposeMethod.getReturnType());
        }
        getMethodCodeBlockBuilder
                .addStatement("return (T) component.$L()", exposeMethod.getSimpleName());
    }

    public void buildExposeMethod() {
        classBuilder.addMethod(
                getMethodBuilder
                        .addTypeVariable(TypeVariableName.get("T"))
                        .returns(TypeVariableName.get("T").box())
                        .addParameter(className.box(), "component")
                        .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("T").box()), "clazz")
                        .addCode(getMethodCodeBlockBuilder
                                .endControlFlow()
                                .addStatement("return null")
                                .build())
                        .build()
        );
    }

    public TypeSpec build() {
        return classBuilder.build();
    }

    private String toHelperName(Element module) {
        return module.getSimpleName() + "Helper";
    }
}
