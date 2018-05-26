package il.ac.technion.cs.sd.pay.test;

import il.ac.technion.cs.sd.pay.ext.SecureDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;




public class AuxSecureDB implements SecureDatabase {
    private Map<ByteArrayWrapper,byte[]> map;

    public AuxSecureDB(){
        map = new HashMap<>();
    }
    @Override
    public void addEntry(byte[] key, byte[] val) throws DataFormatException {
        if(key==null || val == null) throw new DataFormatException();
        if(key.length > 100 || val.length > 100 )throw new DataFormatException();
        ByteArrayWrapper keyWrapper = new ByteArrayWrapper(key);
        map.put(keyWrapper,val);
    }

    @Override
    public byte[] get(byte[] key) throws NoSuchElementException {
        ByteArrayWrapper keyWrapper = new ByteArrayWrapper(key);
        if (!map.containsKey(keyWrapper)) {
            throw new NoSuchElementException();
        }
        return map.get(keyWrapper);
    }
}
