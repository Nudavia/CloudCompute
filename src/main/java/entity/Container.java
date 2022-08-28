package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: ldw
 * @Date: 2022/3/31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Container {
    private String id;
    private String image;
    private boolean running;
    private String ip;
    private String nodeIp;
    private String name;
    // docker run -d --name mpi01 --privileged=true centos_mpi:v1 /usr/sbin/init

}
