package edu.utexas.arlut.ciads.example;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Set;

import static com.google.common.collect.Sets.newLinkedHashSetWithExpectedSize;

@Slf4j
public class LinkedHashSet {
    public static void main(String[] args) throws GitAPIException, IOException {
        Set<String> s = newLinkedHashSetWithExpectedSize(5);

        s.add("bb");
        s.add("cc");
        s.add("dd");
        log.info("set {}", s);
        s.add("bb");
        log.info("set {}", s);
        s.add("ee");
        log.info("set {}", s);
        s.add("aa");
        log.info("set {}", s);




    }
}