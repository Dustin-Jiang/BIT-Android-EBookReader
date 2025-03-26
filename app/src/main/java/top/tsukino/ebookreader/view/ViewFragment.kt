package top.tsukino.ebookreader.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

const val CONTAINER_POSITION = "container_position"

open class ViewFragment : Fragment() {
    protected val containerPosition: Int get() = arguments?.getInt(CONTAINER_POSITION) ?: 0
}
