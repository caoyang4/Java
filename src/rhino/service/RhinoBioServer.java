package src.rhino.service;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.service.command.Command;
import src.rhino.service.command.CommandResponse;
import src.rhino.service.command.RhinoServerHandler;
import src.rhino.service.command.RhinoServerHandlerImpl;

/**
 * Created by zhen on 2018/11/29.
 */
public class RhinoBioServer {

    private final static Logger logger = LoggerFactory.getLogger(RhinoBioServer.class);
    private final static String CAT_TYPE = "Rhino.Client.Server";
    private static final int DEFAULT_PORT = 6680;
    private static final int DEFAULT_SO_TIMEOUT = 2000;
    private static RhinoBioServer Instance = new RhinoBioServer();
    private ExecutorService requestService = Executors.newFixedThreadPool(5);
    private ServerSocket serverSocket;
    private int port;
    private AtomicReference<RhinoBioServer.Status> status = new AtomicReference<>(Status.NOT_START);

    private enum Status {
        NOT_START, RUNNING, STOP
    }

    private RhinoBioServer() {
    }

    public static RhinoBioServer getInstance() {
        return Instance;
    }

    public int start() {
        if (status.compareAndSet(Status.NOT_START, Status.RUNNING)) {
            try {
                logger.info("rhino-client server starting...");
//                Cat.logEvent(CAT_TYPE, "STARTING");
                runServer();
                logger.info("rhino-client server start success on " + port);
//                Cat.logEvent(CAT_TYPE, "STARTED@" + port);
            } catch (Exception e) {
//                Cat.logEvent(CAT_TYPE, "FAILED");
                logger.info("rhino-client server start failed...");
                status.set(Status.NOT_START);
            }
        }
        return port;
    }

    public void bind() {
        int port = DEFAULT_PORT;
        int retryTime = 99;
        ServerSocket serverSocket = null;
        while (retryTime > 0) {
            try {
                serverSocket = new ServerSocket(port);
                break;
            } catch (BindException be) {
                port++;
                retryTime--;
            } catch (IOException e) {
                logger.info("fail to init server with port " + port);
                throw new RuntimeException("fail to init server with port " + port, e);
            }
        }

        if (retryTime == 0) {
            logger.info("all ports are unavailable");
            throw new RuntimeException("all ports are unavailable ," + port);
        }
        this.serverSocket = serverSocket;
        this.port = port;
    }

    public void runServer() {
        bind();
        Thread bioServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (serverSocket != null) {
                    try {
                        Socket socket = serverSocket.accept();
                        socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
                        requestService.submit(new RequestTask(socket));
                    } catch (SocketException se) {
                        if ((se.getMessage().equals("Socket is closed")
                                || se.getMessage().equals("Socket closed"))) {
                            logger.info("stop listening");
                            break;
                        }
                    } catch (Throwable t) {
                        //ignore exception
                    }
                }
            }
        }, "rhino-server");
        bioServerThread.setDaemon(true);
        bioServerThread.start();
    }

    public void destroy() {
        if (status.compareAndSet(Status.RUNNING, Status.STOP)) {
            requestService.shutdown();
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    serverSocket = null;
                } catch (IOException e) {
                    //ignore exception
                }
            }
//            Cat.logEvent(CAT_TYPE, "STOP");
        }
    }

    private static class RequestTask implements Runnable {
        private Socket socket;

        public RequestTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            DataInputStream inputStream = null;
            DataOutputStream outputStream = null;
            try {
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
                RhinoServerHandler rhinoServerHandler = new RhinoServerHandlerImpl();
                Command command = rhinoServerHandler.decode(inputStream);
                logger.info("receive command: " + command.getName());
                CommandResponse response = new CommandResponse();
                try {
                    Object data = command.run();
//                    Cat.logEvent(CAT_TYPE, "HANDLE SUCCESS:" + command.getName());
                    response.setSuccess(data);
                } catch (Exception e) {
//                    Cat.logEvent(CAT_TYPE, "HANDLE FAILED:" + command.getName());
//                    Cat.logError(e);
                    response.setError(e.getMessage());
                }
                rhinoServerHandler.encode(response, outputStream);
            } catch (Throwable t) {
                //ignore
            } finally {
                closeQuietly(inputStream, outputStream, socket);
            }
        }

        private void closeQuietly(Closeable... closeable) {
            for (Closeable item : closeable) {
                if (item != null) {
                    try {
                        item.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }

    }
}
