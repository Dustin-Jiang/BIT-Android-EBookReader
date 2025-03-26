package top.tsukino.ebookreader.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import top.tsukino.ebookreader.view.CONTAINER_POSITION
import top.tsukino.ebookreader.view.ViewFragment

object FragmentUtils {
    /**
     * 克隆Fragment并恢复其状态
     * @param fragment 要克隆的Fragment
     * @param fragmentManager Fragment管理器
     * @return 克隆后的新Fragment
     */
    fun <T : ViewFragment> cloneFragmentTo(fragment: T, fragmentManager: FragmentManager, position: Int = 1): T {
        // 保存Fragment状态
        val savedState = fragmentManager.saveFragmentInstanceState(fragment)
        
        // 获取Fragment的Class
        val fragmentClass = fragment.javaClass
        
        // 创建新的Fragment实例
        val newFragment = fragmentClass.newInstance()
        
        // 克隆并设置参数
        fragment.arguments?.let { args ->
            newFragment.arguments = args.clone() as Bundle
        }

        // 恢复保存的状态
        newFragment.setInitialSavedState(savedState)

        newFragment.requireArguments().putInt(CONTAINER_POSITION, position)

        return newFragment
    }
} 