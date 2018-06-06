package com.haha.zy.audio.reader.wav;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BaseWAVFileReader {
    private DataInputStream mDataInputStream;
    private WavFileHeader mWavFileHeader;

    public boolean openFile(String filePath)
            throws IOException {
        if (this.mDataInputStream != null) {
            closeFile();
        }
        this.mDataInputStream = new DataInputStream(new FileInputStream(filePath));
        return readHeader();
    }

    public void closeFile()
            throws IOException {
        if (this.mDataInputStream != null) {
            this.mDataInputStream.close();
            this.mDataInputStream = null;
        }
    }

    public WavFileHeader getmWavFileHeader() {
        return this.mWavFileHeader;
    }

    public int readData(byte[] buffer, int offset, int count) {
        if ((this.mDataInputStream == null) || (this.mWavFileHeader == null)) {
            return -1;
        }
        try {
            int nbytes = this.mDataInputStream.read(buffer, offset, count);
            if (nbytes == -1) {
                return 0;
            }
            return nbytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean readHeader() {
        if (this.mDataInputStream == null) {
            return false;
        }
        WavFileHeader header = new WavFileHeader();

        byte[] intValue = new byte[4];
        byte[] shortValue = new byte[2];
        try {
            header.mChunkID = "" +

                    ((char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte());
            System.out.println("Read file chunkID:" + header.mChunkID);

            this.mDataInputStream.read(intValue);
            header.mChunkSize = byteArrayToInt(intValue);

            header.mFormat = "" +

                    ((char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte());

            header.mSubChunk1ID = "" +

                    ((char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte());

            this.mDataInputStream.read(intValue);
            header.mSubChunk1Size = byteArrayToInt(intValue);

            this.mDataInputStream.read(shortValue);
            header.mAudioFormat = byteArrayToShort(shortValue);

            this.mDataInputStream.read(shortValue);
            header.mNumChannel = byteArrayToShort(shortValue);

            this.mDataInputStream.read(intValue);
            header.mSampleRate = byteArrayToInt(intValue);

            this.mDataInputStream.read(intValue);
            header.mBiteRate = (byteArrayToInt(intValue) / 100);

            this.mDataInputStream.read(shortValue);
            header.mBlockAlign = byteArrayToShort(shortValue);

            this.mDataInputStream.read(shortValue);
            header.mBitsPerSample = byteArrayToShort(shortValue);

            header.mSubChunk2ID = "" +

                    ((char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte());

            this.mDataInputStream.read(intValue);
            header.mSubChunk2Size = byteArrayToInt(intValue);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        this.mWavFileHeader = header;

        return true;
    }

    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
