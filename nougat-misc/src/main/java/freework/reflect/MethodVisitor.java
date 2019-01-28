package freework.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class MethodVisitor {
    private final boolean includingHierarchy;

    protected MethodVisitor() {
        this(false);
    }

    protected MethodVisitor(boolean includingHierarchy) {
        this.includingHierarchy = includingHierarchy;
    }

    public void visit(Class<?> clazz) {
        if (includingHierarchy) {
            final Class<?> superclass = clazz.getSuperclass();
            if (null != superclass) {
                visit(superclass);
            }

            final Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                visit(anInterface);
            }
        }
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            visitMethod(method);
        }
    }

    protected void visitMethod(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            visitStaticMethod(method);
        } else {
            visitInstanceMethod(method);
        }
    }

    protected void visitStaticMethod(Method method) {
    }

    protected void visitInstanceMethod(Method method) {
    }
}