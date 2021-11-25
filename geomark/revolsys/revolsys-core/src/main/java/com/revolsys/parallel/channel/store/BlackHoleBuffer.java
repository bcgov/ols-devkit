package com.revolsys.parallel.channel.store;

import com.revolsys.parallel.channel.ChannelValueStore;

/**
 * <H2>Description</H2> The ZeroBuffer class is an implementation of
 * ChannelValueStore which allows one Object to be sent across the Channel at any
 * one time.
 * <P>
 * The getState method will return EMPTY if the Channel does not contain an
 * Object and FULL if it does.
 *
 * @author P.D.Austin
 */
public class BlackHoleBuffer<T> extends ChannelValueStore<T> {
  /**
   * Returns a new Object with the same creation parameters as this Object. This
   * method should be overridden by subclasses to return a new Object that is
   * the same type as this Object. The new instance should be created by
   * constructing a new instance with the same parameters as the original.
   * <I>NOTE: Only the sizes of the data should be cloned not the stored
   * data.</I>
   *
   * @return The cloned instance of this Object.
   */
  @Override
  protected Object clone() {
    return this;
  }

  /**
   * Returns the Object from the ZeroBuffer.
   * <P>
   * <I>NOTE: getState should be called before this method to check that the
   * state is not EMPTY. If the state is EMPTY the ZeroBuffer will be left in an
   * undefined state.</I>
   * <P>
   * Pre-condition: The state must not be EMPTY
   *
   * @return The next available Object from the ChannelValueStore
   */
  @Override
  protected T get() {
    return null;
  }

  /**
   * Returns the current state of the ZeroBuffer, should be called to ensure the
   * Pre-conditions of the other methods are not broken.
   *
   * @return The current state of the ZeroBuffer (EMPTY or FULL)
   */
  @Override
  protected int getState() {
    return EMPTY;
  }

  /**
   * Puts a new Object into the ZeroBuffer.
   * <P>
   * <I>NOTE: getState should be called before this method to check that the
   * state is not FULL. If the state is FULL the ZeroBuffer will be left in an
   * undefined state.</I>
   * <P>
   * Pre-condition: The state must not be FULL
   *
   * @param value The object to put in the ChannelValueStore
   */
  @Override
  protected void put(final T value) {
  }
}
