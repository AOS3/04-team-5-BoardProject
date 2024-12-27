package com.lion.boardproject.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lion.boardproject.vo.ReplyVO
import kotlinx.coroutines.tasks.await

class ReplyRepository {
    companion object {
        private const val COLLECTION_REPLIES = "ReplyData"

        // 댓글 추가
        suspend fun addReply(replyVO: ReplyVO): String {
            val fireStore = FirebaseFirestore.getInstance()
            val documentReference = fireStore.collection(COLLECTION_REPLIES).add(replyVO).await()
            return documentReference.id
        }

        // 특정 게시글의 댓글 목록 조회
        suspend fun getReplyList(boardId: String): MutableList<Map<String, *>> {
            val firestore = FirebaseFirestore.getInstance()

            val result = firestore.collection(COLLECTION_REPLIES)
                .whereEqualTo("replyBoardId", boardId)
                .orderBy("replyTimeStamp", Query.Direction.ASCENDING)
                .get()
                .await()

            return result.map { document ->
                mapOf(
                    "documentId" to document.id,
                    "replyVO" to document.toObject(ReplyVO::class.java)
                )
            }.reversed().toMutableList()
        }

        // 댓글 삭제
        suspend fun deleteReply(replyId: String) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection(COLLECTION_REPLIES)
                .document(replyId)
                .delete()
                .await()
        }

    }
}