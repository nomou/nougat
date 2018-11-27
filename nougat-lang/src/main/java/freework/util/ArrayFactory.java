package freework.util;

/**
 * 数组工厂接口.
 *
 * @author vacoor
 */
public interface ArrayFactory<E> {

    /**
     * 创建一个给定长度的数组.
     *
     * @param len 数组长度
     * @return 数组
     */
    E[] create(final int len);

}
