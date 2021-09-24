package com.german.letterservice.config;


import com.german.letterservice.util.holders.WebSocketSessionHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.util.Objects;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Value("${letter-service.messaging.broker.host}")
    private String brokerHost;

    @Value("${letter-service.messaging.broker.port}")
    private Integer brokerPort;

    @Value("${letter-service.messaging.broker.client-login}")
    private String brokerClientLogin;

    @Value("${letter-service.messaging.broker.client-passcode}")
    private String brokerClientPasscode;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/letter-service-websocket")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        ReactorNettyTcpClient<byte[]> client = new ReactorNettyTcpClient<>(tcpClient -> tcpClient.host(this.brokerHost).port(this.brokerPort),new StompReactorNettyCodec());


        registry
                .enableStompBrokerRelay("/topic")
                .setAutoStartup(true)
                .setClientLogin(this.brokerClientLogin)
                .setClientPasscode(this.brokerClientPasscode)
                .setTcpClient(client);
    }



    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
            @Override
            public WebSocketHandler decorate(final WebSocketHandler handler) {
                return new WebSocketHandlerDecorator(handler) {

                    @Override
                    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {

                        String username = Objects.requireNonNull(session.getPrincipal()).getName();

                        WebSocketSessionHolder.addSession(username, session);

                        super.afterConnectionEstablished(session);
                    }
                };
            }
        });
    }



}
