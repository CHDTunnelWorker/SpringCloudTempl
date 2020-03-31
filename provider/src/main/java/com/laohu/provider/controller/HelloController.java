package com.laohu.provider.controller;

import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: springcloud
 * @description: 服务提供者控制类
 * @author: Holland
 * @create: 2020-03-23 12:23
 **/
@RestController
@RequestMapping("/provider")
public class HelloController {

    @Autowired
    private DiscoveryClient client;

    @RequestMapping(value = "/eurekaInfo",method = RequestMethod.GET)
    public String eurekaInfo(){
        List<ServiceInstance> provider = client.getInstances("provider");
        ServiceInstance instance = provider.get(0);
        System.out.println("服务提供者实例host地址: "+instance.getHost()+",服务id: "+instance.getServiceId()+",实例id: "+
                instance.getInstanceId()+"////服务元数据: "+instance.getMetadata());
        return "hello provider";
    }

    @RequestMapping(value = "/userRibbonString")
    public String userRibbonString(
            @RequestParam String name,@RequestParam(required = false) Integer age
    )
    {
        if(age == null){
            age = 20;
        }
        User user = new User(name,age);
        return user.toString();
    }

    @RequestMapping(value = "/userRibbonEntity")
    public User userRibbonEntity(
            @RequestParam String name,@RequestParam(required = false) Integer age
    )
    {
        if(age == null){
            age = 20;
        }
        User user = new User(name,age);
        return user;
    }

}
