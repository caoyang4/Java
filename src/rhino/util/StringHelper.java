
package src.rhino.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * Created by zhanjun on 2017/4/25.
 */
public class StringHelper {

    public static String generateKey(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        if(clazz.getClassLoader() != null){
            stringBuilder.append(clazz.getClassLoader().hashCode()).append(".");
        }
        stringBuilder.append(clazz.getCanonicalName());
        return stringBuilder.toString();
    }

    public static String generateKey(Class<?> clazz, Method method) {
        StringBuilder stringBuilder = new StringBuilder();
        if(clazz.getClassLoader() != null){
            stringBuilder.append(clazz.getClassLoader().hashCode()).append(".");
        }
        stringBuilder.append(clazz.getCanonicalName());
        stringBuilder.append(".");
        stringBuilder.append(method.getName());
        stringBuilder.append(genMethodParameter(method));
        return stringBuilder.toString();
    }

    public static String genMethodParameter(Method method) {
        return genMethodParameter(method.getParameterTypes());
    }

    public static String genMethodParameter(Class<?>[] parameterTypes) {
        StringBuilder stringBuilder = new StringBuilder("(");
        int len = parameterTypes.length;
        for (int k = 0; k < len; k++) {
            Class param = parameterTypes[k];
            stringBuilder.append(param.getSimpleName());
            if (k != len - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }


    public static void writeTo(OutputStream os, String charset, String content) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes(charset));
        copy(bais, os);
    }

    public static String readFrom(InputStream is, String charsetName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(16 * 1024);
        copy(is, baos);
        return baos.toString(charsetName);
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] content = new byte[4096];

        try {
            while (true) {
                int size = is.read(content);

                if (size == -1) {
                    break;
                } else {
                    os.write(content, 0, size);
                }
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            }finally {
                //Do nothing
            }
        }
    }
}
