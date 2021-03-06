package cn.moe.server;

import cn.moe.server.proxy.MyReflex;
import cn.moe.service.Service;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.util.HashMap;
import java.util.Map;


public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    
    private final static String LOC = "302";
    private final static String NOT_FOND = "404";
    private final static String BAD_REQUEST = "400";
    private final static String INTERNAL_SERVER_ERROR = "500";
    private static Map<String, HttpResponseStatus> mapStatus = new HashMap<String, HttpResponseStatus>();
    private volatile static Map<String, Service> mapService = new HashMap<String, Service>();

    static {
        mapStatus.put(LOC, HttpResponseStatus.FOUND);
        mapStatus.put(NOT_FOND, HttpResponseStatus.NOT_FOUND);
        mapStatus.put(BAD_REQUEST, HttpResponseStatus.BAD_REQUEST);
        mapStatus.put(INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            boolean keepaLive = HttpUtil.isKeepAlive(request);
            //System.out.println("method " + request.method());
            System.out.println("uri " + request.uri());
            String uri = request.uri().replace("/", "").trim();
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            if (mapStatus.get(uri) != null) {
                response.setStatus(mapStatus.get(uri));
                response.content().writeBytes(mapStatus.get(uri).toString().getBytes());
            } else {
                MyReflex.invokeService(request,response);
                /*
                if(request.uri().length()<=1)return;
                String path = request.uri().substring(1,request.uri().length());
                path.split("[?]");
                if(path.contains("?"))
                    path = path.substring(0, path.indexOf("?"));
                System.out.println(path);
                Service service = null;
                try {
                    service = MyReflex.getServiceByReflex(path);
                    MyReflex.invokeService(service,httpResponse,request);
                    //service.execute(httpResponse, request);
                } catch (ClassNotFindError classNotFindError) {
                    System.out.println(classNotFindError.getMessage());
                } catch (NoSuchParameter noSuchParameter) {
                    System.out.println(noSuchParameter.getMessage());
                }
                */

                /*
                if (mapService.containsKey(path)) {
                    service = mapService.get(path);
                    System.out.println("mapService");
                } else {
                    try {
                        System.out.println("getServiceByReflex");
                        service = MyReflex.getServiceByReflex(path);
                    } catch (Exception e) {
                        System.out.println("no service");
                        return;
                    }
                    mapService.put(path, service);
                }*/
                /*if(request.uri().contains("query")) {
                    String username = Parse.getRequestParam(request.uri(),"username");
                    httpResponse.content().writeBytes(Https.get(Https.currentAndDayCourses(new User(username,1))).getBytes());
                }else if(request.uri().contains("signin")){
                    String username = Parse.getRequestParam(request.uri(),"username");
                    String cid = Parse.getRequestParam(request.uri(),"cid");
                    String cname = Parse.getRequestParam(request.uri(),"cname");
                    httpResponse.content().writeBytes(Https.get(Https.signCourse(new User(username,1),new CurrentCourse(cid,cname))).getBytes());
                }*/
            }
            //重定向处理
            if (response.status().equals(HttpResponseStatus.FOUND)) {
                response.headers().set(HttpHeaderNames.LOCATION, "https://www.baidu.com/");
            }
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            if (keepaLive) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response);
            } else {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

}