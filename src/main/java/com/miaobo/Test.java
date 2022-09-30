package com.miaobo;

import com.miaobo.service.UserService;
import com.miaobo.service.UserServiceInterface;
import com.spring.MiaoboApplicationContext;

import java.lang.reflect.InvocationTargetException;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        MiaoboApplicationContext applicationContext = new MiaoboApplicationContext(AppConfig.class);
        UserServiceInterface userServiceInterface = (UserServiceInterface) applicationContext.getBean("userService");
        userServiceInterface.test();
//        System.out.println(userService.beanName);
//        System.out.printf("name:", userService.name);
    }
}
