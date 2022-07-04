package org.example;

import org.example.agent.DynamicInstrumentationLoader;
import org.example.aspect.aop.xml.AopXmlGen;

public class SecHARETool {

    public static void init() {
        // generate aop.xml
        AopXmlGen.execute();

        // dynamically attach java agent to jvm if not already present
        DynamicInstrumentationLoader.waitForInitialized();
    }

}
