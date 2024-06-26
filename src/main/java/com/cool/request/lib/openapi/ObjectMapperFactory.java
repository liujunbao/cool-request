package com.cool.request.lib.openapi;

import com.cool.request.lib.openapi.media.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectMapperFactory {

    public static ObjectMapper createJson(JsonFactory jsonFactory) {
        return create(jsonFactory, false);
    }

    public static ObjectMapper createJson() {
        return create(null, false);
    }

    public static ObjectMapper createYaml(YAMLFactory yamlFactory) {
        return create(yamlFactory, false);
    }

    public static ObjectMapper createYaml() {
        return createYaml(false);
    }

    public static ObjectMapper createYaml(boolean openapi31) {
        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        factory.enable(YAMLGenerator.Feature.SPLIT_LINES);
        factory.enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS);

        return create(factory, openapi31);
    }

    public static ObjectMapper createJson31(JsonFactory jsonFactory) {
        return create(jsonFactory, true);
    }

    public static ObjectMapper createJson31() {
        return create(null, true);
    }

    public static ObjectMapper createYaml31(YAMLFactory yamlFactory) {
        return create(yamlFactory, true);
    }

    public static ObjectMapper createYaml31() {
        return createYaml(true);
    }

    public static ObjectMapper create(JsonFactory jsonFactory, boolean openapi31) {
        ObjectMapper mapper = jsonFactory == null ? new ObjectMapper() : new ObjectMapper(jsonFactory);

        if (!openapi31) {
            // handle ref schema serialization skipping all other props
            mapper.registerModule(new SimpleModule() {
                @Override
                public void setupModule(SetupContext context) {
                    super.setupModule(context);
                    context.addBeanSerializerModifier(new BeanSerializerModifier() {
                        @Override
                        public JsonSerializer<?> modifySerializer(
                                SerializationConfig config, BeanDescription desc, JsonSerializer<?> serializer) {
                            if (Schema.class.isAssignableFrom(desc.getBeanClass())) {
                                return new SchemaSerializer((JsonSerializer<Object>) serializer);
                            } else if (MediaType.class.isAssignableFrom(desc.getBeanClass())) {
                                return new MediaTypeSerializer((JsonSerializer<Object>) serializer);
                            } else if (Example.class.isAssignableFrom(desc.getBeanClass())) {
                                return new ExampleSerializer((JsonSerializer<Object>) serializer);
                            }
                            return serializer;
                        }
                    });
                }
            });
        } else {
            mapper.registerModule(new SimpleModule() {
                @Override
                public void setupModule(SetupContext context) {
                    super.setupModule(context);
                    context.addBeanSerializerModifier(new BeanSerializerModifier() {
                        @Override
                        public JsonSerializer<?> modifySerializer(
                                SerializationConfig config, BeanDescription desc, JsonSerializer<?> serializer) {
                            if (Schema.class.isAssignableFrom(desc.getBeanClass())) {
                                return new Schema31Serializer((JsonSerializer<Object>) serializer);
                            } else if (MediaType.class.isAssignableFrom(desc.getBeanClass())) {
                                return new MediaTypeSerializer((JsonSerializer<Object>) serializer);
                            } else if (Example.class.isAssignableFrom(desc.getBeanClass())) {
                                return new ExampleSerializer((JsonSerializer<Object>) serializer);
                            }
                            return serializer;
                        }
                    });
                }
            });
        }

        if (!openapi31) {
            Module deserializerModule = new DeserializationModule();
            mapper.registerModule(deserializerModule);
        } else {
            Module deserializerModule = new DeserializationModule31();
            mapper.registerModule(deserializerModule);
        }
        mapper.registerModule(new JavaTimeModule());

        Map<Class<?>, Class<?>> sourceMixins = new LinkedHashMap<>();

        sourceMixins.put(ApiResponses.class, ExtensionsMixin.class);
        sourceMixins.put(Contact.class, ExtensionsMixin.class);
        sourceMixins.put(Encoding.class, ExtensionsMixin.class);
        sourceMixins.put(EncodingProperty.class, ExtensionsMixin.class);
        sourceMixins.put(Example.class, ExampleMixin.class);
        sourceMixins.put(ExternalDocumentation.class, ExtensionsMixin.class);
        sourceMixins.put(Link.class, ExtensionsMixin.class);
        sourceMixins.put(LinkParameter.class, ExtensionsMixin.class);
        sourceMixins.put(MediaType.class, MediaTypeMixin.class);
        sourceMixins.put(OAuthFlow.class, ExtensionsMixin.class);
        sourceMixins.put(OAuthFlows.class, ExtensionsMixin.class);
        sourceMixins.put(Operation.class, OperationMixin.class);
        sourceMixins.put(PathItem.class, ExtensionsMixin.class);
        sourceMixins.put(Paths.class, ExtensionsMixin.class);
        sourceMixins.put(Scopes.class, ExtensionsMixin.class);
        sourceMixins.put(Server.class, ExtensionsMixin.class);
        sourceMixins.put(ServerVariable.class, ExtensionsMixin.class);
        sourceMixins.put(ServerVariables.class, ExtensionsMixin.class);
        sourceMixins.put(Tag.class, ExtensionsMixin.class);
        sourceMixins.put(XML.class, ExtensionsMixin.class);
        sourceMixins.put(ApiResponse.class, ExtensionsMixin.class);
        sourceMixins.put(Parameter.class, ExtensionsMixin.class);
        sourceMixins.put(RequestBody.class, ExtensionsMixin.class);
        sourceMixins.put(Header.class, ExtensionsMixin.class);
        sourceMixins.put(SecurityScheme.class, ExtensionsMixin.class);
        sourceMixins.put(Callback.class, ExtensionsMixin.class);


        if (!openapi31) {
            sourceMixins.put(Schema.class, SchemaMixin.class);
            sourceMixins.put(DateSchema.class, DateSchemaMixin.class);
            sourceMixins.put(Components.class, ComponentsMixin.class);
            sourceMixins.put(Info.class, InfoMixin.class);
            sourceMixins.put(License.class, LicenseMixin.class);
            sourceMixins.put(OpenAPI.class, OpenAPIMixin.class);
            sourceMixins.put(Discriminator.class, DiscriminatorMixin.class);
        } else {
            sourceMixins.put(Info.class, ExtensionsMixin.class);
            sourceMixins.put(Schema.class, Schema31Mixin.class);
            sourceMixins.put(Components.class, Components31Mixin.class);
            sourceMixins.put(OpenAPI.class, OpenAPI31Mixin.class);
            sourceMixins.put(DateSchema.class, DateSchemaMixin.class);
            sourceMixins.put(Discriminator.class, Discriminator31Mixin.class);
        }
        mapper.setMixIns(sourceMixins);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }

    public static ObjectMapper createJsonConverter() {

        ObjectMapper mapper = new ObjectMapper();


        Module deserializerModule = new DeserializationModule();
        mapper.registerModule(deserializerModule);
        mapper.registerModule(new JavaTimeModule());

        Map<Class<?>, Class<?>> sourceMixins = new LinkedHashMap<>();

        sourceMixins.put(ApiResponses.class, ExtensionsMixin.class);
        sourceMixins.put(ApiResponse.class, ExtensionsMixin.class);
        sourceMixins.put(Callback.class, ExtensionsMixin.class);
        sourceMixins.put(Components.class, ComponentsMixin.class);
        sourceMixins.put(Contact.class, ExtensionsMixin.class);
        sourceMixins.put(Encoding.class, ExtensionsMixin.class);
        sourceMixins.put(EncodingProperty.class, ExtensionsMixin.class);
        sourceMixins.put(Example.class, ExampleMixin.class);
        sourceMixins.put(ExternalDocumentation.class, ExtensionsMixin.class);
        sourceMixins.put(Header.class, ExtensionsMixin.class);
        sourceMixins.put(Info.class, ExtensionsMixin.class);
        sourceMixins.put(License.class, ExtensionsMixin.class);
        sourceMixins.put(Link.class, ExtensionsMixin.class);
        sourceMixins.put(LinkParameter.class, ExtensionsMixin.class);
        sourceMixins.put(MediaType.class, MediaTypeMixin.class);
        sourceMixins.put(OAuthFlow.class, ExtensionsMixin.class);
        sourceMixins.put(OAuthFlows.class, ExtensionsMixin.class);
        sourceMixins.put(OpenAPI.class, OpenAPIMixin.class);
        sourceMixins.put(Operation.class, OperationMixin.class);
        sourceMixins.put(Parameter.class, ExtensionsMixin.class);
        sourceMixins.put(PathItem.class, ExtensionsMixin.class);
        sourceMixins.put(Paths.class, ExtensionsMixin.class);
        sourceMixins.put(RequestBody.class, ExtensionsMixin.class);
        sourceMixins.put(Scopes.class, ExtensionsMixin.class);
        sourceMixins.put(SecurityScheme.class, ExtensionsMixin.class);
        sourceMixins.put(Server.class, ExtensionsMixin.class);
        sourceMixins.put(ServerVariable.class, ExtensionsMixin.class);
        sourceMixins.put(ServerVariables.class, ExtensionsMixin.class);
        sourceMixins.put(Tag.class, ExtensionsMixin.class);
        sourceMixins.put(XML.class, ExtensionsMixin.class);

        sourceMixins.put(Schema.class, SchemaConverterMixin.class);
        mapper.setMixIns(sourceMixins);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }


    public static ObjectMapper buildStrictGenericObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        try {
            mapper.configure(DeserializationFeature.valueOf("FAIL_ON_TRAILING_TOKENS"), true);
        } catch (Throwable e) {
            // add only if supported by Jackson version 2.9+
        }
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

}
