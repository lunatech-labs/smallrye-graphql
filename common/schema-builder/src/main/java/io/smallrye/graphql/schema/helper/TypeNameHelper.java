package io.smallrye.graphql.schema.helper;

import java.util.List;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * Helping with Name of types in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TypeNameHelper {
    private static final Logger LOG = Logger.getLogger(TypeNameHelper.class.getName());

    private TypeNameHelper() {
    }

    /**
     * Get the name for any type.
     * 
     * This will figure out the correct type based on the class info
     * 
     * @param referenceType initial reference type
     * @param classInfo the type class info
     * @param annotationsForThisClass annotations on this class
     * @return name of this type
     */
    public static String getAnyTypeName(ReferenceType referenceType, ClassInfo classInfo, Annotations annotationsForThisClass) {
        return getAnyTypeName(referenceType, classInfo, annotationsForThisClass, null);
    }

    public static String getAnyTypeName(ReferenceType referenceType, ClassInfo classInfo, Annotations annotationsForThisClass,
            String parametrizedTypeNameExtension) {
        if (Classes.isEnum(classInfo)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.ENUM, parametrizedTypeNameExtension);
        } else if (Classes.isInterface(classInfo)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.INTERFACE,
                    parametrizedTypeNameExtension);
        } else if (referenceType.equals(ReferenceType.TYPE)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.TYPE, parametrizedTypeNameExtension);
        } else if (referenceType.equals(ReferenceType.INPUT)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.INPUT, parametrizedTypeNameExtension,
                    INPUT);
        } else if (referenceType.equals(ReferenceType.SCALAR)) {
            return classInfo.name().withoutPackagePrefix();
        } else {
            LOG.warn("Using default name for " + classInfo.simpleName() + " [" + referenceType.name() + "]");
            return classInfo.name().withoutPackagePrefix();
        }
    }

    private static String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName,
            String parametrizedTypeNameExtension) {
        return getNameForClassType(classInfo, annotations, typeName, parametrizedTypeNameExtension, null);
    }

    private static String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName,
            String parametrizedTypeNameExtension, String postFix) {
        if (annotations.containsKeyAndValidValue(typeName)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(typeName);
            return annotationValue.asString().trim();
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotationValue(Annotations.NAME).asString().trim();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(classInfo.name().local());

        if (parametrizedTypeNameExtension != null)
            sb.append(parametrizedTypeNameExtension);
        if (postFix != null)
            sb.append(postFix);
        return sb.toString();
    }

    public static String createParametrizedTypeNameExtension(List<Type> parametrizedTypeArguments) {
        if (parametrizedTypeArguments == null || parametrizedTypeArguments.isEmpty())
            return null;
        StringBuilder sb = new StringBuilder();
        for (Type gp : parametrizedTypeArguments) {
            appendParametrizedArgumet(sb, gp);
        }
        return sb.toString();
    }

    public static String createParametrizedTypeNameExtension(Reference reference) {
        if (reference.getParametrizedTypeArguments() == null || reference.getParametrizedTypeArguments().isEmpty())
            return null;
        StringBuilder sb = new StringBuilder();
        for (Reference gp : reference.getParametrizedTypeArguments().values()) {
            sb.append("_");
            sb.append(gp.getName());
        }
        return sb.toString();
    }

    private static final void appendParametrizedArgumet(StringBuilder sb, Type gp) {
        sb.append("_");
        sb.append(gp.name().local());
        if (gp.kind().equals(Kind.PARAMETERIZED_TYPE)) {
            for (Type t : gp.asParameterizedType().arguments()) {
                appendParametrizedArgumet(sb, t);
            }
        }
    }

    private static final String INPUT = "Input";
}
