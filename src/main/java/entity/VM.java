package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: ldw
 * @Date: 2022/3/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VM {
    private String nodeIp;
    private String domainName;
    private int state;
    private String ip;
    private long memory;
    private int vcpu;
    private long capacity;
    private boolean persistent;
}
