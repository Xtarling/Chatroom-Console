package cn.xtarling.chatroom.server;


import cn.xtarling.chatroom.utils.CloseUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author WYX
 * @date 2021-2-8 - 17:28
 * --------------------------------
 * 名称：登录管理服务器
 * 用途：实现验证登录，判断是否为管理员
 * 步骤：
 * 1. 创建服务器
 * 2. 启动服务，阻塞式等待连接(accept)
 * 3. 操作: 输入输出流操作
 * 4. 释放资源
 */
public class ChatRoomLoginServer {
    private static final String roomPwd = "666";
    private static final String adminPwd = "admin";

    ServerSocket loginServerSocket;
    boolean isRunning;

    public ChatRoomLoginServer(int port) {
        System.out.println("[--------登录管理服务器--------]");

        isRunning = true;

        try {
            loginServerSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("登录管理服务器初始化失败！");
            e.printStackTrace();
            isRunning = false;
            CloseUtils.close(loginServerSocket);
        }
    }

    public void startService() {
        int count = 0;
        try {
            while (isRunning) {
                Socket client = loginServerSocket.accept();
                count++;
                System.out.println("-----" + count + "号客户端正在尝试登录-----");
                new Thread(new Channel(client), count + "号客户端").start();
            }
        } catch (IOException e) {
            System.out.println("登录管理服务器状态异常！");
            e.printStackTrace();
        } finally {
            isRunning = false;
            CloseUtils.close(loginServerSocket);
        }
    }

    class Channel implements Runnable { //代表连接通道
        private final Socket client;
        private DataInputStream dis;
        private DataOutputStream dos;
        private boolean loginSuccess;

        public Channel(Socket client0) {
            client = client0;
            try {
                dis = new DataInputStream(client.getInputStream());
                dos = new DataOutputStream(client.getOutputStream());
                loginSuccess = false;
            } catch (IOException e) {
                System.out.println(Thread.currentThread().getName() + "登录通道初始化失败！");
                e.printStackTrace();
                CloseUtils.close(dos, dis);
            }
        }

        private void send(String msg) {
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                System.out.println("发送信息失败！");
                e.printStackTrace();
            }
        }

        private String receive() {
            String msg = "";
            try {
                msg = dis.readUTF();
            } catch (IOException e) {
                System.out.println("接收信息失败！");
                e.printStackTrace();
            }
            return msg;
        }

        private void verify() {
            String[] userInfo = receive().split("&");
            String userName = userInfo[0];
            String userPwd = userInfo[1];

            if (userName.contains("admin") && userPwd.equals(adminPwd)) {
                send("TRUE_ADMIN");
                loginSuccess = true;
                System.out.println(Thread.currentThread().getName() + "登录成功。" + "用户名为：" + userName + "，密码为：" + userPwd);
            }
            else if (userPwd.equals(roomPwd)) {
                send("TRUE_USER");
                loginSuccess = true;
                System.out.println(Thread.currentThread().getName() + "登录成功。" + "用户名为：" + userName + "，密码为：" + userPwd);
            }
            else {
                send("FALSE");
            }
        }

        @Override
        public void run() {
            while (isRunning && !loginSuccess) {
                verify();
            }
            CloseUtils.close(dos, dis, client);
        }
    }

    public static void main(String[] args) {
        ChatRoomLoginServer loginServer = new ChatRoomLoginServer(8000); //设置登录服务器端口号
        loginServer.startService();
    }
}
