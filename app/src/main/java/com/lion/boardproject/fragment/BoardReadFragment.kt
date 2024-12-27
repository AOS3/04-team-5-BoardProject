package com.lion.boardproject.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lion.boardproject.BoardActivity
import com.lion.boardproject.R
import com.lion.boardproject.adapter.OnReplyMenuClickListener
import com.lion.boardproject.adapter.ReplyAdapter
import com.lion.boardproject.databinding.FragmentBoardReadBinding
import com.lion.boardproject.model.BoardModel
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.service.BoardService
import com.lion.boardproject.util.hideKeyboard
import com.lion.boardproject.viewmodel.BoardReadViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class BoardReadFragment(val boardMainFragment: BoardMainFragment) : Fragment(), OnReplyMenuClickListener {

    private val boardReadViewModel by lazy { BoardReadViewModel(this@BoardReadFragment) }

    lateinit var fragmentBoardReadBinding: FragmentBoardReadBinding
    lateinit var boardActivity: BoardActivity
    lateinit var boardDocumentId: String
    lateinit var boardModel: BoardModel

    private val adapter: ReplyAdapter by lazy { ReplyAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBoardReadBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_board_read, container, false)

        fragmentBoardReadBinding.apply {
            boardReadViewModel = this@BoardReadFragment.boardReadViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        boardActivity = activity as BoardActivity

        fragmentBoardReadBinding.imageViewBoardRead.visibility = View.INVISIBLE

        gettingArguments()
        settingToolbar()
        settingBoardData()

        fragmentBoardReadBinding.rvBoardReplyList.adapter = adapter

        // 댓글 목록 로드
        boardReadViewModel.loadReplyList(boardDocumentId)

        return fragmentBoardReadBinding.root
    }


    override fun onMenuClick(view: View, replyModel: ReplyModel) {
        PopupMenu(requireContext(), view).apply {
            // menuId와 title을 작성자에 따라 선택
            val (menuId, menuTitle) = when (boardActivity.loginUserDocumentId == replyModel.replyWriterId) {
                true -> 1 to "삭제"
                false -> 2 to "신고"
            }
            menu.add(Menu.NONE, menuId, Menu.NONE, menuTitle)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> showDeleteConfirmDialog(replyModel)
                    2 -> boardReadViewModel.reportReply()
                }
                true
            }
            show()
        }
    }

    // 이전 화면으로 돌아가는 메서드
    fun movePrevFragment() {
        boardMainFragment.removeFragment(BoardSubFragmentName.BOARD_WRITE_FRAGMENT)
        boardMainFragment.removeFragment(BoardSubFragmentName.BOARD_READ_FRAGMENT)
    }

    // 툴바를 구성하는 메서드
    fun settingToolbar() {
        fragmentBoardReadBinding.apply {
            // 메뉴를 보이지 않게 설정한다.
            toolbarBoardRead.menu.children.forEach {
                it.isVisible = false
            }

            toolbarBoardRead.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menuItemBoardReadModify -> {
                        // 글의 문서 번호를 전달한다.
                        val dataBundle = Bundle()
                        dataBundle.putString("boardDocumentId", boardDocumentId)
                        boardMainFragment.replaceFragment(
                            BoardSubFragmentName.BOARD_MODIFY_FRAGMENT,
                            true,
                            true,
                            dataBundle
                        )
                    }

                    R.id.menuItemBoardReadDelete -> {
                        val builder = MaterialAlertDialogBuilder(boardActivity)
                        builder.setTitle("글 삭제")
                        builder.setMessage("삭제시 복구할 수 없습니다")
                        builder.setNegativeButton("취소", null)
                        builder.setPositiveButton("삭제") { dialogInterface: DialogInterface, i: Int ->
                            proBoardDelete()
                        }
                        builder.show()
                    }
                }
                true
            }
        }
    }

    // arguments의 값을 변수에 담아준다.
    fun gettingArguments() {
        boardDocumentId = arguments?.getString("boardDocumentId")!!
    }

    // 글 데이터를 가져와 보여주는 메서드
    fun settingBoardData() {
        // 서버에서 데이터를 가져온다.
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                BoardService.selectBoardDataOneById(boardDocumentId)
            }
            boardModel = work1.await()

            fragmentBoardReadBinding.apply {
                boardReadViewModel?.textFieldBoardReadTitleText?.value = boardModel.boardTitle
                boardReadViewModel?.textFieldBoardReadTextText?.value = boardModel.boardText
                boardReadViewModel?.textFieldBoardReadTypeText?.value =
                    boardModel.boardTypeValue.str
                boardReadViewModel?.textFieldBoardReadNickName?.value =
                    boardModel.boardWriterNickName

                // 작성자와 로그인한 사람이 같으면 메뉴를 보기에 한다.
                if (boardModel.boardWriteId == boardActivity.loginUserDocumentId) {
                    toolbarBoardRead.menu.children.forEach {
                        it.isVisible = true
                    }
                }
            }

            // 첨부 이미지가 있다면
            if (boardModel.boardFileName != "none") {
                val work1 = async(Dispatchers.IO) {
                    // 이미지에 접근할 수 있는 uri를 가져온다.
                    BoardService.gettingImage(boardModel.boardFileName)
                }

                val imageUri = work1.await()
                boardActivity.showServiceImage(
                    imageUri,
                    fragmentBoardReadBinding.imageViewBoardRead
                )
                fragmentBoardReadBinding.imageViewBoardRead.visibility = View.VISIBLE
            }
        }
    }

    // 글 삭제 처리 메서드
    fun proBoardDelete() {
        CoroutineScope(Dispatchers.Main).launch {
            // 만약 첨부 이미지가 있다면 삭제한다.
            if (boardModel.boardFileName != "none") {
                val work1 = async(Dispatchers.IO) {
                    BoardService.removeImageFile(boardModel.boardFileName)
                }
                work1.join()
            }
            // 글 정보를 삭제한다.
            val work2 = async(Dispatchers.IO) {
                BoardService.deleteBoardData(boardDocumentId)
            }
            work2.join()
            // 글 목록 화면으로 이동한다.
            boardMainFragment.removeFragment(BoardSubFragmentName.BOARD_READ_FRAGMENT)
        }
    }

    /*
    * 댓글 삭제 확인 Dialog
    */
    private fun showDeleteConfirmDialog(replyModel: ReplyModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("댓글 삭제")
            .setMessage("댓글을 삭제하시겠습니까?")
            .setNegativeButton("취소", null)
            .setPositiveButton("삭제") { _, _ ->
                boardReadViewModel.deleteReply(replyModel.replyDocumentId, boardDocumentId)
            }
            .show()
    }
}