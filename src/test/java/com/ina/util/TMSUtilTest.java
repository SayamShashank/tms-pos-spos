package com.ina.util;

import com.ina.common.exception.CommonValidationException;
import com.ina.common.response.message.InaPayMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TMSUtilTest {

    @Test
    void testThrowValidationException(){
        InaPayMessages mockMessages= mock(InaPayMessages.class);
        CommonValidationException commonValidationException=TMSUtil.throwValidationException("ref123", "200", mockMessages);
        assertNotNull(commonValidationException);
    }
}