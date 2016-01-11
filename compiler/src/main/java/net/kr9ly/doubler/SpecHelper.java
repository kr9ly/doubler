package net.kr9ly.doubler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Map;

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
public class SpecHelper {

    public static AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", "net.kr9ly.doubler.DoublerProcessor")
                .build();
    }

    public static String getTypeName(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        return processingEnv.getTypeUtils().asElement(typeMirror).getSimpleName().toString();
    }

    public static String getPackageName(ProcessingEnvironment processingEnv, Element element) {
        return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
    }

    public static AnnotationValue getAnnotationValue(Element elem, Class<? extends Annotation> action) {
        return getAnnotationValue(elem, action.getName(), "value");
    }

    public static AnnotationValue getAnnotationValue(Element elem, String annotationClassName, String valueName) {
        for (AnnotationMirror am : elem.getAnnotationMirrors()) {
            if (annotationClassName.equals(
                    am.getAnnotationType().toString())) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                    if (valueName.equals(
                            entry.getKey().getSimpleName().toString())) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }

    public static TypeName getTypeName(Object classObjectOrString) {
        if (classObjectOrString instanceof String) {
            return ClassName.bestGuess((String) classObjectOrString);
        } else if (classObjectOrString instanceof DeclaredType) {
            return TypeName.get((DeclaredType) classObjectOrString);
        }
        return ClassName.get((Class<?>) classObjectOrString);
    }

    public static boolean hasAnnotation(Element elem, Class<? extends Annotation> action) {
        String actionName = action.getName();
        for (AnnotationMirror am : elem.getAnnotationMirrors()) {
            if (actionName.equals(
                    am.getAnnotationType().toString())) {
                return true;
            }
        }
        return false;
    }
}
