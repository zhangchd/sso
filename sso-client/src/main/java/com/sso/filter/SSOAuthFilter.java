package com.sso.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.sso.util.HttpClientUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * author: zhangchundi
 * date: 16/5/9
 * since: 1.0
 */
public class SSOAuthFilter implements Filter {

    private String ssoService;

    private String ssoLogin;

    private String cookieName;

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
        ssoService = fConfig.getInitParameter("SSOService");
        ssoLogin = fConfig.getInitParameter("SSOLogin");
        cookieName = fConfig.getInitParameter("cookieName");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String params = request.getQueryString();
        String URL = ssoLogin + "?gotoURL=" + (params != null ? request.getRequestURL().append("?").append(params).toString() : request.getRequestURL().toString());

        Cookie ticket = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    ticket = cookie;
                    break;
                }
            }
        if (request.getRequestURI().equals(request.getContextPath() + "/logout"))
            doLogout(request, response, chain, ticket, URL);
        else if (ticket != null)
            authCookie(request, response, chain, ticket, URL);
        else
            response.sendRedirect(URL);
    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub
    }

    private void doLogout(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Cookie ticket, String URL) throws IOException, ServletException {
        try {
            Map<String, String> resultmap = new HashMap<String, String>();
            resultmap.put("action", "logout");
            resultmap.put("cookieName", ticket.getValue());
            post(request, response, chain, URL, resultmap);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            response.sendRedirect(ssoLogin + "?gotoURL=" + request.getRequestURL().toString().replace("logout", "index.jsp"));
        }
    }

    private void authCookie(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Cookie ticket, String URL) throws IOException, ServletException {
        try {
            Map<String, String> resultmap = new HashMap<String, String>();
            resultmap.put("action", "authTicket");
            resultmap.put("cookieName", ticket.getValue());

            Map<String, String> result = post(request, response, chain, URL, resultmap);
            if ("true".equals(result.get("error"))) {
                response.sendRedirect(URL);
            } else {
                request.setAttribute("username", result.get("username"));
                chain.doFilter(request, response);
            }
        } catch (JSONException e) {
            response.sendRedirect(URL);
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> post(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String URL, Map<String, String> params) throws IOException, ServletException, JSONException {
//        HttpClient httpClient = new HttpClient();
//        PostMethod postMethod = new PostMethod(ssoService);
//        postMethod.addParameters(params);
//        switch (httpClient.executeMethod(postMethod)) {
//            case HttpStatus.SC_OK:
//                return JSONObject.parseObject(postMethod.getResponseBodyAsString()+"");
//            default:
//                // 其它处理
//                return null;
//        }

        HttpClientUtil http = new HttpClientUtil();

        String result = http.doPost(ssoService, params, "UTF8");
        Map<String, String> resultMap = (Map<String, String>) JSON.parse(result);

        return resultMap;

    }

}

