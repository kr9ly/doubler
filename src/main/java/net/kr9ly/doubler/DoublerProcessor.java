package net.kr9ly.doubler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import dagger.Module;
import dagger.Provides;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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
@SupportedAnnotationTypes({"dagger.Module", "net.kr9ly.InjectorsSupport"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DoublerProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        generateExposeComponents(roundEnv);
        generateInjectorsComponents(roundEnv);
        return false;
    }

    private void generateExposeComponents(RoundEnvironment roundEnv) {
        Set<? extends Element> modules = roundEnv.getElementsAnnotatedWith(Module.class);
        for (Element module : modules) {
            TypeSpec.Builder supportBuilder = TypeSpec.interfaceBuilder(toSupportName(module))
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
            TypeSpec.Builder supportBuilder = TypeSpec.interfaceBuilder(toSupportName(injectorsElement))
                    .addModifiers(Modifier.PUBLIC);

            Set<? extends Element> injectClasses = roundEnv.getElementsAnnotatedWith((TypeElement) injectorsElement);
            for (Element injectClass : injectClasses) {
                MethodSpec injectMethod = MethodSpec.methodBuilder("inject")
                        .addParameter(TypeName.get(injectClass.asType()), "injectTo")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build();
                supportBuilder.addMethod(injectMethod);
            }

            JavaFile javaFile = JavaFile.builder(getPackageName(injectorsElement), supportBuilder.build())
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write component file.", injectorsElement);
            }
        }
    }

    private String getPackageName(Element element) {
        return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
    }

    private String toSupportName(Element module) {
        return module.getSimpleName() + "Support";
    }

    private String toExposeMethodName(TypeMirror typeMirror) {
        String typeName = getTypeName(typeMirror);
        return typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
    }

    private String getTypeName(TypeMirror typeMirror) {
        return processingEnv.getTypeUtils().asElement(typeMirror).getSimpleName().toString();
    }
}
