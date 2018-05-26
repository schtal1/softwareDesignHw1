package library;

import com.google.inject.Inject;
import il.ac.technion.cs.sd.pay.ext.SecureDatabase;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseFactory;

import java.util.*;
import java.util.zip.DataFormatException;

public class Library {
    private SecureDatabaseFactory factory;
    private Map<String, SecureDatabase> dbMap;

    @Inject
    Library(SecureDatabaseFactory factory) {
        this.factory = factory;
        this.dbMap = new HashMap<>();
    }

    private SecureDatabase openDb(String dbName) {
        if (!dbMap.containsKey(dbName)) {
            dbMap.put(dbName, factory.open(dbName));
        }
        return dbMap.get(dbName);
    }

    public void add(String dbName, String key, String value) throws DataFormatException {
        byte[] bKey = key.getBytes();
        byte[] bValue = value.getBytes();
        openDb(dbName).addEntry(bKey, bValue);
    }

    public void add_all(String dbName, String key, Collection<String> values) throws DataFormatException {
        int index = 1;
        for (String value: values) {
            byte[] bKey = (key + "_" + String.valueOf(index)).getBytes();
            byte[] bValue = value.getBytes();
            openDb(dbName).addEntry(bKey, bValue);
            index++;
        }
    }

    public String get(String dbName, String key) {
        byte[] bKey = key.getBytes();
        try {
            byte[] bValue = openDb(dbName).get(bKey);
            return new String(bValue);
        } catch (NoSuchElementException | InterruptedException e) {
            return null;
        }
    }

    public List<String> get_all(String dbName, String key) {
        int index = 1;
        List<String> list = new LinkedList<>();
        while (true) {
            try {
                byte[] bKey = (key + "_" + String.valueOf(index)).getBytes();
                byte[] bValue = openDb(dbName).get(bKey);
                if(bValue == null || bValue.length == 0) break; // key not found
                list.add(new String(bValue));
            } catch (NoSuchElementException | InterruptedException e) {
                break;
            }
            index++;
        }
        return list;
    }

}
