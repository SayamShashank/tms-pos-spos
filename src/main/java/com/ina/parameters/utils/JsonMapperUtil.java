package com.ina.parameters.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ina.parameters.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.XML;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ina.constants.AppConstants.CARD_SCHEME;

@Slf4j
public class JsonMapperUtil {


    public static List<AidData> mapAidLists(JSONObject cardSchemeList) {
        return Optional.ofNullable(cardSchemeList.optJSONArray(CARD_SCHEME))
                .map(cardSchemes -> IntStream.range(0, cardSchemes.length())
                        .mapToObj(cardSchemes::getJSONObject)
                        .map(JsonMapperUtil::mapAidList)
                        .filter(aid -> aid != null && !aid.isEmpty())
                        .flatMap(List::stream)
                        .toList())
                .orElseGet(ArrayList::new);
    }


    private static List<AidData> mapAidList(JSONObject cardScheme) {
        List<AidData> aidList=new ArrayList<>();
        String emvTerminalType = cardScheme.optString("emvTerminalType",null);
        String cardSchemeAcId=cardScheme.optString("cardSchemeAcquirerId",null);
        Optional.ofNullable(cardScheme.optJSONObject("aidList"))
                .map(aidListObj -> aidListObj.optJSONArray("aidForm"))
                .ifPresent(aidFormArray -> {
                    List<AidData> aidDataList = IntStream.range(0, aidFormArray.length())
                            .mapToObj(aidFormArray::optJSONObject)
                            .filter(Objects::nonNull)
                            .map(aidFormObject -> mapAIDData(aidFormObject, emvTerminalType,cardSchemeAcId))
                            .toList();
                    aidList.addAll(aidDataList);
                });
        return aidList;
    }



    private static AidData mapAIDData(JSONObject aidJson, String emvTerminalType,String cardSchemeAcId) {
        AidData aidData = new AidData();
        if (aidJson != null) {
            JSONObject iccReaderForm = Optional.ofNullable(aidJson.optJSONObject("iccReaderList"))
                    .map(obj -> obj.optJSONObject("iccReaderForm"))
                    .orElse(new JSONObject());
            JSONObject contactLessReaderList = aidJson.optJSONObject("contactLessReaderList");
            aidData.setTerminalCapability(iccReaderForm.optString("terminalCapability", null));
            aidData.setTerminalCapability(iccReaderForm.optString("addTerminalCapability", null));

            if (contactLessReaderList != null) {
                aidData.setCvmLimit(padTo12DigitsIfNeeded(contactLessReaderList.optString("termCVMRequiredLimit", null)));
                aidData.setFloorLimit(padTo12DigitsIfNeeded(contactLessReaderList.optString("terminalContactlessFloorLimit", null)));
                aidData.setTransactionLimit(padTo12DigitsIfNeeded(contactLessReaderList.optString("termContactlessTxnLimit", null)));

            }
            aidData.setAid(aidJson.optString("aid",null));
            aidData.setEmvTerminalType(emvTerminalType);
            aidData.setCardSchemeAcquirerId(cardSchemeAcId);
            aidData.setApplicationName(aidJson.optString("aidLabel",null));
            aidData.setTacDenial(aidJson.optString("denialActionCode", null));
            aidData.setTacOnline(aidJson.optString("onlineActionCode", null));
            aidData.setTacDefault(aidJson.optString("defaultActionCode", null));
        }
        return aidData;
    }
    private static String padTo12DigitsIfNeeded(String value) {
        if (value == null || value.isEmpty()) return null;
        if (value.length() < 12) {
            return String.format("%012d", Long.parseLong(value));
        }
        return value;
    }

    public static List<RidData> mapRidLists(JSONObject cardSchemeList) {
        return Optional.ofNullable(cardSchemeList.optJSONArray(CARD_SCHEME))
                .map(cardSchemes -> IntStream.range(0, cardSchemes.length())
                        .mapToObj(cardSchemes::getJSONObject)
                        .map(JsonMapperUtil::mapRidList) // This returns List<RidData>
                        .filter(ridDataList -> ridDataList != null && !ridDataList.isEmpty())
                        .flatMap(List::stream) // Flatten all List<RidData> into one
                        .toList())
                .orElseGet(ArrayList::new);
    }



    public static List<RidData> mapRidList(JSONObject cardScheme) {
        List<RidData> ridDataList = Optional.ofNullable(cardScheme.optJSONObject("ridList"))
                .map(ridListJson -> ridListJson.optJSONArray("ridForm"))
                .map(ridForm -> IntStream.range(0, ridForm.length())
                        .mapToObj(ridForm::optJSONObject)
                        .map(JsonMapperUtil::mapRidData)
                        .filter(Objects::nonNull)
                        .toList())
                .orElse(new ArrayList<>());

        return new ArrayList<>(ridDataList);
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

        Optional.ofNullable(cardSchemeList.optJSONArray(CARD_SCHEME))
                .filter(array -> array.length() > 0)
                .map(array -> IntStream.range(0, array.length())
                        .mapToObj(array::optJSONObject)
                        .filter(Objects::nonNull)
                        .findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .ifPresent(obj -> {
                    merchantTerminalData.setMerchantId(obj.optString("merchantId", null));
                    merchantTerminalData.setMerchantCategoryCode(obj.optString("merchantCategoryCode", null));
                });

        return merchantTerminalData;
    }


    @NotNull
    public static Result getResult(List<String> appParamsList,List<String> merchParams) throws JsonProcessingException {
        JSONObject merchParamJsonObject =getJsonObject(merchParams);
        JSONObject appParamJsonObject = getJsonObject(appParamsList);
        log.info("JSON app decoded params:{} ",  appParamJsonObject);
        log.info("JSON merch decoded params:{} ",merchParamJsonObject);
        JSONObject madaAppData = appParamJsonObject.getJSONObject("madaAppData");
        JSONObject madaTrmlData =appParamJsonObject.getJSONObject("madaTrmlData");
        JSONObject cardSchemeList = madaAppData.getJSONObject("cardSchemeList");
        List<AidData> aidLists = mapAidLists(cardSchemeList);
        List<RidData> ridList =mapRidLists(cardSchemeList);
        log.info("JSON app decoded ridList:{} ", ridList);
        MerchantTerminalData terminalConfig = mapMerchantParam(cardSchemeList);
        terminalConfig.setTerminalId(madaTrmlData.optString("terminalId",null));
        terminalConfig.setTerminalCountryCode(madaTrmlData.optString("terminalCountryCode",null));
        terminalConfig.setTerminalCurrencyCode(madaTrmlData.optString("terminalCurrencyCode",null));
        JSONObject merchantData =merchParamJsonObject.getJSONObject("merchant");
        MerchantDetails merchantDetails =getMerchantDetails(merchantData);
        ObjectMapper objectMapper = new ObjectMapper();
        String aidListsJson = objectMapper.writeValueAsString(aidLists);
        String ridJsonList=objectMapper.writeValueAsString(ridList);
        String terminalData= objectMapper.writeValueAsString(terminalConfig);
        String merchantDetailsParams=objectMapper.writeValueAsString(merchantDetails);
        log.info("JSON decoded aid params:{} ", aidListsJson);
        log.info("JSON decoded rid params:{} ", ridJsonList);
        log.info("JSON decoded terminalConfig params:{} ", terminalData);
        log.info("JSON decoded merchantDetails params:{} ", merchantDetailsParams);
        return new Result(aidLists, ridList, terminalConfig,merchantDetails);
    }
    private static MerchantDetails getMerchantDetails(JSONObject merchantData) {

        MerchantDetails merchantDetails = new MerchantDetails();
        merchantDetails.setMerchantNameEN(merchantData.optString("retailerNameEng",null));
        merchantDetails.setMerchantNameAR(merchantData.optString("retailerNameArab",null));
        merchantDetails.setMerchantNameAddress1AR(merchantData.optString("retailerAddress1Arab",null));
        merchantDetails.setMerchantNameAddress1EN(merchantData.optString("retailerAddress1Eng",null));
        merchantDetails.setMerchantNameAddress2EN(merchantData.optString("retailerAddress2Eng",null));
        merchantDetails.setMerchantNameAddress2AR(merchantData.optString("retailerAddress2Arab",null));
        merchantDetails.setMerchantNameCityAR(merchantData.optString("retailerCityArab",null));
        merchantDetails.setMerchantNameCityEN(merchantData.optString("retailerCityEng",null));
        merchantDetails.setMerchantPostalCode(merchantData.optString("retailerPostalCode",null));
        return merchantDetails;
    }

    @NotNull
    private static JSONObject getJsonObject(List<String> paramsList) {
        String joinedParams = String.join(",", paramsList);
        String encodedParams = Base64.encodeBase64String(joinedParams.getBytes(StandardCharsets.UTF_8));
        String decodedParams = new String(Base64.decodeBase64(encodedParams), StandardCharsets.UTF_8);
        log.info("decoded Params: {}", decodedParams);

        return XML.toJSONObject(decodedParams);

    }


    public record Result(List<AidData> aidLists, List<RidData> ridList, MerchantTerminalData terminalConfig,MerchantDetails merchantDetails) {
    }




}
