package Handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class getUserTimeHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException{
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String requestMethod = exchange.getRequestMethod();
        String str = "";
        int rCode = 200;
        List<List<String>> response = new ArrayList<>();
        String path = exchange.getRequestURI().getPath();
        String name = path.substring(path.lastIndexOf("/") + 1);
        System.out.println(name);

        if (requestMethod.equalsIgnoreCase("GET")) {
            //ObjectMapper mapper = new ObjectMapper();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            StringBuilder requestBody = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            try {
                // Запрос, чтобы узнать дату
                TypedQuery<String> queryDate = session.createQuery(
                        "SELECT orders.date\n" +
                                "FROM Orders orders \n" +
                                "JOIN Users users ON users.user_id = orders.user_id\n" +
                                "WHERE users.user_name = :name"
                );
                queryDate.setParameter("name",name);
                List<String> date = new ArrayList<>();
                date = queryDate.getResultList();
                // запрос чтобы узнать время
                for (int i = 0; i < date.size();i++) {
                    List<String> result = new ArrayList<>();
                    Query query = session.createQuery(
                            "SELECT orders_time.st, orders_time.en\n" +
                                    "FROM Orders_time orders_time \n" +
                                    "JOIN Orders orders ON orders.order_id = orders_time.order_id\n" +
                                    "JOIN Users users ON users.user_id = orders.user_id\n" +
                                    "WHERE orders.date = :date AND users.user_name = :name"
                    );
                    query.setParameter("date",date.get(i));
                    query.setParameter("name",name);
                    //List<java.sql.Time> time = query.getResultList();
                    Iterator itr = query.getResultList().iterator();
                    while(itr.hasNext()) {
                        Object[] arrObj = (Object[])itr.next();
                        for(Object obj:arrObj) {
                            result.add(String.valueOf(obj)) ;
                        }
                    }
                    response.add(result);

                }
                response.add(date);
//                for (int i = 0; i < query.getResultList().size();i++) {
//                    List<Object> time = Arrays.asList(query.getResultList().get(i));
//                    System.out.println(time.get(0));
//                }

//                System.out.println(date.get(0));
//                System.out.println(query.getResultList().get(0));
                //response.add(query.getResultList().get(0).toString());
                //System.out.println(query.getResultList().get(0).toString());

                transaction.commit();
            } catch (Exception e){
                e.printStackTrace();
                if (transaction != null) {
                    transaction.rollback();
                }
                rCode = 400;
                str = e.getMessage();
            } finally {
                if (session != null) {
                    session.close();
                    sessionFactory.close();
                }

            }


        } else {
            session.close();
            sessionFactory.close();
            rCode = 400;
            str = "method of requrest is wrong";
        }

        //response.put("message", str);
        System.out.println(response);
        System.out.println(rCode);
        byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(rCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
