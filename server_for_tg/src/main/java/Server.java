import Handlers.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    private final int PORT = 8000;
    private final int THREADS = 3;
    private HttpServer httpServer;

    public Server() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), THREADS);
            httpServer.createContext("/user/getUsers", new UserRegisterHandler());
            httpServer.createContext("/user/postUser", new postUserHandler());
            httpServer.createContext("/user/getUser", new getUserHandler());
            httpServer.createContext("/user/getUserById", new getUserByIdHandler());
            httpServer.createContext("/order/getUserTime", new getUserTimeHandler());
            httpServer.createContext("/order/getAllTime", new getAllTimeHandler());
            httpServer.createContext("/order/postDate", new postDateHandler());
            httpServer.createContext("/order/postTime", new postTimeHandler());
            httpServer.createContext("/order/getOrderId", new getOrderIdHandler());
            httpServer.createContext("/order/deleteOrder", new deleteOrderHandler());
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
