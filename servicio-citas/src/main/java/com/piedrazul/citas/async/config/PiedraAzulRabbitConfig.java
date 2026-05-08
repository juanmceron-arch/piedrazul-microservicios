package com.piedrazul.citas.async.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "piedrazul.async", name = "enabled", havingValue = "true")
public class PiedraAzulRabbitConfig {

    public static final String ESPECIALISTA_EXCHANGE = "piedrazul.especialistas.exchange";
    public static final String ESPECIALISTA_CREADO_QUEUE = "piedrazul.citas.especialista-creado";
    public static final String ESPECIALISTA_CREADO_ROUTING_KEY = "especialista.creado";

    @Bean
    public DirectExchange especialistaExchange() {
        return new DirectExchange(ESPECIALISTA_EXCHANGE, true, false);
    }

    @Bean
    public Queue especialistaCreadoQueue() {
        return new Queue(ESPECIALISTA_CREADO_QUEUE, true);
    }

    @Bean
    public Binding especialistaCreadoBinding(Queue especialistaCreadoQueue, DirectExchange especialistaExchange) {
        return BindingBuilder
                .bind(especialistaCreadoQueue)
                .to(especialistaExchange)
                .with(ESPECIALISTA_CREADO_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
