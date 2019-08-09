package com.hummer.api.web;

import com.hummer.api.dto.KafkaMessageReq;
import com.hummer.api.dto.QueryStringDto;
import com.hummer.common.utils.ObjectCopyUtils;
import com.hummer.message.facade.publish.MessageBus;
import com.hummer.rest.annotations.BindRestParameterSimpleModel;
import com.hummer.rest.model.ResourceResponse;
import com.hummer.rest.utils.ParameterAssertUtil;
import com.hummer.spring.plugin.context.PropertiesContainer;
import com.hummer.spring.plugin.context.SpringApplicationContext;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * Created by MRomeh on 08/08/2017.
 */
@RestController
@RequestMapping("/v1")
@Api(value = "Applciation demo")
@Validated
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);


    @Value("${test.A}")
    private int value;

    @GetMapping(value = "/all", produces = "application/json")
    @ResponseBody
    public String getAllAlerts() {

        log.debug("Trying to retrieve all alerts");
        log.info("---------------------{}---------------------", SpringApplicationContext.getBean("demo"));
        log.info("*********************{}*********************", value);
        log.info("+++++++++++++++++++++{}+++++++++++++++++++++", PropertiesContainer.get("test.A", String.class));
        return PropertiesContainer.get("test.A", String.class);

    }

    @GetMapping(value = "/query")
    public ResourceResponse<QueryStringDto> queryStringParse(@BindRestParameterSimpleModel @Valid
                                                                 QueryStringDto queryStringDto
        , Errors errors) {
        ParameterAssertUtil.assertRequestFristValidated(errors);
        return ResourceResponse.ok(ObjectCopyUtils.copy(queryStringDto, QueryStringDto.class));
    }

    @GetMapping(value = "/query2")
    public ResourceResponse<QueryStringDto> queryStringParse2(@ModelAttribute @Valid
                                                                  QueryStringDto queryStringDto
        , Errors errors) {
        ParameterAssertUtil.assertRequestFristValidated(errors);
        return ResourceResponse.ok(ObjectCopyUtils.copy(queryStringDto, QueryStringDto.class));
    }

    @GetMapping(value = "/query3")
    public ResourceResponse<QueryStringDto> queryStringParse3(@RequestParam
                                                              @NotEmpty(message = "uuId can't null")
                                                                  String uuId,
                                                              @RequestParam
                                                              @NotNull(message = "class id can't null")
                                                                  Integer classId,
                                                              @RequestParam
                                                              @NotNull(message = "class id can't null")
                                                                  Collection<String> users
    ) {
        QueryStringDto queryStringDto = new QueryStringDto();
        queryStringDto.setClassId(classId);
        queryStringDto.setUuId(uuId);
        queryStringDto.setUsers(users);
        return ResourceResponse.ok(queryStringDto);
    }


    @PostMapping(value = "/message_bus")
    public ResourceResponse sendMessage(@RequestBody KafkaMessageReq req) {
        MessageBus
            .builder()
            .appId("test")
            .body(req)
            .callback((o, e) -> log.info("send message done"))
            .kafka(MessageBus.Kafka.builder().topicId("log-type-group-out2").messageKey(req.getId()).build())
            .build()
            .publish();

        return ResourceResponse.ok();
    }
}