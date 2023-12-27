package Handlers;

import Entity.Orders;
import Entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class getUserByIdHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String requestMethod = exchange.getRequestMethod();
        String str = "";
        Orders order = new Orders();
        int rCode = 200;
        Integer response = -1;
        if (requestMethod.equalsIgnoreCase("POST")) {
            ObjectMapper mapper = new ObjectMapper();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            StringBuilder requestBody = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            System.out.println(requestBody.toString());
            try {
                //Product product = mapper.readValue(requestBody.toString(), Product.class);
                Users user = mapper.readValue(requestBody.toString(), Users.class);
                Criteria criteria = session.createCriteria(Users.class);
                criteria.add(Restrictions.eq("user_name", user.getUser_name()));
                criteria.add(Restrictions.eq("is_admin", user.getIs_admin()));
// Получаем список продуктов, удовлетворяющих критерию
                List<Users> o = criteria.list();
                response = o.get(0).getUser_id();
                str = "Успешно!";
                transaction.commit();
            } catch (ConstraintViolationException e) {
                order = mapper.readValue(requestBody.toString(), Orders.class);
//                CriteriaBuilder builder = session.getCriteriaBuilder();
//                CriteriaQuery<Orders> criteria = builder.createQuery(Orders.class);
//                Root<Orders> root = criteria.from(Orders.class);
//                Predicate or = builder.equal(root.get("date"), user.getUsername());
//                criteria.select(root).where();
                str = order.getOrder_id().toString();

            }
            catch (Exception e){
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
