package com.laohu.provider.controller;

import com.alibaba.fastjson.JSON;
import entity.User;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
    public String eurekaInfo() throws Exception{
        //为了测试断路器的阻塞断开,让处理线程休息几秒
        int sleepTime = new Random().nextInt(4000);
        System.out.println("sleepTime: "+sleepTime);
        Thread.sleep(sleepTime);

        List<ServiceInstance> provider = client.getInstances("provider");
        ServiceInstance instance = provider.get(0);
        System.out.println("服务提供者实例host地址: "+instance.getHost()+",服务id: "+instance.getServiceId()+",实例id: "+
                instance.getInstanceId()+"////服务元数据: "+instance.getMetadata());
        return "hello provider";
    }

    /**
     * 测试ribbon负载均衡访问返回字符串,参数为普通属性
     * @param name
     * @param age
     * @return
     */
    @GetMapping(value = "/getStringFromRibbonParam")
    public String getStringFromRibbonParam(
            @RequestParam String name,@RequestParam(required = false) String age
    )
    {
        Integer ageNum = null;
        if(StringUtils.isBlank(age)){
            ageNum = 20;
        } else {
            ageNum = Integer.valueOf(age);
        }
        User user = new User(4L,name,ageNum);
        //展示被调用的服务
        System.out.println(user.toString());
        return user.toString();
    }

    /**
     * 测试ribbon负载均衡访问返回实体类对象,参数为对象
     * @return
     */
    @GetMapping(value = "/getObjectFromRibbonEntity")
    public User getObjectFromRibbonEntity(
            User user
    )
    {
        if(user.getAge() == null){
            user.setAge(20);
        }
        //展示被调用的服务
        System.out.println(user.toString());
        return user;
    }

    /**
     * 测试ribbon负载均衡访问返回字符串,参数为对象
     * 因为通过restTemplate进行post请求,如果将request的body参数指定为一个普通对象的话(非httpentity对象)
     * 那么在请求时,底层会将该对象参数json化,通过application/json的方式将参数传出,因此在提供者接口上,必须以@RequestBody
     * 来接受参数
     * @return
     */
    @PostMapping(value = "/postStringFromRibbonEntity")
    public String getStringFromRibbonEntity(
         @RequestBody   User user
    )
    {
        if(user.getAge() == null){
            user.setAge(20);
        }
        //展示被调用的服务
        System.out.println(user.toString());
        return JSON.toJSONString(user);
    }

    /**
     * 测试ribbon负载均衡访问返回对象,参数为对象
     * @return
     */
    @PostMapping(value = "/postObjectFromRibbonEntity")
    public String  postObjectFromRibbonEntity(
            @RequestBody  User user
    )
    {
        if(user.getAge() == null){
            user.setAge(20);
        }
        //展示被调用的服务
        System.out.println(user.toString());
        return user.toString();
    }

    /**
     * 测试Hystrix请求命令合并操作
     * @param id
     * @return
     */
    @GetMapping("/getUserById")
    public User getUserById(Long id){
        return new User().setId(id).setName("laohu").setAge(23);
    }

    /**
     * 测试Hystrix请求命令合并操作
     * @param ids
     * @return
     */
    @GetMapping("/getUserByIds")
    public List<User> getUserByIds(String ids){
        String[] split = StringUtils.split(ids, ",");
        List<String> idStrList = new ArrayList<>(Arrays.asList(split));
        List<Long> idList = idStrList.stream().map(a -> Long.parseLong(a)).collect(Collectors.toList());
        List<User> users = idList.stream().map(id -> new User().setId(id).setName("yanbo").setAge(24))
                .collect(Collectors.toList());
        return users;
    }
}
