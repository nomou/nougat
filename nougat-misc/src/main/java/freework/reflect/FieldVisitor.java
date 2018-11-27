package freework.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 字段访问器
 *
 * @author vacoor
 */
public abstract class FieldVisitor {
    private final boolean includingHierarchy;   // 是否遍历超类字段

    /**
     * 创建一个不遍历超类字段的 Visitor
     */
    protected FieldVisitor() {
        this(false);
    }

    /**
     * 创建一个字段 Visitor
     * @param includingHierarchy 是否遍历超类字段
     */
    protected FieldVisitor(boolean includingHierarchy) {
        this.includingHierarchy = includingHierarchy;
    }

    /**
     * 遍历给定类的字段
     *
     * @param clazz 要遍历的类对象
     */
    public void visit(Class<?> clazz) {
        if (includingHierarchy) {
            // 优先遍历超类
            final Class<?> superclass = clazz.getSuperclass();
            if (null != superclass) {
                visit(superclass);
            }

            // 其次遍历接口
            final Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                visit(anInterface);
            }
        }
        // 遍历当前类
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            visitField(field);
        }
    }

    /**
     * 处理给定字段
     *
     * @param field 遍历的字段
     */
    protected void visitField(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            visitStaticField(field);
        } else {
            visitInstanceField(field);
        }
    }

    /**
     * 处理静态字段
     *
     * @param field 静态字段
     */
    protected void visitStaticField(Field field) {
    }

    /**
     * 处理实例字段
     *
     * @param field 实例字段
     */
    protected void visitInstanceField(Field field) {
    }
}