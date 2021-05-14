package cn.xtarling.chatroom.client;


import cn.xtarling.chatroom.utils.CloseUtils;

import java.io.*;
import java.net.Socket;

/**
 * @author WYX
 * @date 2021-2-8 - 21:45
 * --------------------------------
 * 名称：客户端
 * 作用：实现登录和聊天的操作
 */
public class ChatRoomClient {
    private String userName;
    private String userPwd;
    private String feedbackInfo;
    private boolean loginSuccess;

    public ChatRoomClient() {
        System.out.println("##-----群聊<相亲相爱一家人>-----##");
    }

    private void login(String host, int port) {
        System.out.println("[--------客户端登录--------]");

        BufferedReader console;
        Socket loginSocket = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            console = new BufferedReader(new InputStreamReader(System.in));
            loginSocket = new Socket(host, port);
            dis = new DataInputStream(loginSocket.getInputStream());
            dos = new DataOutputStream(loginSocket.getOutputStream());

            while (!loginSuccess) {
                System.out.print("请输入昵称：");
                userName = console.readLine();
                System.out.print("请输入密码：");
                userPwd = console.readLine();

                dos.writeUTF(userName + "&" + userPwd);
                dos.flush();

                feedbackInfo = dis.readUTF();
                if (feedbackInfo.equals("FALSE")) {
                    System.out.println("-----密码错误，请重新输入。反馈代码：" + feedbackInfo + "-----");
                }
                else {
                    System.out.println("-----登录成功！反馈代码：" + feedbackInfo + "-----");
                    loginSuccess = true;
                }
            }
        } catch (IOException e) {
            System.out.println("登录过程异常，请重启客户端。");
            e.printStackTrace();
        } finally {
//            CloseUtils.close(dos, dis, loginSocket, console);
            CloseUtils.close(dos, dis, loginSocket);
        }
    }

    private void beginChat(String host, int port) {
        System.out.println("-----开始畅聊吧！-----");

        Socket chatSocket = null;
        try {
            chatSocket = new Socket(host, port);
            new Thread(new Receive(chatSocket)).start();
            new Thread(new Send(chatSocket, userName, feedbackInfo)).start();
        } catch (IOException e) {
            System.out.println("与聊天室的连接失败！");
            e.printStackTrace();
//            CloseUtils.close(chatSocket);
        }
    }

    public static void main(String[] args) throws IOException {
        ChatRoomClient client = new ChatRoomClient();
        client.login("localhost", 8000); //填写登录服务器的IP和端口号
        if (client.loginSuccess) {
            client.beginChat("localhost", 8001); //填写聊天室服务器的IP和端口号
        }
//        client.login("192.168.1.106", 8000); //填写登录服务器的IP和端口号
//        if (client.loginSuccess) {
//            client.beginChat("192.168.1.106", 8001); //填写聊天室服务器的IP和端口号
//        }
    }
}
