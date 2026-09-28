package com.bankofcli.repository.contract;

import com.bankofcli.model.SystemLog;

import java.util.List;

public interface SystemLogRepository {

    SystemLog save(SystemLog systemLog);

    List<SystemLog> findByUserId(long userId, int limit);
}