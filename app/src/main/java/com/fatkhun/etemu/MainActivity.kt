package com.fatkhun.etemu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.fatkhun.core.helper.PermissionHelper
import com.fatkhun.core.model.CategoriesItem
import com.fatkhun.core.model.LostFoundForm
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.ui.BaseActivity
import com.fatkhun.core.ui.PagingLoadStateAdapter
import com.fatkhun.core.utils.AlertDialogInterface
import com.fatkhun.core.utils.Constant
import com.fatkhun.core.utils.PrefKey
import com.fatkhun.core.utils.RC
import com.fatkhun.core.utils.RemoteCallback
import com.fatkhun.core.utils.afterTextChangedDebounce
import com.fatkhun.core.utils.gone
import com.fatkhun.core.utils.handleApiCallback
import com.fatkhun.core.utils.hideSoftKeyboard
import com.fatkhun.core.utils.logError
import com.fatkhun.core.utils.showCustomDialog
import com.fatkhun.core.utils.showing
import com.fatkhun.core.utils.stoped
import com.fatkhun.core.utils.toJson
import com.fatkhun.etemu.adapter.LostFoundPagingAdapter
import com.fatkhun.etemu.databinding.ActivityMainBinding
import com.fatkhun.etemu.databinding.DialogSortFilterBinding
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
    private var searchCategory = 0
    private var searchStatus = ""
    private var searchType = ""
    private var listCategory: MutableList<CategoriesItem> = mutableListOf()

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

    override fun getLayoutId(): View {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

            logError("$refresher Empty View $displayEmpty")
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
        binding.ivLogout.setOnClickListener {
            showCustomDialog(this,
                "Apakah ingin keluar aplikasi?", "",
                "Iya, Lanjutkan",
                "Batal",
                false,
                object : AlertDialogInterface {
                    override fun onPositiveButtonClicked() {
                        lifecycleScope.launch {
                            preferenceVM.clearDataValue(PrefKey.DATA_USER)
                            preferenceVM.clearDataValue(PrefKey.IS_LOGIN)
                            delay(200)
                            startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                            finish()
                        }
                    }

                    override fun onNegativeButtonClicked() {}
                })
        }
        binding.fabAddItem.setOnClickListener {
            startActivity(Intent(this, PostingActivity::class.java))
        }
        binding.fbRiwayat.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.fbFilter.setOnClickListener {
            dialogFilter()
        }
        binding.btnFilter.setOnClickListener {
            dialogFilter()
        }
        setupSearch()
    }

    private fun dialogFilter() {
        openFilterDialog(this, true, object: ListFilterListener{
            override fun onOrderByStatus(status: String) {
                searchStatus = status
            }

            override fun onOrderByType(tipe: String) {
                searchType = tipe
            }

            override fun onOrderByCategory(idKategori: Int) {
                searchCategory = idKategori
            }

            override fun onPostAction() {
                onRefresh()
            }


        })
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
                    getCategory()
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
        return searchCategory > 0 || searchStatus.isNotEmpty() || searchType.isNotEmpty()
    }

    private fun setupObserve(searchText: String, categoryId: Int, status: String, type: String) {
        if (isNewCreated) getItemList(searchText, categoryId, status, type)
    }

    private fun getItemList(search: String, categoryId: Int, status: String, type: String) {
        val form = LostFoundForm(
            keyword = search,
            user_id = storeDataHelper.getDataUser().id,
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

    private fun getCategory() {
        mainVM.getCategoryList().observe(this) { response ->
            handleApiCallback(
                this,
                response,
                false,
                object : RemoteCallback<String> {
                    override fun do_callback(id: Int, t: String) {}
                    override fun failed_callback(id: Int, t: String) {}
                }) { res, code ->
                if (code == RC().SUCCESS) {
                    res?.let {
                        listCategory = it.data
                    }
                }
            }
        }
    }

    fun openFilterDialog(
        context: Context,
        expandDialog: Boolean,
        listener: ListFilterListener
    ) {
        BottomSheetDialog(context, com.fatkhun.core.R.style.BottomSheetDialog)
            .apply {
                val binding: DialogSortFilterBinding = DialogSortFilterBinding.inflate(layoutInflater)
                setContentView(binding.root)

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

                resetFilter(binding, this, listener)

                binding.tvReset.setOnClickListener {
                    listener.onOrderByStatus(searchStatus)
                    listener.onOrderByType(searchType)
                    listener.onOrderByCategory(searchCategory)
                    resetFilter(binding,this,listener)
                }

                setOnCancelListener {
                    listener.onOrderByStatus(searchStatus)
                    listener.onOrderByType(searchType)
                    listener.onOrderByCategory(searchCategory)
                }

                if (expandDialog) behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false

                show()
            }
    }

    private fun resetFilter(binding: DialogSortFilterBinding, bottomSheetDialog: BottomSheetDialog, listener: ListFilterListener) {
        var selectedType = searchType
        var selectedStatus = searchStatus
        var selectedCategory = searchCategory

        when(searchType.lowercase()) {
            "lost" -> binding.rbLost.isChecked = true
            "found" -> binding.rbFound.isChecked = true
            else -> {
                // Tidak perlu set apa-apa
            }
        }
        when(searchStatus.lowercase()) {
            "open" -> binding.rbStatusOpen.isChecked = true
            "claimed" -> binding.rbStatusComplete.isChecked = true
            else -> {
                // Tidak perlu set apa-apa
            }
        }

        // SET LISTENER SETELAH SET NILAI AWAL
        binding.rgTipeBarang.setOnCheckedChangeListener { group, checkedId  ->
            selectedType = when (checkedId) {
                R.id.rbLost -> "lost"
                R.id.rbFound -> "found"
                else -> ""
            }
        }

        binding.rgStatusBarang.setOnCheckedChangeListener { group, checkedId  ->
            selectedStatus = when (checkedId) {
                R.id.rbStatusOpen -> "open"
                R.id.rbStatusComplete -> "claimed"
                else -> ""
            }
        }
        binding.chipGroup.removeAllViews()
        listCategory.forEachIndexed { index, item ->
            val chip = layoutInflater.inflate(
                R.layout.component_chip_category,
                binding.chipGroup,
                false
            ) as Chip
            chip.text = item.name
            chip.isCheckable = true
            chip.id = index + 1
            binding.chipGroup.addView(chip)
        }
        binding.chipGroup.isSingleSelection = true
        binding.chipGroup.children.forEachIndexed { index, view ->
            val chip = view as Chip
            if (listCategory[index].id == searchCategory) {
                chip.isChecked = true
                selectedCategory = listCategory[index].id
            }
            chip.setOnCheckedChangeListener {  _, isChecked ->
                if (isChecked) {
                    selectedCategory = listCategory[index].id
                } else {
                    selectedCategory = 0
                }
            }
        }

        binding.btnRequest.setOnClickListener { thisView ->
            lifecycleScope.launch {
                listener.onOrderByStatus(selectedStatus)
                listener.onOrderByType(selectedType)
                listener.onOrderByCategory(selectedCategory)
                delay(200)
                listener.onPostAction()
                bottomSheetDialog.dismiss()
            }
        }
    }

    interface ListFilterListener {
        fun onOrderByStatus(status: String)
        fun onOrderByType(tipe: String)
        fun onOrderByCategory(idKategori: Int)
        fun onPostAction()
    }
}