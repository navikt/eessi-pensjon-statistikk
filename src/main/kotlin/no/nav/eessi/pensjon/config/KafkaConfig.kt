package no.nav.eessi.pensjon.config

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.ConsumerFactory
import java.time.Duration

@Configuration
class KafkaConfig {

    @Bean
    fun sedSendtAuthRetry(registry: KafkaListenerEndpointRegistry): ApplicationRunner? {
        return ApplicationRunner {
            val statisikkListener = registry.getListenerContainer("statistikkListener")
            statisikkListener.containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(4L)
            statisikkListener.start()

            val sedSendtListener = registry.getListenerContainer("sedSendtListener")
            sedSendtListener.containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(4L)
            sedSendtListener.start()

            val sedMottattListener = registry.getListenerContainer("sedMottattListener")
            sedMottattListener.containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(4L)
            sedMottattListener.start()
        }
    }

    @Bean
    fun kafkaListenerContainerFactory(configurer: ConcurrentKafkaListenerContainerFactoryConfigurer,
                                      kafkaConsumerFactory: ConsumerFactory<Any, Any>): ConcurrentKafkaListenerContainerFactory<*, *> {

        val factory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        configurer.configure(factory, kafkaConsumerFactory)
        return factory
    }
}