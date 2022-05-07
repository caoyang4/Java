package src.basis.annotation;

import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import com.google.auto.service.AutoService;
import src.basis.annotation.CheckGetter;

@AutoService(Processor.class)
@SupportedAnnotationTypes("src.basis.annotation.CheckGetter")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CheckGetterProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotatedClass : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(CheckGetter.class))) {
            for (VariableElement field : ElementFilter.fieldsIn(annotatedClass.getEnclosedElements())) {
                if (!containsGetter(annotatedClass, field.getSimpleName().toString())) {
                    processingEnv.getMessager().printMessage(Kind.ERROR,
                            String.format("getter class not found for '%s.%s'.", annotatedClass.getSimpleName(), field.getSimpleName()));
                }
            }
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(CheckGetter.class);
        for (Element element:elements){
            ElementKind kind = element.getKind();
            if(kind == ElementKind.FIELD){
                VariableElement variableElement = (VariableElement)element;
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                if(!containsGetter(typeElement,variableElement.getSimpleName().toString())){
                    processingEnv.getMessager().printMessage(Kind.ERROR,
                            String.format("getter field not found for '%s.%s'.", typeElement.getSimpleName(), variableElement.getSimpleName()));
                }
            }
        }

        return true;
    }

    private static boolean containsGetter(TypeElement typeElement, String name) {
        String getter = "get" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (!executableElement.getModifiers().contains(Modifier.STATIC)
                    && executableElement.getSimpleName().toString().equals(getter)
                    && executableElement.getParameters().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
