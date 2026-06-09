package processor;

import annotations.GenerateJsonSerializer;
import annotations.JsonField;
import annotations.JsonIgnore;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes("annotations.GenerateJsonSerializer")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class JsonProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("JSON Processor started!");

        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateJsonSerializer.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                System.err.println("Only classes can be annotated with GenerateJsonSerializer");
                continue;
            }

            System.out.println("Processing: " + element.getSimpleName());
            generateFullSerializer(element);
        }
        return true;
    }

    private void generateFullSerializer(Element element) {
        try {
            String packageName = processingEnv.getElementUtils().getPackageOf(element).toString();
            String className = element.getSimpleName().toString();
            String serializerName = className + "Serializer";

            StringBuilder serializerLogic = new StringBuilder();

            for (Element enclosed : element.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.FIELD) {
                    VariableElement field = (VariableElement) enclosed;

                    if (field.getAnnotation(JsonIgnore.class) != null) {
                        continue;
                    }

                    String fieldName = field.getSimpleName().toString();
                    JsonField jsonFieldAnn = field.getAnnotation(JsonField.class);
                    if (jsonFieldAnn != null && !jsonFieldAnn.name().isEmpty()) {
                        fieldName = jsonFieldAnn.name();
                    }

                    TypeMirror fieldType = field.asType();
                    boolean isString = fieldType.toString().equals("java.lang.String");
                    boolean isPrimitive = fieldType.getKind().isPrimitive();

                    String getterName = getGetterName(field);

                    serializerLogic.append("        if (!first) sb.append(\",\");\n");
                    serializerLogic.append("        sb.append(\"\\\"" + fieldName + "\\\":\");\n");

                    if (isPrimitive) {
                        serializerLogic.append("        sb.append(obj." + getterName + "());\n");
                    } else if (isString) {
                        serializerLogic.append("        if (obj." + getterName + "() == null) {\n");
                        serializerLogic.append("            sb.append(\"null\");\n");
                        serializerLogic.append("        } else {\n");
                        serializerLogic.append("            sb.append(\"\\\"\").append(escapeJson(obj." + getterName + "())).append(\"\\\"\");\n");
                        serializerLogic.append("        }\n");
                    } else {
                        serializerLogic.append("        if (obj." + getterName + "() == null) {\n");
                        serializerLogic.append("            sb.append(\"null\");\n");
                        serializerLogic.append("        } else {\n");
                        serializerLogic.append("            sb.append(obj." + getterName + "());\n");
                        serializerLogic.append("        }\n");
                    }

                    serializerLogic.append("        first = false;\n");
                }
            }

            String code = generateSerializerCode(packageName, serializerName, className, serializerLogic.toString());

            JavaFileObject file = packageName.isEmpty() ?
                    processingEnv.getFiler().createSourceFile(serializerName) :
                    processingEnv.getFiler().createSourceFile(packageName + "." + serializerName);

            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                out.print(code);
            }

            System.out.println("Generated: " + serializerName);

        } catch (Exception e) {
            System.err.println("Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getGetterName(VariableElement field) {
        String fieldName = field.getSimpleName().toString();
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    private String generateSerializerCode(String packageName, String serializerName,
                                          String className, String fieldLogic) {
        String packageDecl = (packageName != null && !packageName.isEmpty()) ?
                "package " + packageName + ";\n\n" : "";

        return packageDecl +
                "public class " + serializerName + " {\n" +
                "    private static String escapeJson(String s) {\n" +
                "        if (s == null) return \"\";\n" +
                "        return s.replace(\"\\\"\", \"\\\\\\\"\")\n" +
                "                 .replace(\"\\n\", \"\\\\n\")\n" +
                "                 .replace(\"\\r\", \"\\\\r\")\n" +
                "                 .replace(\"\\t\", \"\\\\t\");\n" +
                "    }\n" +
                "\n" +
                "    public static String toJson(" + className + " obj) {\n" +
                "        if (obj == null) return \"null\";\n" +
                "\n" +
                "        StringBuilder sb = new StringBuilder();\n" +
                "        sb.append(\"{\");\n" +
                "        boolean first = true;\n" +
                fieldLogic +
                "        sb.append(\"}\");\n" +
                "        return sb.toString();\n" +
                "    }\n" +
                "}\n";
    }
}