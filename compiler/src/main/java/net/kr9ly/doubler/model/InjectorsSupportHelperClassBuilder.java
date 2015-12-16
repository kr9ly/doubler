package net.kr9ly.doubler.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import net.kr9ly.doubler.SpecHelper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

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
public class InjectorsSupportHelperClassBuilder {

    private ProcessingEnvironment processingEnv;

    private ClassName className;

    private TypeSpec.Builder classBuilder;

    private MethodSpec.Builder injectMethodBuilder;

    private CodeBlock.Builder injectMethodCodeBlockBuilder;

    private boolean isFirst = true;

    public InjectorsSupportHelperClassBuilder(ProcessingEnvironment processingEnv, Element injectorsElement) {
        this.processingEnv = processingEnv;

        className = ClassName.bestGuess(SpecHelper.getPackageName(processingEnv, injectorsElement) + "." + toSupportName(injectorsElement));

        classBuilder = TypeSpec.classBuilder(toSupportHelperName(injectorsElement))
                .addAnnotation(SpecHelper.getGeneratedAnnotation())
                .addModifiers(Modifier.PUBLIC);

        injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        injectMethodCodeBlockBuilder = CodeBlock.builder();
    }

    public void addInjectCode(Element injectClass) {
        if (isFirst) {
            injectMethodCodeBlockBuilder
                    .beginControlFlow("if (injectTo instanceof $T)", injectClass);
            isFirst = false;
        } else {
            injectMethodCodeBlockBuilder
                    .nextControlFlow("else if (injectTo instanceof $T)", injectClass);
        }
        injectMethodCodeBlockBuilder
                .addStatement("component.inject(($T) injectTo)", injectClass);
    }

    public void buildInjectMethod() {
        classBuilder.addMethod(
                injectMethodBuilder
                        .addParameter(className.box(), "component")
                        .addParameter(Object.class, "injectTo")
                        .addCode(injectMethodCodeBlockBuilder
                                .endControlFlow()
                                .build())
                        .build()
        );
    }

    public TypeSpec build() {
        return classBuilder.build();
    }

    private String toSupportName(Element module) {
        return module.getSimpleName() + "Support";
    }

    private String toSupportHelperName(Element module) {
        return module.getSimpleName() + "SupportHelper";
    }
}
