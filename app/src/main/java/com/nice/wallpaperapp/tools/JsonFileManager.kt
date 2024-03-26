package com.nice.wallpaperapp.tools

import com.nice.wallpaperapp.model.InfoModel
import com.nice.wallpaperapp.model.WallpaperModel
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import java.nio.charset.StandardCharsets

object JsonFileManager {

    /**
     * 将给定的 JSON 字符串转换为 WallpaperModel 对象的列表。
     *
     * @param jsonString 要转换的 JSON 字符串。
     * @return 转换后的 WallpaperModel 对象列表。
     */
    @JvmStatic
    fun formatJsonStr(jsonString: String): List<WallpaperModel> {
        val data = mutableListOf<WallpaperModel>()
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val any = array.getJSONObject(i)
            val title = any.getString("name")
            val jsonArray = any.getJSONArray("data")
            val infoModels = mutableListOf<InfoModel>()
            for (k in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(k)
                val preview = jsonObject.getString("previewThumb")
                val original = jsonObject.getString("original")
//                val links = jsonObject.getJSONObject("links")
                val source = jsonObject.getString("source")
                infoModels.add(InfoModel(preview, original, source))
            }
            data.add(WallpaperModel(title, infoModels))
        }
        return data
    }

    /**
     * 打开文件
     */
    @JvmStatic
    fun openFile(open: InputStream): String {
        val stringWriter = StringWriter()
        val charArray = CharArray(open.available())
        var l = 0;
        val bufReader = BufferedReader(InputStreamReader(open, StandardCharsets.UTF_8))
        while (bufReader.read(charArray).also {
                l = it
            } != -1) {
            stringWriter.write(charArray, 0, l)
        }
        return stringWriter.toString()
    }

}