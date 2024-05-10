package net.mfuertes.aac.helpers

import android.util.Log
import java.net.ServerSocket
import java.net.Socket

class ProxyThread(private val ip: String, private val callback: (() -> Unit)?) : Thread() {

    private var mReceiverConnected = false
    private var mSenderConnected = false
    private var mRunning = false

    private lateinit var receiverSocket: Socket
    private lateinit var senderSocket: Socket

    fun exit() {
        mRunning = false
    }

    override fun run() {
        serverThread.start()
        clientThread.start()
        callback?.invoke()
        serverThread.join()
        clientThread.join()
    }

    private var serverThread = Thread {
        try {
            val buffer = ByteArray(16384)
            var len: Int
            val serverSocket = ServerSocket(5288)
            receiverSocket = serverSocket.accept()

            mReceiverConnected = true
            while (!mSenderConnected) {
                sleep(100)
            }

            val receiverOutputStream = receiverSocket.getOutputStream()
            val senderInputStream = senderSocket.getInputStream()

            mRunning = true;
            while (mRunning) {
                len = senderInputStream.read(buffer)
                receiverOutputStream.write(buffer.copyOf(len))
            }
        } catch (ex: Exception) {
            mRunning = false
        }
    }

    private var clientThread = Thread {
        try {
            val buffer = ByteArray(16384)
            var len: Int
            senderSocket = Socket(ip, 5288)
            mSenderConnected = true

            while (!mReceiverConnected) {
                sleep(100)
            }

            val receiverInputStream = receiverSocket.getInputStream()
            val senderOutputStream = senderSocket.getOutputStream()

            mRunning = true
            while (mRunning) {
                len = receiverInputStream.read(buffer)
                senderOutputStream.write(buffer.copyOf(len))
            }
        } catch (ex: Exception) {
            mRunning = false
        }

    }
}