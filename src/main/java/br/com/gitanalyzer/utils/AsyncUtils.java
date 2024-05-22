package br.com.gitanalyzer.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUtils {

	public static ExecutorService getExecutorServiceForLogs() {
		return Executors.newFixedThreadPool(Constants.numberOfThreadsToGenerateLogsFiles);
	}

	public static ExecutorService getExecutorServiceForTf() {
		return Executors.newFixedThreadPool(Constants.numberOfThreadsToCalculateTf);
	}

	public static ExecutorService getExecutorServiceMax() {
		return Executors.newFixedThreadPool(Constants.numberOfMaxThreads);
	}
}
