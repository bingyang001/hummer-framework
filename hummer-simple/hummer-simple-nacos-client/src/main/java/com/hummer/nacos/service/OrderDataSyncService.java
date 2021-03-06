package com.hummer.nacos.service;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * OrderDataSyncService
 *
 * @author chen wei
 * @version 1.0
 * <p>Copyright: Copyright (c) 2020</p>
 * @date 2020/10/14 11:56
 */
public interface OrderDataSyncService {


    void orderStatusUpdate(String businessCode, Integer originStatus, Integer targetStatus) throws SQLIntegrityConstraintViolationException;

    void orderChange();
}
