package cn.moe.server.loader;

public class ClassNotFindError extends Throwable {
    public ClassNotFindError(String message) {
        super(message);
    }
}
