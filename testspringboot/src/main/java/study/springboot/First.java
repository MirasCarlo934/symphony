package study.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class First {
    public static void main(String[] args) {
        System.out.println("========== START running my first spring boot app ==========");
        SpringApplication.run(First.class, args);
        System.out.println("========== END running my first spring boot app ==========");
    }
}
