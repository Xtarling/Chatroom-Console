package cn.xtarling.chatroom.client;


import cn.xtarling.chatroom.utils.CloseUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author WYX
 * @date 2021-2-8 - 21:46
 * --------------------------------
 * 名称：客户端接收辅助类
 * 作用：接受中转服务器发来的信息，打印到控制台
 */
public class Receive implements Runnable {
    private final Socket client;
    private DataInputStream dis;
    private boolean isRunning;

    public Receive(Socket client0) {
        client = client0;
        isRunning = true;
        try {
            dis = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            System.out.println("数据接收流初始化错误！");
            e.printStackTrace();
            release();
        }
    }

    private String receive() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            System.out.println("消息接收过程异常！");
            e.printStackTrace();
            release();
        }
        return "";
    }

    private void release() {
        isRunning = false;
        CloseUtils.close(dis, client);
    }

    @Override
    public void run() {
        while (isRunning) {
            String msg = receive();
            if (!msg.equals("")) {
                if (msg.charAt(0) == '#') { //??
                    System.out.println(msg.substring(1));
                    release();
                }
                else {
                    System.out.println(msg);
                }
            }
        }
    }
}
