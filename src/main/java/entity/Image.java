package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: ldw
 * @Date: 2022/3/31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    private String id;
    private String name;
    private String nodeIp;
    private String size;
}
