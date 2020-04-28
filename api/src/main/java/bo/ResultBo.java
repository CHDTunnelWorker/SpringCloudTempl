package bo;

import lombok.Data;

/**
 * @program: springboot
 * @description: 返回前台的结果类
 * @author: Holland
 * @create: 2019-09-02 11:34
 **/
@Data
public class ResultBo<T> {
    /**
     * 表示请求是否成功
     */
    private boolean result;
    /**
     * 响应信息信息
     */
    private String msg;
    /**
     * 封装结果数据
     */
    private T data;

    /**
     * @Description:无参构造
     * @param: []
     * @return:
     * @auther: Holland
     * @date: 2019/9/2 11:41
     */
    public ResultBo(){
        super();
    }
    /**
     * @Description:请求成功后,msg和data参数构造
     * @param: [String msg,T data]
     * @return:
     * @auther: Holland
     * @date: 2019/9/2 11:41
     */
    public ResultBo(String msg, T data){
        super();
        this.result = true;
        this.msg = msg;
        this.data = data;
    }
    /**
     * @Description:请求成功后,默认请求成功信息,data参数构造
     * @param: [T data]
     * @return:
     * @auther: Holland
     * @date: 2019/9/2 11:41
     */
    public ResultBo(T data){
        super();
        this.result = true;
        this.msg = "请求成功";
        this.data = data;
    }
    /**
     * @Description:无数据构造
     * @param: [boolean result,String msg]
     * @return:
     * @auther: Holland
     * @date: 2019/9/2 11:41
     */
    public ResultBo(boolean result, String msg){
        super();
        this.result = result;
        this.msg = msg;
    }
    /**
     * @Description:请求成功有数据
     * @param: [data]
     * @return: com.haoma.springboot.bo.ResultBo<T>
     * @auther: Holland
     * @date: 2019/9/2 11:57
     */
    public static <T> ResultBo<T> success(T data){
        return new ResultBo<T>(data);
    }
    /**
     * @Description:请求成功,需要信息和数据
     * @param: [msg, data]
     * @return: com.haoma.springboot.bo.ResultBo<T>
     * @auther: Holland
     * @date: 2019/9/2 11:57
     */
    public static <T> ResultBo<T> success(String msg,T data){
        return new ResultBo<T>(msg,data);
    }
    /**
     * @Description:请求成功,无数据返回
     * @param: []
     * @return: com.haoma.springboot.bo.ResultBo<T>
     * @auther: Holland
     * @date: 2019/9/2 11:58
     */
    public static <T> ResultBo<T> success(){
        return new ResultBo<T>(true,"请求成功");
    }
    /**
     * @Description:请求失败,无特殊失败信息
     * @param: []
     * @return: com.haoma.springboot.bo.ResultBo<T>
     * @auther: Holland
     * @date: 2019/9/2 11:58
     */
    public static <T> ResultBo<T> fail(){
        return new ResultBo<>(false,"请求失败");
    }
    /**
     * @Description: 请求失败,返回具体的失败信息
     * @param: [msg]
     * @return: com.haoma.springboot.bo.ResultBo<T>
     * @auther: Holland
     * @date: 2019/9/2 11:59
     */
    public static <T> ResultBo<T> fail(String msg){
        return new ResultBo<T>(false,msg);
    }
}
