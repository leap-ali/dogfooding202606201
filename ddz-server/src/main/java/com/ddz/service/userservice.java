package com.ddz.service;

import com.ddz.entity.User;
import java.util.List;

public interface UserService {

    User register(String username, String password);

    User login(String username, String password);

    User guestLogin();

    User getUserById(Long id);

    User getUserInfo(Long id);

    void updateScore(Long id, int scoreChange, boolean isWin);

    List<User> getTopPlayers(int limit);
}
