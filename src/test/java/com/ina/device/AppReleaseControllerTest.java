package com.ina.device;

import com.ina.CommonObjects;
import com.ina.common.device.model.ReleaseRequest;
import com.ina.common.device.service.CommonAppReleaseService;
import com.ina.common.model.CommonResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppReleaseControllerTest extends CommonObjects {

    @InjectMocks
    AppReleaseController appReleaseController;

    @Mock
    CommonAppReleaseService commonAppReleaseService;
    @Test
    void createTest(){
        ReleaseRequest releaseRequest=new ReleaseRequest();
        releaseRequest.setApiInContext(buildApiInContext());
        CommonResponse commonResponse=new CommonResponse();
        commonResponse.setApiOutContext(buildApiOutContextData());
        when(commonAppReleaseService.createNewRelease(releaseRequest))
                .thenReturn(commonResponse);
        CommonResponse response=appReleaseController.create(releaseRequest);
        assertNotNull(response);
    }

}