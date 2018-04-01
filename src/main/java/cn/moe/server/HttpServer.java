package cn.moe.server;

import cn.moe.server.loader.LoaderServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpServer {

    public static void start(final int port) throws Exception {

        LoaderServer loaderServer = LoaderServer.getInstance();
        new Thread(loaderServer).start();
        /*
        int i = 0;
        while (true) {
            i++;
            String name = "times:" + i;
            System.out.println(name);
            loaderServer.test();
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }

        }
        */
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup woker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {

            serverBootstrap.channel(NioServerSocketChannel.class)
                    .group(boss, woker)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("server-decoder", new HttpServerCodec());
                            ch.pipeline().addLast(new HttpServerHandler());
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            woker.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        start(8080);
    }
}