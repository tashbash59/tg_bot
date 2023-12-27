import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TgBot extends TelegramLongPollingBot {
    private static boolean isSelectData = false;
    private static boolean isSelectTime = false;
    private static boolean isDeleteOrder = false;
    private static int isDeleteOrderForUser = 0;
    private static boolean isBlockTime = false;
    private static boolean isAdmin = false;
    private static Users user;
    private static Integer order_id;
    private static final String adminName = "Tashbash59";
    private static ArrayList<ArrayList<String>> orderTime = new ArrayList<>();
    private static ArrayList<String> orderDate = new ArrayList<>();
   // private static final ArrayList<Integer> blockedTime = new ArrayList<>(Arrays.asList(16,17));
    private static final Admin admin = new Admin();


    @Override
    public void onUpdateReceived(Update update) {
        String username =  update.getMessage().getFrom().getUserName();
        regUser(username);
        setUser(username);
        Message message = update.getMessage();
        isAdmin = username.equals(adminName);
        //Tashbash59
        // получаем записи у пользователя и вставляем их в массивы
        hhtpShowOrder(update.getMessage().getFrom().getUserName());

        if (message != null && message.hasText()) {
            switch (message.getText()) {
                case "/start":
                    BackToMenu(message, "Выбери нужную команду",isAdmin);
                    break;
                case "Записаться на стирку":
                    BackToMenu(message, "Записаться на стирку",isAdmin);
                    break;
                case "Список моих записей":
                    BackToMenu(message, "Список моих записей",isAdmin);
                    break;
                case "Удалить запись на стирку":
                    BackToMenu(message, "Удалить запись на стирку",isAdmin);
                    break;
                case "Удалить запись у пользователя":
                    BackToMenu(message, "Удалить запись у пользователя",isAdmin);
                    break;
//                case "Закрыть возможность записи":
//                    BackToMenu(message, "Закрыть возможность записи",isAdmin);
//                    break;
                default:
                    if (isSelectData) {
                        //orderDate.add(message.getText());
                        SelectTime(message);
                    } else if (isSelectTime) {
                        //orderTime.add(message.getText());
                        // ВЫЗВАТЬ ЗАПРОС НА ДОБАВЛЕНИЕ ВРЕМЕНИ НА СЕРВ
                        httpPostTime(message.getText());
                        BackToMenu(message, "Вы записались на стирку!",isAdmin);
                        isSelectTime = false;
                    } else if (isDeleteOrder) {
                        //DeleteOrder(message, message.getText());
                        //Удалить данные http
                        deleteDate(Integer.parseInt(message.getText()));
                        BackToMenu(message, "Данные удалены",isAdmin);
                    } else if (isDeleteOrderForUser == 1) {
                        BackToMenu(message, "",isAdmin);
                    } else if (isBlockTime) {
                        admin.closeTime(message);
                        BackToMenu(message,"Выбранное время заблокировано для записи",isAdmin);
                    } else {
                        BackToMenu(message, "Вы ввели неправильные данные, попробуйте еще раз",isAdmin);
                    }
                    break;
            }
        }
    }

    public void deleteDate(Integer num) {

        order_id = hhtpGetDataId(orderDate.get(num-1));

        try {
            String requestUrl = "http://localhost:8000/order/deleteOrder/" + order_id;

            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса (GET, POST и т.д.)
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Включаем возможность отправки данных в тело запроса
            connection.setDoOutput(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));



            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void setUser(String username) {
        try {
            String requestUrl = "http://localhost:8000/user/getUser/" + username;

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

            ObjectMapper mapper = new ObjectMapper();

// Шаг 2: использовать метод readValue
            user = mapper.readValue(response.toString(), Users.class);

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void regUser(String username){
        try {
            String requestUrl = "http://localhost:8000/user/postUser";

            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса (GET, POST и т.д.)
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Включаем возможность отправки данных в тело запроса
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                // Дано: данные, которые нужно отправить в теле запроса
                String data = "{\"user_name\":\"" + username + "\",\"is_admin\":false}";
                // Записать данные в выходной поток
                os.write(data.getBytes());
            }


            // Получаем ответ от сервера
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String inputLine;

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void httpPostTime(String st_en) {
        try {

            String[] arr = st_en.split("-");
            String requestUrl = "http://localhost:8000/order/postTime/";

            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса (GET, POST и т.д.)
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


            // Включаем возможность отправки данных в тело запроса
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                // Дано: данные, которые нужно отправить в теле запроса
                String data = "{\"order_id\":" + order_id + ",\"st\":\""+ arr[0]
                        +":00\",\"en\":\"" + arr[1] +":00\"}";
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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void SelectTime(Message message) {
        String time = hhtpGetTime(Integer.parseInt(message.getText()));
        LocalDate todayDate = LocalDate.now();
        hhtpPostData(todayDate.plusDays(Integer.parseInt(message.getText())).toString());
        order_id = hhtpGetDataId(todayDate.plusDays(Integer.parseInt(message.getText())).toString());

        Pattern pattern = Pattern.compile ("\\[(\\d+:\\d+:\\d+, \\d+:\\d+:\\d+)\\]");
        Matcher matcher = pattern.matcher (time);
        ArrayList<Integer> result = new ArrayList<>();
        while (matcher.find ()) {
            // Шаг 6: использовать метод group (int) объекта Matcher
            String match = matcher.group (1); // получить подстроку, соответствующую первой группе
            // Шаг 7: разделить полученную подстроку по символу ","
            String[] parts = match.split(","); // получить массив строк
            // Шаг 8: взять первый элемент массива строк и разделить его по символу ":"
            String[] subparts = parts[0].split (":"); // получить массив строк
            // Шаг 9: взять первый элемент массива строк и преобразовать его в число типа int
            int num = Integer.parseInt (subparts[0]); // получить число
            // Шаг 10: добавить полученное число в список
            result.add (num); // добавить число в список
        }


        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        isSelectData = false;
        isSelectTime = true;
        ArrayList<Integer> blockedTime = Admin.getBlockedTime();
        StringBuilder text = new StringBuilder("Выберите время на которое хотите записаться\n Свободное время: \n");
        int from = 0;
        int after = 1;
        int index = 1;

        for (int i = 0; i < 24; i++) {
            if (!blockedTime.contains(from) && !result.contains(from)) {
                if (after < 10) {
                    text.append(index).append(") ").append("0").append(from).append(":00-")
                            .append("0").append(after).append(":00 \n");
                } else {
                    text.append(index).append(") ")
                            .append(from).append(":00-").append(after).append(":00 \n");
                }
                index++;
            }
            from += 1;
            after += 1;
        }

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text.toString());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String hhtpGetTime(int dateNumber) {
        StringBuffer response = new StringBuffer();
        try {
            LocalDate todayDate = LocalDate.now();
            LocalDate date = todayDate.plusDays(dateNumber);
            String requestUrl = "http://localhost:8000/order/getAllTime/" + date;

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

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response);

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    public void ShowError(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(String.valueOf(text));
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void DeleteOrder(Message message,String selectedTime) {

        String[] words = selectedTime.split(" ");

        int[] numbers = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            try {
                numbers[i] = Integer.parseInt(words[i]);
            } catch (Exception e) {
                ShowError(message,"Вы ввели неправильные данные");
                return;
            }
        }
        Arrays.sort(numbers);
        int[] reverseNumbers = new int[numbers.length];

        for (int i = 0; i < reverseNumbers.length;i++) {
            reverseNumbers[i] = numbers[numbers.length-i-1];
        }
        for (int i = 0; i < reverseNumbers.length;i++) {
            System.out.println(reverseNumbers[i]);
        }

        for (int i = 0; i < reverseNumbers.length; i++) {
            orderTime.remove(reverseNumbers[i]-1);
            orderDate.remove(reverseNumbers[i]-1);
        }
        System.out.println(2);
    }

    public void ShowDeleteOrder(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        StringBuilder text = new StringBuilder("**Напишите через пробел номера записей, которые хотите удалить**\n");
        for (int i = 0; i < orderDate.size(); i++) {
            text.append(i+1+ ") " + orderDate.get(i) + ":\n");
            for (int j = 0; j <orderTime.get(i).size();j = j+2) {
                text.append("\t" + orderTime.get(i).get(j) + "-" + orderTime.get(i).get(j+1) + "\n");
            }
        }
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(String.valueOf(text));
        isDeleteOrder = true;
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void SelectData(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        isSelectData = true;
        String text = "**Выберите дату** ";
        LocalDate todayDate = LocalDate.now();
        LocalDate nextWeekDate = todayDate.plusWeeks(1);
        int i = 1;
        for (LocalDate date = todayDate; date.isBefore(nextWeekDate); date = date.plusDays(1)) {
            text += "\n" + i + ") " + date.plusDays(1);
            i++;
        }
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void ShowOrder(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        StringBuilder text = new StringBuilder("Список ваших записей:\n");
        for (int i = 0; i < orderDate.size(); i++) {
            text.append(orderDate.get(i) + ":\n");
            for (int j = 0; j <orderTime.get(i).size();j = j+2) {
                text.append("\t" + orderTime.get(i).get(j) + "-" + orderTime.get(i).get(j+1) + "\n");
            }
        }
        sendMessage.setText(String.valueOf(text));
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Integer hhtpGetDataId(String d) {
        try {
            String requestUrl = "http://localhost:8000/order/getOrderId";

            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса (GET, POST и т.д.)
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


            // Включаем возможность отправки данных в тело запроса
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                // Дано: данные, которые нужно отправить в теле запроса
                String data = "{\"user_id\":" + user.getUser_id() + ",\"date\":\""+ d +"\"}";
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

    public Integer hhtpPostData(String d) {
        try {
            String requestUrl = "http://localhost:8000/order/postDate/";

            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса (GET, POST и т.д.)
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


            // Включаем возможность отправки данных в тело запроса
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                // Дано: данные, которые нужно отправить в теле запроса
                String data = "{\"user_id\":" + user.getUser_id() + ",\"date\":\""+ d +"\"}";
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


    private void hhtpShowOrder(String username) {
        try {
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
            orderTime.clear();
            orderDate.clear();
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

    public void BackToMenu (Message message, String text,Boolean isAdmin) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        // Создаем клавиатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new
                ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow.add("Записаться на стирку");
        keyboardFirstRow.add("Список моих записей");
        keyboardFirstRow.add("Удалить запись на стирку");

        KeyboardRow keyboardSecondRow = new KeyboardRow();


        if (isAdmin) {
            keyboardSecondRow.add("Удалить запись у пользователя");
//            keyboardSecondRow.add("Закрыть возможность записи");
        }


        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанавливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        if (text == "Записаться на стирку") {
            SelectData(message);
        } else if (text == "Список моих записей") {
            ShowOrder(message);
        } else if (text == "Удалить запись на стирку") {
            ShowDeleteOrder(message);
        } else if (text == "Удалить запись у пользователя" && isAdmin) {
            sendMessage = admin.showUsers(message,"Выберите пользователя \n");
            isDeleteOrderForUser = 1;
        } else if (isDeleteOrderForUser != 0) {
            if (isDeleteOrderForUser == 1) {
                String user = admin.getUsers().get(Integer.parseInt(message.getText())-1);
                sendMessage = admin.showUsersOrder(message,user);
                isDeleteOrderForUser = 2;
            } else if (isDeleteOrderForUser == 2) {
                //admin.deleteUsersOrder(message,orderDate,orderTime,message.getText());
                Integer id = admin.getUserId();
                isDeleteOrderForUser = 0;
                sendMessage.setText("Записи этого пользователя удалены");
            }
        } else if (text == "Закрыть возможность записи") {
            sendMessage.setText("Напишите часы, в которые стиралка работать не будет, например 16 17 18");
            isBlockTime = true;
        } else {
            sendMessage.setText(text);
        }
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "stiralka1_bot";
    }

    @Override
    public String getBotToken() {
        return "6381318117:AAHT1niJRR_-AVEQ35KguyrGmyEZH1hznTk";
    }
}
