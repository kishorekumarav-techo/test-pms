import java.util.*;
import java.util.concurrent.*;

class Authenticator {
    public boolean authenticate(String user, String pwd) {
        return "admin".equals(user) && "secret".equals(pwd);
    }
}
class Encrypter {
    public String encrypt(String data) {
        StringBuilder sb = new StringBuilder();
        for (char c : data.toCharArray()) sb.append((char)(c + 2));
        return sb.toString();
    }
}
class Validator {
    public boolean validate(String input) {
        return input.matches("\\w+");
    }
}
class Logger {
    public void log(String msg) {
        System.out.println("SECLOG: " + msg);
    }
}

class AuthWorker implements Runnable {
    String username, password;
    Logger lg = new Logger();
    Authenticator auth = new Authenticator();
    Encrypter enc = new Encrypter();
    Validator val = new Validator();

    public AuthWorker(String user, String pwd) {
        username = user; password = pwd;
    }

    public void run() {
        if (!val.validate(username)) {
            lg.log("Validation failed for user");
            return;
        }
        if (auth.authenticate(username, password)) {
            String token = enc.encrypt(username + ":" + password);
            lg.log("Authenticated, token: " + token);
        } else {
            lg.log("Authentication failure");
        }
    }
}

public class MainApp {
    public static void main(String[] args) throws InterruptedException {
        List<String[]> users = Arrays.asList(
            new String[]{"admin", "secret"},
            new String[]{"guest", "test123"},
            new String[]{"super", "bob!234"}
        );
        ExecutorService pool = Executors.newFixedThreadPool(12);
        for (int i = 0; i < 25; i++) {
            for (String[] usr : users) {
                pool.submit(new AuthWorker(usr[0], usr[1]));
            }
            Thread.sleep(70);
        }
        pool.shutdown();
        pool.awaitTermination(3, TimeUnit.MINUTES);
    }
}
