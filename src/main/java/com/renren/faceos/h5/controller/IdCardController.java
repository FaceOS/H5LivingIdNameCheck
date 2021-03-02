package com.renren.faceos.h5.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.renren.faceos.h5.domain.IdName;
import com.renren.faceos.h5.utils.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@RestController
@RequestMapping("/idCard")
public class IdCardController {
    protected final Logger logger = LoggerFactory.getLogger(IdCardController.class);

    @PostMapping("/uploadMp4")
    @ResponseBody
    public String uploadMp4(@RequestBody MultipartFile file) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 500);
        jsonObject.put("message", "活体检测失败");
        Base64.Encoder encoder = Base64.getEncoder();
        try {
            String base64Code = encoder.encodeToString(file.getBytes());
            String url = "http://49.232.119.50:8080/api/silent";
            JSONObject base64Json = new JSONObject();
            base64Json.put("videoBase64", base64Code);
            String base64JsonResult = HttpClientUtils.httpPost(url, JSON.toJSONString(base64Json));
            JSONObject base64JsonResultJsonObject = JSONObject.parseObject(base64JsonResult);
            int code = base64JsonResultJsonObject.getIntValue("code");
            if (code == 0) {
                JSONObject data = base64JsonResultJsonObject.getJSONObject("data");
                Float score = data.getFloat("score");
                if (score > 0.9) {
                    jsonObject.put("code", 0);
                    jsonObject.put("message", "活体检测成功");
                }
            }
            return jsonObject.toJSONString();

        } catch (Exception e) {
            e.printStackTrace();
            return jsonObject.toJSONString();
        }
    }


    @PostMapping("/cxzx")
    @ResponseBody
    public String cxzx(@RequestBody String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        String name = jsonObject.getString("Name");
        String checkType = jsonObject.getString("CheckType");
        String faceBase64 = jsonObject.getString("FaceBase64");
        String cardNo = jsonObject.getString("CardNo");

        IdName idName = new IdName();
        idName.setLoginName("faceos");
        idName.setPwd("faceos");

        IdName.ParamBean paramBean = new IdName.ParamBean();
        paramBean.setName(name);
        paramBean.setIdCard(cardNo);

        String url = "";
        if (checkType.equals("1") || checkType.equals("3")) {
            url = "https://49.233.242.197:8313/CreditFunc/v2.1/IDNameCheck";
            idName.setServiceName("IDNameCheck");
        } else {
            url = "https://49.233.242.197:8313/CreditFunc/v2.1/IdNamePhotoCheck";
            idName.setServiceName("IdNamePhotoCheck");
            paramBean.setImage(faceBase64);
        }
        idName.setParam(paramBean);

        try {
            return HttpClientUtils.httpPost(url, JSON.toJSONString(idName));
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}
