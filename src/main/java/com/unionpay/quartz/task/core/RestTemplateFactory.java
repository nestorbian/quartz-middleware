package com.unionpay.quartz.task.core;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateFactory {
    @Autowired
    private RestTemplateProperties config;

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(factory));
        restTemplate.getInterceptors().add(new HttpLoggingInterceptor());
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator();
        while (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            if (converter instanceof StringHttpMessageConverter) {
                iterator.remove();
            }
        }
        messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        
        return restTemplate;
    }
    
    @Bean  
    public ClientHttpRequestFactory httpRequestFactory(HttpClient httpClient) {  
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient); 
        factory.setBufferRequestBody(config.getBufferRequestBody());
        factory.setReadTimeout(config.getReadTimeout());
        return factory;  
    }  
    
    @Bean
    public HttpClient getHttpClient(PoolingHttpClientConnectionManager conManager, HttpRequestRetryHandler retryHandler, RequestConfig requestConfig) {
        return HttpClientBuilder.create().setConnectionManager(conManager)
                .setRetryHandler(retryHandler).setDefaultRequestConfig(requestConfig)
                .build();
    }
    
    @Bean
    public RequestConfig getRequestConfig() {
        return RequestConfig.custom()  
                .setSocketTimeout(config.getSocketTimeout())  
                .setConnectTimeout(config.getConnectTimeout())  
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout())  
                .build(); 
    }
    
    @Bean
    public PoolingHttpClientConnectionManager getHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(
                config.getTimeToLive(), TimeUnit.MILLISECONDS);
        poolingHttpClientConnectionManager.setMaxTotal(config.getMaxTotal());
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(config.getDefaultMaxPerRoute());
        return poolingHttpClientConnectionManager;
    }
    
    @Bean
    public HttpRequestRetryHandler getHttpRequestRetryHandler() {
        return new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception,
                                        int executionCount, HttpContext context) {
                if (executionCount >= config.getRetryTime()) {// ����Ѿ������ˣ��ͷ���
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// ������������������ӣ���ô������
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// ��Ҫ����SSL�����쳣
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// ��ʱ
                    return false;
                }
                if (exception instanceof UnknownHostException) {// Ŀ����������ɴ�
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// ���ӱ��ܾ�
                    return false;
                }
                if (exception instanceof SSLException) {// SSL�����쳣
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext
                        .adapt(context);
                HttpRequest request = clientContext.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };
    }
}
