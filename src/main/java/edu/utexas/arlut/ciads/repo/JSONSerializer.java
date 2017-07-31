package edu.utexas.arlut.ciads.repo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class JSONSerializer implements Serializer {

    public static Serializer of() {
        return new JSONSerializer();
    }

    //    @Override
//    public String serialize(Object o) {
//        try {
//            //            return mapper.writeValueAsString(o);
//            return tlMapper.get().writerWithDefaultPrettyPrinter().writeValueAsString(o);
//        } catch (JsonProcessingException e) {
//            log.error("Error serializing Object {} {}", o.getClass().getSimpleName(), e);
//        }
//        return "";
//    }
    private static final byte[] NO_BYTES = {};

    @Override
    public byte[] serialize(Object o) {
        try {
            return tlMapper.get().writerWithDefaultPrettyPrinter().writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            log.error("Error serializing Object {} {}", o.getClass().getSimpleName(), e);
        }
        return NO_BYTES;
    }

    @Override
    public <T> T deserialize(byte[] b, Class<T> clazz) {
        try {
            return (T) tlMapper.get().readValue(b, clazz);
        } catch (IOException e) {
            log.error("Error deserializing to type {} {}", clazz.getSimpleName(), e);
            log.error("\t{}", b);
            return null;
        }
    }

    @Override
    public <T> T deserialize(String s, Class<T> clazz) {
        return deserialize(s.getBytes(), clazz);
    }

    private static ThreadLocal<ObjectMapper> tlMapper = new ThreadLocal<ObjectMapper>() {
        @Override
        protected ObjectMapper initialValue() {
            ObjectMapper m = new ObjectMapper();
//            can't resolve jackson-datatypes-collections???
//                    .registerModule(new GuavaModule());
            m.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            return m;
        }
    };
}
