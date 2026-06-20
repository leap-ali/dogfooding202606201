package com.ddz.controller;

import com.ddz.entity.User;
import com.ddz.service.UserService;
import com.ddz.util.JwtUtil;
import com.ddz.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return Result.error("用户名和密码不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            return Result.error("用户名长度3-20位");
        }
        if (password.length() < 6 || password.length() > 20) {
            return Result.error("密码长度6-20位");
        }
        try {
            User user = userService.register(username, password);
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.ok(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return Result.error("用户名和密码不能为空");
        }
        try {
            User user = userService.login(username, password);
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.ok(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/guest-login")
    public Result<Map<String, Object>> guestLogin() {
        try {
            User user = userService.guestLogin();
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.ok(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        User user = userService.getUserInfo(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.ok(user);
    }

    @GetMapping("/top")
    public Result<List<User>> getTopPlayers(@RequestParam(defaultValue = "20") int limit) {
        List<User> list = userService.getTopPlayers(limit);
        return Result.ok(list);
    }
}
