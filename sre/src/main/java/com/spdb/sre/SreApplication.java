package com.spdb.sre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import com.spdb.sre.handler.CommandHandlerFactory;
import com.spdb.sre.handler.ExecutePowershellHandler;
import com.spdb.sre.handler.ICommandHandler;
import com.spdb.sre.handler.ICreateHandler;
import com.spdb.sre.model.WsRequestType;

@SpringBootApplication
@EnableAsync
public class SreApplication {

	public static void main(String[] args) {

		CommandHandlerFactory.register(WsRequestType.ExecutePowershell, new ICreateHandler() {
			@Override
			public ICommandHandler get() {
				return new ExecutePowershellHandler();
			}
		});

		SpringApplication.run(SreApplication.class, args);
	}

}
