package com.laohu.consumerribbon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @program: springcloud
 * @description: ribbon消费者
 * @author: Holland
 * @create: 2020-03-23 16:41
 **/
@RestController
//@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/consume",method = RequestMethod.GET)
    public String consume(){
        System.out.println("4444");
        //通过ribbon负载均衡,调用服务提供者的服务,注意路径中PROVIDER替代了常规的IP加端口 这体现了eureka重要的服务治理特性
        return restTemplate.getForEntity("http://PROVIDER/provider/eurekaInfo",String.class).getBody();
    }

}
