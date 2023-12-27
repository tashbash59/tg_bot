package Handlers;

import Entity.Users;
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
import javax.persistence.criteria.Root;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class getUserHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String requestMethod = exchange.getRequestMethod();
        String str = "";
        int rCode = 200;
        String response = "";
        String path = exchange.getRequestURI().getPath();
        String id = path.substring(path.lastIndexOf("/") + 1);

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
                TypedQuery<Users> queryDate = session.createQuery(
                        "SELECT user.user_id, user.user_name, user.is_admin\n" +
                                "FROM Users user \n" +
                                "WHERE user.user_name = :username"
                );
                queryDate.setParameter("username",id);
                List<String> result = new ArrayList<>();
                Iterator itr = queryDate.getResultList().iterator();
                while(itr.hasNext()) {
                    Object[] arrObj = (Object[])itr.next();
                    for(Object obj:arrObj) {
                        result.add(String.valueOf(obj));
                    }
                }
                String json = "{\"user_id\":" + result.get(0)
                        + ",\"user_name\":\"" +result.get(1) +
                        "\",\"is_admin\":" + result.get(2) +
                        "}";
                response = json;

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
