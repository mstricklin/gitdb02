package edu.utexas.arlut.ciads.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App1 {
    public static void main(String[] args) {
        log.info("Foo!");
        C c0 = new C();
        log.info("C.m() {}", c0.m());
        log.info("C.s() {}", c0.s());
    }

    public static class P {
        static String s() {
            return "static P";
        }
        String m() {
            return "P";
        }
    }
    public static class C extends P {
        String m() {
            return "C";
        }
    }
}