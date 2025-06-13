const xBtnElement = document.querySelector(".x_btn");
const totalContainer = document.querySelector(".total_container");

xBtnElement.addEventListener("click", function () {
    totalContainer.classList.add("fade-out");
});
