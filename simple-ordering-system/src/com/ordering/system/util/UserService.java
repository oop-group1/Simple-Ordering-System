package com.ordering.system.util;

import com.ordering.system.dao.UserDAO;
import com.ordering.system.model.User;

public class UserService {
    private UserDAO userDao = new UserDAO();

    public User loginUser(String email, String password) {
        User user = userDao.findUserByUsername(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}