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
        System.out.println("���������"+orderService);
    }

    @Override
    public void setBeanName(String name) {
        //�÷����лᴫ��name, �Ϳ����ô����name
        this.beanName = name;

    }

    @Override
    public void afterPropertiesSet() {
        //�ɳ���Ա������ʼ������
        System.out.println("��ʼ��");
    }
}
