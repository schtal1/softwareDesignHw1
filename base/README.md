* For now, all the logic of reader and initializer is written. What's left is write tests to the library and the app.
    * For the app testing, we can write a "fake" Secure database factory that use maps, exactly like how we did for homework 0 (This time we don't need to use Mockito I think).
    * I'm not sure I fully understand how to use Guice, but I think we need to make a second Paybook module for our tests, that will bind our fake db factory to our app.
* I added add_all and get_all to the library to be able to retrieve list of value from a single key in a database. I use it to find 10 biggest... data or list of clients/sellers per seller/client.
* Although we try to sort large lists in the initializer, we should not have performance problems: I tried to sort up to one million of entries and Java handle it in around 1 second.
* I didn't implemented typing (int and string) in the library in the end. I thought that most of the time, clients or sellers won't make huge amounts of payments, so length of the total payments won't be bigger than 10000 most of the time. In that case, it will be more efficient to keep strings of size <4 bytes instead of ints which are always 4 bytes.
* In the end, I ended up using only 3 databases for SecureDatabase:
    * `client_seller_sums` contains all the client/seller total payments.
    * `client_to_sellers` contains, for each client, a list of all the sellers the client made payment with. It also contains the 10 biggest clients and the 10 biggest payment to sellers.
    * `seller_to_clients` contains, for each seller, a list of all the clients that paid the seller. It also contains the 10 biggest sellers and the 10 biggest payments from clients.
    
    You can see in the code that I used special keys to store the 10 biggest... data. This won't make conflicts with client or seller names as it is asserted that client or seller ID have a length of 10 character at most.
* The test are failing because of timing for now. This is because their SecureDatabase never throw NoSuchElementException, and so the get_all method never stop (Maybe we can fix this, but it should not happen with real database).
* If you have any question, you can call me.