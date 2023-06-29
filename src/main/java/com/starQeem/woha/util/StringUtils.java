package com.starQeem.woha.util;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Date: 2023/6/29 10:29
 * @author: Qeem
 */
public class StringUtils {
    public static String delULikeId(String likedUserIds,Long userId){
        String[] idArray = likedUserIds.split(",");
        // 将字符串数组转换成 Stream<String> 对象
        Stream<String> idStream = Arrays.stream(idArray);
        // 过滤掉要删除的 ID
        Stream<String> filteredIdStream = idStream.filter(id -> !id.equals(String.valueOf(userId)));
        // 将过滤后的字符串数组转换成以逗号分隔的字符串
        return filteredIdStream.collect(Collectors.joining(","));
    }
}
