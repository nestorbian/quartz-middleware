package com.unionpay.quartz.task.core;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpLoggingInterceptor implements ClientHttpRequestInterceptor {
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		log.info("**Send http Request**");
		log.info("请求地址：{}", request.getURI());
		log.info("请求方法： {}", request.getMethod());
		log.info("请求内容：{}", new String(body));
		log.info("请求头：{}", request.getHeaders());
		ClientHttpResponse response = execution.execute(request, body);
		log.info("Response: {} {}", response.getStatusCode().value(),
				StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
		log.info("**End**");

		return response;
	}
}
