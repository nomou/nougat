package freework.proc.handle;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public abstract class HandleProvider {

    static HandleProvider provider() {
        final ServiceLoader<HandleProvider> loader = ServiceLoader.load(HandleProvider.class);
        for (final HandleProvider provider : loader) {
            if (provider.isSupported()) {
                return provider;
            }
        }
        throw new ServiceConfigurationError("No available provider found for " + Handle.class.getName());
    }

    protected abstract boolean isSupported();

    public abstract Handle current();

    public abstract Handle of(final int pid);

    public abstract Handle of(final Process process);

}