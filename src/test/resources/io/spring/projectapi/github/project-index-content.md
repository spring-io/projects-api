---
title: Spring AMQP
status: ACTIVE
description: Applies core Spring concepts to the development of AMQP-based messaging solutions.
image: spring-amqp.svg?v=2
featured: true
stackOverflow: https://stackoverflow.com/questions/tagged/spring-amqp%20spring-rabbit%20spring-rabbitmq
github: http://github.com/spring-projects/spring-amqp
site: http://spring.io/projects/spring-amqp
order: 255
supportPolicy: UPSTREAM
---

The Spring AMQP project applies core Spring concepts to the development of AMQP-based messaging solutions. It provides a "template" as a high-level abstraction for sending and receiving messages. It also provides support for Message-driven POJOs with a "listener container". These libraries facilitate management of AMQP resources while promoting the use of dependency injection and declarative configuration. In all of these cases, you will see similarities to the JMS support in the Spring Framework.

The project consists of two parts; spring-amqp is the base abstraction, and spring-rabbit is the RabbitMQ implementation.

[](#features)Features
---------------------

*   Listener container for asynchronous processing of inbound messages

*   `RabbitTemplate` for sending and receiving messages

*   `RabbitAdmin` for automatically declaring queues, exchanges and bindings