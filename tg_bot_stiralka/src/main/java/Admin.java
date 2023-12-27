import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Admin {
    private static ArrayList<String> users = new ArrayList<>();
    private static final ArrayList<Integer> blockedTime = new ArrayList<>();
    private static ArrayList<ArrayList<String>> orderTime = new ArrayList<>();
    private static ArrayList<String> orderDate = new ArrayList<>();
    private static Integer userId;

    public static Integer getUserId() {
        return userId;
    }

    public ArrayList<String> getUsers() {
        hhtpGetUsers();
        return users;
    }
    public static ArrayList<Integer> getBlockedTime() {
        return blockedTime;
    }

    public void closeTime(Message message) {
        String text = message.getText();
        String[] words = text.split(" ");
        for (int i = 0; i < words.length; i++) {
            blockedTime.add(Integer.parseInt(words[i]));
        }
    }

    public SendMessage showUsers(Message message, String messageText) {
        hhtpGetUsers();
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        StringBuilder text = new StringBuilder(messageText);
        for (int i = 0; i < users.size();i++) {
            users.set(i, users.get(i).replaceAll("_", " "));
            text.append(i+1).append(") ").append(users.get(i)).append("\n");
        }
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(String.valueOf(text));
        return sendMessage;
    }
    public SendMessage showUsersOrder(Message message, String user) {
        hhtpShowTimeUser(user);
        userId = hhtpGetUserId(user);
        System.out.println(userId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < orderDate.size(); i++) {
            text.append(i+1+ ") " + orderDate.get(i) + ":\n");
            for (int j = 0; j <orderTime.get(i).size();j = j+2) {
                text.append("\t" + orderTime.get(i).get(j) + "-" + orderTime.get(i).get(j+1) + "\n");
            }
        }
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(String.valueOf(text));

        return sendMessage;
    }
    private Integer hhtpGetUserId(String user) {
        try {
            String requestUrl = "http://localhost:8000/user/getUserById";

            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса (GET, POST и т.д.)
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


            // Включаем возможность отправки данных в тело запроса
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                // Дано: данные, которые нужно отправить в теле запроса
                user = user.replaceAll(" ", "");
                String data = "{\"user_name\":\"" + user + "\",\"is_admin\": false}";
                // Записать данные в выходной поток
                os.write(data.getBytes());
            }


            // Получаем ответ от сервера
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();
            return Integer.parseInt(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void hhtpShowTimeUser(String username) {
        try {
            username = username.replaceAll("\\s+", "");
            System.out.println(username + " 1111");
            String requestUrl = "http://localhost:8000/order/getUserTime/" + username;

            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса (GET, POST и т.д.)
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Включаем возможность отправки данных в тело запроса
            connection.setDoOutput(false);


            // Получаем ответ от сервера
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println(response);

            Pattern p = Pattern.compile("\\[((.|\\n)*?)\\]"); // Создаем регулярное выражение
            Matcher m = p.matcher(response); // Создаем Matcher, который будет искать совпадения с нашим регулярным выражением
            List<ArrayList<String>> list = new ArrayList<ArrayList<String>>(); // Создаем список для хранения массивов
            while(m.find()) { // Ищем совпадения
                list.add(new ArrayList<String>(Arrays.asList(m.group().split(",")))); // Разделяем каждую подстроку по запятым и добавляем в список
            }
            for (ArrayList<String> row : list) {
                for (int col = 0; col < row.size(); col++) {
                    row.set(col, row.get(col).replaceAll("\\[|\\]", ""));
                }
            }

            for(int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }

            orderDate = list.get(list.size() - 1);
            list.remove(list.size() - 1); // Удаляем последний элемент из массива
            for (int i = 0; i < list.size(); i++) {
                orderTime.add(list.get(i));
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void deleteUsersOrder(Message message,ArrayList<String> date, ArrayList<ArrayList<String>> time, String selTime) {
        String[] words = selTime.split(" ");

        int[] numbers = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            try {
                numbers[i] = Integer.parseInt(words[i]);
            } catch (Exception e) {
                System.out.println("1");
            }
        }
        Arrays.sort(numbers);
        int[] reverseNumbers = new int[numbers.length];

        for (int i = 0; i < reverseNumbers.length; i++) {
            reverseNumbers[i] = numbers[numbers.length - i - 1];
        }
        for (int i = 0; i < reverseNumbers.length; i++) {
            System.out.println(reverseNumbers[i]);
        }

        for (int i = 0; i < reverseNumbers.length; i++) {
            time.remove(reverseNumbers[i] - 1);
            date.remove(reverseNumbers[i] - 1);
        }

    }

    private void hhtpGetUsers() {
        try {
            URL url = new URL("http://localhost:8000/user/getUsers");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса (GET, POST и т.д.)
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Включаем возможность отправки данных в тело запроса
            connection.setDoOutput(false);


            // Получаем ответ от сервера
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String[] parts = response.toString().replaceAll("]","").split(",");  // Разделяем строку
//
            ArrayList<String> result = new ArrayList<>();  // Создаем список для хранения результатов

            for (String part: parts) {  // Проходимся по каждому элементу
                if (part.startsWith("[")) {  // Если элемент начинается с [
                    part = part.substring(1, part.length());  // Удаляем скобки
                    result.addAll(Arrays.asList(part.split(",")));  // Добавляем в список
                } else {
                    result.add(part);  // Иначе добавляем элемент без изменений
                }
            }
            System.out.println(result);
            users = result;

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
