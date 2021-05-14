package cn.xtarling.chatroom.client;


import cn.xtarling.chatroom.utils.CloseUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author WYX
 * @date 2021-2-8 - 21:45
 * --------------------------------
 * 名称：客户端发送辅助类
 * 作用：获取键盘输入的信息或者指令，往中转服务器发送信息
 */
public class Send implements Runnable {
    private Socket client;
    private BufferedReader console;
    private DataOutputStream dos;
    private boolean isRunning;
    private String userName;
    private String adminInfo;

    public Send(Socket client0, String name0, String feedbackInfo) {
        client = client0;
        console = new BufferedReader(new InputStreamReader(System.in));
        userName = name0;
        adminInfo = feedbackInfo;
        isRunning = true;
        try {
            dos = new DataOutputStream(client.getOutputStream());
            send(userName + "#" + adminInfo);
        } catch (IOException e) {
            System.out.println("数据输出流初始化错误！");
            e.printStackTrace();
            release();
        }
    }

    private void send(String msg) {
        try {
            dos.writeUTF(msg);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("消息发送过程异常！");
            release();
        }
    }

    private String getStrFromConsole() {
        try {
            return console.readLine();
        } catch (IOException e) {
            System.out.println("发送端控制台输入流异常！");
            e.printStackTrace();
            release();
        }
        return "";//?为何这么写
    }

    private void release() {
        isRunning = false;
        CloseUtils.close(dos, client);
    }

    @Override
    public void run() {
        while (isRunning) {
            String msg = getStrFromConsole();
            if (!msg.equals("")) {
                send(msg);
            }
        }
    }
}
