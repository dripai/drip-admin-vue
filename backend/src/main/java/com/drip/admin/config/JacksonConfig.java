package com.drip.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper(SimpleModule longAsStringModule) {
        return new ObjectMapper().findAndRegisterModules().registerModule(longAsStringModule);
    }

    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer(tools.jackson.databind.module.SimpleModule jackson3LongAsStringModule) {
        return builder -> builder.addModule(jackson3LongAsStringModule);
    }

    @Bean
    public SimpleModule longAsStringModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        return module;
    }

    @Bean
    public tools.jackson.databind.module.SimpleModule jackson3LongAsStringModule() {
        tools.jackson.databind.module.SimpleModule module = new tools.jackson.databind.module.SimpleModule();
        module.addSerializer(Long.class, tools.jackson.databind.ser.std.ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, tools.jackson.databind.ser.std.ToStringSerializer.instance);
        return module;
    }
}
