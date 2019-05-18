package com.lyf.socket;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * From :   com.fasterxml.jackson.core.util.ByteArrayBuilder
 *          com.fasterxml.jackson.core.util.BufferRecycler
 */
public final class ByteArrayBuilder extends OutputStream {
    public static final byte[] NO_BYTES = new byte[0];
    private static final int INITIAL_BLOCK_SIZE = 500;
    private static final int MAX_BLOCK_SIZE = 262144;
    static final int DEFAULT_BLOCK_ARRAY_SIZE = 40;
    private final BufferRecycler _bufferRecycler;
    private final LinkedList<byte[]> _pastBlocks;
    private int _pastLen;
    private byte[] _currBlock;
    private int _currBlockPtr;

    public ByteArrayBuilder() {
        this((BufferRecycler) null);
    }

    public ByteArrayBuilder(BufferRecycler br) {
        this(br, 500);
    }

    public ByteArrayBuilder(int firstBlockSize) {
        this((BufferRecycler) null, firstBlockSize);
    }

    public ByteArrayBuilder(BufferRecycler br, int firstBlockSize) {
        this._pastBlocks = new LinkedList();
        this._bufferRecycler = br;
        this._currBlock = br == null ? new byte[firstBlockSize] : br.allocByteBuffer(2);
    }

    public void reset() {
        this._pastLen = 0;
        this._currBlockPtr = 0;
        if (!this._pastBlocks.isEmpty()) {
            this._pastBlocks.clear();
        }

    }

    public int size() {
        return this._pastLen + this._currBlockPtr;
    }

    public void release() {
        this.reset();
        if (this._bufferRecycler != null && this._currBlock != null) {
            this._bufferRecycler.releaseByteBuffer(2, this._currBlock);
            this._currBlock = null;
        }

    }

    public void append(int i) {
        if (this._currBlockPtr >= this._currBlock.length) {
            this._allocMore();
        }

        this._currBlock[this._currBlockPtr++] = (byte) i;
    }

    public void appendTwoBytes(int b16) {
        if (this._currBlockPtr + 1 < this._currBlock.length) {
            this._currBlock[this._currBlockPtr++] = (byte) (b16 >> 8);
            this._currBlock[this._currBlockPtr++] = (byte) b16;
        } else {
            this.append(b16 >> 8);
            this.append(b16);
        }

    }

    public void appendThreeBytes(int b24) {
        if (this._currBlockPtr + 2 < this._currBlock.length) {
            this._currBlock[this._currBlockPtr++] = (byte) (b24 >> 16);
            this._currBlock[this._currBlockPtr++] = (byte) (b24 >> 8);
            this._currBlock[this._currBlockPtr++] = (byte) b24;
        } else {
            this.append(b24 >> 16);
            this.append(b24 >> 8);
            this.append(b24);
        }

    }

    public void appendFourBytes(int b32) {
        if (this._currBlockPtr + 3 < this._currBlock.length) {
            this._currBlock[this._currBlockPtr++] = (byte) (b32 >> 24);
            this._currBlock[this._currBlockPtr++] = (byte) (b32 >> 16);
            this._currBlock[this._currBlockPtr++] = (byte) (b32 >> 8);
            this._currBlock[this._currBlockPtr++] = (byte) b32;
        } else {
            this.append(b32 >> 24);
            this.append(b32 >> 16);
            this.append(b32 >> 8);
            this.append(b32);
        }

    }

    public byte[] toByteArray() {
        int totalLen = this._pastLen + this._currBlockPtr;
        if (totalLen == 0) {
            return NO_BYTES;
        } else {
            byte[] result = new byte[totalLen];
            int offset = 0;

            int len;
            for (Iterator var4 = this._pastBlocks.iterator(); var4.hasNext(); offset += len) {
                byte[] block = (byte[]) var4.next();
                len = block.length;
                System.arraycopy(block, 0, result, offset, len);
            }

            System.arraycopy(this._currBlock, 0, result, offset, this._currBlockPtr);
            offset += this._currBlockPtr;
            if (offset != totalLen) {
                throw new RuntimeException("Internal error: total len assumed to be " + totalLen + ", copied " + offset + " bytes");
            } else {
                if (!this._pastBlocks.isEmpty()) {
                    this.reset();
                }

                return result;
            }
        }
    }

    public byte[] resetAndGetFirstSegment() {
        this.reset();
        return this._currBlock;
    }

    public byte[] finishCurrentSegment() {
        this._allocMore();
        return this._currBlock;
    }

    public byte[] completeAndCoalesce(int lastBlockLength) {
        this._currBlockPtr = lastBlockLength;
        return this.toByteArray();
    }

    public byte[] getCurrentSegment() {
        return this._currBlock;
    }

    public void setCurrentSegmentLength(int len) {
        this._currBlockPtr = len;
    }

    public int getCurrentSegmentLength() {
        return this._currBlockPtr;
    }

    public void write(byte[] b) {
        this.write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) {
        while (true) {
            int max = this._currBlock.length - this._currBlockPtr;
            int toCopy = Math.min(max, len);
            if (toCopy > 0) {
                System.arraycopy(b, off, this._currBlock, this._currBlockPtr, toCopy);
                off += toCopy;
                this._currBlockPtr += toCopy;
                len -= toCopy;
            }

            if (len <= 0) {
                return;
            }

            this._allocMore();
        }
    }

    public void write(int b) {
        this.append(b);
    }

    public void close() {
    }

    public void flush() {
    }

    private void _allocMore() {
        int newPastLen = this._pastLen + this._currBlock.length;
        if (newPastLen < 0) {
            throw new IllegalStateException("Maximum Java array size (2GB) exceeded by `ByteArrayBuilder`");
        } else {
            this._pastLen = newPastLen;
            int newSize = Math.max(this._pastLen >> 1, 1000);
            if (newSize > 262144) {
                newSize = 262144;
            }

            this._pastBlocks.add(this._currBlock);
            this._currBlock = new byte[newSize];
            this._currBlockPtr = 0;
        }
    }
}

class BufferRecycler {
    public static final int BYTE_READ_IO_BUFFER = 0;
    public static final int BYTE_WRITE_ENCODING_BUFFER = 1;
    public static final int BYTE_WRITE_CONCAT_BUFFER = 2;
    public static final int BYTE_BASE64_CODEC_BUFFER = 3;
    public static final int CHAR_TOKEN_BUFFER = 0;
    public static final int CHAR_CONCAT_BUFFER = 1;
    public static final int CHAR_TEXT_BUFFER = 2;
    public static final int CHAR_NAME_COPY_BUFFER = 3;
    private static final int[] BYTE_BUFFER_LENGTHS = new int[]{8000, 8000, 2000, 2000};
    private static final int[] CHAR_BUFFER_LENGTHS = new int[]{4000, 4000, 200, 200};
    protected final byte[][] _byteBuffers;
    protected final char[][] _charBuffers;

    public BufferRecycler() {
        this(4, 4);
    }

    protected BufferRecycler(int bbCount, int cbCount) {
        this._byteBuffers = new byte[bbCount][];
        this._charBuffers = new char[cbCount][];
    }

    public final byte[] allocByteBuffer(int ix) {
        return this.allocByteBuffer(ix, 0);
    }

    public byte[] allocByteBuffer(int ix, int minSize) {
        int DEF_SIZE = this.byteBufferLength(ix);
        if (minSize < DEF_SIZE) {
            minSize = DEF_SIZE;
        }

        byte[] buffer = this._byteBuffers[ix];
        if (buffer != null && buffer.length >= minSize) {
            this._byteBuffers[ix] = null;
        } else {
            buffer = this.balloc(minSize);
        }

        return buffer;
    }

    public void releaseByteBuffer(int ix, byte[] buffer) {
        this._byteBuffers[ix] = buffer;
    }

    public final char[] allocCharBuffer(int ix) {
        return this.allocCharBuffer(ix, 0);
    }

    public char[] allocCharBuffer(int ix, int minSize) {
        int DEF_SIZE = this.charBufferLength(ix);
        if (minSize < DEF_SIZE) {
            minSize = DEF_SIZE;
        }

        char[] buffer = this._charBuffers[ix];
        if (buffer != null && buffer.length >= minSize) {
            this._charBuffers[ix] = null;
        } else {
            buffer = this.calloc(minSize);
        }

        return buffer;
    }

    public void releaseCharBuffer(int ix, char[] buffer) {
        this._charBuffers[ix] = buffer;
    }

    protected int byteBufferLength(int ix) {
        return BYTE_BUFFER_LENGTHS[ix];
    }

    protected int charBufferLength(int ix) {
        return CHAR_BUFFER_LENGTHS[ix];
    }

    protected byte[] balloc(int size) {
        return new byte[size];
    }

    protected char[] calloc(int size) {
        return new char[size];
    }
}
