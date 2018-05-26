package il.ac.technion.cs.sd.pay.test;

import com.google.inject.AbstractModule;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseFactory;

public class AuxDatabaseModule  extends AbstractModule {

    public AuxDatabaseModule() {
    }

    protected void configure() {
        this.bind(SecureDatabaseFactory.class).toInstance((unused) -> {
            return new AuxSecureDB();
        });
    }
}
