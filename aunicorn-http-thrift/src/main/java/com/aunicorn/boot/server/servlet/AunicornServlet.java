package com.aunicorn.boot.server.servlet;

import com.aunicorn.boot.server.exception.ServerException;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class AunicornServlet extends HttpServlet {

	private static final long serialVersionUID = -156027764180529490L;
	private final TProcessor processor; 
	private final TProtocolFactory compactProtocolFactory = new TCompactProtocol.Factory();
	private final TProtocolFactory jsonProtocolFactory = new TJSONProtocol.Factory();
	private final TProtocolFactory binaryProtocolFactory = new TBinaryProtocol.Factory();

	private final Collection<Map.Entry<String, String>> customHeaders ;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AunicornServlet(TProcessor processor ) {
		super();
		this.processor = processor; 
		this.customHeaders = new ArrayList<Map.Entry<String, String>>();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException { 
		
		String protocol = request.getHeader("protocol");
		TProtocolFactory protocolFactory = getTProtocolFactory(protocol);
		TProtocol outProtocol = null;
		try {
			response.setContentType("application/x-thrift");
			if (null != this.customHeaders) {
				for (Map.Entry<String, String> header : this.customHeaders) {
					response.addHeader(header.getKey(), header.getValue());
				}
			}
			InputStream in = request.getInputStream();
			OutputStream out = response.getOutputStream(); 
			TTransport transport = new TIOStreamTransport(in, out);
			TProtocol inProtocol = protocolFactory.getProtocol(transport);
			outProtocol = protocolFactory.getProtocol(transport);
			processor.process(inProtocol, outProtocol);
			out.flush();
		} catch (TException te) {
			te.printStackTrace();
			throw new ServletException(te);
		} catch (ServerException ex) {
			if(outProtocol != null) {
				try {
                    response.setStatus(ex.getCode());
					TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, ex.getMessage());
					outProtocol.writeMessageBegin(new TMessage("Biz throw exception", TMessageType.EXCEPTION, 0));
					x.write(outProtocol);
					outProtocol.writeMessageEnd();
					outProtocol.getTransport().flush();
					return;
				} catch (TTransportException e) {
					throw new ServletException(e);
				} catch (TException e) {
					throw new ServletException(e);
				}
			}
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	TProtocolFactory getTProtocolFactory(String protocol){
		TProtocolFactory protocolFactory;
		if ("json".equals(protocol)) {
			protocolFactory = jsonProtocolFactory;
		} else if ("compact".equals(protocol)) {
			protocolFactory = compactProtocolFactory;
		} else {
			protocolFactory = binaryProtocolFactory;
		}
		return protocolFactory;
	}

	public void addCustomHeader(final String key, final String value) {
		this.customHeaders.add(new Map.Entry<String, String>() {
			public String getKey() {
				return key;
			}

			public String getValue() {
				return value;
			}

			public String setValue(String value) {
				return null;
			}
		});
	}

	public void setCustomHeaders(Collection<Map.Entry<String, String>> headers) {
		this.customHeaders.clear();
		this.customHeaders.addAll(headers);
	}
}

