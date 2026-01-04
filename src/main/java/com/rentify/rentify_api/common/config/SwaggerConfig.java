package com.rentify.rentify_api.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://43.201.87.180:8080");
        devServer.setDescription("back-end-server");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("local-server");

        return new OpenAPI()
            .info(new Info()
                .title("Universal-device-rental")
                .version("v1")
                .description("다방면용 전자기기 대여 서비스 백엔드 서버")
            ).servers(List.of(devServer, localServer));
    }
}
