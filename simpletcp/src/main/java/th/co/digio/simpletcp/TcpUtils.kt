package th.co.digio.simpletcp

import android.content.Context
import android.net.wifi.WifiManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.util.regex.Pattern

/**
 * @author supitsara
 */
object TcpUtils {
    private val PARTIAL_IP_ADDRESS = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$")

    @JvmStatic
    fun getIpAddress(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var ip = "192.168.43.1"
        if (!isHotspot(wifiManager)) {
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            ip = (ipAddress and 0xFF).toString() + "." +
                    (ipAddress shr 8 and 0xFF) + "." +
                    (ipAddress shr 16 and 0xFF) + "." +
                    (ipAddress shr 24 and 0xFF)
        }
        return ip
    }

    private fun isHotspot(wifiManager: WifiManager): Boolean {
        val methods = wifiManager.javaClass.declaredMethods
        for (method in methods) {
            if (method.name == "isWifiApEnabled") {
                try {
                    return method.invoke(wifiManager) as Boolean
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }

    @JvmStatic
    fun forceInputIp(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var previousText = ""
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (PARTIAL_IP_ADDRESS.matcher(s).matches()) {
                    previousText = s.toString()
                } else {
                    s.replace(0, s.length, previousText)
                }
            }
        })
    }

    private fun copyTo(inputStream: InputStream, out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
        var bytesCopied: Long = 0
        val buffer = ByteArray(bufferSize)
        var bytes = inputStream.read(buffer)
        while (bytes >= 0) {
            out.write(buffer, 0, bytes)
            bytesCopied += bytes
            bytes = inputStream.read(buffer)
        }
        return bytesCopied
    }

    @JvmStatic
    fun readBytes(inputStream: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream(maxOf(DEFAULT_BUFFER_SIZE, inputStream.available()))
        copyTo(inputStream, buffer)
        return buffer.toByteArray()
    }
}