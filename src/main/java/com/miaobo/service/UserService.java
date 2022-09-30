package com.miaobo.service;

import com.spring.*;

@Component("userService")
@Scope("prototype")
public class UserService implements BeanNameAware, InitializingBean, UserServiceInterface {
    @Autowired
    private OrderService orderService;

    public String beanName;

    public String name;

    public void setName(String name) {
        this.name = name;
    }

    public void test(){
        System.out.println("被代理对象："+orderService);
    }

    @Override
    public void setBeanName(String name) {
        //该方法中会传入name, 就可以用传入的name
        this.beanName = name;

    }

    @Override
    public void afterPropertiesSet() {
        //由程序员决定初始化后功能
        System.out.println("初始化");
    }
}
