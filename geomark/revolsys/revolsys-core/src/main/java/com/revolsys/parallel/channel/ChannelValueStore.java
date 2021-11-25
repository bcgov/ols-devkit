package com.revolsys.parallel.channel;

/**
 * @author Paul Austin
 * @param <T> The type of Object stored
 */
public abstract class ChannelValueStore<T> implements Cloneable {
  /** Specifies that the ChannelValueStore is empty and cannot give further data */
  protected static int EMPTY = 1;

  /** Specifies that the ChannelValueStore is full and cannot accept further data */
  protected static int FULL = 2;

  /**
   * Specifies that the ChannelValueStore is neither empty or full and can give
   * and accept further data.
   */
  protected static int NONEMPTYFULL = 0;

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
  protected abstract Object clone();

  /**
   * Returns the next available Object from the ChannelValueStore. <I>NOTE:
   * getState should be called before this method to check that the state is not
   * EMPTY. If the state is EMPTY unpredictable results will occur.</I>
   * Pre-condition: The state must not be EMPTY
   *
   * @return The next available Object from the ChannelValueStore
   */
  protected abstract T get();

  /**
   * Returns the current state of the ChannelValueStore, should be called to
   * ensure the Pre-conditions of the other methods are not broken.
   *
   * @return The current state of the ChannelValueStore (EMPTY, NONEMPTYFULL or
   *         FULL)
   */
  protected abstract int getState();

  /**
   * Puts a new Object into the ChannelValueStore. <I>NOTE: getState should be
   * called before this method to check that the state is not FULL. If the state
   * is FULL unpredictable results will occur.</I> Pre-condition: The state must
   * not be FULL
   *
   * @param value The object to put in the ChannelValueStore
   */
  protected abstract void put(T value);
}
