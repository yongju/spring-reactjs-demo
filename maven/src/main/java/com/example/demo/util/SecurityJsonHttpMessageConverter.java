package com.example.demo.util;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.TypeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

@Slf4j
public class SecurityJsonHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private static final Map<String, JsonEncoding> ENCODINGS = CollectionUtils.newHashMap(JsonEncoding.values().length);

    public SecurityJsonHttpMessageConverter() {
        this(Jackson2ObjectMapperBuilder.json().build());
    }

    public SecurityJsonHttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        log.info("canRead() {} {} {}", type, contextClass, mediaType);
        return super.canRead(type, contextClass, mediaType);
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        log.info("canRead() {}", clazz);
        return super.canRead(clazz, mediaType);
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        log.info("canWrite() {} {} {}", type, clazz, mediaType);
        return super.canWrite(type, clazz, mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        log.info("canWrite() {}", clazz);
        return super.canWrite(clazz, mediaType);
    }

    @Override
    public Object read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

        JavaType javaType = this.getJavaType(type, contextClass);
        Annotation annotation = javaType.getRawClass().getAnnotation(Security.class);
        log.info("read() {} {} {}", type, contextClass, annotation);
        if (annotation == null) {
            return super.read(type, contextClass, inputMessage);
        } else {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            log.info("Values of " + annotationType.getName());
            Object value = null;

            for (Method method : annotationType.getDeclaredMethods()) {
                try {
                    value = method.invoke(annotation, (Object[])null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                log.info(" " + method.getName() + ": " + value);

            }
            return readSecurityJavaType(javaType, (String) value, inputMessage);
        }
    }

    private Object readSecurityJavaType(JavaType javaType, String value, HttpInputMessage inputMessage) throws IOException {
        MediaType contentType = inputMessage.getHeaders().getContentType();
        Charset charset = this.getCharset(contentType);
        ObjectMapper objectMapper = new ObjectMapper();
        Assert.state(objectMapper != null, "No ObjectMapper for " + javaType);
        boolean isUnicode = ENCODINGS.containsKey(charset.name()) || "UTF-16".equals(charset.name()) || "UTF-32".equals(charset.name());

        try {
            /* decrypt */
            InputStream is = inputMessage.getBody();
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            byte[] src = Base64.getDecoder().decode(buffer);
            Object ret = objectMapper.readValue(src, javaType);

            Arrays.fill(buffer, (byte) 0x00);
            Arrays.fill(src, (byte) 0x00);

            return ret;
        } catch (InvalidDefinitionException var10) {
            throw new HttpMessageConversionException("Type definition error: " + var10.getType(), var10);
        } catch (JsonProcessingException var11) {
            throw new HttpMessageNotReadableException("JSON parse error: " + var11.getOriginalMessage(), var11, inputMessage);
        }
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

        JavaType javaType = this.getJavaType(type, null);
        Annotation annotation = javaType.getRawClass().getAnnotation(Security.class);
        log.info("writeInternal() {} {} {} {}", object, type, outputMessage, annotation);
        if (annotation == null) {
            super.writeInternal(object, type, outputMessage);
        } else {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            log.info("Values of " + annotationType.getName());
            Object value = null;

            for (Method method : annotationType.getDeclaredMethods()) {
                try {
                    value = method.invoke(annotation, (Object[])null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                log.info(" " + method.getName() + ": " + value);

            }

            writeSecurityJavaType(object, type, outputMessage);
        }

    }

    private void writeSecurityJavaType(Object object, Type type, HttpOutputMessage outputMessage) {
        MediaType contentType = outputMessage.getHeaders().getContentType();
        JsonEncoding encoding = this.getJsonEncoding(contentType);
        Class<?> clazz = object instanceof MappingJacksonValue ? ((MappingJacksonValue)object).getValue().getClass() : object.getClass();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            OutputStream outputStream = StreamUtils.nonClosing(outputMessage.getBody());

            /* base64 */
            outputStream.write(Base64.getEncoder().encode(objectMapper.writeValueAsBytes(object)));
            outputStream.flush();
        } catch (Exception e) {

        }
    }



    static {
        JsonEncoding[] var0 = JsonEncoding.values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            JsonEncoding encoding = var0[var2];
            ENCODINGS.put(encoding.getJavaName(), encoding);
        }

        ENCODINGS.put("US-ASCII", JsonEncoding.UTF8);
    }

}
