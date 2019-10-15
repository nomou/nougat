package freework.web.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注处理Http GET请求的方法.
 *
 * @author vacoor
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Get {

    /**
     * 指定处理的Url-Pattern.
     */
    String[] value() default {};

}
