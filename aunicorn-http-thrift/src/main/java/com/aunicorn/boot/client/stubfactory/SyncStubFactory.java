package com.aunicorn.boot.client.stubfactory;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import java.lang.reflect.InvocationTargetException;

public final class SyncStubFactory implements StubFactory {

    @Override
    public Object createStub(Class<?> stubType, TProtocol protocol) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> targetClz = getTargetClass(stubType);
        return targetClz.getDeclaredConstructor(TProtocol.class).newInstance(protocol);
    }

    @Override
    public boolean isApplicable(Class<?> stubType) {
        if(!stubType.getName().endsWith("$Iface")){
            return false;
        }
        return getTargetClass(stubType) != null;
    }

    Class<?> getTargetClass(Class<?> stubType){
        for (final Class<?> clz : stubType.getDeclaringClass().getDeclaredClasses()) {
            if(TServiceClient.class.isAssignableFrom(clz)){
                return clz;
            }
        }
        return null;
    }
}
