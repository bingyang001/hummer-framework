package com.hummer.common.utils;

import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

/**
 * this class wrapper http servlet sample feature
 *
 * @Author: lee
 * @version:1.0.0
 * @Date: 2019/6/21 15:53
 **/
public class HttpServletRequestUtil {
    private HttpServletRequestUtil() {

    }

    public static String getCurrentUrl(HttpServletRequest request) {
        StringBuffer buffer = request.getRequestURL();
        if (buffer != null) {
            if (!Strings.isNullOrEmpty(request.getQueryString())) {
                return String.format("%s?%s", buffer.toString()
                        , request.getQueryString());
            } else {
                return buffer.toString();
            }
        }
        return null;
    }

    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader(HttpHeaders.USER_AGENT);
    }

    public static String getHeaderFirstByKey(ServerHttpRequest request, String key) {
        if (request == null) {
            return null;
        }

        HttpHeaders httpHeaders = request.getHeaders();
        if (httpHeaders == null) {
            return null;
        }

        return httpHeaders.getFirst(key);
    }

    public static String getHeaderFirstByKey(ServerHttpRequest request, String key, Supplier<String> defaultKey) {
        String val = getHeaderFirstByKey(request, key);
        return Strings.isNullOrEmpty(val) ? defaultKey.get() : val;
    }

    public static String getHeaderFirstByKey(ServerHttpRequest request, String key
            , String defaultVal) {
        String val = getHeaderFirstByKey(request, key);
        return Strings.isNullOrEmpty(val) ? defaultVal : val;
    }
}
