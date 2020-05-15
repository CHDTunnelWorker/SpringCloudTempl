package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @program: parent
 * @description: 用户测试实体类
 * @author: Holland
 * @create: 2020-03-31 21:51
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Accessors(chain = true)
public class User {
    private Long id;
    private String name;
    private Integer age;
}
