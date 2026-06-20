package com.ddz.service.impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddz.entity.User;
import com.ddz.mapper.UserMapper;
import com.ddz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public User register(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User exist = userMapper.selectOne(wrapper);
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(SecureUtil.md5(password));
        user.setNickname(username);
        user.setAvatar("/default-avatar.png");
        user.setScore(1000);
        user.setWins(0);
        user.setLosses(0);
        user.setIsGuest(0);
        user.setStatus(1);
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!SecureUtil.md5(password).equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public User guestLogin() {
        String username;
        while (true) {
            username = "guest_" + RandomUtil.randomNumbers(6);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, username);
            User exist = userMapper.selectOne(wrapper);
            if (exist == null) {
                break;
            }
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(SecureUtil.md5("guest"));
        user.setNickname("游客" + RandomUtil.randomNumbers(4));
        user.setAvatar("/default-avatar.png");
        user.setScore(1000);
        user.setWins(0);
        user.setLosses(0);
        user.setIsGuest(1);
        user.setStatus(1);
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getUserInfo(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    @Transactional
    public void updateScore(Long id, int scoreChange, boolean isWin) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return;
        }
        user.setScore(user.getScore() + scoreChange);
        if (isWin) {
            user.setWins(user.getWins() + 1);
        } else {
            user.setLosses(user.getLosses() + 1);
        }
        userMapper.updateById(user);
    }

    @Override
    public List<User> getTopPlayers(int limit) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getScore);
        wrapper.last("limit " + limit);
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(u -> u.setPassword(null));
        return list;
    }
}
