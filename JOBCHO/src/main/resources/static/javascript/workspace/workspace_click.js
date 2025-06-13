document.addEventListener("DOMContentLoaded", () => {

	const inviteChatMemberImg = document.querySelector("#invite_chat_member_img");
	const closeBtn = document.querySelector(".close_btn");
	const inviteChatMemberContainer = document.querySelector(".modal_overlay");

	const main = document.querySelector("main");

	inviteChatMemberImg.addEventListener("click", () => {
		document.getElementById("modalBackdrop").style.display = "block";
		inviteChatMemberContainer.style.display = "block";
	});

	closeBtn.addEventListener("click", () => {
		document.getElementById("modalBackdrop").style.display = "none";
		inviteChatMemberContainer.style.display = "none";
	});

});