package net.kr9ly.doubler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import dagger.Module;
import dagger.Provides;
import net.kr9ly.doubler.model.*;

import javax.annotation.processing.*;
import javax.inject.Inject;
import javax.inject.Scope;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
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
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DoublerProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        generateProvidersComponents(roundEnv);
        generateInjectorsComponents(roundEnv);
        generateExposeComponents(roundEnv);
        generateExposeHelpers(roundEnv);
        generateInjectorsHelpers(roundEnv);
        return false;
    }

    private void generateExposeComponents(RoundEnvironment roundEnv) {
        Set<? extends Element> modules = roundEnv.getElementsAnnotatedWith(Module.class);
        for (Element module : modules) {
            ExposeSupportInterfaceBuilder builder = new ExposeSupportInterfaceBuilder(processingEnv, module);

            for (Element member : module.getEnclosedElements()) {
                if (member.getAnnotation(Provides.class) != null) {
                    builder.addExposeMethod((ExecutableElement) member);
                }
            }

            JavaFile javaFile = JavaFile.builder(SpecHelper.getPackageName(processingEnv, module), builder.build())
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write component file.", module);
            }
        }
    }

    private void generateExposeHelpers(RoundEnvironment roundEnv) {
        Set<? extends Element> exposes = roundEnv.getElementsAnnotatedWith(ExposeHelper.class);
        for (Element expose : exposes) {
            ExposeHelperClassBuilder builder = new ExposeHelperClassBuilder(processingEnv, expose);

            for (Element member : expose.getEnclosedElements()) {
                if (member.getKind() == ElementKind.METHOD) {
                    builder.addExposeCode((ExecutableElement) member);
                }
            }

            builder.buildExposeMethod();

            JavaFile javaFile = JavaFile.builder(SpecHelper.getPackageName(processingEnv, expose), builder.build())
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write component file.", expose);
            }
        }
    }

    private void generateInjectorsComponents(RoundEnvironment roundEnv) {
        Set<? extends Element> injectorsElements = roundEnv.getElementsAnnotatedWith(InjectorsSupport.class);
        for (Element injectorsElement : injectorsElements) {
            InjectorsSupportInterfaceBuilder supportBuilder = new InjectorsSupportInterfaceBuilder(processingEnv, injectorsElement);

            Set<? extends Element> injectClasses = roundEnv.getElementsAnnotatedWith((TypeElement) injectorsElement);
            for (Element injectClass : injectClasses) {
                supportBuilder.addInjectMethod(injectClass);
            }

            JavaFile javaFile = JavaFile.builder(SpecHelper.getPackageName(processingEnv, injectorsElement), supportBuilder.build()).build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write component file.", injectorsElement);
            }
        }
    }

    private void generateInjectorsHelpers(RoundEnvironment roundEnv) {
        Set<? extends Element> injectorsElements = roundEnv.getElementsAnnotatedWith(InjectorsHelper.class);
        for (Element injectorsElement : injectorsElements) {
            InjectorsSupportHelperClassBuilder builder = new InjectorsSupportHelperClassBuilder(processingEnv, injectorsElement);

            for (Element member : injectorsElement.getEnclosedElements()) {
                if (member.getKind() == ElementKind.METHOD) {
                    builder.addInjectCode((ExecutableElement) member);
                }
            }

            if (!injectorsElement.getEnclosedElements().isEmpty()) {
                builder.closeInjectControlFlow();
            }

            builder.buildInjectMethod();

            JavaFile javaFile = JavaFile.builder(SpecHelper.getPackageName(processingEnv, injectorsElement), builder.build())
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write component file.", injectorsElement);
            }
        }
    }

    private void generateProvidersComponents(RoundEnvironment roundEnv) {
        Set<? extends Element> providersElements = roundEnv.getElementsAnnotatedWith(ProvidersSupport.class);
        for (Element providersElement : providersElements) {
            ProvidersModuleClassBuilder moduleBuilder = new ProvidersModuleClassBuilder(processingEnv, providersElement);

            Set<? extends Element> provideClasses = roundEnv.getElementsAnnotatedWith((TypeElement) providersElement);
            for (Element provideClass : provideClasses) {
                ProvideBuilderClassBuilder builderClassBuilder = new ProvideBuilderClassBuilder(processingEnv, provideClass);

                moduleBuilder.beginProvideMethod(provideClass, providersElement);
                moduleBuilder.beginProvideBuilderMethod(provideClass);

                for (AnnotationMirror annotation : providersElement.getAnnotationMirrors()) {
                    if (annotation.getAnnotationType().asElement().getAnnotation(Scope.class) != null) {
                        moduleBuilder.addAnnotationToProvideMethod(annotation);
                        moduleBuilder.addAnnotationToProvideBuilderMethod(annotation);
                    }
                }

                boolean makeProvideMethod = true;
                boolean makeBuilderMethod = false;

                if (SpecHelper.hasAnnotation(provideClass, ProvidedBy.class)) {
                    builderClassBuilder.addProviderInjectField();
                    moduleBuilder.addProviderToProvideMethod(provideClass);
                    builderClassBuilder.beginProvidedBuildMethod();
                } else {
                    for (Element member : provideClass.getEnclosedElements()) {
                        if (member.getKind() == ElementKind.CONSTRUCTOR && SpecHelper.hasAnnotation(member, Inject.class)) {
                            ExecutableElement constructor = (ExecutableElement) member;

                            for (VariableElement parameter : constructor.getParameters()) {
                                if (SpecHelper.hasAnnotation(parameter, Assisted.class)) {
                                    makeProvideMethod = false;
                                    makeBuilderMethod = true;
                                    builderClassBuilder.addParameterToBuilderMethod(parameter);
                                } else {
                                    builderClassBuilder.addInjectField(parameter);
                                    moduleBuilder.addVariableToProvideBuilderMethod(parameter);
                                    moduleBuilder.addVariableToProvideMethod(parameter);
                                }
                                builderClassBuilder.addParameterToConstructor(parameter);
                            }
                            break;
                        }
                    }
                    builderClassBuilder.beginBuildMethod();
                }

                for (Element member : provideClass.getEnclosedElements()) {
                    if (member.getKind() == ElementKind.FIELD &&
                            (member.getAnnotation(Inject.class) != null)) {
                        VariableElement field = (VariableElement) member;

                        builderClassBuilder.addInjectField(field);
                        builderClassBuilder.addBuildMethodStatement(field);
                        moduleBuilder.addVariableToProvideBuilderMethod(field);
                        moduleBuilder.addVariableToProvideMethod(field);
                    }
                }

                builderClassBuilder.addBuildMethod();

                JavaFile javaFile = JavaFile.builder(SpecHelper.getPackageName(processingEnv, provideClass), builderClassBuilder.build())
                        .build();

                try {
                    javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write builder file.", provideClass);
                }

                if (makeProvideMethod) {
                    moduleBuilder.addProvideMethod();
                }
                if (makeBuilderMethod) {
                    moduleBuilder.addProvideBuilderMethod();
                }
            }

            JavaFile javaFile = JavaFile.builder(SpecHelper.getPackageName(processingEnv, providersElement), moduleBuilder.build())
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write component file.", providersElement);
            }
        }
    }
}
