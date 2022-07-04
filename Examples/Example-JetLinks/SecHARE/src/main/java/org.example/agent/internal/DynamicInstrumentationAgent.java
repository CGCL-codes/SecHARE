package org.example.agent.internal;

import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public final class DynamicInstrumentationAgent {

    private static Instrumentation s_instrumentation;
    private static ClassFileTransformer s_transformer = new ClassPreProcessorAgentAdapter();

    private DynamicInstrumentationAgent() {
    }

    public static void premain(final String args, final Instrumentation instrumentation) throws Exception {
        if (s_instrumentation == null) {
            s_instrumentation = instrumentation;
            s_instrumentation.addTransformer(s_transformer);
        }
    }

    public static void agentmain(final String args, final Instrumentation instrumentation) throws Exception {
        premain(args, instrumentation);
    }

    public static Instrumentation getInstrumentation() {
        if (s_instrumentation == null) {
            throw new UnsupportedOperationException("AspectJ weaving agent was neither started via '-javaagent' (preMain) nor attached via 'VirtualMachine.loadAgent' (agentMain)");
        } else {
            return s_instrumentation;
        }
    }

}
