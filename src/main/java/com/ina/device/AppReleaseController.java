package com.ina.device;



import com.ina.common.device.model.ReleaseRequest;
import com.ina.common.device.service.CommonAppReleaseService;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import static com.ina.constants.ApiEndpoints.CREATE;
import static com.ina.constants.ApiEndpoints.RELEASE;

@RestController
@RequestMapping(value = RELEASE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AppReleaseController {

    private final CommonAppReleaseService appReleaseService;

    public AppReleaseController(CommonAppReleaseService appReleaseService) {
        this.appReleaseService = appReleaseService;
    }

    @PostMapping(CREATE)
    public CommonResponse create(@RequestBody @Validated ReleaseRequest request) {
        return appReleaseService.createNewRelease(request);
    }

}
