package com.lux.wallpaperapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.lux.wallpaperapp.databinding.ActivityMainBinding
import com.lux.wallpaperapp.fragment.ViewPagerFragment
import com.lux.wallpaperapp.model.WallpaperModel
import com.lux.wallpaperapp.tools.JsonFileManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fragmentList: ArrayList<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 获取ConnectivityManager实例
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        // 检查网络连接状态
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            // 已连接到互联网
            Log.d("NetworkStatus", "Connected to the Internet")
        } else {
            // 未连接到互联网
            Log.d("NetworkStatus", "Not connected to the Internet")
        }

        // 设置Padding上边距留出沉浸式状态栏空间
        binding.root.setPadding(0, dpCovertPx(this), 0, 0)
        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE) or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT
        }

        // 抽屉布局
        initDrawView()
        // 请求读写权限
        writePermission()

        binding.drawerParent.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                // 设置监听事件防止 Drawer 穿透
                drawerView.isClickable = true
            }

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })

        // 得到wallpaperModelList 并截取 3--15
        val wallpaperModelList: MutableList<WallpaperModel> = mutableListOf()
        wallpaperModelList.addAll(
            JsonFileManager.formatJsonStr(
                JsonFileManager.openFile(assets.open("my_wallpaper.json"))
            )
        )

        // 随机打乱List
        wallpaperModelList.shuffle()
        // 遍历父节点数量创建Tab
        for (i in wallpaperModelList) {
            // 添加
            binding.tabLayout.addTab(
                // 新建自定义的item_tab
                binding.tabLayout.newTab().setCustomView(R.layout.tab_item_view)
            )
        }
        fragmentList = arrayListOf()
        // 遍历赋值Tab名称
        for (i in 0 until binding.tabLayout.tabCount) {
            val tabView = binding.tabLayout.getTabAt(i)?.customView
            if (tabView != null) {
                // 给Tab赋值name
                val wallpaperModel = wallpaperModelList[i]
                val textName = tabView.findViewById<TextView>(R.id.text_wallpaper_name)
                textName.text = wallpaperModel.name

                // 创建viewpager的fragment集合,带 wallpaperModel 参数
                fragmentList.add(ViewPagerFragment(wallpaperModel))
            }
        }

        /**
         * 设置 ViewPager 的 offscreenPageLimit 属性，指定 ViewPager 在当前页面附近应该保留多少个页面。
         * 这样可以提前加载并保留附近的页面，以提高用户体验和流畅度。
         * 通常设置为当前可见页面数量的一半或稍多一点。
         */
        binding.viewpager.offscreenPageLimit = 3
        // 创建 viewpager
        binding.viewpager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            // viewpager的数量
            override fun getCount(): Int {
                return fragmentList.size
            }

            // 绑定fragment
            override fun getItem(position: Int): Fragment {
                return fragmentList[position]
            }

            override fun getPageTitle(position: Int): CharSequence {
                return wallpaperModelList[position].name
            }
        }
        // 将tab已viewpager对应，实现跳转
        binding.tabLayout.setupWithViewPager(binding.viewpager)
    }

    private fun initDrawView() {
        // 绑定抽屉中的GOOGLE按钮
        binding.layoutRate.setOnClickListener() {
            viewUrl()
        }
        // 绑定抽屉中的分享按钮
        binding.layoutShare.setOnClickListener() {
            shareUrl()
        }
        // 绑定抽屉中的版本信息
        val versionName = getVersionName()
        binding.textAppVersion.text = versionName

        // 打开抽屉
        binding.imageMenu.setOnClickListener() {
            binding.drawerParent.openDrawer(GravityCompat.START)
        }

    }

    /**
     * 获取应用程序的版本名称
     * @return 应用程序的版本名称，如果获取失败则返回 null
     */
    private fun getVersionName(): String {
        val pInfo: PackageInfo
        try {
            pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            return ""
        }
        return "Version: " + pInfo.versionName
    }

    /**
     * 打开分享链接
     */
    private fun shareUrl() {
        // 商店中包的位置
        val url = getString(R.string.share_link) + packageName
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(intent)
    }

    /**
     * 打开商店链接
     */
    private fun viewUrl() {
        // 商店中包的位置
        val url = getString(R.string.share_link) + packageName
        // 创建intent打开链接
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        startActivity(intent)
    }

    /**
     * 检查并请求外部存储读取和写入权限（仅适用于 Android 9.0 以下的版本）。
     * 如果权限未被授予，则向用户请求相应权限。
     */
    private fun writePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 请求外部存储权限
                ActivityCompat.requestPermissions(this, permissions, 15)
            }
        }
    }

    /**
     * 自适应设备状态栏高度
     */
    private fun dpCovertPx(context: Context): Int {
        // 获取当前设备的屏幕密度，并赋值给变量 scale
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}