/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.spring.rabbit;

import static io.opentelemetry.javaagent.instrumentation.spring.rabbit.AbstractMessageListenerContainerInstrumentation.InvokeListenerAdvice.RABBIT_CHANNEL_CONTEXT_KEY;

import com.rabbitmq.client.Channel;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.amqp.core.Message;

enum SpringRabbitMessageAttributesGetter implements MessagingAttributesGetter<Message, Void> {
  INSTANCE;

  @Override
  public String system(Message message) {
    return "rabbitmq";
  }

  @Override
  public String destinationKind(Message message) {
    return "queue";
  }

  @Override
  @Nullable
  public String destination(Message message) {
    return message.getMessageProperties().getReceivedRoutingKey();
  }

  @Override
  public boolean temporaryDestination(Message message) {
    return false;
  }

  @Override
  @Nullable
  public String protocol(Message message) {
    return null;
  }

  @Override
  @Nullable
  public String protocolVersion(Message message) {
    return null;
  }

  @Override
  @Nullable
  public String url(Message message) {
    Context current = Context.current();
    if (current != null) {
      Object o = current.get(RABBIT_CHANNEL_CONTEXT_KEY);
      if (o != null) {
        Channel channel = (Channel) o;
        String host = channel.getConnection().getAddress().getHostAddress();
        Integer port = channel.getConnection().getPort();
        return host + ":" + port;
      }
    }
    return null;
  }

  @Override
  @Nullable
  public String conversationId(Message message) {
    return null;
  }

  @Override
  public Long messagePayloadSize(Message message) {
    return message.getMessageProperties().getContentLength();
  }

  @Override
  @Nullable
  public Long messagePayloadCompressedSize(Message message) {
    return null;
  }

  @Override
  @Nullable
  public String messageId(Message message, @Nullable Void unused) {
    return message.getMessageProperties().getMessageId();
  }

  @Override
  public List<String> header(Message message, String name) {
    Object value = message.getMessageProperties().getHeaders().get(name);
    if (value != null) {
      return Collections.singletonList(value.toString());
    }
    return Collections.emptyList();
  }
}
