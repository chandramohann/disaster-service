package de.extremeenvironment.disasterservice.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by on 30.06.16.
 *
 * @author David Steiman
 */
@Configuration
@Profile("!test")
@EnableFeignClients(basePackages = "de.extremeenvironment.disasterservice.client")
public class FeignConfiguration {
}
