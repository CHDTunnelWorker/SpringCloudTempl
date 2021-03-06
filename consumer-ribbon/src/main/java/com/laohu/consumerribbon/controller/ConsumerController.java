package com.laohu.consumerribbon.controller;

import bo.ResultBo;
import com.google.common.base.Stopwatch;
import com.laohu.consumerribbon.service.ConsumerService;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: springcloud
 * @description: ribbon消费者
 * @author: Holland
 * @create: 2020-03-23 16:41
 **/
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @Autowired
    private ConsumerService consumerService;

    @GetMapping(value = "/consume")
    public String consume(){
        StopWatch test = new StopWatch("test");
        test.start("task1");
        String result = consumerService.consume();
        test.stop();
        System.out.println(test.prettyPrint());
        return result;
    }

    /**
     * 测试Hystrix的请求命令结果缓存
     * @param name
     * @param age
     * @return
     */
    @GetMapping(value = "/consumeHystrixCache")
    public String consumeHystrixCache(
            String name,String age
    )
    {
        return consumerService.consumeHystrixCache(name,age);
    }

    /**
     * @Description: 测试ribbon的get请求负载均衡
     * @param:
     * @return:
     * @auther: Holland
     * @date: 2020/4/28 11:59
     */
    @GetMapping(value = "/testGetRibbonConsume")
    public ResultBo testGetRibbonConsume(){
        //通过路径的参数占位符指定参数位置顺序,与参数属性顺序一一对应,来将参数传到提供者的接口上;中间的string.class表示调用返回的结果是String类型的
//        ResponseEntity<String> response = restTemplate.getForEntity("http://PROVIDER/provider/getStringFromRibbonParam?name={1}&age={2}", String.class,
//                "huweikang", 23);
//        String body = response.getBody();
//        System.out.println("ribbon:GET通过顺序占位符请求提供者,获取string类型结果: "+body);


        //通过路径的占位符指定参数名,再通过map封装对应的参数名数值,来将参数传到提供者的接口上;中间的string.class表示调用返回的结果是String类型的
        Map<String,String> paramsMap = new ConcurrentHashMap<>();
        paramsMap.put("strName","hezhen");
        paramsMap.put("strAge","26");
        //getForEntity请求方式,响应结果包含httpheader httpstatus等信息
//        ResponseEntity<String> response = restTemplate.getForEntity("http://PROVIDER/provider/getStringFromRibbonParam?name={strName}&age={strAge}",
//                String.class, paramsMap);
        //String body = response.getBody();

        //getforObject请求方式,该方法可以直接获得响应的body对象,适用于只关注body内容的情况
//        String objectBody = restTemplate.getForObject("http://PROVIDER/provider/getStringFromRibbonParam?name={strName}&age={strAge}",
//                String.class, paramsMap);
//        System.out.println("ribbon:GET通过getforObject,参数名占位符请求提供者,获取string类型结果: "+objectBody);

        //getforObject请求方式,获取的结果对象为user对象
        User forObject = restTemplate.getForObject("http://PROVIDER/provider/getObjectFromRibbonEntity?name={strName}&age={strAge}",
                User.class, paramsMap);
        System.out.println("ribbon:GET通过getforObject,参数名占位符请求提供者,获取User类型结果: "+forObject.toString());


        //通过路径的占位符指定参数名,再通过构造URI来将参数传到提供者接口上,这个请求方式暂时用的比较少
//        UriComponents uriComponents = UriComponentsBuilder.fromUriString("http://PROVIDER/provider/getStringFromRibbonParam?name={strName}")
//                .build()
//                .expand("yanbo")
//                .encode();
//        URI uri = uriComponents.toUri();
//        String uriBody = restTemplate.getForEntity(uri, String.class).getBody();
//        System.out.println("ribbon:GET通过参数名占位符以及URI请求提供者,获取string类型结果: "+uriBody);
        return ResultBo.success();
    }

    /**
     * @Description: 测试ribbon的post请求负载均衡
     * @param:
     * @return:
     * @auther: Holland
     * @date: 2020/4/28 11:59
     */
    @GetMapping(value = "/testPostRibbonConsume")
    public ResultBo testPostRibbonConsume(){
        String body = null;
        //第一种请求方式:postForEntity,与getForEntity请求方式类似
//        User laohu = new User("laohu", 23);
//        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://PROVIDER/provider/postStringFromRibbonEntity", laohu,
//                String.class);
//        body = responseEntity.getBody();
//        System.out.println("ribbon:POST通过postForEntity,参数通过Object request进行传输,获取string类型结果: "+body);

        //第二种请求方式:postForObject,与getForObject请求方式类似,也是面向针对body内容的场景
//        User laohe = new User("laohe", 26);
//        User user = restTemplate.postForObject("http://PROVIDER/provider/postObjectFromRibbonEntity", laohe, User.class);
//        System.out.println("ribbon:POST通过postForObject,参数通过Object request进行传输,获取User类型结果: "+user.toString());

        //第三种请求方式:postForLocation 以post请求提交资源,并返回新资源的URI(注意,提供者接口必须得返回URI资源,如果接口返回使用了@ResponseBody,则不能正确接收到URI对象)
        //这种请求方式一般用到需要返回URI资源的场景种,比如登录后需要资源跳转等
        User laoyan = new User(4L,"laoyan", 24);
        URI uri = restTemplate.postForLocation("http://PROVIDER/provider/postObjectFromRibbonEntity", laoyan);
        System.out.println("ribbon:POST通过postForLocation,参数通过Object request进行传输,获取URI类型结果: "+uri);
        return ResultBo.success();
    }

    /**
     * 测试Hystrix请求命令合并操作
     * @param id
     * @return
     */
    @GetMapping("/findUser")
    public ResultBo findUser(Long id){
        User user = consumerService.findUser(id);
        return ResultBo.success(user);
    }

    /**
     * 测试Hystrix请求命令合并操作
     * @param ids
     * @return
     */
    @GetMapping("/findAllUsers")
    public ResultBo findAllUsers(String ids){
        List<User> users = consumerService.findAllUsers(ids);
        return ResultBo.success(users);
    }
}
