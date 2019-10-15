package freework.web.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注后置处理方法.
 *
 * <p>后置处理方法在逻辑处理方法后执行, 可以进行后置处理, 比如将逻辑方法的返回值转换为JSON返回等</p>
 *
 * @author vacoor
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface After {

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
