package utils;

import java.util.Random;

/**
 * @Auther: ldw
 * @Date: 2022/3/17
 */
public class RandomCreator {
    public static String createIP() {
        return "192.168.43.131";
    }

    public static String createMac() {
        String SEPARATOR_OF_MAC = ":";
        Random random = new Random();
        String[] mac = {
                String.format("%02x", 0x52),
                String.format("%02x", 0x54),
                String.format("%02x", 0x00),
                String.format("%02x", random.nextInt(0xff)),
                String.format("%02x", random.nextInt(0xff)),
                String.format("%02x", random.nextInt(0xff))
        };
        return String.join(SEPARATOR_OF_MAC, mac);
    }
}

