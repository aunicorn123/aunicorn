package cn.aunicorn.boot.client.stubfactory;

import org.apache.thrift.protocol.TProtocol;
import java.lang.reflect.InvocationTargetException;

public interface StubFactory {


    Object createStub(Class<? extends Object> stubType, TProtocol protocol) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;

    boolean isApplicable(Class<? extends Object> stubType);

}
