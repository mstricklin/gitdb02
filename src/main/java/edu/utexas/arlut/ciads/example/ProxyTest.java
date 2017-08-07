package edu.utexas.arlut.ciads.example;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ProxyTest {

    public static void main(String[] args) {
        TestIF t = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(),
                                                   new Class<?>[] {TestIF.class},
                                                   new TestInvocationHandler(new TestImpl()));
        t.hello("Duke");
//        System.out.printf("t.hello(Duke): %s%n", t.hello("Duke"));
//        System.out.printf("t.toString(): %s%n", t);
//        System.out.printf("t.hashCode(): %H%n", t);
//        System.out.printf("t.equals(t): %B%n", t.equals(t));
//        System.out.printf("t.equals(new Object()): %B%n", t.equals(new Object()));
//        System.out.printf("t.equals(null): %B%n", t.equals(null));
    }

    // =================================


    interface TestIF {
        String hello(String name);
    }
    static class TestImpl implements TestIF {
        public String hello(String name) {
            log.info("TestImpl call hello({})", name);
            return String.format("Hello %s, this is %s %s", name, this, getClass().getSimpleName());
        }
    }
    // =================================
    static class TestInvocationHandler implements InvocationHandler {
        private Object testImpl;

        public TestInvocationHandler(Object impl) {
            this.testImpl = impl;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            log.info("TestInvocationHandler.invoke {}", method);
//            log.info("invoke {} {} {}", proxy, method, args);
            if(Object.class  == method.getDeclaringClass()) {
                String name = method.getName();
                if("equals".equals(name)) {
                    return proxy == args[0];
                } else if("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                } else if("toString".equals(name)) {
                    return proxy.getClass().getName() + "@" +
                            Integer.toHexString(System.identityHashCode(proxy)) +
                            ", with InvocationHandler " + this;
                } else {
                    throw new IllegalStateException(String.valueOf(method));
                }
            }
            return method.invoke(testImpl, args);
        }
    }
}