package cn.aunicorn.controller;

import cn.aunicorn.api.HelloService;
import cn.aunicorn.boot.client.inject.AunicornClient;
import org.apache.thrift.TException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class TestController{

	@AunicornClient(url = "/hello", serviceId = "service-provider")
	private HelloService.Iface helloService;


	@RequestMapping("/index")
	public String service(HttpServletRequest req, HttpServletResponse resp) throws TException {
		return "hello u99933";
	}

	@RequestMapping("/i")
	public String sayHello(HttpServletRequest req, HttpServletResponse resp) throws TException {
		return helloService.sayHello("f22f");

	}
}
