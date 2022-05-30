package src.rhino.server;

import java.net.BindException;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.AppUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * Rhino HTTP服务端，用于各组件秒级监控数据的获取
 * Created by zmz on 2020/10/29.
 */
public class RhinoHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(RhinoHttpServer.class);

    private static final int PORT = 6680;
    private static final int BACKUP_PORT = 13579;
    private static final boolean disableHttpServer = false;

    private EventLoopGroup bossGroup, workerGroup;
    private Channel serverChannel;

    private static volatile RhinoHttpServer httpServer;

    public static RhinoHttpServer getInstance(){
        if(disableHttpServer){
            return null;
        }

        if(httpServer == null){
            synchronized (RhinoHttpServer.class) {
                if (httpServer == null) {
                    httpServer = new RhinoHttpServer();
                }
            }
        }
        return httpServer;
    }

    private RhinoHttpServer(){
        start();
    }

    public void start(){
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("RhinoHttpSeverBossGroup"));
        workerGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("RhinoHttpSeverWokerGroup"));

        /*ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast("decoder", new HttpRequestDecoder());
                            pipeline.addLast("encorder", new HttpResponseEncoder());
                            pipeline.addLast("handler", new RhinoHttpServerHandler());
                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            serverChannel = b.bind(PORT).sync().channel();
            logger.info("Start RhinoHttpServer bind " + PORT);
        } catch (Throwable e) {
        	if(e instanceof BindException) {
        		try {
        			serverChannel = b.bind(BACKUP_PORT).sync().channel();
        			logger.info("Start RhinoHttpServer bind " + BACKUP_PORT);
        			return;
        		}catch(Exception ex) {
        			logger.error("RhinoHttpServer start failed,port:" + BACKUP_PORT, ex);
        		}
        	}
        	logger.error("RhinoHttpServer start failed,port:" + PORT, e);*/
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//        }
    }

    public void shutdown(){
        try {
            if(serverChannel != null){
                serverChannel.close().sync();
            }

            if(bossGroup != null){
//                bossGroup.shutdownGracefully().sync();
            }

            if(workerGroup != null){
//                workerGroup.shutdownGracefully().sync();
            }
        } catch (Throwable e) {
            logger.warn("RhinoHttpServer close failed", e);
        }
    }
}
