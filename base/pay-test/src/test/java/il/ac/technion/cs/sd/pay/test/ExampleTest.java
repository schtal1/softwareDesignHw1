package il.ac.technion.cs.sd.pay.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.pay.app.PayBookInitializer;
import il.ac.technion.cs.sd.pay.app.PayBookReader;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Scanner;
import static org.junit.Assert.assertEquals;

public class ExampleTest {

    @Rule public Timeout globalTimeout = Timeout.seconds(30);

    private static PayBookReader setupAndGetReader(String fileName) throws FileNotFoundException {
        String fileContents =
                new Scanner(new File(ExampleTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
        Injector injector = Guice.createInjector(new PayBookModule(), new SecureDatabaseModule());
        injector.getInstance(PayBookInitializer.class).setup(fileContents);
        return injector.getInstance(PayBookReader.class);
    }

    @Test
    public void testSimple() throws Exception {
        PayBookReader reader = setupAndGetReader("small.xml");
        assertEquals(Arrays.asList("Foobar", "Boobar", "Moobar"), reader.getRichestSellers());
        assertEquals(OptionalDouble.of(10.0), reader.getPayment("123", "Foobar"));
        assertEquals(Optional.empty(), reader.getFavoriteSeller("124"));
    }
}
