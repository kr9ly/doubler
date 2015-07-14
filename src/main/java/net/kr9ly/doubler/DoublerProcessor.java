package net.kr9ly.doubler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import dagger.Module;
import dagger.Provides;

import javax.annotation.Generated;
import javax.annotation.processing.*;
import javax.inject.Inject;
import javax.inject.Scope;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

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
@AutoService(Processor.class)
@SupportedAnnotationTypes({"dagger.Module", "net.kr9ly.*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DoublerProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        generateProvidersComponents(roundEnv);
        generateInjectorsComponents(roundEnv);
        generateExposeComponents(roundEnv);
        return false;
    }

    private void generateExposeComponents(RoundEnvironment roundEnv) {
        Set<? extends Element> modules = roundEnv.getElementsAnnotatedWith(Module.class);
        for (Element module : modules) {
            TypeSpec.Builder supportBuilder = TypeSpec.interfaceBuilder(toSupportName(module))
                    .addAnnotation(getGeneratedAnnotation())
                    .addModifiers(Modifier.PUBLIC);

            for (Element member : module.getEnclosedElements()) {
                if (member.getAnnotation(Provides.class) != null) {
                    ExecutableElement method = (ExecutableElement) member;
                    TypeMirror returnType = method.getReturnType();
                    MethodSpec exposeMethod = MethodSpec.methodBuilder(toExposeMethodName(returnType))
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(TypeName.get(returnType))
                            .build();
                    supportBuilder.addMethod(exposeMethod);
                }
            }

            JavaFile javaFile = JavaFile.builder(getPackageName(module), supportBuilder.build())
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write component file.", module);
            }
        }
    }

    private void generateInjectorsComponents(RoundEnvironment roundEnv) {
        Set<? extends Element> injectorsElements = roundEnv.getElementsAnnotatedWith(InjectorsSupport.class);
        for (Element injectorsElement : injectorsElements) {
            ClassName supportName = ClassName.bestGuess(getPackageName(injectorsElement) + "." + toSupportName(injectorsElement));

            TypeSpec.Builder supportBuilder = TypeSpec.interfaceBuilder(supportName.simpleName())
                    .addAnnotation(getGeneratedAnnotation())
                    .addModifiers(Modifier.PUBLIC);

            TypeSpec.Builder supportHelperBuilder = TypeSpec.classBuilder(toSupportHelperName(injectorsElement))
                    .addAnnotation(getGeneratedAnnotation())
                    .addModifiers(Modifier.PUBLIC);

            MethodSpec.Builder supportHelperInjectBuilder = MethodSpec.methodBuilder("inject")
                    .addModifiers(Modifier.STATIC, Modifier.PUBLIC);

            CodeBlock.Builder supportHelperInjectCodeBlockBuilder = CodeBlock.builder();

            boolean isFirst = true;
            Set<? extends Element> injectClasses = roundEnv.getElementsAnnotatedWith((TypeElement) injectorsElement);
            for (Element injectClass : injectClasses) {
                MethodSpec injectMethod = MethodSpec.methodBuilder("inject")
                        .addParameter(TypeName.get(injectClass.asType()), "injectTo")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build();
                supportBuilder.addMethod(injectMethod);


                if (isFirst) {
                    supportHelperInjectCodeBlockBuilder
                            .beginControlFlow("if (injectTo instanceof $T)", injectClass);
                    isFirst = false;
                } else {
                    supportHelperInjectCodeBlockBuilder
                            .nextControlFlow("else if (injectTo instanceof $T)", injectClass);
                }
                supportHelperInjectCodeBlockBuilder
                        .addStatement("component.inject(($T) injectTo)", injectClass);
            }

            CodeBlock supportHelperInjectCodeBlock = supportHelperInjectCodeBlockBuilder
                    .endControlFlow()
                    .build();

            MethodSpec supportHelperInject = supportHelperInjectBuilder
                    .addParameter(supportName.box(), "component")
                    .addParameter(Object.class, "injectTo")
                    .addCode(supportHelperInjectCodeBlock)
                    .build();

            supportHelperBuilder.addMethod(supportHelperInject);

            JavaFile javaFile = JavaFile.builder(getPackageName(injectorsElement), supportBuilder.build())
                    .build();

            JavaFile helperFile = JavaFile.builder(getPackageName(injectorsElement), supportHelperBuilder.build())
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
                helperFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write component file.", injectorsElement);
            }
        }
    }

    private void generateProvidersComponents(RoundEnvironment roundEnv) {
        Set<? extends Element> providersElements = roundEnv.getElementsAnnotatedWith(ProvidersSupport.class);
        for (Element providersElement : providersElements) {
            TypeSpec.Builder supportBuilder = TypeSpec.classBuilder(toModuleName(providersElement))
                    .addAnnotation(getGeneratedAnnotation())
                    .addAnnotation(Module.class)
                    .addModifiers(Modifier.PUBLIC);

            Set<? extends Element> provideClasses = roundEnv.getElementsAnnotatedWith((TypeElement) providersElement);
            for (Element provideClass : provideClasses) {
                ClassName builderName = ClassName.bestGuess(getPackageName(provideClass) + "." + toBuilderName(provideClass));

                TypeSpec.Builder builderClassBuilder = TypeSpec.classBuilder(builderName.simpleName())
                        .addAnnotation(getGeneratedAnnotation())
                        .addModifiers(Modifier.PUBLIC);

                MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC);

                MethodSpec.Builder provideMethodBuilder = MethodSpec.methodBuilder(toProvidesMethodName(provideClass.getSimpleName().toString()))
                        .returns(TypeName.get(provideClass.asType()))
                        .addStatement("$T builder = new $T()", builderName.box(), builderName.box())
                        .addAnnotation(Provides.class)
                        .addModifiers(Modifier.PUBLIC);

                MethodSpec.Builder provideBuilderMethodBuilder = MethodSpec.methodBuilder(toProvidesMethodName(builderName.simpleName()))
                        .returns(builderName.box())
                        .addStatement("$T builder = new $T()", builderName.box(), builderName.box())
                        .addAnnotation(Provides.class)
                        .addModifiers(Modifier.PUBLIC);

                MethodSpec.Builder buildMethodBuilder = MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.get(provideClass.asType()));

                for (AnnotationMirror annotation : providersElement.getAnnotationMirrors()) {
                    if (annotation.getAnnotationType().asElement().getAnnotation(Scope.class) != null) {
                        provideMethodBuilder.addAnnotation(AnnotationSpec.get(annotation));
                        provideBuilderMethodBuilder.addAnnotation(AnnotationSpec.get(annotation));
                    }
                }

                boolean assisted = false;
                StringBuilder constructorParams = new StringBuilder();
                for (Element member : provideClass.getEnclosedElements()) {
                    if (member.getKind() == ElementKind.CONSTRUCTOR && member.getAnnotation(Inject.class) != null) {
                        ExecutableElement constructor = (ExecutableElement) member;

                        for (VariableElement parameter : constructor.getParameters()) {
                            String parameterName = parameter.getSimpleName().toString();

                            if (parameter.getAnnotation(Assisted.class) != null) {
                                assisted = true;
                                buildMethodBuilder.addParameter(TypeName.get(parameter.asType()), parameterName);
                            } else {
                                addInjectField(builderClassBuilder, parameter.asType(), parameterName);

                                provideBuilderMethodBuilder
                                        .addStatement("builder.$L($L)", toSetterName(parameterName), parameterName)
                                        .addParameter(TypeName.get(parameter.asType()), parameterName);
                            }
                            constructorBuilder.addParameter(TypeName.get(parameter.asType()), parameterName);
                            constructorParams.append(parameterName).append(", ");
                        }
                    }
                }

                String constructorParamsStr = "";
                if (constructorParams.length() > 0) {
                    constructorParamsStr = constructorParams.substring(0, constructorParams.length() - 2);
                }
                buildMethodBuilder.addStatement("$T instance = new $T($L)", provideClass.asType(), provideClass.asType(), constructorParamsStr);

                for (Element member : provideClass.getEnclosedElements()) {
                    if (member.getKind() == ElementKind.FIELD && member.getAnnotation(Inject.class) != null) {
                        VariableElement field = (VariableElement) member;

                        String fieldName = field.getSimpleName().toString();

                        addInjectField(builderClassBuilder, field.asType(), fieldName);

                        buildMethodBuilder
                                .addStatement("instance.$L = $L", fieldName, fieldName);

                        provideMethodBuilder
                                .addStatement("builder.$L($L)", toSetterName(fieldName), fieldName)
                                .addParameter(TypeName.get(field.asType()), fieldName);
                    }
                }

                buildMethodBuilder.addStatement("return instance");
                builderClassBuilder.addMethod(buildMethodBuilder.build());

                JavaFile javaFile = JavaFile.builder(getPackageName(provideClass), builderClassBuilder.build())
                        .build();

                try {
                    javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write builder file.", provideClass);
                }

                provideMethodBuilder.addStatement("return builder.build()");
                provideBuilderMethodBuilder.addStatement("return builder");

                if (!assisted) {
                    supportBuilder.addMethod(provideMethodBuilder.build());
                }
                supportBuilder.addMethod(provideBuilderMethodBuilder.build());
            }

            JavaFile javaFile = JavaFile.builder(getPackageName(providersElement), supportBuilder.build())
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write component file.", providersElement);
            }
        }
    }

    private void addInjectField(TypeSpec.Builder builderClassBuilder, TypeMirror type, String fieldName) {
        builderClassBuilder
                .addField(TypeName.get(type), fieldName, Modifier.PRIVATE)
                .addMethod(
                        MethodSpec.methodBuilder(toSetterName(fieldName))
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(TypeName.get(type), fieldName)
                                .addStatement("this.$L  = $L", fieldName, fieldName)
                                .build()
                );
    }

    private AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", "net.kr9ly.doubler.DoublerProcessor")
                .build();
    }

    private String getPackageName(Element element) {
        return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
    }

    private String getTypeName(TypeMirror typeMirror) {
        return processingEnv.getTypeUtils().asElement(typeMirror).getSimpleName().toString();
    }

    private String toSupportName(Element module) {
        return module.getSimpleName() + "Support";
    }

    private String toSupportHelperName(Element module) {
        return module.getSimpleName() + "SupportHelper";
    }

    private String toModuleName(Element module) {
        return module.getSimpleName() + "Module";
    }

    private String toBuilderName(Element module) {
        return module.getSimpleName() + "Builder";
    }

    private String toExposeMethodName(TypeMirror typeMirror) {
        String typeName = getTypeName(typeMirror);
        return typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
    }

    private String toProvidesMethodName(String className) {
        return "provide" + className;
    }

    private String toSetterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
