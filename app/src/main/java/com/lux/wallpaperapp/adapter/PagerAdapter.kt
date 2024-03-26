package com.lux.wallpaperapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.lux.wallpaperapp.R
import com.lux.wallpaperapp.model.InfoModel
import com.lux.wallpaperapp.model.WallpaperModel
import com.lux.wallpaperapp.tools.OffSSLTool
import java.io.InputStream

class PagerAdapter(
    private val context: Context, model: WallpaperModel, private val listener: OnItemClickListener
) : RecyclerView.Adapter<PagerAdapter.ThumbVH>() {

    private val infoModels = model.infoModel

    /**
     * 点击单个item的回调函数
     */
    interface OnItemClickListener {
        fun onItemClick(position: Int, infoModel: InfoModel)
    }

    inner class ThumbVH(view: View) : RecyclerView.ViewHolder(view) {

        // 获取需要渲染图片的item
        val imgItemView: ImageView = itemView.findViewById(R.id.image_item)

        // 获取图片根节点
        val rootItemLayout = itemView.findViewById<LinearLayout>(R.id.root_layout)

        /**
         * 调用库加载图片
         *
         * 加载指定 URL 的图像到指定的 ImageView 中。
         * @param context 上下文对象，用于获取 Picasso 实例。
         * @param preUrl 要加载的图像的 URL。
         * @param imageViewThumb 要显示图像的 ImageView。
         */
        fun loadItemImg(context: Context, preUrl: String, imageViewThumb: ImageView) {
            // 解决glide加载https证书问题
            try {
                Glide.get(context).registry.replace<GlideUrl, InputStream>(
                    GlideUrl::class.java, InputStream::class.java,
                    OkHttpUrlLoader.Factory(OffSSLTool.getSSLOkHttpClient())
                )
                Glide.with(context)
                    .load(preUrl)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    // 加载占位图
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.img_loading)
                    )
                    // 淡入动画
                    .transition(DrawableTransitionOptions.withCrossFade())
                    // 加载失败占位图
                    .error(R.drawable.img_loading_err)
                    .into(imageViewThumb)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 绑定item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerAdapter.ThumbVH {
        val view = LayoutInflater.from(context).inflate(R.layout.img_item_view2, parent, false)
        return ThumbVH(view)
    }

    /**
     * 绑定渲染到图片item
     *
     * 360x460
     */
    override fun onBindViewHolder(holder: PagerAdapter.ThumbVH, position: Int) {
        val infoModel = infoModels[position % infoModels.size]

        holder.loadItemImg(context, infoModel.preUrl, holder.imgItemView)

        // 单个图片的根布局添加点击事件
        holder.rootItemLayout.setOnClickListener() {
            listener.onItemClick(position, infoModel)
        }
    }

    /**
     * 渲染图片item的数量
     */
    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }
}
