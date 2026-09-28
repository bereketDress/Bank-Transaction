package com.bankofcli.service.contract;

import com.bankofcli.model.User;

public interface UserService {
    User registerUser(String name, String email, String password);
    User loginUser(String email, String password);
    User getUser(long userId);
}
