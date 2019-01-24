package com.example.demo.config;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandInterceptor;

public class MyProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

	@Override
	protected CommandInterceptor createTransactionInterceptor() {
		return null;
	}

}
