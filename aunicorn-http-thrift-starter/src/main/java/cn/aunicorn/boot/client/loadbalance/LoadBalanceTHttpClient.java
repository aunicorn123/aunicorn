package cn.aunicorn.boot.client.loadbalance;


import cn.aunicorn.boot.common.AunicornRpcContext;
import org.apache.http.HttpStatus;
import org.apache.thrift.TConfiguration;
import org.apache.thrift.transport.TEndpointTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.cloud.netflix.ribbon.apache.RibbonApacheHttpRequest;
import org.springframework.cloud.netflix.ribbon.apache.RibbonApacheHttpResponse;
import org.springframework.cloud.netflix.ribbon.apache.RibbonLoadBalancingHttpClient;
import org.springframework.cloud.netflix.ribbon.support.RibbonCommandContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

public class LoadBalanceTHttpClient extends TEndpointTransport {

    private final RibbonLoadBalancingHttpClient client;
    private final AunicornRpcContext rpcContext;
    private  RibbonApacheHttpResponse response = null;
    private final ByteArrayOutputStream requestBuffer_ = new ByteArrayOutputStream();
    private InputStream inputStream_ = null;

    public LoadBalanceTHttpClient(RibbonLoadBalancingHttpClient client, AunicornRpcContext rpcContext) throws TTransportException {
        super(new TConfiguration());
        this.client = client;
        this.rpcContext = rpcContext;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void open() throws TTransportException {

    }

    @Override
    public void close() {
        if (null != inputStream_) {
            try {
                inputStream_.close();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }finally {
                inputStream_ = null;
            }
        }
        if(null != response){
            response.close();
        }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TTransportException {
        try {
            int ret = inputStream_.read(buf, off, len);
            if (ret == -1) {
                throw new TTransportException("No more data available.");
            }
            return ret;
        } catch (IOException iox) {
            throw new TTransportException(iox);
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        requestBuffer_.write(buf, off, len);
    }


    @Override
    public void updateKnownMessageSize(long size) throws TTransportException {

    }

    @Override
    public void checkReadBytesAvailable(long numBytes) throws TTransportException {

    }

    @Override
    public void flush() throws TTransportException {
        if (null == this.client) {
            throw new TTransportException("Null HttpClient, aborting.");
        }
        // Extract request and reset buffer
        byte[] data = requestBuffer_.toByteArray();
        requestBuffer_.reset();
        //创建请求
        RibbonApacheHttpRequest httpRequest = getHttpClientRequest(this.rpcContext, data);
        doPost(httpRequest);
    }

    @Override
    public TConfiguration getConfiguration() {
        return new TConfiguration();
    }

    void doPost(RibbonApacheHttpRequest httpRequest) throws TTransportException{
        try {
            response = this.client.executeWithLoadBalancer(httpRequest);
            int responseCode = response.getStatus();
            byte[] result = IOUtils.toByteArray(response.getInputStream());
            if (responseCode != HttpStatus.SC_OK) {
                throw new TTransportException("HTTP Response code: " + responseCode);
            }
            inputStream_ = new ByteArrayInputStream(result);
        } catch (Exception e) {
            throw new TTransportException(e);
        } finally {
            if(null != response){
                response.close();
            }
        }
    }

    RibbonApacheHttpRequest getHttpClientRequest(AunicornRpcContext context, byte[] data){
        String method =  "POST";
        Boolean retryable = true;
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", "application/x-thrift");
        headers.add("Accept", "application/x-thrift");
        headers.add("User-Agent", "Java/THttpClient/HC");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        InputStream requestEntity = new ByteArrayInputStream(data);

        RibbonCommandContext ribbonCommandContext = new RibbonCommandContext(context.getServiceId(), method,
                context.getUrl(), retryable, headers, params, requestEntity);
        return new RibbonApacheHttpRequest(ribbonCommandContext);
    }
}
