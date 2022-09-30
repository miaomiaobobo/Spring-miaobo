package com.miaobo.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;
import com.spring.Scope;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component("miaoboBeanPostProcessor")
@Scope("prototype")
public class MiaoboBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessorBeforeInitializing(Object bean, String beanName) {
        System.out.println("初始化前");
        //只针对某个Bean处理需要判断名字
        if (beanName.equals("userService")) {
            ((UserService)bean).setName("小狗");
        }
        return bean;
    }

    @Override
    public Object postProcessorAfterInitializing(Object bean, String beanName) {
        System.out.println("初始化后");
        //针对某个类中的方法进行动态代理
        if (beanName.equals("userService")) {
            Object proxyInstance = Proxy.newProxyInstance(MiaoboBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理步骤执行");
                    return method.invoke(bean, args);//执行被代理Bean对象的方法
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
