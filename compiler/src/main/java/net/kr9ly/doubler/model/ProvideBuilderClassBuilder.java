package net.kr9ly.doubler.model;

import com.squareup.javapoet.*;
import net.kr9ly.doubler.ProvidedBy;
import net.kr9ly.doubler.SpecHelper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
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
public class ProvideBuilderClassBuilder {

    private ProcessingEnvironment processingEnv;

    private TypeSpec.Builder classBuilder;

    private MethodSpec.Builder constructorBuilder;

    private MethodSpec.Builder buildMethodBuilder;

    private StringBuilder constructorParams = new StringBuilder();

    private Element provideClass;

    public ProvideBuilderClassBuilder(ProcessingEnvironment processingEnv, Element provideClass) {
        this.processingEnv = processingEnv;
        this.provideClass = provideClass;

        ClassName builderName = ClassName.bestGuess(SpecHelper.getPackageName(processingEnv, provideClass) + "." + toBuilderName(provideClass));

        classBuilder = TypeSpec.classBuilder(builderName.simpleName())
                .addAnnotation(SpecHelper.getGeneratedAnnotation())
                .addModifiers(Modifier.PUBLIC);

        constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        buildMethodBuilder = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(provideClass.asType()));
    }

    public void addParameterToBuilderMethod(VariableElement variable) {
        String variableName = variable.getSimpleName().toString();
        buildMethodBuilder.addParameter(TypeName.get(variable.asType()), variableName);
    }

    public void addParameterToConstructor(VariableElement parameter) {
        String parameterName = parameter.getSimpleName().toString();

        constructorBuilder.addParameter(TypeName.get(parameter.asType()), parameterName);
        constructorParams.append(parameterName).append(", ");
    }

    public void addProviderInjectField() {
        AnnotationValue annotationValue = SpecHelper.getAnnotationValue(provideClass, ProvidedBy.class);
        ClassName className = ClassName.bestGuess(annotationValue.getValue().toString());
        String parameterName = "__provider";
        classBuilder
                .addField(className, parameterName, Modifier.PRIVATE)
                .addMethod(
                        MethodSpec.methodBuilder(toSetterName(parameterName))
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(className, parameterName)
                                .addStatement("this.$L  = $L", parameterName, parameterName)
                                .build()
                );
    }

    public void addInjectField(VariableElement variable) {
        TypeMirror type = variable.asType();
        String parameterName = variable.getSimpleName().toString();
        classBuilder
                .addField(TypeName.get(type), parameterName, Modifier.PRIVATE)
                .addMethod(
                        MethodSpec.methodBuilder(toSetterName(parameterName))
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(TypeName.get(type), parameterName)
                                .addStatement("this.$L  = $L", parameterName, parameterName)
                                .build()
                );
    }

    public void beginProvidedBuildMethod() {
        buildMethodBuilder.addStatement("$T instance = __provider.provide($T.class)", provideClass.asType(), provideClass.asType());
    }

    public void beginBuildMethod() {
        String constructorParamsStr = "";
        if (constructorParams.length() > 0) {
            constructorParamsStr = constructorParams.substring(0, constructorParams.length() - 2);
        }
        buildMethodBuilder.addStatement("$T instance = new $T($L)", provideClass.asType(), provideClass.asType(), constructorParamsStr);
    }

    public void addBuildMethodStatement(VariableElement field) {
        String fieldName = field.getSimpleName().toString();
        buildMethodBuilder
                .addStatement("instance.$L = $L", fieldName, fieldName);
    }

    public void addBuildMethod() {
        buildMethodBuilder.addStatement("return instance");
        classBuilder.addMethod(buildMethodBuilder.build());
    }

    public TypeSpec build() {
        return classBuilder.build();
    }

    private String toBuilderName(Element module) {
        return module.getSimpleName() + "Builder";
    }

    private String toSetterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
