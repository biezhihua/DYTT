package com.bzh.dytt.ui.detail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bzh.dytt.AppExecutors
import com.bzh.dytt.R
import com.bzh.dytt.base.BaseFragment
import com.bzh.dytt.databinding.DetailFragmentBinding
import com.bzh.dytt.databinding.ItemDetailBinding
import com.bzh.dytt.di.Injectable
import com.bzh.dytt.util.ThunderHelper
import com.bzh.dytt.util.autoCleared
import com.bzh.dytt.vo.MovieDetail
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import kotlinx.android.synthetic.main.detail_fragment.*
import javax.inject.Inject

class DetailFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: DetailViewModel

    private lateinit var adapter: DetailAdapter

    private var thunderHelper: ThunderHelper = ThunderHelper()

    private var binding by autoCleared<DetailFragmentBinding>()

    private var refreshObserver: Observer<Boolean> = Observer {
        if (it == true) {
            swipe_refresh_layout.isEnabled = true
            swipe_refresh_layout.isRefreshing = true
        } else {
            swipe_refresh_layout.isEnabled = false
            swipe_refresh_layout.isRefreshing = false
        }
    }

    private fun getSimpleTitle(name: String): String {
        if (name.isNotEmpty()) {
            val startIndex = name.indexOf("《")
            val lastIndex = name.lastIndexOf("》")
            if (startIndex != -1 && lastIndex != -1) {
                return name.substring(startIndex + 1, lastIndex)
            }
        }
        return name
    }

    private var detailListObserver: Observer<List<String>> = Observer {
        adapter.submitList(it)
    }

    fun onClickDownload(movieDetail: MovieDetail) {
        val url = movieDetail.downloadUrl?.split(";".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray().orEmpty()
        LovelyChoiceDialog(activity)
                .setTopColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.ic_download_white)
                .setItemsMultiChoice(url) { _, items ->
                    if (!thunderHelper.checkIsInstall(activity)) {
                        val dialogFragment = InnerDialogFragment()
                        dialogFragment.show(activity?.supportFragmentManager!!, "InnerDialog")
                    } else {
                        for (item in items) {
                            thunderHelper.startThunder(activity, item)
                        }
                    }

                }
                .setConfirmButtonText(R.string.ok)
                .show()
    }

    override fun doCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.detail_fragment, container, false)

        binding.setLifecycleOwner(this)
        binding.actionBar = (activity as AppCompatActivity).supportActionBar

        viewModel = viewModelFactory.create(DetailViewModel::class.java)
        lifecycle.addObserver(viewModel)
        binding.viewModel = viewModel
        binding.detailFragment = this

        return binding.root
    }

    override fun doViewCreated(view: View, savedInstanceState: Bundle?) {
        super.doViewCreated(view, savedInstanceState)

        if (arguments != null) {
            val movieDetail = arguments?.getParcelable<MovieDetail>(MOVIE_DETAIL)
            viewModel.paramsLiveData.value = movieDetail
            if (movieDetail != null && activity != null) {
                val actionBar = (activity as AppCompatActivity).supportActionBar
                actionBar?.title = movieDetail.name?.let { getSimpleTitle(it) }
            }
        }

        adapter = DetailAdapter(appExecutors)
        binding.detailRecyclerView.adapter = adapter

        viewModel.swipeRefreshStatus.observe(this, refreshObserver)
        viewModel.detailList.observe(this, detailListObserver)
    }

    inner class DetailAdapter constructor(appExecutors: AppExecutors) : ListAdapter<String, DetailItemHolder>(AsyncDifferConfig
            .Builder<String>(object : DiffUtil.ItemCallback<String>() {

                override fun areContentsTheSame(p0: String, p1: String): Boolean {
                    return p0 == p1
                }

                override fun areItemsTheSame(p0: String, p1: String): Boolean {
                    return p0 == p1
                }

            })
            .setBackgroundThreadExecutor(appExecutors.diskIO())
            .build()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DetailItemHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_detail,
                        parent,
                        false
                ))

        override fun onBindViewHolder(holder: DetailItemHolder, position: Int) {
            getItem(position).let {
                with(holder) {
                    bind(it)
                }
            }
        }
    }

    inner class DetailItemHolder(val binding: ItemDetailBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(content: String) {
            binding.content.text = content
        }
    }

    companion object {

        private const val TAG = "DetailFragment"

        const val MOVIE_DETAIL = "MOVIE_DETAIL"

        fun newInstance(movieDetail: MovieDetail): DetailFragment {
            val args = Bundle()
            args.putParcelable(MOVIE_DETAIL, movieDetail)
            val fragment = DetailFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

class InnerDialogFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(R.string.un_install_xunlei_label).setPositiveButton(R.string.ok, null)
        return builder.create()
    }
}