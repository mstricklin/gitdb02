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

        String p = "00/00/00/00000017";
        getKey(p);

    }

    static Integer getKey(String p) {
        int lI = 1+p.lastIndexOf('/');
        String subP = p.substring(lI);
        log.info("{} => {}", p, subP);
        Integer i = Integer.parseInt(subP, 16);
        log.info("parsed {}", i);
        return 7;
    }
}
