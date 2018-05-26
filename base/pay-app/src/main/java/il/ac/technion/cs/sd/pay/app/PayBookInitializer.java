package il.ac.technion.cs.sd.pay.app;

public interface PayBookInitializer {
  /** Saves the XML data persistently, so that it could be run using PayBookReader. */
  void setup(String xmlData);
}
