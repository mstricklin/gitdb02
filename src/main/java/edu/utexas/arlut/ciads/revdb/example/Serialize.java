package edu.utexas.arlut.ciads.revdb.example;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class Serialize {

    static ObjectMapper m;

    public static void main(String[] args) throws IOException {

        m = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        m.enableDefaultTyping(); // default to using DefaultTyping.OBJECT_AND_NON_CONCRETE
//        m.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        m.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
//        m.writeTree();
        m.addMixIn(AI.class, KeyMixin.class);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        A a0 = new A("0One", "0Two", "0Three");
        String s0 = m.writerWithDefaultPrettyPrinter().writeValueAsString(a0);
        log.info("s0 {}",s0);

//        JsonNode tree = mapper.valueToTree(value);

        ObjectWriter writer = m.writerFor(AI.class);
//                               .withAttribute("key", 27);
        String s1 = writer.withAttribute("key", 28).writeValueAsString(a0);
        log.info("s1 {}", s1);

                A a1 = get(s1);
        log.info("a1 {}", a1);
    }

    @JsonAppend(
            attrs = {
                    @JsonAppend.Attr(value = "key")
            }
    )
//    @JsonIgnoreProperties(value={ "key" }, allowSetters=true)
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    public abstract static class KeyMixin {
        @JsonIgnore
        private String key;
        @JsonIgnore
        public abstract void setKey(String k);
    }

    static <T extends AI> T get(String s) throws IOException {
        T a1 = (T) m.readValue(s, AI.class);
        return a1;
    }

    interface AI {
    }

    @RequiredArgsConstructor
    @ToString
    static class A implements AI {
        final String s0;
        final String s1;
        final String s2;
        static final String type = "A";
        static final String clazz = A.class.getName();

    }


}