package com.wyhCat.engin.support;

import com.wyhCat.connector.HttpExchangeRequest;
import com.wyhCat.utils.HttpUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//用于处理请求中的query参数和表单数据
public class Parameters {

    //请求主体
    final HttpExchangeRequest exchangeRequest;

    //字符集，设置编码格式等
    Charset charset;

    //参数集
    Map<String, String[]> parameters;

    public Parameters(HttpExchangeRequest exchangeRequest, String charset) {
        this.exchangeRequest = exchangeRequest;
        this.charset = Charset.forName(charset);
    }

    //设置字符集
    public void setCharset(String charset) {
        this.charset = Charset.forName(charset);
    }

    //获取参数
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values == null) {
            return null;
        }
        return values[0];
    }

    //老版本jdk用的
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    //获取参数
    public String[] getParameterValues(String name) {
        return getParameterMap().get(name);
    }

    //获取全部参数
    public Map<String, String[]> getParameterMap() {
        if (this.parameters == null) {
            this.parameters = initParameters();
        }
        return this.parameters;
    }


    Map<String, String[]> initParameters() {
        Map<String, List<String>> params = new HashMap<>();
        String query = this.exchangeRequest.getRequestURI().getRawQuery();
        if (query != null) {
            params = HttpUtils.parseQuery(query, charset);
        }
        //如果是post请求
        if ("POST".equals(this.exchangeRequest.getRequestMethod())) {
            String value = HttpUtils.getHeader(this.exchangeRequest.getRequestHeaders(), "Content-Type");
            //如果是表单类型
            if (value != null && value.startsWith("application/x-www-form-urlencoded")) {
                String requestBody;
                //尝试获取响应体
                try {
                    requestBody = new String(this.exchangeRequest.getRequestBody(), charset);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                //把表单转化成map
                Map<String, List<String>> postParams = HttpUtils.parseQuery(requestBody, charset);
                // 把请求参数和表单参数合并返回
                for (String key : postParams.keySet()) {
                    List<String> postValues = postParams.get(key);
                    List<String> queryValues = params.get(key);
                    if (queryValues == null) {
                        params.put(key, postValues);
                    } else {
                        queryValues.addAll(postValues);
                    }
                }
            }
        }
        if (params.isEmpty()) {
            return Map.of();
        }
        //把包装类转换成原始数组返回
        Map<String, String[]> paramsMap = new HashMap<>();
        for (String key : params.keySet()) {
            List<String> values = params.get(key);
            paramsMap.put(key, values.toArray(String[]::new));
        }
        return paramsMap;
    }
}