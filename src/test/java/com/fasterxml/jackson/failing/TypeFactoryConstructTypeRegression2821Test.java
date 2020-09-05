package com.fasterxml.jackson.failing;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

// Tests for [#2821]
public class TypeFactoryConstructTypeRegression2821Test extends BaseMapTest {
    static final class Wrapper {
        // if Entity<?> -> Entity , the test passes
        private final List<Entity<?>> entities;

        @JsonCreator
        public Wrapper(List<Entity<?>> entities) {
            this.entities = entities;
        }

        public List<Entity<?>> getEntities() {
            return this.entities;
        }
    }

    public static class Entity<T> {
        @JsonIgnore
        private final Attributes attributes;

        private final T data;

        public Entity(Attributes attributes, T data) {
            this.attributes = attributes;
            this.data = data;
        }

        // if @JsonUnwrapped is removed, the test passes
        @JsonUnwrapped
        public Attributes getAttributes() {
            return attributes;
        }

        public T getData() {
            return data;
        }

        @JsonCreator
        public static <T> Entity<T> create(Attributes attributes, T data) {
            return new Entity<>(attributes, data);
        }
    }

    public static class Attributes {
        private final String id;

        public Attributes(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @JsonCreator
        public static Attributes create(String id) {
            return new Attributes(id);
        }

        // if this method is removed, the test passes
        public static Attributes dummyMethod(Map attributes) {
            return null;
        }
    }

    // this test passes with Jackson 2.11.1, but fails with Jackson 2.11.2
    public void testReproduceSerializerBug() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        Entity<String> entity = new Entity<>(new Attributes("id"), "hello");
        Wrapper val = new Wrapper(Collections.<Entity<?>>singletonList(entity));
        // fails with com.fasterxml.jackson.databind.JsonMappingException: Strange Map type java.util.Map: cannot determine type parameters (through reference chain: com.github.lhotari.jacksonbug.JacksonBugIsolatedTest$Wrapper["entities"]->java.util.Collections$SingletonList[0]->com.github.lhotari.jacksonbug.JacksonBugIsolatedTest$Entity["attributes"])
        System.out.println(objectMapper.writeValueAsString(val));
    }
}
