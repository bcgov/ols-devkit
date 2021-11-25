package com.revolsys.parallel.channel;

import com.revolsys.collection.map.ThreadLocalMap;

public class ChannelThreadConnection {

  private static final ThreadLocalMap<ChannelOutput<?>, ChannelThreadConnection> connections = new ThreadLocalMap<>();

  public static void writeConnect(final ChannelOutput<?> channel) {
    synchronized (connections) {
      ChannelThreadConnection connection = connections.get(channel);
      if (connection == null) {
        connection = new ChannelThreadConnection(channel);
        connections.put(channel, connection);
        channel.writeConnect();
      }
    }
  }

  private final ChannelOutput<?> channel;

  private ChannelThreadConnection(final ChannelOutput<?> channel) {
    this.channel = channel;
  }

  @Override
  public void finalize() {
    this.channel.writeDisconnect();
  }
}
