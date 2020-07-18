package org.stormrealms.stormmenus.util;

import java.lang.reflect.Field;

public class ReflectUtil
{
    public static Object getPrivateField(String fieldName, @SuppressWarnings("rawtypes") Class classs, Object object)
    {
        Field field;
        Object o = null;
        try
        {
            field = classs.getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return o;
    }
}
