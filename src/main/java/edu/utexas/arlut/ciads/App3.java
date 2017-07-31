package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.repo.JSONSerializer;
import edu.utexas.arlut.ciads.repo.Serializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App3 {
    public static void main(String[] args) {
        log.info("App3");

        AFrame af0 = new AFrame(1, "1aa", "1bb", "1cc");
        log.info("af0 {}", af0);

        Serializer s = JSONSerializer.of();
        byte[] b = s.serialize(af0.i);

        log.info("{}", new String(b));
    }
}
