package com.laohu.consumerribbon.service.impl;

import com.laohu.consumerribbon.service.ConsumerService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCollapser;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.annotation.ObservableExecutionMode;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheKey;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheRemove;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import rx.Observable;
import rx.Subscriber;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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


    /**
     * @HystrixCommand 这样定义该方法,只是执行了HystrixCommand 的execute同步执行
     * @return
     */
    @Override
    @HystrixCommand(fallbackMethod = "consumeFallback")
    public String consume() {
        System.out.println("4444");
        //通过ribbon负载均衡,调用服务提供者的服务,注意路径中PROVIDER替代了常规的IP加端口 这体现了eureka重要的服务治理特性
        return restTemplate.getForEntity("http://PROVIDER/provider/eurekaInfo",String.class).getBody();
    }

    /**
     * 测试Hystrix的请求命令结果缓存
     * @CacheResult 为请求命令开启缓存功能.不指定的话,获取缓存对象的key值为所有的参数,即string类型的name值和age值
     * 参数cacheKeyMethod 指定请求命令结果缓存key的值的函数,优先级高
     *
     * @CacheKey 指定参数为请求命令结果缓存key,也可以指定参数对象中的属性为请求命令结果缓存key,但优先级低;
     *
     * @param name
     * @param age
     * @return
     */
    @Override
    @HystrixCommand
    @CacheResult(cacheKeyMethod = "defineHystrixCacheKey")
    public String consumeHystrixCache(@CacheKey("name") String name, String age) {
        return restTemplate
                .getForEntity("http://PROVIDER/provider/getStringFromRibbonParam?name={1}&age={2}",String.class,name,age)
                .getBody();
    }

    /**
     * 更新操作,需要清除对应的查询请求命令的缓存
     *
     * @CacheRemove: 该注解为清除失效缓存注解
     * commandKey参数: 必填属性,用于指明清除哪个具体的请求命令结果缓存
     * @param name
     * @param age
     * @return
     */
    @CacheRemove(commandKey = "consumeHystrixCache")
    @HystrixCommand
    public String updateConsumeHystrixCache(@CacheKey("name") String name, String age) {
        //假设此处是更新操作
        return restTemplate
                .getForEntity("http://PROVIDER/provider/getStringFromRibbonParam?name={1}&age={2}",String.class,name,age)
                .getBody();
    }

    /**
     * 定义Hystrix的缓存key的方法
     * @param name
     * @return
     */
    private String defineHystrixCacheKey(String name){
        return name;
    }

    /**
     * @HystrixCommand 这样定义该方法,执行了HystrixCommand 的queue异步执行
     * @return
     */
    @HystrixCommand(fallbackMethod = "consumeFallback")
    public Future<String> consumeByAsync(){
        return new AsyncResult<String>() {
            @Override
            public String invoke() {
                return restTemplate.getForEntity("http://PROVIDER/provider/eurekaInfo",String.class).getBody();
            }
        };
    }

    /**
     * @HystrixCommand 这样定义该方法,
     * observableExecutionMode参数: 为EAGER 则该方法为observe()执行方式;如果为LAZY 则该方法为toObserve()执行方式
     * ignoreExceptions参数: 指定忽略异常,当命令执行出现指定忽略异常时,不会触发服务降级逻辑
     * groupKey参数: 指定命令分组,不指定默认为继承Hystrix命令的类名,属于必须有的参数,因为Hystrix默认的仪表盘数据统计以及线程池划分,都是通过组名来实现;
     * commandKey参数:指定具体命令的名称,虽然命令在一个组下,但是每个命令本身还是最好与其他命令进行隔离,因此最好单独指定每个命令的名称
     * threadPoolKey参数:指定具体命令的线程池名称,细粒度的将线程池划分到每个具体命令
     * @return
     */
    @HystrixCommand(observableExecutionMode = ObservableExecutionMode.EAGER,fallbackMethod = "consumeFallback"
    ,ignoreExceptions = NullPointerException.class,groupKey = "StringGroup",commandKey = "consumeByObservable",
    threadPoolKey = "consumeByObservableThread")
    public Observable<String> consumeByObservable(){
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    if(!subscriber.isUnsubscribed()){
                        String body = restTemplate.getForEntity("http://PROVIDER/provider/eurekaInfo", String.class).getBody();
                        subscriber.onNext(body);
                        subscriber.onCompleted();
                    }
                } catch (Exception e){
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 服务降级逻辑
     * 参数:将Throwable e 作为形参,则可以在降级逻辑中获取到命令执行异常的具体信息
     * @param e
     * @return
     */
    public String consumeFallback(Throwable e){
        return "access PROVIDER error!";
    }

    /**
     * 测试Hystrix请求命令合并操作
     * @HystrixCollapser注解:在需要进行请求命令合并的操作上加入
     * batchMethod参数:用于指定合并后批量请求的实现方法名称
     * collapserProperties参数:为合并请求器设置了相关属性
     * @HystrixProperty注解:设置相关hystrix属性,在此处设置了合并请求的收集请求的时间窗口时长
     * @param id
     * @return
     */
    @Override
    @HystrixCollapser(batchMethod = "findAllUsers",collapserProperties = {
            @HystrixProperty(name = "timerDelayInMilliseconds",value = "100")
    })
    public User findUser(Long id) {
        return restTemplate.getForEntity("http://PROVIDER/provider/getUserById?id={1}", User.class,id).getBody();
    }

    /**
     * 测试Hystrix请求命令合并操作
     * @param ids
     * @return
     */
    @Override
    public List<User> findAllUsers(String ids) {
        return restTemplate.getForEntity("http://PROVIDER/provider/getUserByIds?ids={1}", List.class,ids).getBody();
    }
}
