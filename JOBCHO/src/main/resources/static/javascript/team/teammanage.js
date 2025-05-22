// 권한 변경할 때 확인 문구
document.addEventListener("DOMContentLoaded", () => {
  const radioAll   = document.querySelector('input[value="all"]');
  const radioAdmin = document.querySelector('input[value="admin_only"]');
  let currentValue = "admin_only";

  function handleChange(event, targetValue) {
    const confirmed = confirm("정말로 변경하시겠습니까?");
    if (confirmed) {
      currentValue = targetValue;
    } else {
      if (currentValue === "all") {
        radioAll.checked = true;
      } else {
        radioAdmin.checked = true;
      }
    }
  }

  radioAll.addEventListener("change", e => handleChange(e, "all"));
  radioAdmin.addEventListener("change", e => handleChange(e, "admin_only"));
});

// 이미지 업로드 자동 제출
document.addEventListener("DOMContentLoaded", () => {
  const imageElement = document.getElementById("teamIcon");
  const fileInput    = document.getElementById("fileInput");
  const form         = document.getElementById("uploadForm");

  if (imageElement && fileInput && form) {
    imageElement.addEventListener("click", () => {
      fileInput.click();
    });

    fileInput.addEventListener("change", () => {
      if (fileInput.files.length > 0) {
        form.submit();
      }
    });
  }
});
