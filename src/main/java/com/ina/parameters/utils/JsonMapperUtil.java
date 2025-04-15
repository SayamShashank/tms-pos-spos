package com.ina.parameters.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ina.parameters.model.*;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.json.XML;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
@Slf4j
public class JsonMapperUtil {
    public static List<AidList> mapAidLists(JSONObject cardSchemeList) {
        return Optional.ofNullable(cardSchemeList.optJSONArray("cardScheme"))
                .map(cardSchemes -> IntStream.range(0, cardSchemes.length())
                        .mapToObj(cardSchemes::getJSONObject)
                        .map(JsonMapperUtil::mapAidList)
                        .filter(aidList -> aidList.getAidDataList() != null && !aidList.getAidDataList().isEmpty())
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }


    private static AidList mapAidList(JSONObject cardScheme) {
        AidList aidList = new AidList();
        String emvTerminalType = cardScheme.optString("emvTerminalType",null);
        Optional.ofNullable(cardScheme.optJSONObject("aidList"))
                .map(aidListJson -> aidListJson.optJSONObject("aidForm"))
                .ifPresent(aidFormObject ->
                        aidList.setAidDataList(Collections.singletonList(mapAIDData(aidFormObject,emvTerminalType))));

        return aidList;
    }

    private static AidData mapAIDData(JSONObject aidJson, String emvTerminalType) {
        AidData aidData = new AidData();
        if (aidJson != null) {
            JSONObject iccReaderForm = Optional.ofNullable(aidJson.optJSONObject("iccReaderList"))
                    .map(obj -> obj.optJSONObject("iccReaderForm"))
                    .orElse(new JSONObject());
            JSONObject contactLessReaderList = aidJson.optJSONObject("contactLessReaderList");
            aidData.setTerminalCapability(iccReaderForm.optString("terminalCapability", null));
            if (contactLessReaderList != null) {
                aidData.setCvmLimit(contactLessReaderList.optString("termCVMRequiredLimit", null));
                aidData.setFloorLimit(contactLessReaderList.optString("terminalContactlessFloorLimit", null));
                aidData.setTransactionLimit(contactLessReaderList.optString("termContactlessTxnLimit", null));
            }
            aidData.setAid(aidJson.optString("aid",null));
            aidData.setEmvTerminalType(emvTerminalType);
            aidData.setTacDenial(aidJson.optString("denialActionCode", null));
            aidData.setTacOnline(aidJson.optString("onlineActionCode", null));
            aidData.setTacDefault(aidJson.optString("defaultActionCode", null));
        }
        return aidData;
    }
    public static List<RidList> mapRidLists(JSONObject cardSchemeList) {
        return Optional.ofNullable(cardSchemeList.optJSONArray("cardScheme"))
                .map(cardSchemes -> IntStream.range(0, cardSchemes.length())
                        .mapToObj(cardSchemes::getJSONObject)
                        .map(JsonMapperUtil::mapRidList)
                        .filter(aidList -> !aidList.getRidDataList().isEmpty())
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }

    public static RidList mapRidList(JSONObject cardScheme) {
        RidList ridList = new RidList();
        List<RidData> ridDataList = Optional.ofNullable(cardScheme.optJSONObject("ridList"))
                .map(ridListJson -> ridListJson.optJSONArray("ridForm"))
                .map(ridForm -> IntStream.range(0, ridForm.length())
                        .mapToObj(ridForm::optJSONObject)
                        .map(JsonMapperUtil::mapRidData)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());

        ridList.setRidDataList(ridDataList);
        return ridList;
    }

    private static RidData mapRidData(JSONObject ridJson) {
        if (ridJson == null) {
            return null;
        }

        RidData ridData = new RidData();
        ridData.setRid(ridJson.optString("rid"));
        ridData.setKeyId(ridJson.optString("keyIndex"));
        ridData.setRsaArithmeticIndex(ridJson.optString("hashId"));
        ridData.setModule(ridJson.optString("publicKey"));
        ridData.setExpiry(ridJson.optString("caPublicKeyExpDate"));
        ridData.setChecksum(ridJson.optString("checkSum"));
        ridData.setExponent(ridJson.optString("exponent"));

        return ridData;
    }
    public static MerchantTerminalData mapMerchantParam(JSONObject cardSchemeList) {
        MerchantTerminalData merchantTerminalData = new MerchantTerminalData();
        Optional.ofNullable(cardSchemeList.optJSONArray("cardScheme"))
                .filter(cardSchemes -> !cardSchemes.isEmpty())
                .map(cardSchemes -> cardSchemes.optJSONObject(0))
                .ifPresent(firstObject -> {
                    String merchantId = firstObject.optString("merchantId", null);
                    String merchantCategoryCode = firstObject.optString("merchantCategoryCode", null);
                    merchantTerminalData.setMerchantId(merchantId);
                    merchantTerminalData.setMerchantCategoryCode(merchantCategoryCode);
                });
        return merchantTerminalData;
    }
    @NotNull
    public static Result getResult(List<String> appParamsList, List<String> merchParamsBa) throws JsonProcessingException {
        String encodedAppParams = org.apache.commons.codec.binary.Base64.encodeBase64String(String.join(",", appParamsList).getBytes());
        String decodedParams = new String(Base64.decodeBase64(encodedAppParams));
        log.info("app Params: {}", decodedParams);
        log.info("merch decode params: " + merchParamsBa);
        JSONObject jsonObject = XML.toJSONObject(decodedParams);
        log.info("JSON app decoded params: " + jsonObject);
        JSONObject madaAppData = jsonObject.getJSONObject("madaAppData");
        JSONObject madaTrmlData =jsonObject.getJSONObject("madaTrmlData");
        JSONObject cardSchemeList = madaAppData.getJSONObject("cardSchemeList");
        List<AidList> aidLists = mapAidLists(cardSchemeList);
        aidLists.forEach(aidList ->  log.info("JSON app decoded aidList:{} ", aidList));
        List<RidList> ridList =mapRidLists(cardSchemeList);
        log.info("JSON app decoded ridList:{} ", ridList);
        MerchantTerminalData terminalConfig = mapMerchantParam(cardSchemeList);
        terminalConfig.setTerminalId(madaTrmlData.optString("terminalId",null));
        terminalConfig.setTerminalCountryCode(madaTrmlData.optString("terminalCountryCode",null));
        terminalConfig.setTerminalCurrencyCode(madaTrmlData.optString("terminalCurrencyCode",null));
        ObjectMapper objectMapper = new ObjectMapper();
        String aidListsJson = objectMapper.writeValueAsString(aidLists);
        String ridJsonList=objectMapper.writeValueAsString(ridList);
        String terminalData= objectMapper.writeValueAsString(terminalConfig);
        log.info("JSON decoded aid params:{} ", aidListsJson);
        log.info("JSON decoded rid params:{} ", ridJsonList);
        log.info("JSON decoded terminalConfig params:{} ", terminalData);
        return new Result(aidLists, ridList, terminalConfig, objectMapper);
    }

    public record Result(List<AidList> aidLists, List<RidList> ridList, MerchantTerminalData terminalConfig, ObjectMapper objectMapper) {
    }




}
