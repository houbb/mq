package com.github.houbb.mq.common.util;

import com.github.houbb.heaven.annotation.CommonEager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
@CommonEager
public final class RegexUtils {

    private RegexUtils(){}

    /**
     * 是否匹配
     * @param pattern 正则
     * @param text 文本
     * @return 结果
     */
    public static boolean match(final Pattern pattern,
                                final String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    /**
     * 是否匹配
     * @param regex 正则
     * @param text 文本
     * @return 结果
     */
    public static boolean match(final String regex,
                                final String text) {
        Pattern pattern = Pattern.compile(regex);
        return match(pattern, text);
    }

}
