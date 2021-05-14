# JavaSE控制台版聊天室

**Author：Xtarling**

## 简介

本项目使用JavaSE相关知识实现一个控制台窗口的多人聊天室，网络应用模型选用C/S模型，采取服务端消息分发的思想进行程序设计。

实现功能：

- 登录校验
- 多人群聊
- 普通用户附加功能：私聊
- 管理员附加功能：私聊、发布广告、修改广告内容、踢出聊天室成员

## 项目结构

```shell
\---src
    \---cn
        \---xtarling
            \---chatroom
                +---client
                |       ChatRoomClient.java
                |       Receive.java
                |       Send.java
                |
                +---server
                |       ChatRoomLoginServer.java
                |       ChatRoomServer.java
                |
                \---utils
                        CloseUtils.java
```

## 使用说明

- 启动时，先运行ChatRoomLoginServer.java和ChatRoomServer.java，再运行ChatRoomClient.java。
- ChatRoomClient.java可运行多个实例，代表不同的客户端。



祝各位玩得开心！

