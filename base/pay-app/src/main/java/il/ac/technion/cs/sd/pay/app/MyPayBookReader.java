package il.ac.technion.cs.sd.pay.app;

import com.google.inject.Inject;
import library.Library;

import java.util.*;

public class MyPayBookReader implements PayBookReader {
    private Library lib;

    @Inject
    public MyPayBookReader(Library lib) {
        this.lib = lib;
    }

    @Override
    public boolean paidTo(String clientId, String sellerId) {
        // Entry exists if client paid to seller
        return lib.get("client_seller_sums", clientId + "+" + sellerId) != null;
    }

    @Override
    public OptionalDouble getPayment(String clientId, String sellerId) {
        // Entry exists if client paid to seller
        String payment = lib.get("client_seller_sums", clientId + "+" + sellerId);
        if (payment != null) {
            return OptionalDouble.of((double) Integer.parseInt(payment));
        }
        return OptionalDouble.empty();
    }

    @Override
    public List<String> getBiggestSpenders() {
        return lib.get_all("client_to_sellers", "best_client");
    }

    @Override
    public List<String> getRichestSellers() {
        return lib.get_all("seller_to_clients", "best_seller");
    }

    @Override
    public Optional<String> getFavoriteSeller(String clientId) {
        List<String> sellers = lib.get_all("client_to_sellers", clientId);
        int best_amount = -1;
        String best_seller = null;
        for (String seller: sellers) {
            int amount = Integer.parseInt(lib.get("client_seller_sums", clientId + "+" + seller));
            if (amount > best_amount) {
                best_amount = amount;
                best_seller = seller;
            }
        }
        if (best_seller != null) {
            return Optional.of(best_seller);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getBiggestClient(String sellerId) {
        List<String> clients = lib.get_all("seller_to_clients", sellerId);
        int best_amount = -1;
        String best_client = null;
        for (String client: clients) {
            int amount = Integer.parseInt(lib.get("client_seller_sums", client + "+" + sellerId));
            if (amount > best_amount) {
                best_amount = amount;
                best_client = client;
            }
        }
        if (best_client != null) {
            return Optional.of(best_client);
        }
        return Optional.empty();
    }

    private Map<String, Integer> getBiggestPayments(String dbName, String key) {
        List<String> bestPays = lib.get_all(dbName, key);
        Map<String, Integer> map = new HashMap<>();
        for (String payment: bestPays) {
            String[] data = payment.split("\\+");
            map.put(data[0], Integer.parseInt(data[1]));
        }
        return map;
    }

    @Override
    public Map<String, Integer> getBiggestPaymentsToSellers() {
        return getBiggestPayments("client_to_sellers", "best_payments");
    }

    @Override
    public Map<String, Integer> getBiggestPaymentsFromClients() {
        return getBiggestPayments("seller_to_clients", "best_payments");
    }
}
