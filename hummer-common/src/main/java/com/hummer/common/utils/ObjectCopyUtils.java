package com.hummer.common.utils;

import com.hummer.common.SysConstant;
import com.hummer.common.exceptions.SysException;
import net.sf.cglib.beans.BeanCopier;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * use cglib copy object.
 * <p>
 *     notice: ony copy of first one level pojo object
 * </p>
 *
 * @Author: lee
 * @since:1.0.0
 * @Date: 2019/7/2 16:41
 **/
public class ObjectCopyUtils {
    private ObjectCopyUtils() {

    }

    /**
     * copy object
     *
     * @param source source object
     * @param target target object
     * @return T
     * @author liguo
     * @date 2019/7/2 16:50
     * @since 1.0.0
     **/
    public static <T> T copy(Object source, Class<T> target) {
        if (source == null) {
            return null;
        }

        T target2 = newInstance(target);
        BeanCopier copier = BeanCopier.create(source.getClass(), target, false);
        copier.copy(source, target2, null);
        return target2;
    }

    /**
     * copy object
     *
     * @param source source object
     * @param target target object
     * @return T
     * @author chen wei
     * @date 2020/4/24 16:50
     */
    public static void copy(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }

        BeanCopier copier = BeanCopier.create(source.getClass(), target.getClass(), false);
        copier.copy(source, target, null);
    }

    /**
     * copy list
     *
     * @param sourceData source
     * @param clazz      target
     * @return java.util.List<T>
     * @author liguo
     * @date 2019/7/2 16:54
     * @since 1.0.0
     **/
    public static <T> List<T> copyByList(List<?> sourceData, Class<T> clazz) {
        if (CollectionUtils.isEmpty(sourceData)) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>(sourceData.size());
        for (Object data : sourceData) {
            result.add(copy(data, clazz));
        }
        return result;
    }

    /**
     * copy list
     *
     * @param sourceData source
     * @param clazz      target
     * @return java.util.List<T>
     * @author liguo
     * @date 2019/7/2 16:54
     * @since 1.0.0
     **/
    public static <T> Collection<T> copyByList(Collection<?> sourceData, Class<T> clazz) {
        if (CollectionUtils.isEmpty(sourceData)) {
            return Collections.emptyList();
        }
        Collection<T> result = new ArrayList<>(sourceData.size());
        for (Object data : sourceData) {
            result.add(copy(data, clazz));
        }
        return result;
    }

    private static <T> T newInstance(Class<T> clazz) {
        Assert.notNull(clazz, "The class must not be null");
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Throwable throwable) {
            throw new SysException(SysConstant.SYS_ERROR_CODE,
                String.format("instance %s failed", clazz.getSimpleName()), throwable);
        }
    }
}
