package il.ac.technion.cs.sd.pay.test;

import com.google.inject.AbstractModule;
import il.ac.technion.cs.sd.pay.app.MyPayBookInitializer;
import il.ac.technion.cs.sd.pay.app.MyPayBookReader;
import il.ac.technion.cs.sd.pay.app.PayBookInitializer;
import il.ac.technion.cs.sd.pay.app.PayBookReader;



// This module is in the testing project, so that it could easily bind all dependencies from all levels.
class PayBookModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PayBookInitializer.class).to(MyPayBookInitializer.class);
        bind(PayBookReader.class).to(MyPayBookReader.class);
    }
}
