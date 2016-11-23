package com.github.brunodles.environmentsmods.processor;

import com.github.brunodles.annotationprocessorhelper.ProcessorBase;
import com.github.brunodles.annotationprocessorhelper.SupportedAnnotations;
import com.github.brunodles.environmentmods.annotation.ModFor;
import com.github.brunodles.environmentmods.annotation.Moddable;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@SupportedAnnotations({Moddable.class, ModFor.class})
public class ModsProcessor extends ProcessorBase {

    private static final String TAG = "ModsProcessor";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        HashCollection<TypeMirror, ExecutableElement> classes = findAnnotations(annotations, roundEnv);

        for (TypeMirror target : classes.keySet()) {
            TypeSpec modClass = createModClass(classes, target);
            writeModClass(target, modClass);
        }
        return true;
    }

    private HashCollection<TypeMirror, ExecutableElement> findAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        HashCollection<TypeMirror, ExecutableElement> classes = new HashCollection<>();
        for (TypeElement annotation : annotations)
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                ModFor modFor = element.getAnnotation(ModFor.class);
                if (modFor != null)
                    classes.add(getTarget(modFor), (ExecutableElement) element);
                Moddable moddable = element.getAnnotation(Moddable.class);
                if (moddable != null)
                    classes.add(element.asType());
            }
        return classes;
    }

    private TypeSpec createModClass(HashCollection<TypeMirror, ExecutableElement> classes, TypeMirror target) {
        TypeElement targetElement = asTypeElement(target);
        MethodSpec.Builder applyBuilder = MethodSpec.methodBuilder("apply")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ClassName.get(target), "target");

        for (ExecutableElement element : classes.getValues(target))
            applyBuilder.addStatement(invoke(target, element));

        return TypeSpec.classBuilder(getModClassName(targetElement))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(applyBuilder.build())
                .build();
    }

    private void writeModClass(TypeMirror target, TypeSpec modClass) {
        TypeElement targetElement = asTypeElement(target);
        JavaFile javaFile = JavaFile.builder(packageFrom(targetElement), modClass)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            fatalError(TAG, "Failed to write a mod class for " + targetElement.getSimpleName() + "\n " + e.getMessage());
        }
    }

    private ClassName getModClassName(TypeElement targetElement) {
        return ClassName.get(packageFrom(targetElement), targetElement.getSimpleName() + "Mods");
    }

    private String packageFrom(TypeElement element) {
        String name = element.getQualifiedName().toString();
        return name.substring(0, name.lastIndexOf("."));
    }

    private String invoke(TypeMirror target, ExecutableElement element) {
        String methodName = element.getSimpleName().toString();
        TypeElement methodClass = asTypeElement(element.getEnclosingElement().asType());

        Set<Modifier> modifiers = element.getModifiers();
        if (!modifiers.contains(Modifier.STATIC)) {
            fatalError(TAG, "The method \"" + methodName + "\" on the class \""
                    + methodClass.getSimpleName().toString() +
                    "\" annotated with @ModFor is not static. Make it static to be runnable.");
        }
        if (!modifiers.contains(Modifier.PUBLIC)) {
            fatalError(TAG, "The method \"" + methodName + "\" on the class \""
                    + methodClass.getSimpleName().toString() +
                    "\" annotated with @ModFor is not public. Make it public to be runnable.");
        }
        StringBuilder fullMethodNameBuilder = new StringBuilder(methodClass.getQualifiedName().toString());
        fullMethodNameBuilder.append(".")
                .append(methodName)
                .append("(");

        ExecutableType exec = (ExecutableType) element.asType();
        List<? extends TypeMirror> parameters = exec.getParameterTypes();
        if ((parameters.size() == 1)
                && (target.equals(parameters.get(0))))
            fullMethodNameBuilder.append("target");

        fullMethodNameBuilder.append(")");
        return fullMethodNameBuilder.toString();
    }

    private static TypeMirror getTarget(ModFor annotation) {
        try {
            annotation.value();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null;
    }
}
