package com.hummer.nacos.rest;

import com.hummer.nacos.service.OrderDataSyncService;
import com.hummer.rest.model.ResourceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * DataSyncController
 *
 * @author chen wei
 * @version 1.0
 * <p>Copyright: Copyright (c) 2020</p>
 * @date 2020/10/14 11:52
 */
@Api("DataSyncController")
@RestController
@RequestMapping("/v1/data/sync")
public class DataSyncController {

    @Resource
    private OrderDataSyncService orderDataSyncService;

    @ApiOperation(value = "order/change")
    public ResourceResponse<Void> orderChangeTest(
            @RequestParam("businessCode") String businessCode,
            @RequestParam("originStatus") Integer originStatus,
            @RequestParam("targetStatus") Integer targetStatus
    ) {

        orderDataSyncService.orderStatusUpdate(businessCode, originStatus, targetStatus);
        return ResourceResponse.ok();
    }
}
