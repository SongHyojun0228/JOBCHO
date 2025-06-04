// 🌿 프로필 이미지 미리보기
const profileImgInput = document.querySelector(".profile_img_input");
const originProfileImg = document.querySelector(".origin_profile_img");
const profilePreviewImg = document.querySelector(".preview");

function readURL(input) {
	if (input.files && input.files[0]) {
		var reader = new FileReader();
		reader.onload = function(e) {
			document.querySelector(".preview").src = e.target.result;
			originProfileImg.style.display = "none";
			profilePreviewImg.style.display = "block";
		};
		reader.readAsDataURL(input.files[0]);
	} else {
		document.querySelector(".preview").src = "";
		originProfileImg.style.display = "block";
		profilePreviewImg.style.display = "none";
	}
}

document.addEventListener("DOMContentLoaded", () => {
	// 🌿 프로필 이미지 변경
	const profileChangeBtn = document.querySelector(".btn_upload");
	const profileNoneBlock = document.querySelector(".profile_none_block");
	const profileCancelBtn = document.querySelector(".profile_cancel_btn");

	profileChangeBtn.addEventListener("click", () => {
		if (profileNoneBlock.style.display == "block") {
			profileNoneBlock.style.display = "none";
		} else {
			profileNoneBlock.style.display = "block";
		}
	});

	profileCancelBtn.addEventListener("click", () => {
		profileNoneBlock.style.display = "none";
		profilePreviewImg.style.display = "none";
		originProfileImg.style.display = "block";
	});

	const toggleItems = document.querySelectorAll(".personal_info_items");

	toggleItems.forEach((item) => {
		const arrowImg = item.querySelector("img[alt='arrow_down']");
		const userInfo = item.querySelector(".user_info");
		const changeForm = item.querySelector(".change_form");
		const inputContent = item.querySelector(".change_form input");
		const confirmBtn = item.querySelector(".confirm_btn");

		if (!arrowImg || !changeForm || !userInfo) return;

		// 🌿 토글
		item.addEventListener("click", () => {
			if (changeForm.style.display === "block") return;
			const isDown = arrowImg.src.includes("arrow-down.png");

			arrowImg.src = isDown
				? "/images/arrow-up.png"
				: "/images/arrow-down.png";

			if (!item.classList.contains("profile_delete")) {
				userInfo.style.display = isDown ? "none" : "block";
			}
			changeForm.style.display = isDown ? "block" : "none";
		});

		// 🌿 폼 내에서 클릭 시 버블링 방지
		changeForm.addEventListener("click", (e) => {
			e.stopPropagation();
		});

		// 🌿 취소 버튼 처리
		const cancelBtn = changeForm.querySelector(".cancel_btn");
		cancelBtn.addEventListener("click", (e) => {
			e.stopPropagation();
			arrowImg.src = "/images/arrow-down.png";
			userInfo.style.display = "block";
			changeForm.style.display = "none";
			inputContent.value = "";
			confirmBtn.disabled = true;
			confirmBtn.style.backgroundColor = "rgb(169, 169, 169)";
			confirmBtn.style.borderColor = "rgb(169, 169, 169)";
		});

		if (item.classList.contains("profile_name")) {
			// 🌿 이름 변경 입력값이 있을 경우 버튼 컬러 변경 
			const nameInput = item.querySelector(".name_input");
			nameInput.addEventListener("keyup", () => {
				if (nameInput.value.length > 0) {
					confirmBtn.disabled = false;
					confirmBtn.style.backgroundColor = "rgb(6, 195, 115)";
					confirmBtn.style.borderColor = "rgb(6, 195, 115)";
				} else {
					confirmBtn.disabled = true;
					confirmBtn.style.backgroundColor = "rgb(169, 169, 169)";
					confirmBtn.style.borderColor = "rgb(169, 169, 169)";
				}
			});
		}

		if (item.classList.contains("profile_email")) {
			// 🌿 이메일 변경 시 검증
			const emailInput = item.querySelector(".email_input");
			if (emailInput) {
				const emailForm = emailInput.closest("form");
				const emailCaution = document.querySelector(".email_caution");
				const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

				emailForm.addEventListener("keyup", (e) => {
					const emailValue = emailInput.value.trim();

					if (!emailRegex.test(emailValue)) {
						e.preventDefault();
						emailCaution.style.display = "block";
						emailCaution.textContent = "올바른 이메일 형식이 아닙니다.";
						confirmBtn.disabled = true;
						confirmBtn.style.backgroundColor = "rgb(169, 169, 169)";
						confirmBtn.style.borderColor = "rgb(169, 169, 169)";
					} else {
						emailCaution.style.display = "none";
						confirmBtn.disabled = false;
						confirmBtn.style.backgroundColor = "rgb(6, 195, 115)";
						confirmBtn.style.borderColor = "rgb(6, 195, 115)";
					}
				});
			}
		}

		if (item.classList.contains("profile_pw")) {
			// 🌿 비밀번호 변경 시 형식 검증
			const newPwInput = item.querySelector(".new_pw_input");
			if (newPwInput) {
				const pwCaution = item.querySelector(".pw_caution");
				const pwRegex = /^(?=.*[a-zA-Z])(?=.*[\W_n]).{8,}$/;

				newPwInput.addEventListener("keyup", (e) => {
					const newPwInputValue = newPwInput.value.trim();

					if (!pwRegex.test(newPwInputValue)) {
						e.preventDefault();
						pwCaution.style.display = "block";
						pwCaution.textContent = "비밀번호는 8~20자 영문, 숫자, 특수문자로 입력하세요.";
						confirmBtn.disabled = true;
						confirmBtn.style.backgroundColor = "rgb(169, 169, 169)";
						confirmBtn.style.borderColor = "rgb(169, 169, 169)";
					} else {
						pwCaution.style.display = "none";
						confirmBtn.disabled = false;
						confirmBtn.style.backgroundColor = "rgb(6, 195, 115)";
						confirmBtn.style.borderColor = "rgb(6, 195, 115)";
					}
				});
			}
		}

	});

});