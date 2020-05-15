package com.laohu.consumerribbon.service;

import entity.User;

import java.util.List;

public interface ConsumerService {

    String consume();

    String consumeHystrixCache(String name, String age);

    User findUser(Long id);

    List<User> findAllUsers(String ids);
}
