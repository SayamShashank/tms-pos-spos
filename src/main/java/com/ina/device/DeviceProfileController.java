package com.ina.device;

import com.ina.common.device.model.DeviceProfileBlockRequest;
import com.ina.common.device.service.DeviceProfileUpdateStatusService;
import com.ina.common.model.CommonRequest;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ina.common.constants.ApiEndpoints.BLOCK;
import static com.ina.common.constants.ApiEndpoints.UNBLOCK;
import static com.ina.constants.ApiEndpoints.*;
import static com.ina.constants.ApiEndpoints.UPDATE_DEVICE_REVOKE_STATUS;

@RestController
@RequestMapping(value = SERVER_CONTEXT_FOR_TMS + API + VERSION+DEVICE_PROFILE,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceProfileController {
    private  final DeviceProfileUpdateStatusService deviceProfileUpdateStatusService;


    public DeviceProfileController(DeviceProfileUpdateStatusService deviceProfileUpdateStatusService) {
        this.deviceProfileUpdateStatusService = deviceProfileUpdateStatusService;
    }

    @PostMapping(BLOCK)
    public CommonResponse executeTransaction(@RequestBody @Validated DeviceProfileBlockRequest request) {
        return deviceProfileUpdateStatusService.deviceBlockAndReset(request);
    }

    @PostMapping(UNBLOCK)
    public CommonResponse executeTransaction(@RequestBody @Validated CommonRequest request) {
        return deviceProfileUpdateStatusService.deviceUnblock(request);
    }

    @PostMapping(UPDATE_DEVICE_REVOKE_STATUS)
    public CommonResponse updateDeviceRevokeStatus(@RequestBody @Validated Request request) {
        return deviceProfileUpdateStatusService.updateDeviceInitAndRevokeStatus(request);
    }

}
