package com.lux.wallpaperapp.model

import java.io.Serializable
class WallpaperModel(
    var name: String, var infoModel: List<InfoModel>
) : Serializable {}