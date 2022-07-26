package com.aunicorn.controller;

import com.aunicorn.boot.server.service.AunicornService;
import org.apache.thrift.TException;
import com.aunicorn.api.HelloService;

@AunicornService("/hello")
public class BizTest implements HelloService.Iface{

	@Override
	public String sayHello(String name) throws TException {

		return "hello " + name;
	}
}
