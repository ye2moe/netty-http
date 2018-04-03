package cn.moe.server.proxy;

public class NoSuchParameter extends Throwable {
    public NoSuchParameter(String value) {
        super(value);
    }
}
