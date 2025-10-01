package com.fatkhun.etemu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.fatkhun.core.model.LostFoundForm
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.ui.BaseActivity
import com.fatkhun.core.ui.PagingLoadStateAdapter
import com.fatkhun.core.utils.Constant
import com.fatkhun.core.utils.afterTextChangedDebounce
import com.fatkhun.core.utils.gone
import com.fatkhun.core.utils.hideSoftKeyboard
import com.fatkhun.core.utils.logError
import com.fatkhun.core.utils.showing
import com.fatkhun.core.utils.stoped
import com.fatkhun.core.utils.toJson
import com.fatkhun.etemu.adapter.LostFoundPagingAdapter
import com.fatkhun.etemu.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isNewCreated = true
    private var searchTextInput = ""
    private var searchCategory = ""
    private var searchStatus = ""
    private var searchType = ""

    private val lostFoundPagingAdapter: LostFoundPagingAdapter by lazy {
        LostFoundPagingAdapter(this@MainActivity, object : LostFoundPagingAdapter.Callback{
            override fun onClickItem(
                pos: Int,
                item: LostFoundItemList
            ) {
                isNewCreated = false
                startActivity(Intent(this@MainActivity, DetailPostingActivity::class.java).apply {
                    putExtra("detail",item.toJson())
                })
            }

        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.rvListItem.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvListItem.setHasFixedSize(true)
        binding.rvListItem.adapter = lostFoundPagingAdapter.withLoadStateFooter(
            footer = PagingLoadStateAdapter(lostFoundPagingAdapter)
        )
        // 1) Observe stream Paging SEKALI
        mainVM.pagingLostFound.observe(this) { pagingData ->
            lostFoundPagingAdapter.submitData(lifecycle, pagingData)
        }
        lostFoundPagingAdapter.addLoadStateListener {
            val refresher = it.refresh
            val displayEmpty = (refresher is LoadState.NotLoading && lostFoundPagingAdapter.itemCount == 0)
            if (refresher is LoadState.Loading && isNewCreated) binding.lytShimmer.showing()
            if (refresher is LoadState.NotLoading) binding.lytShimmer.stoped()
            if (refresher is LoadState.Error) binding.lytShimmer.stoped()

            logError("Empty View $displayEmpty")
            if (binding.edtSearch.text!!.isEmpty()) {
                binding.emptyView.isVisible = displayEmpty && !isFiltered()
                binding.emptyViewFilter.isVisible = displayEmpty && isFiltered()
            } else {
                binding.emptyView.gone()
                binding.emptyViewFilter.gone()
                binding.emptyViewSearch.isVisible = displayEmpty
            }
            binding.rvListItem.isVisible = !displayEmpty && !binding.lytShimmer.isVisible
        }
        binding.tilSearch.setEndIconOnClickListener {
            binding.edtSearch.text?.clear()
            searchTextInput = ""
            binding.emptyViewSearch.gone()
            isNewCreated = true
            setupObserve(searchTextInput,searchCategory, searchStatus, searchType)
        }
        binding.swipeLayout.setOnRefreshListener {
            onRefresh()
        }
        binding.fabAddItem.setOnClickListener {
            startActivity(Intent(this, PostingActivity::class.java))
        }
        setupSearch()
    }

    private fun onRefresh() {
        searchTextInput = binding.edtSearch.text.toString()
        binding.emptyViewSearch.gone()
        isNewCreated = true
        setupObserve(searchTextInput, searchCategory, searchStatus, searchType)
        binding.swipeLayout.isRefreshing = false
    }

    private fun setupSearch() {
        binding.edtSearch.setOnFocusChangeListener { _, b ->
            if (b) {
                isNewCreated = true
            }
        }
        binding.edtSearch.afterTextChangedDebounce(1000, {})
        { value ->
            searchingList(value)
        }
        binding.edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchTextInput = binding.edtSearch.text.toString()
                searchingList(searchTextInput)
                hideSoftKeyboard(this)
                return@setOnEditorActionListener true
            }
            false
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

    private fun searchingList(value: String) {
        searchTextInput = value
        if (value.trim().length >= 3) {
            setupObserve(searchTextInput, searchCategory, searchStatus, searchType)
            binding.swipeLayout.isRefreshing = false
        } else if (value.trim().isEmpty()) {
            binding.emptyViewSearch.gone()
            setupObserve(searchTextInput,searchCategory, searchStatus, searchType)
            binding.swipeLayout.isRefreshing = false
        }
    }

    private fun isFiltered(): Boolean {
        return searchCategory.isNotEmpty() || searchStatus.isNotEmpty() || searchType.isNotEmpty()
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
        mainVM.submitKeyLostFound(form)
        // Scroll ke atas hanya untuk query baru
        lifecycleScope.launch {
            delay(300)
            if (isNewCreated) binding.rvListItem.smoothScrollToPosition(0)
        }
    }

    fun openFilterDialog(
        context: Context,
        lifecycleScope: CoroutineScope,
        expandDialog: Boolean,
        listener: ListFilterListener
    ) {
        BottomSheetDialog(context, com.fatkhun.core.R.style.BottomSheetDialog)
            .apply {
                setContentView(layoutInflater.inflate(R.layout.dialog_sort_filter, null))

                val displayMetrics = context.resources.displayMetrics
                val nestedFilter = findViewById<NestedScrollView>(R.id.nested_filter)
                val newHeight = if (expandDialog) {
                    displayMetrics?.heightPixels?.times(0.5)?.toInt() ?: 500
                } else {
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                }

                newHeight.let {
                    nestedFilter?.layoutParams?.height = it
                    nestedFilter?.requestLayout()
                }



                findViewById<Button>(R.id.btn_request)?.setOnClickListener { thisView ->
                    lifecycleScope.launch {
                        //listener.onFirstPostActions(filter)
                        delay(300)
                        //listener.onSecondPostActions(filter)
                        dismiss()
                    }
                }

                if (expandDialog) behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false

                show()
            }
    }

    interface ListFilterListener {
        fun onOrderByChanged(orderBy: String)
        fun onDistanceFilterChanged(distance: Int)
        fun onFirstPostActions(filter: MutableList<String>)
        fun onSecondPostActions(filter: MutableList<String>)
    }
}