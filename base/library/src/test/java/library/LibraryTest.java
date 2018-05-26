package library;

import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.*;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {
    private Library lib;

    @BeforeEach
    public void init() {
        Injector injector = Guice.createInjector(new SimpleDataBaseModule());
        lib = injector.getInstance(Library.class);
    }

    @Test
    public void test_add_get() throws Exception {
        lib.add("db", "foo", "1");
        lib.add("db2", "bar", "12");
        lib.add("db", "baz", "123");
        assertEquals("1", lib.get("db", "foo"));
        assertEquals("12", lib.get("db2", "bar"));
        assertEquals("123", lib.get("db", "baz"));
        assertNull(lib.get("db2", "baz"));
        assertNull(lib.get("db", "bar"));
        assertNull(lib.get("db2", "batman"));
        assertNull(lib.get("db", "batman"));
        String verylong = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        assertThrows(DataFormatException.class, () -> lib.add("db", verylong, "foo"));
        assertThrows(DataFormatException.class, () -> lib.add("db", "test", verylong));
    }

    @Test
    public void test_add_get_all() throws Exception {
        List<String> l1 = Arrays.asList("123", "456", "789", "abc", "def");
        List<String> l2 = Arrays.asList("John", "Paul", "Ringo", "Georges");
        lib.add_all("db1", "l1", l1);
        lib.add_all("db2", "l2", l2);
        assertEquals(l1, lib.get_all("db1", "l1"));
        assertEquals(l2, lib.get_all("db2", "l2"));
        assertEquals(Collections.emptyList(), lib.get_all("db1", "l2"));
        assertEquals(Collections.emptyList(), lib.get_all("db2", "l1"));
        assertEquals(Collections.emptyList(), lib.get_all("db3", "l1"));
        assertEquals(Collections.emptyList(), lib.get_all("db1", "l3"));
        assertNull(lib.get("db1", "l1"));
        assertNull(lib.get("db2", "l2"));
    }
}