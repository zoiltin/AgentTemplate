package com.demo.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

// 每当类被加载，就会调用 transform 函数
public class DeserializeTransformer implements ClassFileTransformer {
    private static final String targetClassName = "java.io.ObjectInputStream";
    private static final String targetMethodName = "resolveClass";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(targetClassName.equals(className.replace('/', '.'))){
            try{
                ClassPool pool = new ClassPool(true);
                pool.appendClassPath(new LoaderClassPath(loader));
                CtClass clazz = pool.getCtClass(targetClassName);
                clazz.defrost();
                CtMethod method = clazz.getDeclaredMethod(targetMethodName);

                // 添加黑名单检测
                method.insertBefore("if ($1 instanceof java.io.ObjectStreamClass){\n" +
                        "    java.util.Set BLACKLISTED_CLASSES = new java.util.HashSet();\n" +
                        "    BLACKLISTED_CLASSES.add(\"java.lang.Runtime\");\n" +
                        "    BLACKLISTED_CLASSES.add(\"java.lang.ProcessBuilder\");\n" +
                        "    BLACKLISTED_CLASSES.add(\"com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl\");\n" +
                        "    BLACKLISTED_CLASSES.add(\"java.security.SignedObject\");\n" +
                        "    BLACKLISTED_CLASSES.add(\"com.sun.jndi.ldap.LdapAttribute\");\n" +
                        "    BLACKLISTED_CLASSES.add(\"org.apache.commons.collections.functors.InvokerTransformer\");\n" +
                        "    BLACKLISTED_CLASSES.add(\"org.apache.commons.collections.map.LazyMap\");\n" +
                        "    BLACKLISTED_CLASSES.add(\"org.apache.commons.collections4.functors.InvokerTransformer\");\n" +
                        "    BLACKLISTED_CLASSES.add(\"org.apache.commons.collections4.map.LazyMap\");\n" +
                        "    BLACKLISTED_CLASSES.add(\"javax.management.BadAttributeValueExpException\");\n" +
                        "\n" +
                        "    if (BLACKLISTED_CLASSES.contains($1.getName())) {\n" +
                        "        throw new java.lang.SecurityException(\"Deserialize Blacklist.\");\n" +
                        "    }\n" +
                        "}");
                return clazz.toBytecode();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return new byte[0];
    }
}