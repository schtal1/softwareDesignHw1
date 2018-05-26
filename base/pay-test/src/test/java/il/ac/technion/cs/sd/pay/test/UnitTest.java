package il.ac.technion.cs.sd.pay.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.pay.app.MyPayBookInitializer;
import il.ac.technion.cs.sd.pay.app.MyPayBookReader;
import il.ac.technion.cs.sd.pay.app.PayBookInitializer;
import il.ac.technion.cs.sd.pay.app.PayBookReader;
import library.Library;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnitTest {

    private static PayBookReader setupAndGetReader(String fileName) throws FileNotFoundException {
        String fileContents =
                new Scanner(new File(ExampleTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
        Injector injector = Guice.createInjector(new AuxDatabaseModule());
        Library lib = injector.getInstance(Library.class);
        MyPayBookInitializer payBookInitializer = new MyPayBookInitializer(lib);
        payBookInitializer.setup(fileContents);
        return new MyPayBookReader(lib);
    }

    @Test
    public void testPaidTo() throws Exception {
        PayBookReader reader = setupAndGetReader("unit.xml");
        assertTrue(reader.paidTo("123", "ab"));
        assertTrue(reader.paidTo("123", "abc"));
        assertTrue(reader.paidTo("123", "ef"));

        assertFalse(reader.paidTo("456", "ab"));
        assertTrue(reader.paidTo("456", "abc"));
        assertTrue(reader.paidTo("456", "ef"));

        assertFalse(reader.paidTo("1234", "ab"));
        assertFalse(reader.paidTo("123", "abcd"));
    }

    @Test
    public void testGetPayment() throws Exception {
        PayBookReader reader = setupAndGetReader("unit.xml");
        assertEquals(OptionalDouble.of(24.0), reader.getPayment("123", "ab"));
        assertEquals(OptionalDouble.of(8.0), reader.getPayment("123", "abc"));
        assertEquals(OptionalDouble.of(1.0), reader.getPayment("123", "ef"));

        assertEquals(OptionalDouble.empty(), reader.getPayment("456", "ab"));
        assertEquals(OptionalDouble.of(3.0), reader.getPayment("456", "abc"));
        assertEquals(OptionalDouble.of(10.0), reader.getPayment("456", "ef"));

        assertEquals(OptionalDouble.empty(), reader.getPayment("1234", "ab"));
        assertEquals(OptionalDouble.empty(), reader.getPayment("123", "abcd"));
    }

    @Test
    public void testGetBiggestSpenders() throws Exception {
        PayBookReader reader = setupAndGetReader("biggestSpenders.xml");
        List<String> spenders = Arrays.asList("7", "6", "3", "4", "5", "12", "10", "8", "2", "9");
        assertEquals(spenders, reader.getBiggestSpenders());
    }

    @Test
    public void testGetRichestSellers() throws Exception {
        PayBookReader reader = setupAndGetReader("biggestSellers.xml");
        List<String> spenders = Arrays.asList("d", "f", "g", "a", "b", "e", "c");
        assertEquals(spenders, reader.getRichestSellers());
    }

    @Test
    public void testGetFavoriteSeller() throws Exception {
        PayBookReader reader = setupAndGetReader("favorite.xml");
        assertEquals(Optional.of("a"), reader.getFavoriteSeller("1"));
        assertEquals(Optional.of("a"), reader.getFavoriteSeller("2"));
        assertEquals(Optional.of("e"), reader.getFavoriteSeller("3"));

        assertEquals(Optional.empty(), reader.getFavoriteSeller("4"));
    }

    @Test
    public void testGetBiggestClient() throws Exception {
        PayBookReader reader = setupAndGetReader("favorite.xml");
        assertEquals(Optional.of("1"), reader.getBiggestClient("a"));
        assertEquals(Optional.of("2"), reader.getBiggestClient("b"));
        assertEquals(Optional.of("2"), reader.getBiggestClient("c"));
        assertEquals(Optional.of("1"), reader.getBiggestClient("d"));
        assertEquals(Optional.of("3"), reader.getBiggestClient("e"));

        assertEquals(Optional.empty(), reader.getBiggestClient("f"));
    }

    @Test
    public void testGetBiggestPaymentsToSellers() throws Exception {
        PayBookReader reader = setupAndGetReader("favorite.xml");
        Map<String, Integer> map = new HashMap<>();
        map.put("1", 78);
        map.put("2", 20);
        map.put("3", 100);
        assertEquals(map, reader.getBiggestPaymentsToSellers());
    }

    @Test
    public void testGetBiggestPaymentsFromClients() throws Exception {
        PayBookReader reader = setupAndGetReader("favorite.xml");
        Map<String, Integer> map = new HashMap<>();
        map.put("e", 100);
        map.put("a", 78);
        map.put("d", 50);
        map.put("b", 20);
        map.put("c", 10);
        assertEquals(map, reader.getBiggestPaymentsFromClients());
    }
}
