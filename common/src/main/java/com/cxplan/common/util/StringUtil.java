/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created on 2017/5/17.
 *
 * @author kenny
 */
public class StringUtil {

    public static final ObjectMapper JSON_MAPPER_NOTNULL = new ObjectMapper();
    static {
        JSON_MAPPER_NOTNULL.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JSON_MAPPER_NOTNULL.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        JSON_MAPPER_NOTNULL.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JSON_MAPPER_NOTNULL.setLocale(Locale.getDefault());
    }

    /**
     * 从Json字符串转换为object，该方法不支持泛型.
     * 如果有泛型支持需要，请使用api{@link #json2Object(String, TypeReference)}
     *
     * @param content json字符串
     * @param type 对象类型
     * @throws IllegalArgumentException 如果转换失败会抛出该异常。
     */
    public static <T extends Object> T json2Object(String content, Class<T> type) {
        try {
            return JSON_MAPPER_NOTNULL.readValue(content, type);
        } catch (IOException e) {
            throw new IllegalArgumentException("Transforming（json2Object） failed: " + e.getMessage(), e);
        }
    }
    /**
     * 从Json字符串转换为object。
     * 如果有对象类型支持泛型，可以使用该方法。
     * @param content json字符串
     * @param type 对象类型定义
     * @throws IllegalArgumentException 如果转换失败会抛出该异常。
     */
    public static <T extends Object> T json2Object(String content, TypeReference type) {
        try {
            return JSON_MAPPER_NOTNULL.readValue(content, type);
        } catch (IOException e) {
            throw new IllegalArgumentException("Transforming（json2Object） failed: " + e.getMessage(), e);
        }
    }

    /**
     * 将业务对象转换为json字符串
     * @param obj 需要被转换成json string的对象。
     * @return json字符串
     */
    public static String toJSONString(Object obj) {
        try{
            return JSON_MAPPER_NOTNULL.writeValueAsString(obj);
        }catch(Exception e){
            throw new IllegalArgumentException("Transforming（toJSONString） failed: " + e.getMessage(), e);
        }
    }
    /**
     * 检查指定内容是否无内容：null 或者空的字符串。
     * 注意空格也是算内容。
     */
    public static boolean isEmpty(Object str) {
        return (str == null || "".equals(str));
    }

    public static boolean isNotEmpty(Object str) {
        return !isEmpty(str);
    }

    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a String is not empty (""), not null and not whitespace only.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is
     *  not empty and not null and not whitespace
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
