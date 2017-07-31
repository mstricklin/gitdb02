package edu.utexas.arlut.ciads.example;

import edu.utexas.arlut.ciads.FrameA;
import edu.utexas.arlut.ciads.repo.JSONSerializer;
import edu.utexas.arlut.ciads.repo.Serializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App3 {
    public static void main(String[] args) {
        log.info("App3");

        FrameA af0 = new FrameA(1, "1aa", "1bb", "1cc");
        log.info("af0 {}", af0);

        Serializer s = JSONSerializer.of();
//        byte[] b = s.serialize(af0.i);

//        log.info("{}", new String(b));
    }
}
