// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;

@Slf4j
public class Serializer {
    public static Serializer of() {
        return new Serializer();
    }
    private Serializer() {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }
    public String serialize(Object o) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
//            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("Error serializing Object {} {}", o.getClass().getSimpleName(), e);
        }
        return "";
    }
    public <T> T deserialize(String s, Class<T> clazz) {
        try {
            return (T)mapper.readValue(s, clazz);
        } catch (IOException e) {
            log.error("Error deserializing to type {} {}", clazz.getSimpleName(), e);
            log.error("\t{}", s);
            return null;
        }
    }

    ObjectId getSHA(String in) {
        ObjectInserter.Formatter formatter = new ObjectInserter.Formatter();
        byte[] b = in.getBytes();
        return formatter.idFor(OBJ_BLOB, in.getBytes(), 0, in.length());
    }

    private final ObjectMapper mapper = new ObjectMapper();

}
