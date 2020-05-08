package com.laohu.consumerribbon.service.impl;

import com.laohu.consumerribbon.service.ConsumerService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

/**
 * @program: springcloud
 * @description: 消费者服务业务实现层
 * @author: Holland
 * @create: 2020-05-08 11:04
 **/
@Service
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    private RestTemplate restTemplate;


    @Override
    @HystrixCommand(fallbackMethod = "consumeFallback")
    public String consume() {
        System.out.println("4444");
        //通过ribbon负载均衡,调用服务提供者的服务,注意路径中PROVIDER替代了常规的IP加端口 这体现了eureka重要的服务治理特性
        return restTemplate.getForEntity("http://PROVIDER/provider/eurekaInfo",String.class).getBody();
    }

    public String consumeFallback(){
        return "access PROVIDER error!";
    }
}
