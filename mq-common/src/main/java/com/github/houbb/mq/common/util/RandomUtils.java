package com.github.houbb.mq.common.util;

import com.github.houbb.heaven.annotation.CommonEager;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.heaven.util.util.RandomUtil;

import java.util.List;
import java.util.Objects;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
@CommonEager
public class RandomUtils {

    /**
     * 随机
     * @param list 列表
     * @param key 分片键
     * @param <T> 泛型
     * @return 结果
     * @since 0.0.3
     */
    public static <T> T random(final List<T> list, String key) {
        if(CollectionUtil.isEmpty(list)) {
            return null;
        }

        if(StringUtil.isEmpty(key)) {
            return RandomUtil.random(list);
        }

        // 获取 code
        int hashCode = Objects.hash(key);
        int index = hashCode % list.size();
        return list.get(index);
    }
}
