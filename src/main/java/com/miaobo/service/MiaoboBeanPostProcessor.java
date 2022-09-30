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
        System.out.println("��ʼ��ǰ");
        //ֻ���ĳ��Bean������Ҫ�ж�����
        if (beanName.equals("userService")) {
            ((UserService)bean).setName("С��");
        }
        return bean;
    }

    @Override
    public Object postProcessorAfterInitializing(Object bean, String beanName) {
        System.out.println("��ʼ����");
        //���ĳ�����еķ������ж�̬����
        if (beanName.equals("userService")) {
            Object proxyInstance = Proxy.newProxyInstance(MiaoboBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("������ִ��");
                    return method.invoke(bean, args);//ִ�б�����Bean����ķ���
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
