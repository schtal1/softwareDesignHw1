package library;

import com.google.inject.AbstractModule;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseFactory;

public class SimpleDataBaseModule  extends AbstractModule {

    public SimpleDataBaseModule() {
    }

    protected void configure() {
        this.bind(SecureDatabaseFactory.class).toInstance((unused) -> {
            return new SimpleSecureDB();
        });
    }
}
