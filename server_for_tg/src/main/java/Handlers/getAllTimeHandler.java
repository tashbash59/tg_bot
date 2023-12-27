package Handlers;

import Entity.Orders;
import Entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class getAllTimeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String requestMethod = exchange.getRequestMethod();
        String str = "";
        int rCode = 200;
        ArrayList<ArrayList<String>> response = new ArrayList<>();
        String path = exchange.getRequestURI().getPath();
        String date = path.substring(path.lastIndexOf("/") + 1);

        if (requestMethod.equalsIgnoreCase("GET")) {
            //ObjectMapper mapper = new ObjectMapper();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            StringBuilder requestBody = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            try {
                TypedQuery<Orders> queryDate = session.createQuery(
                        "SELECT o.st, o.en\n" +
                                "FROM Orders_time o \n" +
                                "JOIN Orders ord ON ord.order_id = o.order_id\n" +
                                "WHERE ord.date = :date"
                );
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date parsed = format.parse(date); // ваша строка
                java.sql.Date sqlDate = new java.sql.Date(parsed.getTime());
                queryDate.setParameter("date",sqlDate);
                System.out.println(queryDate.getResultList());
                Iterator itr = queryDate.getResultList().iterator();
                while(itr.hasNext()) {
                    ArrayList<String> result = new ArrayList<>();
                    Object[] arrObj = (Object[])itr.next();
                    for(Object obj:arrObj) {
                        result.add(String.valueOf(obj));
                    }
                    response.add(result);
                }
                System.out.println(response);
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
        byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(rCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
