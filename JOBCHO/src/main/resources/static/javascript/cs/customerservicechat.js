let stompClient = null;

// ✅ 채팅방 ID & 유저 ID 하드코딩 예시 → 서버 템플릿으로 치환할 것
const chatroomId = 1; // 예: CSChatroom의 ID
const senderId = 1;   // 현재 사용자 ID
const receiverId = 2; // 관리자 ID

function connect() {
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function () {
    // 구독 (특정 채팅방)
    stompClient.subscribe(`/topic/cs.${chatroomId}`, function (message) {
      const msg = JSON.parse(message.body);
      showMessage(msg);
    });
  });
}

function showMessage(msg) {
  const chatBox = document.getElementById('chatMessages');
  const messageDiv = document.createElement('div');
  messageDiv.classList.add(msg.sender.userId === senderId ? 'mine_talk' : 'other_person_talk');
  messageDiv.innerHTML = `<p>${msg.content}</p>`;
  chatBox.appendChild(messageDiv);
}

document.addEventListener('DOMContentLoaded', function () {
  connect();

  document.getElementById('chatForm').addEventListener('submit', function (e) {
    e.preventDefault();
    const content = document.getElementById('chatInput').value;

    const message = {
      content: content,
      sender: { userId: senderId },
      receiver: { userId: receiverId },
      csChatroomId: { csChatroomId: chatroomId }
    };

    stompClient.send("/app/cs.send", {}, JSON.stringify(message));
    document.getElementById('chatInput').value = '';
  });
});
