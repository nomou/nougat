package freework.function;

/**
 * Check the condition can be established.
 *
 * @author vacoor
 * @since 1.0
 */
public interface Condition<T> {
    /**
     * The condition always return true.
     */
    @SuppressWarnings("unchecked")
    Condition TRUE = new Condition() {

        /**
         * 该方法总是返回true.
         *
         * @param value 校验值
         * @return true
         */
        @Override
        public boolean value(final Object value) {
            return true;
        }
    };

    /**
     * Checks the input meets the condition.
     *
     * @param value the input value
     * @return Returns true if the condition is true, otherwise returns false
     */
    boolean value(final T value);

}
