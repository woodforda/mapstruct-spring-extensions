package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.*;

public class ConversionServiceAdapterGenerator {
    private final Clock clock;

    public ConversionServiceAdapterGenerator(final Clock clock) {
        this.clock = clock;
    }

    public void writeConversionServiceAdapter(
            ConversionServiceAdapterDescriptor descriptor, Writer out) {
        try {
            JavaFile.builder(
                    descriptor.getAdapterClassName().packageName(),
                    createConversionServiceTypeSpec(descriptor))
                    .build()
                    .writeTo(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TypeSpec createConversionServiceTypeSpec(
            final ConversionServiceAdapterDescriptor descriptor) {
        final FieldSpec conversionServiceFieldSpec = buildConversionServiceFieldSpec();
        return TypeSpec.classBuilder(descriptor.getAdapterClassName())
                .addModifiers(PUBLIC)
                .addAnnotation(buildGeneratedAnnotationSpec())
                .addAnnotation(ClassName.get("org.springframework.stereotype", "Component"))
                .addField(conversionServiceFieldSpec)
                .addMethod(buildConstructorSpec(descriptor, conversionServiceFieldSpec))
                .addMethods(buildMappingMethods(descriptor, conversionServiceFieldSpec))
                .build();
    }

    private static MethodSpec buildConstructorSpec(final ConversionServiceAdapterDescriptor descriptor, final FieldSpec conversionServiceFieldSpec) {
        final ParameterSpec constructorParameterSpec = buildConstructorParameterSpec(descriptor, conversionServiceFieldSpec);
        return MethodSpec.constructorBuilder().addModifiers(PUBLIC).addParameter(constructorParameterSpec).addStatement("this.$N = $N", conversionServiceFieldSpec, constructorParameterSpec).build();
    }

    private static ParameterSpec buildConstructorParameterSpec(final ConversionServiceAdapterDescriptor descriptor, final FieldSpec conversionServiceFieldSpec) {
        final ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(conversionServiceFieldSpec.type, conversionServiceFieldSpec.name, FINAL);
        if (StringUtils.isNotEmpty(descriptor.getConversionServiceBeanName())) {
            parameterBuilder.addAnnotation(buildQualifierANnotation(descriptor));
        }
        return parameterBuilder.build();
    }

    private static AnnotationSpec buildQualifierANnotation(ConversionServiceAdapterDescriptor descriptor) {
        return AnnotationSpec
                .builder(ClassName.get("org.springframework.beans.factory.annotation", "Qualifier"))
                .addMember("value", "$S", descriptor.getConversionServiceBeanName())
                .build();
    }

    private static Iterable<MethodSpec> buildMappingMethods(
            final ConversionServiceAdapterDescriptor descriptor,
            final FieldSpec injectedConversionServiceFieldSpec) {
        return descriptor.getFromToMappings().stream()
                .map(
                        sourceTargetPair -> {
                            final ParameterSpec sourceParameterSpec =
                                    buildSourceParameterSpec(sourceTargetPair.getLeft());
                            return MethodSpec.methodBuilder(
                                    "map"
                                            + sourceTargetPair.getLeft().simpleName()
                                            + "To"
                                            + sourceTargetPair.getRight().simpleName())
                                    .addParameter(sourceParameterSpec)
                                    .addModifiers(PUBLIC)
                                    .returns(sourceTargetPair.getRight())
                                    .addStatement(
                                            "return $N.convert($N, $T.class)",
                                            injectedConversionServiceFieldSpec,
                                            sourceParameterSpec,
                                            sourceTargetPair.getRight())
                                    .build();
                        })
                .collect(toList());
    }

    private static ParameterSpec buildSourceParameterSpec(final TypeName sourceClassName) {
        return ParameterSpec.builder(sourceClassName, "source", FINAL).build();
    }

    private static FieldSpec buildConversionServiceFieldSpec() {
        return FieldSpec.builder(ClassName.get("org.springframework.core.convert", "ConversionService"), "conversionService", PRIVATE, FINAL).build();
    }

    private AnnotationSpec buildGeneratedAnnotationSpec() {
        return AnnotationSpec.builder(ClassName.get("javax.annotation", "Generated"))
                .addMember("value", "$S", ConversionServiceAdapterGenerator.class.getName())
                .addMember("date", "$S", DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now(clock)))
                .build();
    }
}
