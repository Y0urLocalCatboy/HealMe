package com.example.healme.data.models

/**
 * Data class representing a single message sent between two users.
 *
 * @property content The content of the message.
 * @property senderId The identifier of the user who sent the message.
 * @property receiverId The identifier of the user who received the message.
 * @property timestamp The timestamp of the message.
 */
data class Message (val content: String = "",
                    val senderId: String = "",
                    val receiverId: String = "",
                    val timestamp: String = ""

) {
    companion object {

        /**
         * Creates a Message object from firebase.
         *
         * @param data The data belonging to a Message.
         * @return A Message object.
         */
        fun fromMap(data: Map<String, Any?>): Message {
            return Message(

                content = data["content"] as? String ?: "",

                senderId = data["senderId"] as? String ?: "",

                receiverId = data["receiverId"] as? String ?: "",

                timestamp = data["timestamp"] as? String ?: ""

            )
        }
    }
}