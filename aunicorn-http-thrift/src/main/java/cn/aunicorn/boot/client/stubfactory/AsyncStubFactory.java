package cn.aunicorn.boot.client.stubfactory;

import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.protocol.TProtocol;
import java.lang.reflect.InvocationTargetException;

public final class AsyncStubFactory implements StubFactory {

    @Override
    public Object createStub(Class<?> stubType, TProtocol protocol) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> targetClz = getTargetClass(stubType);
        return targetClz.getDeclaredConstructor(TProtocol.class).newInstance(protocol);
    }

    @Override
    public boolean isApplicable(Class<?> stubType) {
        return getTargetClass(stubType) != null;
    }

    Class<?> getTargetClass(Class<?> stubType){
        for (final Class<?> clz : stubType.getDeclaringClass().getDeclaredClasses()) {
            if(TAsyncClient.class.isAssignableFrom(clz) && stubType.isAssignableFrom(clz)){
                return clz;
            }
        }
        return null;
    }
}
