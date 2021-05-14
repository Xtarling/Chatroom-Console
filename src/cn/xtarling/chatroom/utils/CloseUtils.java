package cn.xtarling.chatroom.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author WYX
 * @date 2021-2-8 - 18:18
 * --------------------------------
 * 名称：工具类
 * 作用：关闭各种资源
 */
public class CloseUtils {
    public static void close(Closeable... targets) {
        for (Closeable target: targets) {
            try {
                if (target != null) {
                    target.close();
                }
            } catch (IOException e) {
                System.out.println("资源关闭失败");
                e.printStackTrace();
            }
        }
    }
}
