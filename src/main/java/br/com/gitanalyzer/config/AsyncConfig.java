//package br.com.gitanalyzer.config;
//
//import java.util.concurrent.Executor;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//@Configuration
//@EnableAsync
//public class AsyncConfig {
//
//	@Bean(name="taskExecutor")
//	public Executor taskExecutor() {
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(10);    // Adjust the pool size as needed
//		executor.setMaxPoolSize(10);
//		executor.setQueueCapacity(500);
//		executor.setThreadNamePrefix("MyAsyncThread-");
//		executor.initialize();
//		return executor;
//	}
//
//}
