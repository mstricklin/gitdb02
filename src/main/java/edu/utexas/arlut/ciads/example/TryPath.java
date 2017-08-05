package edu.utexas.arlut.ciads.example;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

@Slf4j
public class TryPath {
    public static void main(String[] args) {
        java.nio.file.Path p0 = Paths.get("00", "00", "00", "00000001");
        java.nio.file.Path p1 = Paths.get("01", "00", "00", "00000001");
        java.nio.file.Path p2 = Paths.get("02", "00", "00", "00000001");
        java.nio.file.Path p3 = Paths.get("03", "00", "00", "00000001");
        Set<java.nio.file.Path> pSet = newHashSet(p0, p1, p2, p3);
//        Ints.max()
//        Set<TryPath> parents = newLinkedHashSetWithExpectedSize(pSet.size()*3);
        List<java.nio.file.Path> parents = newArrayList();
        for (java.nio.file.Path p : pSet) {
            if (!parents.contains(p.getParent()))
                parents.add(p.getParent());
        }

        log.info("pSet {}", pSet);
        log.info("parents {}", parents);
        for (int i = 0; null != parents.get(i); i++) {
            java.nio.file.Path p = parents.get(i);
            log.info("\t{} {}", p, p.getParent());
            if (!parents.contains(p.getParent()))
                parents.add(p.getParent());
        }
        log.info("parents {}", parents);

        Path p = Paths.get("01/02/03/00000004");
        log.info("p: {} {}", p, p.getParent());


//        TryPath pz = Paths.get("00/00/00/00000002");
//        log.info("pz {}", pz);
        for (Path sp : parentPaths(p))
            log.info("sp {}", sp);

    }

    static Iterable<Path> parentPaths(Path path) {
        List<Path> paths = newArrayList();
        Path p = path;
        while (null != (p = p.getParent())) {
            paths.add(p);
        }
        return paths;
    }
}
