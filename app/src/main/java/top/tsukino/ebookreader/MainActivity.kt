package top.tsukino.ebookreader

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.color.DynamicColors
import top.tsukino.ebookreader.databinding.ActivityMainBinding
import android.content.res.Configuration
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import top.tsukino.ebookreader.util.FragmentUtils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var isWideScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // 应用动态颜色
        DynamicColors.applyToActivityIfAvailable(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            insets
        }

        // 检查是否为宽屏设备
        isWideScreen = resources.configuration.screenWidthDp >= 600

        if (isWideScreen) {
            setupWideScreenLayout()
        } else {
            setupNormalLayout()
        }
    }

    private fun setupWideScreenLayout() {
        // 初始化左侧导航
        val leftNavHostFragment = supportFragmentManager.findFragmentById(R.id.left_container) as NavHostFragment
        navController = leftNavHostFragment.navController
    }

    private fun setupNormalLayout() {
        // 初始化导航组件
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val newIsWideScreen = newConfig.screenWidthDp >= 600
        if (newIsWideScreen != isWideScreen) {
            isWideScreen = newIsWideScreen
            recreate()
        }
    }

    fun navigateToFragment(fragment: Fragment) {
        if (isWideScreen) {
            // 在宽屏模式下，将当前右侧Fragment移动到左侧，新Fragment放在右侧
            val currentRightFragment = supportFragmentManager.findFragmentById(R.id.right_container)
            if (currentRightFragment != null) {
                // 使用工具类克隆Fragment并恢复状态
                val newFragment = FragmentUtils.cloneFragment(currentRightFragment, supportFragmentManager)

                // 将Fragment添加到左侧
                supportFragmentManager.beginTransaction()
                    .replace(R.id.left_container, newFragment)
                    .commit()
            }
            
            // 将新Fragment添加到右侧
            supportFragmentManager.beginTransaction()
                .replace(R.id.right_container, fragment)
                .commit()
        } else {
            // 在普通模式下，使用正常的导航
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}