package il.ac.technion.cs.sd.pay.app;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * This class will only be instantiated by Guice after
 * {@link PayBookInitializer#setup(java.lang.String) has been called}.
 */
public interface PayBookReader {
  /** Returns true iff client has paid the seller. If the client or seller do not exist, returns false. */
  boolean paidTo(String clientId, String sellerId);

  /**
   * Returns the client's payment sum to the seller, if the client paid the seller.
   * If the client or seller do not exist, or the client didn't pay the seller, returns empty.
   */
  OptionalDouble getPayment(String clientId, String sellerId);

  /**
   * Returns a <b>sorted</b> list of all the clients by their global spending.
   * Secondary sort is by client ID.
   */
  List<String> getBiggestSpenders();

  /**
   * Returns a <b>sorted</b> list of all the sellers by their global earnings.
   * Secondary sort is by seller ID.
   */
  List<String> getRichestSellers();

  /**
   * Returns the seller to whom the client paid the most.
   * If there is more then one - return the first by lexicographical ordering of the ids.
   * If the client does not exist or has no payments, returns empty.
   */
  Optional<String> getFavoriteSeller(String clientId);

  /**
   * Returns the client from whom the seller made the most money.
   * If there is more then one - return the first by lexicographical ordering of the ids.
   * If the client does not exist or has no payments, returns empty.
   */
  Optional<String> getBiggestClient(String sellerId);

  /**
   * Returns the amounts of all the biggest payments made to sellers.
   * A map from seller ID to their biggest payment amount.
   */
  Map<String, Integer> getBiggestPaymentsToSellers();

  /**
   * Returns the amounts of all the biggest payments made by clients.
   * A map from client ID to their biggest payment amount.
   */
  Map<String, Integer> getBiggestPaymentsFromClients();
}
