package edu.utexas.arlut.ciads.revdb.example;

import edu.utexas.arlut.ciads.revdb.main.FrameA;
import edu.utexas.arlut.ciads.revdb.RevDBItem;
import edu.utexas.arlut.ciads.revdb.impl.JSONSerializer;
import edu.utexas.arlut.ciads.revdb.Serializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App3 {
    public static void main(String[] args) {
        log.info("App3");

        RevDBItem k0 = FrameA.builder("shazam").s0("zero").s1("one").s2("two").build();
//        FrameA fa0 = ds.persist(k0);
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
