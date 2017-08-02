package edu.utexas.arlut.ciads.example;

import edu.utexas.arlut.ciads.FrameA;
import edu.utexas.arlut.ciads.repo.IKeyed;
import edu.utexas.arlut.ciads.repo.JSONSerializer;
import edu.utexas.arlut.ciads.repo.Serializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App3 {
    public static void main(String[] args) {
        log.info("App3");

        IKeyed k0 = FrameA.builder("shazam").s0("zero").s1("one").s2("two").build();
//        FrameA fa0 = ds.add(k0);
        log.info("k0 {}", k0);

        Serializer s = JSONSerializer.of();
//        byte[] b = s.serialize(af0.i);

//        log.info("{}", new String(b));
    }
}
