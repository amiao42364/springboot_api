package cn.miaomiao.api.controller;

import cn.miaomiao.api.constant.ResponseCode;
import cn.miaomiao.api.model.BaseResponse;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用来获取一些json数据
 *
 * @author miaomiao
 * @date 2019/5/22 9:43
 */
@RestController
@RequestMapping("/json")
@Slf4j
public class JsonController {
    private static final Map<String, String> arkTypes = new HashMap<>(4);

    /**
     * json数据直接放到内存
     */
    private Map<String, JSONObject> jsonMap = new HashMap<>(4);

    static {
        arkTypes.put("items", "/json/ArkNightsItemData.json");
        arkTypes.put("characters", "/json/ArkNightsCharacterData.json");
        arkTypes.put("level", "/json/ArkNightsLevelData.json");
    }

    /**
     * 获取明日方舟人物数据
     *
     * @return characters
     */
    @RequestMapping("/ark/characters")
    public BaseResponse getArkCharacters() {


        return BaseResponse.ok();
    }

    /**
     * 获取明日方舟物品数据
     *
     * @return items
     */
    @RequestMapping("/ark")
    public BaseResponse getArkItems(@RequestParam String type) {
        if (jsonMap.get(type) != null) {
            return BaseResponse.ok(jsonMap.get(type));
        }

        String path = arkTypes.get(type);
        if(path == null){
            return BaseResponse.error(ResponseCode.FILE_NOT_FIND_ERROR);
        }

        ClassPathResource resource = new ClassPathResource(path);
        File file;
        JSONObject obj = null;
        try {
            file = resource.getFile();
            String fileStr = FileUtils.readFileToString(file, "utf-8");
            obj = JSONObject.parseObject(fileStr);
        } catch (IOException e) {
            log.error("[json数据异常][type]" + type + "[exception]" + e.getMessage());
        }
        jsonMap.put(type, obj);
        return BaseResponse.ok(obj);
    }
}
