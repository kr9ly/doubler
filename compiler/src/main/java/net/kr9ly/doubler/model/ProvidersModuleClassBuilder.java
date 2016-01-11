package net.kr9ly.doubler.model;

import com.squareup.javapoet.*;
import dagger.Module;
import dagger.Provides;
import net.kr9ly.doubler.ProvidedBy;
import net.kr9ly.doubler.SpecHelper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;

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
public class ProvidersModuleClassBuilder {

    private ProcessingEnvironment processingEnv;

    private TypeSpec.Builder classBuilder;

    private MethodSpec.Builder provideMethodBuilder;

    private MethodSpec.Builder provideBuilderMethodBuilder;

    private ClassName builderName;

    public ProvidersModuleClassBuilder(ProcessingEnvironment processingEnv, Element providersElement) {
        this.processingEnv = processingEnv;

        classBuilder = TypeSpec.classBuilder(toModuleName(providersElement))
                .addAnnotation(SpecHelper.getGeneratedAnnotation())
                .addAnnotation(Module.class)
                .addModifiers(Modifier.PUBLIC);
    }

    public void beginProvideMethod(Element provideClass, Element providersElement) {
        builderName = ClassName.bestGuess(SpecHelper.getPackageName(processingEnv, provideClass) + "." + toBuilderName(provideClass));

        String providersClassName = SpecHelper.getPackageName(processingEnv, providersElement) + "." + providersElement.getSimpleName();
        AnnotationValue as = SpecHelper.getAnnotationValue(provideClass, providersClassName, "as");

        TypeName provideAs;
        if (as == null || as.getValue() == Void.class) {
            provideAs = TypeName.get(provideClass.asType());
        } else {
            provideAs = SpecHelper.getTypeName(as.getValue());
        }

        provideMethodBuilder = MethodSpec.methodBuilder(toProvidesMethodName(provideClass.getSimpleName().toString()))
                .returns(provideAs)
                .addStatement("$T builder = new $T()", builderName.box(), builderName.box())
                .addAnnotation(Provides.class)
                .addModifiers(Modifier.PUBLIC);
    }

    public void beginProvideBuilderMethod(Element provideClass) {
        builderName = ClassName.bestGuess(SpecHelper.getPackageName(processingEnv, provideClass) + "." + toBuilderName(provideClass));

        provideBuilderMethodBuilder = MethodSpec.methodBuilder(toProvidesMethodName(builderName.simpleName()))
                .returns(builderName.box())
                .addStatement("$T builder = new $T()", builderName.box(), builderName.box())
                .addAnnotation(Provides.class)
                .addModifiers(Modifier.PUBLIC);
    }

    public void addProvideMethod() {
        provideMethodBuilder.addStatement("return builder.build()");
        classBuilder.addMethod(provideMethodBuilder.build());
    }

    public void addProvideBuilderMethod() {
        provideBuilderMethodBuilder.addStatement("return builder");
        classBuilder.addMethod(provideBuilderMethodBuilder.build());
    }

    public void addAnnotationToProvideMethod(AnnotationMirror annotation) {
        provideMethodBuilder.addAnnotation(AnnotationSpec.get(annotation));
    }

    public void addAnnotationToProvideBuilderMethod(AnnotationMirror annotation) {
        provideBuilderMethodBuilder.addAnnotation(AnnotationSpec.get(annotation));
    }

    public void addVariableToProvideMethod(VariableElement variable) {
        provideMethodBuilder
                .addCode(getCodeBlock(variable))
                .addParameter(getParameterSpec(variable));
    }

    public void addVariableToProvideBuilderMethod(VariableElement variable) {
        provideBuilderMethodBuilder
                .addCode(getCodeBlock(variable))
                .addParameter(getParameterSpec(variable));
    }

    public void addProviderToProvideMethod(Element provideClass) {
        AnnotationValue annotationValue = SpecHelper.getAnnotationValue(provideClass, ProvidedBy.class);
        provideMethodBuilder
                .addCode(getProviderCodeBlock())
                .addParameter(ParameterSpec.builder(SpecHelper.getTypeName(annotationValue.getValue()), "__provider").build());
    }

    public TypeSpec build() {
        return classBuilder.build();
    }

    private CodeBlock getCodeBlock(VariableElement variable) {
        String parameterName = variable.getSimpleName().toString();
        return CodeBlock.builder().addStatement("builder.$L($L)", toSetterName(parameterName), parameterName).build();
    }

    private CodeBlock getProviderCodeBlock() {
        String parameterName = "__provider";
        return CodeBlock.builder().addStatement("builder.$L($L)", toSetterName(parameterName), parameterName).build();
    }

    private ParameterSpec getParameterSpec(VariableElement variable) {
        String parameterName = variable.getSimpleName().toString();
        return ParameterSpec.builder(TypeName.get(variable.asType()), parameterName).build();
    }

    private String toModuleName(Element module) {
        return module.getSimpleName() + "Module";
    }

    private String toBuilderName(Element module) {
        return module.getSimpleName() + "Builder";
    }

    private String toProvidesMethodName(String className) {
        return "provide" + className;
    }

    private String toSetterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
