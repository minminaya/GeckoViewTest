package com.minminaya.geckoviewtest

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.minminaya.geckoviewtest.databinding.FragmentFirstBinding
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.ContentDelegate
import org.mozilla.geckoview.GeckoView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var count = AtomicInteger(0)
    private var geckoViewList = mutableListOf<GeckoView>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (sRuntime == null) {
            // GeckoRuntime can only be initialized once per process
            sRuntime = GeckoRuntime.create(requireContext())
        }
        for (i in 1..5) {
            val geckoView = GeckoView(requireContext())
            geckoViewList.add(geckoView)
//        val layoutParams = geckoView.layoutParams
//        layoutParams.width = 1080
//        layoutParams.height = 1920
//        geckoView.measure(
//            View.MeasureSpec.makeMeasureSpec(
//                1080,
//                View.MeasureSpec.EXACTLY
//            ), View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
//        )

            val lp = FrameLayout.LayoutParams(1080, 1920)
            _binding?.rootView?.addView(geckoView, lp)
            val session = GeckoSession()
            // Workaround for Bug 1758212
            session.contentDelegate = object : ContentDelegate {}

            session.open(sRuntime!!)
            geckoView.setSession(session)
            thread {
                session.loadUri("http://fangyanhua.top/")
            }
        }
//        session.loadUri("https://www.baidu.com/")
//        session.loadUri("about:buildconfig")

//        geckoView.visibility = View.INVISIBLE
        binding.buttonFirst.setOnClickListener {
//            thread {
//
//            }
            for (i in 1..5) {
                geckoViewList.forEachIndexed { index, geckoView ->
                    capture(geckoView, index)
                }
            }
        }
    }

    private fun capture(geckoView: GeckoView, geckoViewIndex: Int) {
        val time = measureTimeMillis {
            val capturePixels = geckoView.capturePixels()
            thread {
                val bitmap: Bitmap?
                val pollTime = measureTimeMillis {
                    bitmap = capturePixels.poll()
                }
                Log.d(
                    TAG,
                    "geckoView:$geckoViewIndex, pollTime:${pollTime} ms,currentTimeMillis:${System.currentTimeMillis()}"
                )
                count.addAndGet(1)
                val path =
                    "${context?.getExternalFilesDir(null)?.absolutePath}/bitmap-view$geckoViewIndex-${count.get()}.png"
                thread {
                    val saveBitmapTime = measureTimeMillis {
                        saveBitmap(File(path), bitmap!!, geckoViewIndex)
                    }
                    Log.d(TAG, "geckoView:$geckoViewIndex, saveBitmapTime:${saveBitmapTime} ms ")
                }
                if (count.get() == 1) {
                    start = System.currentTimeMillis()
                }

                if (count.get() == 25) {
                    Log.d(TAG, "总时间:${System.currentTimeMillis() - start} ms ")
                }
            }
        }
        Log.d(TAG, "geckoView:$geckoViewIndex,createBitmap:${time} ms ")
    }

    private var start: Long = 0L

    private fun saveBitmap(saveFile: File, bm: Bitmap, geckoViewIndex: Int): Boolean {
        return try {
            FileOutputStream(saveFile).use {
                Log.i(
                    TAG,
                    "geckoView:$geckoViewIndex,save bitmap success, path = ${saveFile.absolutePath}"
                )
                bm.compress(Bitmap.CompressFormat.PNG, 100, it)
                it.flush()
            }
            true
        } catch (ex: IOException) {
            ex.printStackTrace()
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "lgmlgm"
        private var sRuntime: GeckoRuntime? = null
    }
}