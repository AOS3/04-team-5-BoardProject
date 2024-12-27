package com.lion.boardproject.service

import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.repository.ReplyRepository
import com.lion.boardproject.util.ReplyState
import com.lion.boardproject.vo.ReplyVO

class ReplyService {
    companion object {
        // 댓글 추가
        suspend fun addReply(
            nickName: String,
            content: String,
            writerId: String,
            boardId: String
        ): String {
            val replyVO = ReplyVO().apply {
                replyNickName = nickName
                replyText = content
                replyWriterId = writerId
                replyBoardId = boardId
                replyTimeStamp = System.currentTimeMillis()
                replyState = ReplyState.REPLY_STATE_NORMAL.number
            }

            return ReplyRepository.addReply(replyVO)
        }

        // 댓글 목록 조회
        suspend fun getReplyList(boardId: String): List<ReplyModel> {
            return ReplyRepository.getReplyList(boardId).map { replyMap ->
                val replyVO = replyMap["replyVO"] as ReplyVO
                val documentId = replyMap["documentId"] as String
                replyVO.toReplyModel(documentId)
            }
        }

        // 댓글 삭제
        suspend fun deleteReply(replyId: String) {
            ReplyRepository.deleteReply(replyId)
        }
    }
}