package com.fatkhun.etemu

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.fatkhun.core.helper.PermissionHelper
import com.fatkhun.core.model.LostFoundForm
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.model.PostingUpdateForm
import com.fatkhun.core.ui.BaseActivity
import com.fatkhun.core.ui.PagingLoadStateAdapter
import com.fatkhun.core.utils.AlertDialogInterface
import com.fatkhun.core.utils.Constant
import com.fatkhun.core.utils.RC
import com.fatkhun.core.utils.RemoteCallback
import com.fatkhun.core.utils.dialogAlertOneButton
import com.fatkhun.core.utils.gone
import com.fatkhun.core.utils.handleApiCallback
import com.fatkhun.core.utils.logError
import com.fatkhun.core.utils.showCustomDialog
import com.fatkhun.core.utils.showing
import com.fatkhun.core.utils.stoped
import com.fatkhun.core.utils.toJson
import com.fatkhun.etemu.adapter.HistoryPagingAdapter
import com.fatkhun.etemu.databinding.ActivityHistoryBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private var isNewCreated = true
    private var searchTextInput = ""
    private var searchCategory = ""
    private var searchStatus = ""
    private var searchType = ""

    private val historyPagingAdapter: HistoryPagingAdapter by lazy {
        HistoryPagingAdapter(this, object: HistoryPagingAdapter.Callback{
            override fun onClickItem(
                pos: Int,
                item: LostFoundItemList
            ) {
                startActivity(Intent(this@HistoryActivity, DetailPostingActivity::class.java).apply {
                    putExtra("detail",item.toJson())
                    putExtra("is_claimed", if (item.status.lowercase() == "claimed") 1 else 0)
                    putExtra("is_history", true)
                })
            }

            override fun onClickDone(
                pos: Int,
                item: LostFoundItemList
            ) {
                showCustomDialog(this@HistoryActivity,
                    "Apakah ingin menyelesaikan laporan?",
                    "Ubah status menjadi complete untuk menyelesaikan laporan",
                    "Iya, Lanjutkan",
                    "Batal",
                    false,
                    object : AlertDialogInterface {
                        override fun onPositiveButtonClicked() {
                            mainVM.updatePostingItem(storeDataHelper.getAuthToken(), item._id,
                                PostingUpdateForm(status = "claimed")).observe(this@HistoryActivity) { response ->
                                handleApiCallback(
                                    this@HistoryActivity,
                                    response,
                                    true,
                                    object : RemoteCallback<String> {
                                        override fun do_callback(id: Int, t: String) {}
                                        override fun failed_callback(id: Int, t: String) {}
                                    }) { res, code ->
                                    if (code == RC().SUCCESS) {
                                        onBackPressed()
                                    } else {
                                        res?.let {
                                            dialogAlertOneButton(
                                                this@HistoryActivity,
                                                com.fatkhun.core.R.drawable.ic_ilus_general_warning,
                                                it.message,
                                                "",
                                                "Mengerti"
                                            ) {
                                                it.dismiss()
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        override fun onNegativeButtonClicked() {}
                    })
            }

        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.tvTitleToolbar.text = "Riwayatmu"
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.swipeLayout.setOnRefreshListener {
            isNewCreated = true
            setupObserve(searchTextInput, searchCategory, searchStatus, searchType)
            binding.swipeLayout.isRefreshing = false
        }
        binding.rvListItem.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvListItem.setHasFixedSize(true)
        binding.rvListItem.adapter = historyPagingAdapter.withLoadStateFooter(
            footer = PagingLoadStateAdapter(historyPagingAdapter)
        )
        // 1) Observe stream Paging SEKALI
        mainVM.pagingHistory.observe(this) { pagingData ->
            historyPagingAdapter.submitData(lifecycle, pagingData)
        }
        historyPagingAdapter.addLoadStateListener {
            val refresher = it.refresh
            val displayEmpty = (refresher is LoadState.NotLoading && historyPagingAdapter.itemCount == 0)
            if (refresher is LoadState.Loading && isNewCreated) binding.lytShimmer.showing()
            if (refresher is LoadState.NotLoading) binding.lytShimmer.stoped()
            if (refresher is LoadState.Error) binding.lytShimmer.stoped()

            logError("Empty View $displayEmpty")
            binding.emptyView.isVisible = displayEmpty
            binding.rvListItem.isVisible = !displayEmpty && !binding.lytShimmer.isVisible
        }
        network.observe(this) {
            when (it?.isConnected) {
                true -> {
                    setupObserve(searchTextInput, searchCategory, searchStatus, searchType)
                }

                else -> {}
            }
        }
    }

    private fun setupObserve(searchText: String, categoryId: String, status: String, type: String) {
        if (isNewCreated) getItemList(searchText, categoryId, status, type)
    }

    private fun getItemList(search: String, categoryId: String, status: String, type: String) {
        val form = LostFoundForm(
            keyword = search,
            category_id = categoryId,
            status = status,
            type = type,
            limit = Constant.PAGE_HALF_SIZE,
            offset = 0
        )
        mainVM.submitKeyHistory(storeDataHelper.getAuthToken(), form)
        // Scroll ke atas hanya untuk query baru
        lifecycleScope.launch {
            delay(300)
            if (isNewCreated) binding.rvListItem.smoothScrollToPosition(0)
        }
    }
}