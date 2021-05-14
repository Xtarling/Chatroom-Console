package cn.xtarling.chatroom.server;


import cn.xtarling.chatroom.utils.CloseUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author WYX
 * @date 2021-2-8 - 21:35
 * --------------------------------
 * 名称：指令识别和内容中转服务器
 * 作用：实现基本的群聊和私聊功能，以及附加的（仅限管理员可用的）踢人、发广告、修改广告内容等功能。
 *      基本思路是将接收到的客户端消息进行识别和转发到其它客户端
 */
public class ChatRoomServer {
    private CopyOnWriteArrayList<Channel> userChannels;
    private Map<String, String> adminMap;
    private String advert = "广告位招租，抓紧时间抢购吧！";

    private ServerSocket serverSocket;
    private boolean isRunning;

    public ChatRoomServer(int port) {
        System.out.println("[--------聊天室服务器--------]");

        userChannels = new CopyOnWriteArrayList<>();
        adminMap = new HashMap<>();
        adminMap.put("admin1", "Avalanche");
        adminMap.put("admin2", "WYX");
        isRunning = true;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("聊天室服务器初始化失败！");
            e.printStackTrace();
            isRunning = false;
            CloseUtils.close(serverSocket);
        }
    }

    public void startService() {
        try {
            while (isRunning) {
                Socket client = serverSocket.accept();
                Channel channel = new Channel(client);
//                userChannels.add(channel);

                if (channel.isAdmin) System.out.println("一个客户端建立了连接--->[管理员]" + channel.userName + "，目前服务器连接人数为：" + userChannels.size());
                else System.out.println("一个客户端建立了连接--->[用户]" + channel.userName + "，目前服务器连接人数为：" + userChannels.size());

                new Thread(channel).start();
            }
        } catch (IOException e) {
            System.out.println("聊天室服务器状态异常！");
            e.printStackTrace();
        } finally {
            isRunning = false;
            CloseUtils.close(serverSocket);
        }
    }

    class Channel implements Runnable {
        private Socket client;
        private DataInputStream dis;
        private DataOutputStream dos;
        private boolean isRunning;
        private String userName;
        private boolean isAdmin;

        public Channel(Socket client0) {
            client = client0;
            try {
                dis = new DataInputStream(client.getInputStream());
                dos = new DataOutputStream((client.getOutputStream()));
                userChannels.add(this);
                isRunning = true;

                String[] userInfo = receive().split("#");
                if (userInfo[1].equals("TRUE_ADMIN")) {
                    isAdmin = true;
                    userName = adminMap.get(userInfo[0]);
                }
                else {
                    isAdmin = false;
                    userName = userInfo[0];
                }

                sendGreetings(isAdmin);
            } catch (IOException e) {
                System.out.println("聊天室服务器初始化失败");
                release(this);
            }
        }

        private void sendGreetings(boolean isAdmin) {
            if (isAdmin) {
                send("==========================================================================");
                send("|*\t\t尊敬的管理员[" + userName + "]，欢迎您的到来！\t\t\t\t\t\t\t\t*|");
                send("|*\t\t目前聊天室总人数为：" + userChannels.size() + "\t\t\t\t\t\t\t\t\t\t\t\t*|");
                send("|*\t\t温馨提示：使用[@xx>]格式可以进行私聊，使用#xx可踢出对应成员\t\t\t\t*|");
                send("|*\t\t温馨提示：使用[*广告*]格式可发送系统广告，使用[*广告>xx]可修改原广告内容\t\t*|");
                send("==========================================================================");
                sendOthers("管理员[" + userName + "]加入<相亲相爱一家人>的聊天室，请大家注意言行！", true);
            }
            else {
                send("==========================================================================");
                send("|*\t\t亲爱的用户[" + userName + "]，欢迎您的到来！\t\t\t\t\t\t\t\t\t\t*|");
                send("|*\t\t目前聊天室总人数为：" + userChannels.size() + "\t\t\t\t\t\t\t\t\t\t\t\t*|");
                send("|*\t\t温馨提示：使用[@xx>]格式可以进行私聊\t\t\t\t\t\t\t\t\t*|");
                send("==========================================================================");
                sendOthers("用户[" + userName + "]加入<相亲相爱一家人>的聊天室", true);
            }
        }

        private void send(String msg) {
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                System.out.println("服务器未能成功发送消息！");
                release(this);
            }
        }

        private void sendOthers(String msg, boolean isSys) {
            //发广告
            if (msg.equals("*广告*") && isAdmin) {
                sendOthers("[广告]" + advert, true);
                return;
            }

            //改广告
            if (msg.indexOf("*广告>") == 0 && isAdmin) {
                advert = msg.substring(4);
                send("-----系统通知：广告内容修改成功！-----");
                return;
            }

            //踢人
            if (msg.charAt(0) == '#' && isAdmin) {
                String targetName = msg.substring(1);
                kickOut(targetName);
                return;
            }

            //私聊
            if (msg.charAt(0) == '@' && msg.contains(">")) {
                int index = msg.indexOf(">");
                String targetName = msg.substring(1, index);
                msg = msg.substring(index + 1);
                privateChat(targetName, msg);
                return;
            }

            //正常群发
            for (Channel user: userChannels) {
                if (!isSys) {
                    if (user == this) {
                        if (isAdmin) send("[管理员]自己：" + msg);
                        else send("[用户]自己：" + msg);
                    }
                    else {
                        if (isAdmin) user.send("[管理员]" + userName + "：" + msg);
                        else user.send("[用户]" + userName + "：" + msg);
                    }
                }
                else {
                    user.send("-----系统通知：" + msg + "-----");
                }
            }
        }

        private void kickOut(String targetName) {
            for (Channel user: userChannels) {
                if (targetName.equals(user.userName)) {
                    if (user == this) {
                        send("-----系统提示：对不起，您不能踢出自己！-----");
                    }
                    else {
                        sendOthers("用户[" + user.userName + "]被管理员[" + userName + "]踢出了聊天室", true);
                        user.send("对不起，您被管理员[" + userName + "]踢出了聊天室！");
                        user.isRunning = false;
                        CloseUtils.close(user.dos, user.dis, user.client);
                        userChannels.remove(user);
//                    release(user);
                    }
                    return;
                }
            }
            send("-----系统提示：聊天室无用户或管理员[" + targetName + "]-----");
        }

        private void privateChat(String targetName, String msg) {
            for (Channel user: userChannels) {
                if (targetName.equals(user.userName)) {
                    if (user == this) {
                        send("-----系统提示：对不起，您不能与自己私聊！-----");
                    }
                    else {
                        if (isAdmin) {
                            user.send("管理员[" + userName + "]私聊你：" + msg);
                        }
                        else {
                            user.send("用户[" + userName + "]私聊你：" + msg);
                        }
                    }
                    return;
                }
            }
            send("-----系统提示：聊天室无用户或管理员[" + targetName + "]-----");
        }

        private String receive() {
            String msg = "";
            try {
                msg = dis.readUTF();
            } catch (IOException e) {
//                e.printStackTrace();
                release(this);
                if (isAdmin) {
                    System.out.println("管理员[" + userName + "]断开了与服务器的连接，目前服务器连接人数为：" + userChannels.size() + "人");
                }
                else {
                    System.out.println("用户[" + userName + "]断开了与服务器的连接，目前服务器连接人数为：" + userChannels.size() + "人");
                }
            }
            return msg;
        }

        private void release(Channel user) {
            user.isRunning = false;
            CloseUtils.close(user.dos, user.dis, user.client);
            userChannels.remove(user);
            sendOthers("用户[" + user.userName + "]退出了聊天室", true);
        }

        @Override
        public void run() {
            while (isRunning) {
                String msg = receive();
                if (!msg.equals("")) {
                    sendOthers(msg, false);
                }
            }
        }
    }

    public static void main(String[] args) {
        ChatRoomServer chatRoomServer = new ChatRoomServer(8001); //设置聊天室服务器端口号
        chatRoomServer.startService();
    }
}
