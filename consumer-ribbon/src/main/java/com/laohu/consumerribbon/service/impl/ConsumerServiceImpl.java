package com.laohu.consumerribbon.service.impl;

import com.laohu.consumerribbon.service.ConsumerService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.ObservableExecutionMode;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import rx.Observable;
import rx.Subscriber;

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
}
