package il.ac.technion.cs.sd.pay.app;

import com.google.inject.Inject;
import library.Library;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

public class MyPayBookInitializer implements PayBookInitializer {
    private Library lib;

    @Inject
    public MyPayBookInitializer(Library lib) {
        this.lib = lib;
    }

    private Document getXMLDoc(String xmlData) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(xmlData)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return doc;
    }

    private int comparator(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
        if (e2.getValue().equals(e1.getValue())) {
            return e1.getKey().compareTo(e2.getKey());
        }
        return e2.getValue().compareTo(e1.getValue());
    }

    /**
     * Return a list of keys of the n biggest entries in the map.
     */
    private List<String> getNthBest(Map<String, Integer> map, int n) {
        List<String> list = map.entrySet().stream()
                .sorted(this::comparator)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return list.subList(0, Math.min(n, list.size()));
    }



    /**
     * Return a list of strings representing n biggest entries in the map.
     * Each element of the string is of the form "key+value".
     */
    private List<String> getNthBestMap(Map<String, Integer> map, int n) {
        List<String> list = map.entrySet().stream()
                .sorted(this::comparator)
                .map(e -> e.getKey() + "+" + e.getValue().toString())
                .collect(Collectors.toList());
        return list.subList(0, Math.min(n, list.size()));
    }

    @Override
    public void setup(String xmlData) {
        Map<String, Integer> clientSums = new HashMap<>();
        Map<String, Integer> sellerSums = new HashMap<>();
        Map<String, Integer> clientSellerSums = new HashMap<>();
        Map<String, Set<String>> clientToSellers = new HashMap<>();
        Map<String, Set<String>> sellerToClients = new HashMap<>();
        Map<String, Integer> bestClientPays = new HashMap<>();
        Map<String, Integer> bestSellerPays = new HashMap<>();
        NodeList clients = getXMLDoc(xmlData).getDocumentElement().getChildNodes();

        for (int i = 0; i < clients.getLength(); i++) {
            if (clients.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element client = (Element) clients.item(i);
                NodeList payments = client.getElementsByTagName("Payment");
                for (int j = 0; j < payments.getLength(); j++) {
                    if (payments.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        Element payment = (Element) payments.item(j);
                        String clientID = client.getAttribute("Id").trim();
                        String sellerID = payment.getElementsByTagName("Id").item(0).getTextContent().trim();
                        String clientSellerID = clientID + "+" + sellerID;
                        int amount = Integer.parseInt(payment.getElementsByTagName("Amount").item(0).getTextContent().trim());

                        // Allocate entries if some are missing
                        if (!clientSums.containsKey(clientID)) {
                            clientSums.put(clientID, 0);
                            bestClientPays.put(clientID, 0);
                        }
                        if (!sellerSums.containsKey(sellerID)) {
                            sellerSums.put(sellerID, 0);
                            bestSellerPays.put(sellerID, 0);
                        }
                        if (!clientSellerSums.containsKey(clientSellerID)) {
                            clientSellerSums.put(clientSellerID, 0);
                        }
                        if (!clientToSellers.containsKey(clientID)) {
                            clientToSellers.put(clientID, new HashSet<>());
                        }
                        if (!sellerToClients.containsKey(sellerID)) {
                            sellerToClients.put(sellerID, new HashSet<>());
                        }

                        // Sum up all the data available
                        clientSums.put(clientID, clientSums.get(clientID) + amount);
                        sellerSums.put(sellerID, sellerSums.get(sellerID) + amount);
                        clientSellerSums.put(clientSellerID, clientSellerSums.get(clientSellerID) + amount);
                        clientToSellers.get(clientID).add(sellerID);
                        sellerToClients.get(sellerID).add(clientID);
                        if (clientSellerSums.get(clientSellerID) > bestClientPays.get(clientID)) {
                            bestClientPays.put(clientID, clientSellerSums.get(clientSellerID));
                        }
                        if (clientSellerSums.get(clientSellerID) > bestSellerPays.get(sellerID)) {
                            bestSellerPays.put(sellerID, clientSellerSums.get(clientSellerID));
                        }
                    }
                }
            }
        }

        try {
            for (Map.Entry<String, Integer> entry : clientSellerSums.entrySet()) {
                lib.add("client_seller_sums", entry.getKey(), entry.getValue().toString());
            }
            for (Map.Entry<String, Set<String>> entry: clientToSellers.entrySet()) {
                lib.add_all("client_to_sellers", entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, Set<String>> entry: sellerToClients.entrySet()) {
                lib.add_all("seller_to_clients", entry.getKey(), entry.getValue());
            }

            // Sorting logic
            List<String> bestClients = getNthBest(clientSums, 10);
            lib.add_all("client_to_sellers", "best_client", bestClients);
            List<String> bestSellers = getNthBest(sellerSums, 10);
            lib.add_all("seller_to_clients", "best_seller", bestSellers);

            List<String> bestPaymentsClients = getNthBestMap(bestClientPays, 10);
            lib.add_all("client_to_sellers", "best_payments", bestPaymentsClients);
            List<String> bestPaymentsSellers = getNthBestMap(bestSellerPays, 10);
            lib.add_all("seller_to_clients", "best_payments", bestPaymentsSellers);
        }
        catch (DataFormatException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
