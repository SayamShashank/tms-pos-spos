package com.ina.parameters.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ina.parameters.model.AidData;
import com.ina.parameters.model.MerchantTerminalData;
import com.ina.parameters.model.RidData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class JsonMapperUtilTest {

    @InjectMocks
    JsonMapperUtil jsonMapperUtil;

    @Test
    void testMapMerchantParam_shouldReturnMerchantData() throws JSONException {
        JSONObject cardScheme = new JSONObject()
                .put("merchantId", "MERCHANT123")
                .put("merchantCategoryCode", "5411");

        JSONArray cardSchemeArray = new JSONArray().put(cardScheme);

        JSONObject cardSchemeList = new JSONObject()
                .put("cardScheme", cardSchemeArray);

        MerchantTerminalData merchantData = JsonMapperUtil.mapMerchantParam(cardSchemeList);

        assertNotNull(merchantData);
        assertEquals("MERCHANT123", merchantData.getMerchantId());
        assertEquals("5411", merchantData.getMerchantCategoryCode());
    }



    @Test
    void testGetResult_shouldReturnResult() throws JsonProcessingException, JSONException {
        String sampleXml =
                "<root>" +
                        "<madaAppData><cardSchemeList>" +
                        "<cardScheme>" +
                        "<merchantId>MERCHANT123</merchantId>" +
                        "<merchantCategoryCode>5411</merchantCategoryCode>" +
                        "<aidList><aidForm>" +
                        "<aid>A0000000031010</aid>" +
                        "<aidLabel>Visa Debit</aidLabel>" +
                        "</aidForm></aidList>" +
                        "</cardScheme>" +
                        "</cardSchemeList></madaAppData>" +
                        "<madaTrmlData>" +
                        "<terminalId>TERM123</terminalId>" +
                        "<terminalCountryCode>682</terminalCountryCode>" +
                        "<terminalCurrencyCode>682</terminalCurrencyCode>" +
                        "</madaTrmlData>" +
                        "</root>";

        String merchantXml =
                "<merchant>"
                        + "<reconTime>9999</reconTime>"
                        + "<retailerNameArab>اختبار متجر تجريبي</retailerNameArab>"
                        + "<retailerNumber>123</retailerNumber>"
                        + "<retailerNameEng>TEST DEMO SUPERMARKET</retailerNameEng>"
                        + "<retailerLangInd>1</retailerLangInd>"
                        + "<arabReceipt1>فاتورة تجريبية 1</arabReceipt1>"
                        + "<arabReceipt2>فاتورة تجريبية 2</arabReceipt2>"
                        + "<engReceipt1>Demo Receipt 1</engReceipt1>"
                        + "<engReceipt2>Demo Receipt 2</engReceipt2>"
                        + "<retailerAddress1Arab>عنوان أول تجريبي</retailerAddress1Arab>"
                        + "<retailerAddress1Eng>123 Demo Street</retailerAddress1Eng>"
                        + "<retailerAddress2Arab>عنوان ثاني تجريبي</retailerAddress2Arab>"
                        + "<retailerAddress2Eng>456 Test Avenue</retailerAddress2Eng>"
                        + "<retailerCityEng>TestCity</retailerCityEng>"
                        + "<retailerCityArab>مدينة الاختبار</retailerCityArab>"
                        + "<retailerPostalCode>0000000000</retailerPostalCode>"
                        + "<retailerRegionCode>99</retailerRegionCode>"
                        + "<retailerCountryCode>XXX</retailerCountryCode>"
                        + "<retailerPhoneNumber>1234567890</retailerPhoneNumber>"
                        + "<storeId>9999</storeId>"
                        + "</merchant>";

        JSONObject expectedAppJson = new JSONObject()
                .put("madaAppData", new JSONObject()
                        .put("cardSchemeList", new JSONObject()
                                .put("cardScheme", new JSONObject()
                                        .put("merchantId", "MERCHANT123")
                                        .put("merchantCategoryCode", "5411")
                                        .put("aidList", new JSONObject()
                                                .put("aidForm", new JSONObject()
                                                        .put("aid", "A0000000031010")
                                                        .put("aidLabel", "Visa Debit")
                                                )
                                        )
                                )
                        )
                )
                .put("madaTrmlData", new JSONObject()
                        .put("terminalId", "TERM123")
                        .put("terminalCountryCode", "682")
                        .put("terminalCurrencyCode", "682")
                );

        JSONObject merchantInnerJson = new JSONObject()
                .put("retailerNameEng", "TEST DEMO SUPERMARKET")
                .put("retailerNameArab", "اختبار متجر تجريبي")
                .put("retailerAddress1Arab", "عنوان أول تجريبي")
                .put("retailerAddress1Eng", "123 Demo Street")
                .put("retailerAddress2Eng", "456 Test Avenue")
                .put("retailerAddress2Arab", "عنوان ثاني تجريبي")
                .put("retailerCityArab", "مدينة الاختبار")
                .put("retailerCityEng", "TestCity");

        List<String> appParamsList = Collections.singletonList(sampleXml);
        List<String> merchParamsList = Collections.singletonList(merchantXml);

        JsonMapperUtil.Result result;

        try (MockedStatic<XML> mockedXml = mockStatic(XML.class)) {
            mockedXml.when(() -> XML.toJSONObject(sampleXml))
                    .thenReturn(new JSONObject().put("root", expectedAppJson));

            // ✅ fixed mock: wrap merchant inside root
            mockedXml.when(() -> XML.toJSONObject(merchantXml))
                    .thenReturn(new JSONObject().put("root", new JSONObject().put("merchant", merchantInnerJson)));

            result = JsonMapperUtil.getResult(appParamsList, merchParamsList);
        }

        // assert
        assertNotNull(result);
        assertNotNull(result.aidLists());
        assertNotNull(result.terminalConfig());
        assertNotNull(result.merchantDetails());
        assertEquals("TEST DEMO SUPERMARKET", result.merchantDetails().getMerchantNameEN());
    }



    @Test
    void testMapAidLists_shouldReturnAidList() throws JSONException {
        JSONObject aidForm = new JSONObject()
                .put("aid", "A0000000031010")
                .put("aidLabel", "Visa Debit")
                .put("denialActionCode", "001122")
                .put("onlineActionCode", "334455")
                .put("defaultActionCode", "667788")
                .put("iccReaderList", new JSONObject()
                        .put("iccReaderForm", new JSONObject()
                                .put("terminalCapability", "E0F8")
                                .put("addTerminalCapability", "ABCDEF")))
                .put("contactLessReaderList", new JSONObject()
                        .put("termCVMRequiredLimit", "5000")
                        .put("terminalContactlessFloorLimit", "2000")
                        .put("termContactlessTxnLimit", "10000"));

        JSONObject aidList = new JSONObject()
                .put("aidForm", new JSONArray().put(aidForm)); // non-empty array

        JSONObject cardScheme = new JSONObject()
                .put("emvTerminalType", "22")
                .put("aidList", aidList);

        JSONObject cardSchemeList = new JSONObject()
                .put("cardScheme", new JSONArray().put(cardScheme)); // non-empty array

        List<AidData> aidLists = JsonMapperUtil.mapAidLists(cardSchemeList);

        assertNotNull(aidLists);
        assertFalse(aidLists.isEmpty());

    }

    @Test
    void testMapRidLists_shouldReturnRidList() throws JSONException {
        JSONObject ridForm = new JSONObject()
                .put("rid", "A000000003")
                .put("keyIndex", "01")
                .put("hashId", "SHA1")
                .put("publicKey", "ABCDEF")
                .put("caPublicKeyExpDate", "251231")
                .put("checkSum", "123456")
                .put("exponent", "03");

        JSONObject ridList = new JSONObject()
                .put("ridForm", new JSONArray().put(ridForm));

        JSONObject cardScheme = new JSONObject()
                .put("ridList", ridList);

        JSONObject cardSchemeList = new JSONObject()
                .put("cardScheme", new JSONArray().put(cardScheme));

       List<RidData> ridLists = JsonMapperUtil.mapRidLists(cardSchemeList);

        assertNotNull(ridLists);
    }
    private String getXmlData() {
        return "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:catm.003.001.08\"><AccptrCfgtnUpd><Hdr><DwnldTrf>true</DwnldTrf><FrmtVrsn>8.0</FrmtVrsn><XchgId>250626201623</XchgId><CreDtTm>2025-06-26T20:16:23.2288994</CreDtTm><InitgPty><Id>6383593278311080</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr></InitgPty><RcptPty><Id>SPTMS2.0</Id><Tp>MasterTerminalManager</Tp></RcptPty></Hdr><AccptrCfgtn><DataSet><Id><Tp>Parameters</Tp><CreDtTm>2025-06-26T17:47:09.950+03:00</CreDtTm></Id><POIId><Id>6383593278311080</Id><Tp>OriginatingPOI</Tp></POIId><Cntt></Cntt></DataSet></AccptrCfgtn><SctyTrlr><CnttTp>SIGN</CnttTp><SgndData><DgstAlgo><Algo>HS25</Algo></DgstAlgo><NcpsltdCntt><CnttTp>DATA</CnttTp></NcpsltdCntt><Sgnr><DgstAlgo><Algo>HS25</Algo></DgstAlgo><SgntrAlgo><Algo>ERS2</Algo></SgntrAlgo><Sgntr/></Sgnr></SgndData></SctyTrlr></AccptrCfgtnUpd></Document>";
    }
}
