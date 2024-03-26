package com.nice.wallpaperapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.nice.wallpaperapp.R
import com.nice.wallpaperapp.SetImgActivity
import com.nice.wallpaperapp.adapter.PagerAdapter
import com.nice.wallpaperapp.model.InfoModel
import com.nice.wallpaperapp.model.WallpaperModel

class ViewPagerFragment(private val wallpaperModel: WallpaperModel) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mode: List<InfoModel> = wallpaperModel.infoModel.subList(1, 5)
//        for (i in mode) {
//            Log.e("Data", i.preUrl)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_recyclerview, container, false)

        // 将RecyclerView绑定fragment
        val infoRecyclerView: RecyclerView = view.findViewById(R.id.info_recycler_view)

//        infoRecyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
        infoRecyclerView.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)

        // 创建适配器
        val pagerAdapter: PagerAdapter = PagerAdapter(
            requireContext(),
            wallpaperModel,
            object : PagerAdapter.OnItemClickListener {
                /**
                 * 单个图片item点击事件
                 */
                override fun onItemClick(position: Int, infoModel: InfoModel) {
                    // 跳转详情页面并传递该页面的参数
                    val intent = Intent(requireContext(), SetImgActivity::class.java)
                    intent.putExtra("KEY_EXTRA", infoModel)
                    startActivity(intent)
                }
            })

        infoRecyclerView.adapter = pagerAdapter

        return view
    }

}