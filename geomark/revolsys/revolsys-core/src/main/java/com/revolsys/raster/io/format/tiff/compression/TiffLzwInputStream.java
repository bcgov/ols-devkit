/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.revolsys.raster.io.format.tiff.compression;

import java.io.IOException;
import java.io.InputStream;

import org.jeometry.common.number.Bytes;

public class TiffLzwInputStream extends InputStream {

  private static final int andTable[] = {
    511, 1023, 2047, 4095
  };

  private static final int CODE_CLEAR = 256;

  private static final int CODE_EOI = 257;

  private static final int CODE_FIRST = 258;

  private int bitsToGet = 9;

  private int currentIndex = 1;

  private byte[] currentString = Bytes.EMPTY_ARRAY;

  private final InputStream in;

  private int maxStringIndex;

  private int nextBits = 0;

  private int nextData = 0;

  private int oldCode;

  private byte[][] stringTable;

  public TiffLzwInputStream(final InputStream in) {
    this.in = in;

    initializeTable();
  }

  private byte[] appendChar(final byte oldString[], final byte newChar) {
    final int length = oldString.length;
    final byte[] newString = new byte[length + 1];
    System.arraycopy(oldString, 0, newString, 0, length);
    newString[length] = newChar;
    return newString;
  }

  private int getNextCode() throws IOException {
    int nextByte = this.in.read();
    if (nextByte == -1) {
      return CODE_EOI;
    }
    this.nextData = this.nextData << 8 | nextByte;
    this.nextBits += 8;

    if (this.nextBits < this.bitsToGet) {
      nextByte = this.in.read();
      if (nextByte == -1) {
        return CODE_EOI;
      }
      this.nextData = this.nextData << 8 | nextByte;
      this.nextBits += 8;
    }

    final int code = this.nextData >> this.nextBits - this.bitsToGet & andTable[this.bitsToGet - 9];
    this.nextBits -= this.bitsToGet;

    return code;
  }

  private void initializeTable() {
    this.stringTable = new byte[4096][];
    for (int i = 0; i < CODE_CLEAR; i++) {
      this.stringTable[i] = new byte[1];
      this.stringTable[i][0] = (byte)i;
    }

    this.maxStringIndex = CODE_FIRST;
    this.bitsToGet = 9;
  }

  private byte[] newString(final byte[] oldString, final byte newChar) {
    final byte[] newString = appendChar(oldString, newChar);

    this.stringTable[this.maxStringIndex++] = newString;

    if (this.maxStringIndex == 511) {
      this.bitsToGet = 10;
    } else if (this.maxStringIndex == 1023) {
      this.bitsToGet = 11;
    } else if (this.maxStringIndex == 2047) {
      this.bitsToGet = 12;
    }
    return newString;
  }

  @Override
  public int read() throws IOException {

    if (this.currentIndex >= this.currentString.length) {

      int code = getNextCode();

      if (code == CODE_EOI) {
        return -1;
      } else {
        if (code == CODE_CLEAR) {
          initializeTable();
          code = getNextCode();
          if (code == CODE_EOI) {
            return -1;
          }
          setCurrentString(code, this.stringTable[code]);
        } else {
          if (code < this.maxStringIndex) {
            final byte[] oldString = this.stringTable[this.oldCode];
            final byte[] string = this.stringTable[code];
            newString(oldString, string[0]);
            setCurrentString(code, string);
          } else {
            final byte[] oldString = this.stringTable[this.oldCode];
            final byte[] string = newString(oldString, oldString[0]);
            setCurrentString(code, string);
          }
        }
      }

    }
    return Byte.toUnsignedInt(this.currentString[this.currentIndex++]);

  }

  public void setCurrentString(final int code, final byte[] string) {
    this.oldCode = code;
    this.currentString = string;
    this.currentIndex = 0;
  }
}
