package cn.aunicorn.boot.client.inject;

import cn.aunicorn.boot.client.loadbalance.LoadBalanceTHttpClient;
import cn.aunicorn.boot.common.AunicornRpcContext;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.netflix.ribbon.apache.RibbonLoadBalancingHttpClient;
import org.springframework.context.ApplicationContext;
import static java.util.Objects.requireNonNull;

public class AunicornTTransportFactory {

    private final static int DEFAULT_CONNECT_TIMEOUT = 1000 ;
    private final static int DEFAULT_READ_TIMEOUT = 3000 ;
    private final ApplicationContext applicationContext;

    public AunicornTTransportFactory(final ApplicationContext applicationContext){
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    public TTransport getTransport(AunicornRpcContext rpcContext) {

        if(StringUtils.isBlank(rpcContext.getServiceId())){
           return getHttpTransport(rpcContext);
        }
        return getLoadBalanceTransport(rpcContext);
    }

    /**
     * get http transport
     * @param rpcContext
     * @return
     */
   private TTransport getHttpTransport(AunicornRpcContext rpcContext){
        try {
            THttpClient transport = new THttpClient(rpcContext.getUrl(), null);
            transport.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT );
            transport.setReadTimeout( DEFAULT_READ_TIMEOUT );
            return transport;
        } catch (TTransportException  e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get loadBalance transport
     * @param rpcContext
     * @return
     */
    private TTransport getLoadBalanceTransport(AunicornRpcContext rpcContext){

        SpringClientFactory springClientFactory = applicationContext.getBean(SpringClientFactory.class);
        ILoadBalancer lb = springClientFactory.getLoadBalancer(rpcContext.getServiceId());
        RibbonLoadBalancingHttpClient client = springClientFactory.getClient(rpcContext.getServiceId(), RibbonLoadBalancingHttpClient.class);
        client.setLoadBalancer(lb);
        try {
            TTransport transport = new LoadBalanceTHttpClient(client, rpcContext);
            return transport;
        } catch (TTransportException  e) {
            throw new RuntimeException(e);
        }
    }
}


