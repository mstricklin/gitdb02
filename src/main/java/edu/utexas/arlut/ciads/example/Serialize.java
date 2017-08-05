package edu.utexas.arlut.ciads.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class Serialize {

    static ObjectMapper m;
    public static void main(String[] args) throws IOException {

        m = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD,JsonAutoDetect.Visibility.ANY);

//        m.enableDefaultTyping(); // default to using DefaultTyping.OBJECT_AND_NON_CONCRETE
//        m.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        m.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
//        m.writeTree();

        A a0 = new A("0One", "0Two", "0Three");
        String s = m.writerWithDefaultPrettyPrinter().writeValueAsString(a0);
        log.info(s);

        A a1 = get(s);
        log.info("{}", a1);
    }
    static <T extends AI> T get(String s) throws IOException {
        T a1 = (T)m.readValue(s, AI.class);
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