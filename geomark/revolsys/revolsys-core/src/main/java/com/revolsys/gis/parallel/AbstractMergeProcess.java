package com.revolsys.gis.parallel;

import com.revolsys.collection.ArrayUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.channel.MultiInputSelector;
import com.revolsys.parallel.channel.store.Buffer;
import com.revolsys.parallel.process.AbstractInOutProcess;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public abstract class AbstractMergeProcess extends AbstractInOutProcess<Record, Record> {

  private static final int OTHER_INDEX = 1;

  private static final int SOURCE_INDEX = 0;

  private Channel<Record> otherIn;

  private int otherInBufferSize = 0;

  private void addObjectFromOtherChannel(final Channel<Record>[] channels, final boolean[] guard,
    final Record[] objects, final int channelIndex) {
    int otherIndex;
    if (channelIndex == SOURCE_INDEX) {
      otherIndex = OTHER_INDEX;
    } else {
      otherIndex = SOURCE_INDEX;
    }
    final Channel<Record> otherChannel = channels[otherIndex];
    if (otherChannel == null) {
      guard[otherIndex] = false;
      guard[channelIndex] = true;
    } else if (guard[otherIndex]) {
      while (objects[otherIndex] == null) {
        try {
          final Record object = otherChannel.read();
          if (testObject(object)) {
            objects[otherIndex] = object;
            return;
          }
        } catch (final ClosedException e) {
          guard[otherIndex] = false;
          guard[channelIndex] = true;
          return;
        }
      }
    }
  }

  /**
   * Add an object from the other (otherId) channel.
   *
   * @param object The object to add.
   */
  protected abstract void addOtherObject(Record object);

  private RecordDefinition addSavedObjects(final RecordDefinition currentType,
    final String currentTypeName, final Channel<Record> out, final boolean[] guard,
    final Record[] objects) {
    final Record sourceObject = objects[SOURCE_INDEX];
    final Record otherObject = objects[OTHER_INDEX];
    if (sourceObject == null) {
      if (otherObject == null) {
        return null;
      } else {
        addOtherObject(otherObject);
        objects[OTHER_INDEX] = null;
        guard[OTHER_INDEX] = true;
        return otherObject.getRecordDefinition();
      }
    } else if (otherObject == null) {
      if (sourceObject == null) {
        return null;
      } else {
        addSourceObject(sourceObject);
        objects[SOURCE_INDEX] = null;
        guard[SOURCE_INDEX] = true;
        return sourceObject.getRecordDefinition();
      }
    } else {
      final RecordDefinition sourceType = sourceObject.getRecordDefinition();
      final String sourceTypeName = sourceType.getPath();
      final RecordDefinition otherType = otherObject.getRecordDefinition();
      final String otherTypeName = otherType.getPath();
      if (sourceTypeName.equals(currentTypeName)) {
        addSourceObject(sourceObject);
        objects[SOURCE_INDEX] = null;
        guard[SOURCE_INDEX] = true;
        objects[OTHER_INDEX] = otherObject;
        guard[OTHER_INDEX] = false;
        return currentType;
      } else if (otherTypeName.equals(currentTypeName)) {
        addOtherObject(otherObject);
        objects[SOURCE_INDEX] = sourceObject;
        guard[SOURCE_INDEX] = false;
        objects[OTHER_INDEX] = null;
        guard[OTHER_INDEX] = true;
        return currentType;
      } else {
        processObjects(currentType, out);
        final int nameCompare = sourceTypeName.toString().compareTo(otherTypeName.toString());
        if (nameCompare < 0) {
          // If the first feature type name is < second feature type
          // name
          // then add the first feature and save the second feature
          // for later
          addSourceObject(sourceObject);
          objects[SOURCE_INDEX] = null;
          guard[SOURCE_INDEX] = true;
          objects[OTHER_INDEX] = otherObject;
          guard[OTHER_INDEX] = false;
          return sourceType;
        } else if (nameCompare == 0) {
          // If both features have the same type them add them
          addSourceObject(sourceObject);
          addOtherObject(otherObject);
          objects[SOURCE_INDEX] = null;
          guard[SOURCE_INDEX] = true;
          objects[OTHER_INDEX] = null;
          guard[OTHER_INDEX] = true;
          return sourceType;
        } else {
          // If the first feature type name is > second feature type
          // name
          // then add the second feature and save the first feature
          // for later
          addOtherObject(otherObject);
          objects[SOURCE_INDEX] = sourceObject;
          guard[SOURCE_INDEX] = false;
          objects[OTHER_INDEX] = null;
          guard[OTHER_INDEX] = true;
          return otherType;
        }
      }
    }

  }

  /**
   * Add an object from the source (in) channel.
   *
   * @param object The object to add.
   */
  protected abstract void addSourceObject(Record object);

  /**
   * @return the in
   */
  public Channel<Record> getOtherIn() {
    if (this.otherIn == null) {
      if (this.otherInBufferSize < 1) {
        setOtherIn(new Channel<Record>());
      } else {
        final Buffer<Record> buffer = new Buffer<>(this.otherInBufferSize);
        setOtherIn(new Channel<>(buffer));
      }
    }
    return this.otherIn;
  }

  public int getOtherInBufferSize() {
    return this.otherInBufferSize;
  }

  protected void init(final RecordDefinition recordDefinition) {
  }

  protected abstract void processObjects(RecordDefinition currentType, Channel<Record> out);

  @Override
  @SuppressWarnings("unchecked")
  protected void run(final Channel<Record> in, final Channel<Record> out) {
    setUp();
    try {
      RecordDefinition currentType = null;
      String currentTypeName = null;
      final Channel<Record>[] channels = ArrayUtil.newArray(in, this.otherIn);

      final boolean[] guard = new boolean[] {
        true, true
      };
      final Record[] objects = new Record[2];
      final String[] typePaths = new String[2];
      for (int i = 0; i < 2; i++) {
        try {
          final Channel<Record> channel = channels[i];
          if (channel == null) {
            guard[i] = false;
          } else {
            Record object = null;
            boolean test = false;
            do {
              object = channel.read();
              test = testObject(object);
            } while (!test);
            if (test) {
              objects[i] = object;
              typePaths[i] = objects[i].getRecordDefinition().getPath();
            }

          }
        } catch (final ClosedException e) {
          guard[i] = false;
        }
      }
      final Record otherObject = objects[OTHER_INDEX];
      if (typePaths[SOURCE_INDEX] != null) {
        final Record sourceObject = objects[SOURCE_INDEX];
        if (typePaths[OTHER_INDEX] != null) {
          final int nameCompare = typePaths[SOURCE_INDEX].toString()
            .compareTo(typePaths[OTHER_INDEX].toString());
          if (nameCompare <= 0) {
            currentType = sourceObject.getRecordDefinition();
            currentTypeName = typePaths[SOURCE_INDEX];
            addSourceObject(sourceObject);
            objects[SOURCE_INDEX] = null;
            if (nameCompare != 0) {
              guard[OTHER_INDEX] = false;
            }
          }
          if (nameCompare >= 0) {
            currentType = otherObject.getRecordDefinition();
            currentTypeName = typePaths[OTHER_INDEX];
            addOtherObject(otherObject);
            objects[OTHER_INDEX] = null;
            if (nameCompare != 0) {
              guard[SOURCE_INDEX] = false;
            }
          }
        } else {
          currentType = sourceObject.getRecordDefinition();
          currentTypeName = typePaths[SOURCE_INDEX];
          if (otherObject != null) {
            addSourceObject(otherObject);
          }
        }
      } else {
        currentType = otherObject.getRecordDefinition();
        currentTypeName = typePaths[OTHER_INDEX];
        if (otherObject != null) {
          addOtherObject(otherObject);
        }
        objects[OTHER_INDEX] = null;
      }
      try {
        final MultiInputSelector alt = new MultiInputSelector();
        final boolean running = true;
        while (running) {
          final int channelIndex = alt.select(channels, guard, 1000);
          if (channelIndex >= 0) {
            final Record object = channels[channelIndex].read();
            if (testObject(object)) {
              final RecordDefinition recordDefinition = object.getRecordDefinition();
              final String typePath = recordDefinition.getPath();
              if (currentTypeName == null) {
                currentTypeName = typePath;
                currentType = recordDefinition;
                init(recordDefinition);
              }
              if (typePath.equals(currentTypeName)) {
                currentTypeName = typePath;
                currentType = recordDefinition;

                if (channelIndex == SOURCE_INDEX) {
                  addSourceObject(object);
                } else {
                  addOtherObject(object);
                }
              } else {
                objects[channelIndex] = object;
                addObjectFromOtherChannel(channels, guard, objects, channelIndex);
                currentType = addSavedObjects(currentType, currentTypeName, out, guard, objects);
                if (currentType != null) {
                  currentTypeName = currentType.getPath();
                }
              }
            }
          } else {
            if (channels[0].isClosed()) {
              guard[1] = true;
            } else if (channels[1].isClosed()) {
              guard[0] = true;
            }
          }
        }
      } finally {
        try {
          while (addSavedObjects(currentType, currentTypeName, out, guard, objects) != null) {
          }
          processObjects(currentType, out);
        } finally {

        }
      }
    } finally {
      this.otherIn.readDisconnect();
      tearDown();
    }
  }

  /**
   * @param in the in to set
   */
  public void setOtherIn(final Channel<Record> in) {
    this.otherIn = in;
    in.readConnect();
  }

  public void setOtherInBufferSize(final int otherInBufferSize) {
    this.otherInBufferSize = otherInBufferSize;
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  protected boolean testObject(final Record object) {
    return true;
  }
}
