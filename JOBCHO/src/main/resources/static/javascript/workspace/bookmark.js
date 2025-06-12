function toggleStar(imgElement) {
    const emptyStar = '/images/icons/empty_star.png';
    const filledStar = '/images/icons/star.png';

    const isAdding = imgElement.getAttribute('src').includes('empty_star');
    imgElement.setAttribute('src', isAdding ? filledStar : emptyStar);

    const bookmarkType = imgElement.dataset.type;
    const bookmarkId = imgElement.dataset.id;
	
	console.log(bookmarkType);
	console.log(bookmarkId);
	
	const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
	const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
	
    fetch("/workspace/bookmark/toggle", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
			[header]: token 
        },
        body: JSON.stringify({
            type: bookmarkType,
            targetId: bookmarkId,
            action: isAdding ? 'ADD' : 'REMOVE'
        })
    })
    .then(res => res.json())
    .then(data => {
        if (!data.success) {
            alert("즐겨찾기 저장 중 문제가 발생했습니다.");
        }
    })
    .catch(err => {
        console.error(err);
        alert('에러 발생');
    });
}
