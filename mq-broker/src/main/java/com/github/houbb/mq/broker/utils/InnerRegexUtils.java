package com.github.houbb.mq.broker.utils;

import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.mq.common.util.RegexUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 正则工具
 *
 * @author binbin.hou
 * @since 0.0.9
 */
public final class InnerRegexUtils {

    public static boolean hasMatch(List<String> tagNameList,
                             String tagRegex) {
        if(CollectionUtil.isEmpty(tagNameList)) {
            return false;
        }

        Pattern pattern = Pattern.compile(tagRegex);

        for(String tagName : tagNameList) {
            if(RegexUtils.match(pattern, tagName)) {
                return true;
            }
        }

        return false;
    }

}
