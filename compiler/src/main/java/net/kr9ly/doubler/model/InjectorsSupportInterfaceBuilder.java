package net.kr9ly.doubler.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.kr9ly.doubler.InjectorsHelper;
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
public class InjectorsSupportInterfaceBuilder {

    private ProcessingEnvironment processingEnv;

    private ClassName className;

    private TypeSpec.Builder classBuilder;

    public InjectorsSupportInterfaceBuilder(ProcessingEnvironment processingEnv, Element injectorsElement) {
        this.processingEnv = processingEnv;

        className = ClassName.bestGuess(SpecHelper.getPackageName(processingEnv, injectorsElement) + "." + toSupportName(injectorsElement));

        classBuilder = TypeSpec.interfaceBuilder(className.simpleName())
                .addAnnotation(InjectorsHelper.class)
                .addAnnotation(SpecHelper.getGeneratedAnnotation())
                .addModifiers(Modifier.PUBLIC);
    }

    public void addInjectMethod(Element injectClass) {
        MethodSpec injectMethod = MethodSpec.methodBuilder("inject")
                .addParameter(TypeName.get(injectClass.asType()), "injectTo")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .build();
        classBuilder.addMethod(injectMethod);
    }

    public TypeSpec build() {
        return classBuilder.build();
    }

    private String toSupportName(Element module) {
        return module.getSimpleName() + "Support";
    }
}
