package com.aunicorn.boot.client.inject;

import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

public class THttpClientFactory {

    private final static int DEFAULT_CONNECT_TIMEOUT = 1000 ;
    private final static int DEFAULT_READ_TIMEOUT = 3000 ;

    public THttpClient getTransport(String url) {
        try {
            THttpClient transport = new THttpClient(url, null);
            transport.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT );
            transport.setReadTimeout( DEFAULT_READ_TIMEOUT );
            return transport;
        } catch (TTransportException  e) {
            throw new RuntimeException(e);
        }
    }
}


