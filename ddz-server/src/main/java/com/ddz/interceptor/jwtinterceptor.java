package com.ddz.interceptor;

import com.alibaba.fastjson.JSON;
import com.ddz.util.JwtUtil;
import com.ddz.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if ("OPTIONS".equals(method)) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (!jwtUtil.isExpired(token)) {
                Long userId = jwtUtil.getUserId(token);
                request.setAttribute("userId", userId);
                return true;
            }
        }
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write(JSON.toJSONString(Result.error(401, "未登录或token已过期")));
        out.flush();
        out.close();
        return false;
    }
}
