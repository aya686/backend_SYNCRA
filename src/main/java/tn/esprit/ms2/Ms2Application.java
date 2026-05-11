package tn.esprit.ms2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient

public class Ms2Application {

    public static void main(String[] args) {
        SpringApplication.run(Ms2Application.class, args);
    }


}
    