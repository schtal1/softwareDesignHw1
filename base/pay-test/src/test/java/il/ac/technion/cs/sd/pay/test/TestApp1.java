package il.ac.technion.cs.sd.pay.test;



import library.Library;
import com.google.inject.*;
import il.ac.technion.cs.sd.pay.app.MyPayBookInitializer;
import il.ac.technion.cs.sd.pay.app.MyPayBookReader;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.dom.DOMSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

public class TestApp1 {

    private  MyPayBookReader setupAndGetReader(String fileName) throws FileNotFoundException {
        String fileContents =
                new Scanner(new File(ExampleTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
        return  setupAndGetReaderFromString(fileContents);
    }


    private  MyPayBookReader setupAndGetReaderFromString(String docSTR)  {
        Injector injector = Guice.createInjector(new AuxDatabaseModule());
        Library lib = injector.getInstance(Library.class);
        MyPayBookInitializer payBookInitializer = new MyPayBookInitializer(lib);
        payBookInitializer.setup(docSTR);
        return  new MyPayBookReader(lib);
    }

    

    public  final void printDom(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
        System.out.println(out.toString());
    }


    public  void addTransaction(   Document doc, Element rootElement, String seller, int clientId, int amount){
        Element  clientElement = doc.createElement("Client");
        clientElement.setAttribute("Id",String.valueOf(clientId));
        rootElement.appendChild(clientElement);

        Element  PaymentElement = doc.createElement("Payment");
        clientElement.appendChild(PaymentElement);

        Element  IdElement = doc.createElement("Id");
        IdElement.appendChild(doc.createTextNode(seller));
        PaymentElement.appendChild(IdElement);

        Element  AmountElement = doc.createElement("Amount");
        AmountElement.appendChild(doc.createTextNode(String.valueOf(amount)));
        PaymentElement.appendChild(AmountElement);
    }





    public  void randomTest(int clients, int sellers, int transactions){


        Map<Integer,Integer>[] clientBiggestTransactionsPerSeller = ( Map<Integer,Integer>[] )new HashMap[sellers];
        for(int i = 0; i<sellers; i++)clientBiggestTransactionsPerSeller[i] = new HashMap<>();


        Map<Integer,Integer>[] SellerTotalPerClient = ( Map<Integer,Integer>[] )new HashMap[clients];
        for(int i = 0; i<clients; i++)SellerTotalPerClient[i] = new HashMap<>(); for(int client = 0 ; client < clients; client++){
            for(int seller = 0; seller< sellers; seller++) {
                SellerTotalPerClient[client].put(seller, 0);
                clientBiggestTransactionsPerSeller[seller].put(client,0);
            }
        }

        Random random = new Random();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();


            Element rootElement = doc.createElement("Root");
            doc.appendChild(rootElement);

            for(int i =0; i<transactions; i++){
                int client = random.nextInt(clients );
                int seller = random.nextInt(sellers );
                int transactionSum = random.nextInt(10) +1;

                addTransaction(doc, rootElement, "Seller"+String.valueOf(seller), client, transactionSum);
                if(clientBiggestTransactionsPerSeller[seller].get(client) < transactionSum)clientBiggestTransactionsPerSeller[seller].put(client, transactionSum);
                SellerTotalPerClient[client].put(seller, SellerTotalPerClient[client].get(seller) + transactionSum);
                System.out.println("seller X Client X Sum = " + "Seller_"+seller+" ," +client + " , " +transactionSum );

            }



            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            //  transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(domSource, result);

            Map<Integer,Integer> sellerTotal = new HashMap(); // map seller->total for seller
            for(int seller = 0; seller<sellers; seller++) sellerTotal.put(seller,0);
            Map<Integer,Integer> clientTotal = new HashMap(); // map seller->total for seller
            for(int client = 0; client<clients; client++) clientTotal.put(client,0);

            MyPayBookReader payBook = null;
            payBook = setupAndGetReaderFromString(writer.toString());
            for(int client = 0 ; client < clients; client++){
                for(int seller = 0; seller< sellers; seller++) {

                    // verify paidTo
                    try {
                        if (SellerTotalPerClient[client].get(seller) != 0)
                            assertTrue(payBook.paidTo(String.valueOf(client), "Seller" + String.valueOf(seller)));
                        else assertFalse(payBook.paidTo(String.valueOf(client), "Seller" + String.valueOf(seller)));
                    }catch(AssertionError e){
                        throw e;
                    }
                    //verify getPayment
                    Integer totalClientSeller = SellerTotalPerClient[client].get(seller);
                    OptionalDouble queryVal = payBook.getPayment(String.valueOf(client), "Seller"+String.valueOf(seller));
                    if(totalClientSeller == 0)assertFalse(queryVal.isPresent());
                    else assertEquals((queryVal) ,OptionalDouble.of(totalClientSeller));

                    //make list of total for client
                    sellerTotal.put(seller,sellerTotal.get(seller) + SellerTotalPerClient[client].get(seller) );
                    //make list of total per seller
                    clientTotal.put(client,clientTotal.get(client) + SellerTotalPerClient[client].get(seller) );
                }
            }

            System.out.println("paidTo :: test pass" );
            System.out.println("getPayment :: test pass" );



            //verify getFavoriteSeller
            Map<String, Integer> biggestSellerSumPerClient = new HashMap<>();
            for (int client = 0 ; client<clients; client++){
                Optional<String> res = payBook.getFavoriteSeller(String.valueOf(client));
                int maxValue = 0;
                for(int seller = 0 ; seller<sellers; seller++){
                    if(SellerTotalPerClient[client].get(seller) > maxValue) maxValue = SellerTotalPerClient[client].get(seller);
                }
                if(maxValue ==0) assertFalse(res.isPresent());
                else {
                    biggestSellerSumPerClient.put(String.valueOf(client), maxValue);
                    Set<String> favorites = new HashSet<>(); //in case client paid the same to more than one favorite
                    for (int seller = 0; seller < sellers; seller++) {
                        if (SellerTotalPerClient[client].get(seller) == maxValue)
                            favorites.add("Seller" + String.valueOf(seller));
                    }

                    assertTrue(favorites.contains(res.get()));
                }
            }
            System.out.println("getFavoriteSeller :: test pass" );


            //verify getBiggestPaymentsToSellers
            Map<String, Integer>  biggestPaymentForSeller = payBook.getBiggestPaymentsToSellers();
            List<Payment> top10listClients = new ArrayList<>();
            for(int client = 0 ; client < clients; client++){
                if(biggestSellerSumPerClient.get(String.valueOf(client))!=null) {
                    top10listClients.add(new Payment(biggestSellerSumPerClient.get(String.valueOf(client)),
                            String.valueOf(client), ""));
                    Collections.sort(top10listClients);
                    if (top10listClients.size() > 10) top10listClients.remove(0);
                }
            }
            Integer minimumValInList = top10listClients.get(0).sum;
            Map<String,Integer> myBiggestClientsMap = new HashMap<>();
            for(int i = 0; i<top10listClients.size(); i++){
                myBiggestClientsMap.put(top10listClients.get(i).client,top10listClients.get(i).sum );
            }

            for(int client = 0 ; client < clients; client++){
                if(biggestSellerSumPerClient.get(String.valueOf(client))== minimumValInList){
                    myBiggestClientsMap.put(String.valueOf(client), minimumValInList);
                }
            }

            for(String clientKey :biggestPaymentForSeller.keySet()){
                try {
                    assertEquals(biggestPaymentForSeller.get(clientKey), myBiggestClientsMap.get(clientKey));
                }catch(AssertionError e){
                    throw e;
                }
            }
            System.out.println("getBiggestPaymentsToSellers :: test pass" );




            Map<String, Integer> biggestClientSumPerSeller = new HashMap<>();

            //verify getBiggestClient
            for(int seller = 0 ; seller<sellers; seller++){
                Optional<String> res = payBook.getBiggestClient("Seller" +String.valueOf(seller));

                int maxValue = 0;

                for (int client = 0 ; client<clients; client++){
                    if(SellerTotalPerClient[client].get(seller) > maxValue) maxValue = SellerTotalPerClient[client].get(seller);
                }

                if(maxValue ==0) assertFalse(res.isPresent());
                else {
                    biggestClientSumPerSeller.put("Seller"+String.valueOf(seller),maxValue);
                    Set<String> favorites = new HashSet<>(); //in case seller has more than one favorite
                    for (int client = 0; client < clients; client++) {
                        if (SellerTotalPerClient[client].get(seller) ==maxValue)
                            favorites.add(String.valueOf(client));

                    }
                    assertTrue(favorites.contains(res.get()));
                }
            }
            System.out.println("getBiggestClient :: test pass" );


            //verify getBiggestPaymentsFromClients
            Map<String, Integer>  biggestPaymentToSeller = payBook.getBiggestPaymentsFromClients();
            List<Payment> top10list = new ArrayList<>();
            for(int seller = 0 ; seller < sellers; seller++){
                if(biggestClientSumPerSeller.get("Seller" + String.valueOf(seller)) != null) {
                    top10list.add(new Payment(biggestClientSumPerSeller.get("Seller" + String.valueOf(seller)),
                            "", "Seller" + String.valueOf(seller)));
                    Collections.sort(top10list);
                    if (top10list.size() > 10) top10list.remove(0);
                }
            }
            Integer minimumInList = top10list.get(0).sum;
            Map<String,Integer> myBiggestSellersMap = new HashMap<>();
            for(int i = 0; i<top10list.size(); i++){
                myBiggestSellersMap.put(top10list.get(i).seller,top10list.get(i).sum );
            }

            for(int seller = 0 ; seller < sellers; seller++){
                if(biggestClientSumPerSeller.get("Seller" + String.valueOf(seller))== minimumInList){
                    myBiggestSellersMap.put("Seller" + String.valueOf(seller), minimumInList);
                }
            }

            for(String sellerKey :biggestPaymentToSeller.keySet()){
                assertEquals(biggestPaymentToSeller.get(sellerKey), myBiggestSellersMap.get(sellerKey));
            }
            System.out.println("getBiggestPaymentsFromClients :: test pass" );



            //verify getBiggestSpenders
            List<Map.Entry<Integer, Integer>> biggestClientsAndSums = getBiggestEntries(clientTotal, 10);
            Set<String> biggestSpenders = new HashSet<>();

            for(Map.Entry<Integer, Integer> entry : biggestClientsAndSums){
                biggestSpenders.add(String.valueOf(entry.getKey()));
            }

            List<String> res = payBook.getBiggestSpenders();
            for(String spender : res){
                assertTrue(biggestSpenders.contains(spender));
            }
            System.out.println("getBiggestSpenders :: test pass" );


            //verify getRichestSellers
            List<Map.Entry<Integer, Integer>> biggestSellersAndSums = getBiggestEntries(sellerTotal, 10);
            Set<String> biggestSellers = new HashSet<>();
            for(Map.Entry<Integer, Integer> entry : biggestSellersAndSums){
                biggestSellers.add("Seller"+String.valueOf(entry.getKey()));
            }
            res = payBook.getRichestSellers();
            for(String seller : res){
                assertTrue(biggestSellers.contains(seller));
            }
            System.out.println("getRichestSellers :: test pass" );



            System.out.println("============== " );
        } catch (Exception  e) {
            e.printStackTrace();
            System.exit(1);
            return ;
        }
    }



    public static List<Map.Entry<Integer, Integer>> getBiggestEntries( Map<Integer,Integer> map, int n){
        Comparator< Map.Entry<Integer, Integer>> comparator =
                new Comparator<Map.Entry<Integer, Integer>>()
                {
                    @Override
                    public int compare(Map.Entry<Integer, Integer> e0, Map.Entry<Integer, Integer> e1)
                    {
                        Integer v0 = e0.getValue();
                        Integer v1 = e1.getValue();
                        return v0.compareTo(v1);
                    }
                };

        PriorityQueue<Map.Entry<Integer, Integer>> highest =
                new PriorityQueue<>(n, comparator);
        for (Map.Entry<Integer, Integer> entry : map.entrySet())
        {
            highest.offer(entry);
            while (highest.size() > n)
            {
                highest.poll();
            }
        }

        List<Map.Entry<Integer, Integer>> result = new ArrayList<>();
        while (highest.size() > 0)
        {
            result.add(highest.poll());
        }
        return result;

    }







    @Test
    public  void Test1(){
        System.out.println("Test 1");
        MyPayBookReader payBook = null;
        try {
            payBook = setupAndGetReader("small.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Optional<String> res = payBook.getFavoriteSeller("123");
       // System.out.println("123 favorite seller :" +  (res.isPresent() ? res.get() : "no result"));
        assertEquals(res.get(), "Foobar");


        res = payBook.getFavoriteSeller("123");
        assertEquals(res.get(), "Foobar");
        //System.out.println("payBook.getFavoriteSeller(\"123\") :" +  (res.isPresent() ? res.get() : "no result"));

        res = payBook.getBiggestClient("Moobar");
        assertEquals(res.get(), "123");
       // System.out.println("payBook.getBiggestClient(\"Moobar\") :" +  (res.isPresent() ? res.get() : "no result"));

        res = payBook.getBiggestClient("Foobar");
        assertEquals(res.get(), "123");
       // System.out.println("payBook.getBiggestClient(\"Foobar\") :" +  (res.isPresent() ? res.get() : "no result"));

        List<String> resList = payBook.getBiggestSpenders();
        assertEquals(resList.size(), 1);
        assertEquals(resList.get(0), "123");
//        System.out.println("list of biggest spenders");
//        for(String spender: resList){
//            System.out.println(spender);
//        }
//        System.out.println("----------");
    }

    @Test
    public  void Test2(){
        System.out.println("Test 2");
        MyPayBookReader payBook = null;
        try {
            payBook = setupAndGetReader("test1.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Optional<String> res = payBook.getFavoriteSeller("001");
        assertEquals(res.get(),"Seller1");
       // System.out.println("001 favorite seller :" +  (res.isPresent() ? res.get() : "no result"));


        res = payBook.getBiggestClient("Seller1");
        assertEquals(res.get(),"001");
      //  System.out.println("payBook.getBiggestClient(\"Seller1\") :" +  (res.isPresent() ? res.get() : "no result"));

        res = payBook.getBiggestClient("Seller2");
        assertEquals(res.get(),"002");
      //  System.out.println("payBook.getBiggestClient(\"Seller2\") :" +  (res.isPresent() ? res.get() : "no result"));


        res = payBook.getBiggestClient("Seller3");
        assertEquals(res.get(),"001");
        //System.out.println("payBook.getBiggestClient(\"Seller3\") :" +  (res.isPresent() ? res.get() : "no result"));


        List<String> resList = payBook.getBiggestSpenders();
//        System.out.println("list of biggest spenders");

        assertEquals(resList.size(), 2);
        assertEquals(resList.get(0), "002");
        assertEquals(resList.get(1), "001");
//        for(String spender: resList){
//            System.out.println(spender);
//        }
//        System.out.println("----------");
    }

    @Test
    public void RandomTest1(){
        randomTest(2, 3, 3);
    }


    @Test
    public void RandomTest2(){
        randomTest(15, 3, 100);
    }


    @Test
    public void RandomTest3(){
        randomTest(4, 14, 100);
    }

    @Test
    public void RandomTest4(){
        randomTest(15, 20, 3000);
    }

    // auxilery class to store and order payment by sum
    public static class Payment implements Comparable {
        public Integer sum;
        public String client;
        public String seller;

        Payment(Integer sum, String client, String seller) {
            this.sum = sum;
            this.client = client;
            this.seller = seller;
        }

        public int compareTo(Object o) {
            Payment otherPayment = (Payment)o;
            return !o.getClass().equals(Payment.class) ? 0 : this.sum.compareTo(otherPayment.sum);
        }
    }



}
