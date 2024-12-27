package com.lion.boardproject.viewmodel

import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.lion.boardproject.adapter.ReplyAdapter
import com.lion.boardproject.fragment.BoardReadFragment
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.service.ReplyService
import com.lion.boardproject.util.showToast
import kotlinx.coroutines.launch

class BoardReadViewModel(val boardReadFragment: BoardReadFragment) : ViewModel() {
    // textFieldBoardReadTitle - text
    val textFieldBoardReadTitleText = MutableLiveData(" ")
    // textFieldBoardReadNickName - text
    val textFieldBoardReadNickName = MutableLiveData(" ")
    // textFieldBoardReadType - text
    val textFieldBoardReadTypeText = MutableLiveData(" ")
    // textFieldBoardReadText - text
    val textFieldBoardReadTextText = MutableLiveData(" ")

    private val _replyList = MutableLiveData<List<ReplyModel>>()
    val replyList: LiveData<List<ReplyModel>> = _replyList

    private val _isEmptyList = MutableLiveData<Boolean>()
    val isEmptyList: LiveData<Boolean> = _isEmptyList

    private val _replyCountText = MutableLiveData<String>()
    val replyCountText: LiveData<String> = _replyCountText

    // 댓글 입력 양방향 바인딩용
    val replyInput = MutableLiveData<String>()

    fun loadReplyList(boardId: String) = viewModelScope.launch {
        runCatching {
            ReplyService.getReplyList(boardId)
        }.onSuccess { replyList ->
            _replyList.value = replyList
            _isEmptyList.value = replyList.isEmpty()
            _replyCountText.value = "댓글 ${if (replyList.isEmpty()) "" else "${replyList.size}"}"
        }.onFailure {
            boardReadFragment.showToast("댓글 목록을 불러오는데 실패했습니다")
        }
    }

    fun addReply(nickName: String, writerId: String, boardId: String) = viewModelScope.launch {
        val content = replyInput.value ?: ""
        if (content.isBlank()) {
            boardReadFragment.showToast("댓글을 입력해주세요")
            return@launch
        }

        runCatching {
            ReplyService.addReply(
                nickName = nickName,
                content = content,
                writerId = writerId,
                boardId = boardId
            )
        }.onSuccess {
            replyInput.value = "" // 입력창 초기화
            boardReadFragment.showToast("댓글이 작성되었습니다")
            loadReplyList(boardId)
        }.onFailure {
            boardReadFragment.showToast("댓글 작성에 실패했습니다")
        }
    }

    fun reportReply() = viewModelScope.launch {
        runCatching {
            // 댓글 신고 로직 구현
        }.onSuccess {
            boardReadFragment.showToast("신고가 접수되었습니다")
        }.onFailure {
            boardReadFragment.showToast("신고 처리에 실패했습니다")
        }
    }

    fun deleteReply(replyId: String, boardId: String) = viewModelScope.launch {
        runCatching {
            ReplyService.deleteReply(replyId)
        }.onSuccess {
            boardReadFragment.showToast("댓글이 삭제되었습니다")
            loadReplyList(boardId)
        }.onFailure {
            boardReadFragment.showToast("댓글 삭제에 실패했습니다")
        }
    }

    fun onSubmitClick() {
        addReply(
            nickName = boardReadFragment.boardActivity.loginUserNickName,
            writerId = boardReadFragment.boardActivity.loginUserDocumentId,
            boardId = boardReadFragment.boardDocumentId
        )
    }

    companion object{
        // toolbarBoardRead - onNavigationClickBoardRead
        @JvmStatic
        @BindingAdapter("onNavigationClickBoardRead")
        fun onNavigationClickBoardRead(materialToolbar: MaterialToolbar, boardReadFragment: BoardReadFragment){
            materialToolbar.setNavigationOnClickListener {
                boardReadFragment.movePrevFragment()
            }
        }

        @JvmStatic
        @BindingAdapter("app:replyItems")
        fun RecyclerView.setItems(items: List<ReplyModel>?) {
            items?.let { replyList ->
                (adapter as? ReplyAdapter)?.submitList(replyList)
            }
        }
    }
}