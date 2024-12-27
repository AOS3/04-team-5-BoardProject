package com.lion.boardproject.viewmodel

import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.lion.boardproject.adapter.ReplyAdapter
import com.lion.boardproject.fragment.BoardReadFragment
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.service.ReplyService
import com.lion.boardproject.util.showToast
import kotlinx.coroutines.launch

class ReplyViewModel(private val fragment: BoardReadFragment) : ViewModel() {


}


