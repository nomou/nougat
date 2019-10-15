package freework.web.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注前置处理方法.
 *
 * <p>前置处理方法在逻辑处理方法前执行, 可以进行预处理, 比如鉴权等</p>
 *
 * @author vacoor
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Before {

    /**
     * 执行优先级.
     *
     * <p>执行顺序按照自然顺序.</p>
     */
    int priority() default 0;

    /**
     * 指定在哪些方法(GET/POST/PUT/DELETE/HEAD/OPTIONS)生效, 默认全部生效.
     */
    String[] methods() default {};

}
