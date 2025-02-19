package com.demo;

import com.demo.transformer.DeserializeTransformer;

import java.lang.instrument.Instrumentation;

public class AgentTemplate {
    public static void premain(String args, Instrumentation inst) {
        inst.addTransformer(new DeserializeTransformer());    // 反序列化BlackList
    }
}