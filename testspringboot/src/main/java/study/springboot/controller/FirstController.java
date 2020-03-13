package study.springboot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * FirstController
 *
 * @author cels
 *
 */
@RestController
@RequestMapping("/api/first")

public class FirstController {
    @GetMapping("/hello-world")
    public ResponseEntity<String> get() {
        return ResponseEntity.ok("Hello World!");
    }

    @GetMapping("/ip")
    public String getIp() {
        InetAddress ip;
        String hostname;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            System.out.println("Your current IP address : " + ip);
            System.out.println("Your current Hostname : " + hostname);
            return ("Your current IP address : " + ip +"<br>Your current Hostname : " + hostname);

        } catch (UnknownHostException e) {
            e.printStackTrace();
            return ("Error");
        }
    }
}
