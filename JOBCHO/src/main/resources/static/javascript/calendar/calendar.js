console.log("calendar.js 로드됨");

document.addEventListener("DOMContentLoaded", function() {
	const calendarEl = document.getElementById("calendar");

	const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

	/*
	fetch('/function/calendar/groups', {
		credentials: 'same-origin',
		headers: { [csrfHeader]: csrfToken }
	})
		.then(res => res.json())
		.then(groups => {
			const select = document.getElementById('scheduleGroup');
			select.innerHTML = '';
			groups.forEach(g => {
				const option = document.createElement('option');
				option.value = `${g.type}:${g.id}`;
				option.textContent = g.name;
				select.appendChild(option);
			});
		})
		.catch(err => console.error('그룹 목록 로딩 실패', err));
	*/

	function formatForInput(date) {
		const d = new Date(date);
		const year = d.getFullYear();
		const month = String(d.getMonth() + 1).padStart(2, '0');
		const day = String(d.getDate()).padStart(2, '0');
		const hours = String(d.getHours()).padStart(2, '0');
		const minutes = String(d.getMinutes()).padStart(2, '0');
		return `${year}-${month}-${day}T${hours}:${minutes}`;
	}

	function generateDatesBetween(startTs, endTs, repeat) {
		const dates = [];
		let cursor = new Date(startTs);
		while (cursor.getTime() <= endTs) {
			dates.push(cursor.toISOString());
			if (repeat === 'daily') {
				cursor.setDate(cursor.getDate() + 1);
			} else if (repeat === 'weekly') {
				cursor.setDate(cursor.getDate() + 7);
			} else if (repeat === 'monthly') {
				cursor.setMonth(cursor.getMonth() + 1);
			} else if (repeat === 'yearly') {
				cursor.setFullYear(cursor.getFullYear() + 1);
			} else {
				break;
			}
		}
		return dates;
	}

	const userEvents = [];
	const currentUserName = document.querySelector(".user-schedule")?.dataset.username || "알수없음";

	const calendar = new FullCalendar.Calendar(calendarEl, {
		locale: "ko",
		headerToolbar: {
			left: "prev,next today",
			center: "title",
			right: "dayGridMonth,timeGridWeek,timeGridDay"
		},
		initialDate: new Date(),
		navLinks: true,
		selectable: true,
		selectMirror: true,
		editable: true,
		dateClick: function(info) {
			const scheduleModal = document.getElementById("scheduleModal");
			scheduleModal.style.display = "block";

			document.getElementById("scheduleTitle").value = "";
			document.getElementById("scheduleMemo").value = "";

			document.getElementById("scheduleStart").value = formatForInput(info.date);
			const oneHourLater = new Date(info.date.getTime() + 60 * 60 * 1000);
			document.getElementById("scheduleEnd").value = formatForInput(oneHourLater);

			document.getElementById("scheduleRepeat").value = "none";
			document.querySelectorAll(".color-dot").forEach(d => d.classList.remove("selected"));
			document.querySelector(".color-dot")?.classList.add("selected");

			const addBtn = document.getElementById("addScheduleBtn");
			addBtn.textContent = "저장하기";
			addBtn.dataset.mode = "add";

			selectedEvent = null;
		},

		dayMaxEvents: true,
		eventDisplay: "block",

		//여기부터
		events: function(fetchInfo, successCallback, failureCallback) {
			console.log("events 콜백 진입", fetchInfo.startStr, fetchInfo.endStr);

			// 1) 파라미터 준비
			const activeElems = document.querySelectorAll('.workspace-toggle.active');
			const params = new URLSearchParams();
			activeElems.forEach(el => params.append('workspaceIds', el.dataset.workspaceId));
			params.append('start', fetchInfo.startStr);
			params.append('end', fetchInfo.endStr);

			// 2) API 호출
			fetch(`/function/calendar/events?${params.toString()}`)
				.then(res => res.json())
				.then(data => {
					// delete-after 처리를 위한 배열 초기화
					userEvents.length = 0;

					const baseEvents = [];
					const repeatEvents = [];

					data.forEach(item => {
						const repMap = { 1: 'daily', 2: 'weekly', 3: 'monthly', 4: 'yearly' };
						const rep = repMap[item.checkRepeat] || null;
						const desc = item.description;
						const wr = item.writer;

						// 단일 삭제일자
						const exceptions = (item.exceptionDates || []).map(d => {
							const dt = new Date(d);
							const y = dt.getFullYear();
							const m = String(dt.getMonth() + 1).padStart(2, '0');
							const da = String(dt.getDate()).padStart(2, '0');
							return `${y}-${m}-${da}`;
						});
						// delete-after 기준일
						const repeatEnd = item.repeatEnd ? (() => {
							const dt = new Date(item.repeatEnd);
							const y = dt.getFullYear();
							const m = String(dt.getMonth() + 1).padStart(2, '0');
							const da = String(dt.getDate()).padStart(2, '0');
							return `${y}-${m}-${da}`;
						})() : null;

						// --- 1) 기본 인스턴스: 예외일 아니면 추가
						const startDay = item.startDate.split('T')[0];
						if (!exceptions.includes(startDay)) {
							baseEvents.push({
								id: item.id,
								title: item.title,
								start: item.startDate,
								end: item.endDate,
								backgroundColor: item.color,
								borderColor: item.color,
								classNames: ['group-user', `workspace-${item.workspaceId}`],
								extendedProps: {
									repeat: rep,
									memo: desc,
									writer: wr,
									originalId: item.id
								}
							});
						}

						// --- 2) 반복 인스턴스 생성
						if (rep) {
							// delete-after 로직에서 사용
							userEvents.push({
								id: item.id,
								originalStart: new Date(item.startDate),
								originalEnd: new Date(item.endDate),
								repeat: rep,
								exceptions: exceptions,
								repeatEnd: repeatEnd,
								color: item.color,
								memo: desc,
								writer: wr
							});

							const startTs = new Date(item.startDate).getTime();
							const endTs = new Date(item.endDate).getTime();
							// 원본 제외
							const dates = generateDatesBetween(startTs, Date.parse(fetchInfo.endStr), rep).slice(1);

							dates.forEach(iso => {
								const sDate = new Date(iso)
								const year = sDate.getFullYear()
								const month = String(sDate.getMonth() + 1).padStart(2, '0')
								const date = String(sDate.getDate()).padStart(2, '0')
								const dayStr = `${year}-${month}-${date}`

								if (exceptions.includes(dayStr)) return
								if (repeatEnd && dayStr >= repeatEnd) return

								const s = new Date(iso)
								const e = new Date(s.getTime() + (endTs - startTs))
								repeatEvents.push({
									id: `${item.id}-${iso}`,
									title: item.title,
									start: s.toISOString(),
									end: e.toISOString(),
									backgroundColor: item.color,
									borderColor: item.color,
									classNames: ['group-user', `workspace-${item.workspaceId}`, 'generated-repeat'],
									extendedProps: { repeat: rep, memo: desc, writer: wr, originalId: item.id }
								})
							})
						}
					});

					// 3) 화면에 뿌릴 최종 배열 합치기
					successCallback(baseEvents.concat(repeatEvents));
				})
				.catch(err => {
					console.error("일정 불러오기 실패", err);
					failureCallback(err);
				});
		},


		datesSet: function(info) {

			const centerDate = calendar.getDate();
			const year = centerDate.getFullYear();
			const month = centerDate.getMonth() + 1;

			calendar.getEvents().forEach(event => {
				if (
					event.classNames.includes("generated-repeat") ||
					event.classNames.includes("group-holiday") ||
					event.classNames.includes("fc-event-holiday")
				) {
					event.remove();
				}
			});

			fetch(`/api/holidays?year=${year}&month=${month}`)
				.then(res => res.json())
				.then(data => {
					console.log('▶ 백엔드 응답 전체:', data);
					data.forEach(item => {
						console.log('  - id:', item.id,
							'exceptionDates:', item.exceptionDates,
							'repeatEnd:', item.repeatEnd);
					});
					calendar.getEvents().forEach(event => {
						if (
							event.classNames.includes("group-holiday") ||
							event.classNames.includes("fc-event-holiday")
						) {
							event.remove();
						}
					});

					data.forEach(item => {
						const loc = item.locdate;
						const formatted = `${loc.slice(0, 4)}-${loc.slice(4, 6)}-${loc.slice(6, 8)}`;
						calendar.addEvent({
							title: item.dateName,
							start: formatted,
							allDay: true,
							classNames: ["fc-event-holiday", "group-holiday"]
						});
					});

					// 1) 공휴일 토글
					const holidayBtn = document.querySelector('.holiday-toggle');
					if (holidayBtn && holidayBtn.classList.contains('icon-off')) {
						document.querySelectorAll('.fc-event.group-holiday')
							.forEach(el => el.style.display = 'none');
					}

					// 2) 내 일정 토글
					const userBtn = document.querySelector('.user-schedule');
					if (userBtn && userBtn.classList.contains('icon-off')) {
						document.querySelectorAll('.fc-event.group-user')
							.forEach(el => el.style.display = 'none');
					}

					// 3) 워크스페이스 토글 (active 클래스가 없으면 숨김)
					document.querySelectorAll('.workspace-toggle[data-workspace-id]').forEach(btn => {
						const wsId = btn.dataset.workspaceId;
						const selector = `.fc-event.workspace-${wsId}`;
						if (!btn.classList.contains('active')) {
							document.querySelectorAll(selector)
								.forEach(el => el.style.display = 'none');
						}
					});
					//수정
					/*
					calendar.getEvents().forEach(event => {
						if (event.classNames.includes("generated-repeat")) {
							event.remove();
						}
					});
	
					userEvents.forEach(ev => {
						const repeat = ev.repeat;
						if (!repeat) return;
						let s = new Date(ev.originalStart);
						let e = new Date(ev.originalEnd);
						while (s < info.end) {
							if (ev.repeatEnd && new Date(ev.repeatEnd) <= s) break;
							if (
								s.getTime() !== new Date(ev.originalStart).getTime() &&
								s >= info.start &&
								!ev.exceptions.includes(s.toISOString())
							) {
								calendar.addEvent({
									title: ev.title,
									start: new Date(s),
									end: new Date(e),
									allDay: ev.allDay,
									color: ev.color,
									extendedProps: {
										repeat: ev.repeat,
										memo: ev.extendedProps.memo,
										writer: ev.extendedProps.writer
									},
									classNames: ["group-user", "generated-repeat"]
								});
							}
							if (repeat === "daily") {
								s.setDate(s.getDate() + 1);
								e.setDate(e.getDate() + 1);
							} else if (repeat === "weekly") {
								s.setDate(s.getDate() + 7);
								e.setDate(e.getDate() + 7);
							} else if (repeat === "monthly") {
								s.setMonth(s.getMonth() + 1);
								e.setMonth(e.getMonth() + 1);
							} else if (repeat === "yearly") {
								s.setFullYear(s.getFullYear() + 1);
								e.setFullYear(e.getFullYear() + 1);
							} else {
								break;
							}
						}
					});
					*/
				})
				.catch(err => console.error("공휴일 불러오기 실패:", err));

		},

		//여기부터?


		dayHeaderContent: function(arg) {
			const weekdays = ["일", "월", "화", "수", "목", "금", "토"];
			return weekdays[arg.date.getDay()];
		},
		dayHeaderDidMount: function(arg) {
			const day = arg.date.getDay();
			if (day === 0) arg.el.style.color = "rgb(169,69,65)";
			else if (day === 6) arg.el.style.color = "rgb(79,130,252)";
		},
		dayCellDidMount: function(arg) {
			const day = arg.date.getDay();
			if (day === 0) arg.el.style.color = "rgb(169,69,65)";
			else if (day === 6) arg.el.style.color = "rgb(79,130,252)";
		},
		/*
		select: function(arg) {
			const title = prompt("Event Title:");
			if (title) {
				calendar.addEvent({
					title: title,
					start: arg.start,
					end: arg.end,
					allDay: arg.allDay
				});
			}
			calendar.unselect();
		},
		*/
		eventClick: function(arg) {
			const event = arg.event;
			selectedEvent = event;

			const popup = document.getElementById("schedulePopup");
			popup.style.display = "flex";

			document.getElementById("popupTitle").textContent = event.title;

			const start = new Date(event.start);
			let end = event.end ? new Date(event.end) : null;
			if (!end || start.getTime() === end.getTime()) {
				end = start;
			}

			const timeText =
				`${start.getFullYear()}/${start.getMonth() + 1}/${start.getDate()} ${start.getHours()}:${String(start.getMinutes()).padStart(2, '0')} ~ ` +
				`${end.getFullYear()}/${end.getMonth() + 1}/${end.getDate()} ${end.getHours()}:${String(end.getMinutes()).padStart(2, '0')}`;

			document.getElementById("popupTime").textContent = timeText;

			if (event.classNames.includes("group-holiday")) {
				document.getElementById("popupDescription").textContent = "공휴일";
				document.getElementById("popupWriter").textContent = "공휴일 및 기념일";

				document.getElementById("editBtn").style.display = "none";
				document.getElementById("deleteBtn").style.display = "none";

				const d = start;
				document.getElementById("popupTime").textContent =
					`${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일`;

				return;
			}

			document.getElementById("popupDescription").textContent = event.extendedProps.memo || "설명이 없습니다";
			if (event.classNames.includes("group-holiday")) {
				document.getElementById("popupWriter").textContent = "공휴일 및 기념일";
			} else {
				document.getElementById("popupWriter").textContent =
					event.extendedProps.writer || "작성자 미확인";
			}
			window.currentPopupEvent = event;

			document.getElementById("editBtn").style.display = "";
			document.getElementById("deleteBtn").style.display = "";
		},

	});

	calendar.render();

	document.getElementById("cancelScheduleBtn").addEventListener("click", () => {
		document.getElementById("scheduleModal").style.display = "none";
	});

	fetch('/function/workspace-settings', {
		credentials: 'same-origin',
		headers: { [csrfHeader]: csrfToken }
	})
		.then(res => res.json())
		.then(settings => {
			settings.forEach(s => {
				const toggle = document.querySelector(
					`.workspace-toggle[data-workspace-id="${s.WORKSPACE_ID}"]`
				);
				if (!toggle) return;

				toggle.classList.toggle('active', s.VISIBLE === 'Y');

				const icon = toggle.querySelector('svg.calendar-icon');
				if (icon) icon.style.color = s.COLOR;

				toggle.dataset.color = s.COLOR;
			});
		});

	document.querySelectorAll('.workspace-toggle[data-workspace-id]').forEach(el => {
		el.addEventListener('click', () => {

			const isActive = el.classList.toggle('active');

			const wsId = Number(el.dataset.workspaceId);
			if (!wsId) return;
			const visible = isActive ? 'Y' : 'N';
			const color = el.dataset.color || null;

			fetch('/function/workspace-settings', {
				method: 'POST',
				credentials: 'same-origin',
				headers: {
					'Content-Type': 'application/json',
					[csrfHeader]: csrfToken
				},
				body: JSON.stringify({
					workspaceId: wsId,
					visible: visible,
					color: color
				})
			}).catch(err => console.error('워크스페이스 설정 저장 실패', err));

			calendar.refetchEvents();
		});
	});

	let selectedEvent = null;

	document.getElementById("editBtn")?.addEventListener("click", () => {
		if (!selectedEvent) return;

		document.getElementById("scheduleTitle").value = selectedEvent.title;
		document.getElementById("scheduleMemo").value = selectedEvent.extendedProps.memo || "";
		document.getElementById("scheduleStart").value = formatForInput(selectedEvent.start);
		document.getElementById("scheduleEnd").value = formatForInput(selectedEvent.end);
		const groupSelect = document.getElementById("scheduleGroup");
		if (groupSelect) groupSelect.value = selectedEvent.extendedProps.group || "personal";

		const selectedColor = selectedEvent.backgroundColor;
		document.querySelectorAll(".color-dot").forEach(dot => {
			dot.classList.remove("selected");
			if (dot.dataset.color === selectedColor) {
				dot.classList.add("selected");
			}
		});

		document.getElementById("scheduleRepeat").value = selectedEvent.extendedProps.repeat || "none";
		document.getElementById("addScheduleBtn").textContent = "수정하기";
		document.getElementById("addScheduleBtn").dataset.mode = "edit";

		document.getElementById("scheduleModal").style.display = "block";
		document.getElementById("schedulePopup").style.display = "none";
	});

	document.getElementById("closePopupBtn")?.addEventListener("click", () => {
		document.getElementById("schedulePopup").style.display = "none";
	});

	const repeatModal = document.getElementById("repeatDeleteModal");
	const repeatForm = document.getElementById("repeatDeleteForm");
	const cancelRepeatBtn = document.getElementById("cancelRepeatDelete");

	cancelRepeatBtn.addEventListener("click", () => {
		repeatModal.style.display = "none";
	});

	document.getElementById("deleteBtn")?.addEventListener("click", () => {
		const ev = window.currentPopupEvent;
		const rep = ev.extendedProps.repeat;

		if (!rep || rep === "none") {
			if (confirm("일정을 삭제하시겠습니까?")) {
				const scheduleId = ev.id;

				fetch(`/function/calendar/events/${scheduleId}`, {
					method: 'DELETE',
					credentials: 'same-origin',
					headers: {
						'Content-Type': 'application/json',
						[csrfHeader]: csrfToken
					}
				})
					.then(res => res.json())
					.then(data => {
						if (data.status === 'deleted') {
							ev.remove();
							alert('일정이 삭제되었습니다.');
						} else {
							alert('삭제할 일정을 찾을 수 없습니다');
						}
						document.getElementById("schedulePopup").style.display = "none";
					})
					.catch(err => {
						console.error('삭제 오류:', err);
						alert('삭제중 오류가 발생했습니다.')
					});
			}
			return;
		}
		repeatModal.style.display = "flex";
	});

	repeatForm?.addEventListener("submit", async e => {
		e.preventDefault();
		const scope = new FormData(repeatForm).get("scope");
		const ev = window.currentPopupEvent;

		const originalId = ev.extendedProps.originalId || ev.id;
		const dateIso = ev.start.toISOString();

		let url, options;
		if (scope === "single") {
			url = `/function/calendar/events/${originalId}` +
				`?instanceDate=${encodeURIComponent(dateIso)}`;
			options = {
				method: 'DELETE',
				credentials: 'same-origin',
				headers: { [csrfHeader]: csrfToken }
			};
		} else if (scope === "future") {
			url = `/function/calendar/events/${originalId}/delete-after` +
				`?from=${encodeURIComponent(dateIso)}`;
			options = {
				method: 'POST',
				credentials: 'same-origin',
				headers: { [csrfHeader]: csrfToken }
			};
		} else {
			url = `/function/calendar/events/${originalId}`;
			options = {
				method: 'DELETE',
				credentials: 'same-origin',
				headers: { [csrfHeader]: csrfToken }
			};
		}

		try {
			const res = await fetch(url, options);
			if (!res.ok) throw new Error(`HTTP ${res.status}`);
			await res.json();

			if (scope === "single") {
				ev.remove();
				userEvents.forEach(ue => {
					if (ue.id === ev.id) ue.exceptions.push(ev.start.toISOString());
				});
				calendar.getEvents().forEach(ei => {
					if (ei.classNames.includes("generated-repeat") &&
						ei.extendedProps.repeat === ev.extendedProps.repeat &&
						ei.start.getTime() === ev.start.getTime()) {
						ei.remove();
					}
				});
			}
			else if (scope === "future") {
				userEvents.forEach(ue => {
					if (ue.id === ev.id) {
						ue.repeatEnd = ev.start.toISOString();
						ue.exceptions.push(ev.start.toISOString());
					}
				});
				calendar.getEvents().forEach(ei => {
					if (ei.extendedProps.repeat === ev.extendedProps.repeat &&
						ei.start.getTime() >= ev.start.getTime()) {
						ei.remove();
					}
				});
			}
			else {
				calendar.getEvents().forEach(ei => {
					if (ei.extendedProps.repeat === ev.extendedProps.repeat) ei.remove();
				});
				for (let i = userEvents.length - 1; i >= 0; i--) {
					if (userEvents[i].id === ev.id) userEvents.splice(i, 1);
				}
			}

			calendar.refetchEvents();
			repeatModal.style.display = "none";
			document.getElementById("schedulePopup").style.display = "none";
			alert("삭제가 완료되었습니다.");
		} catch (err) {
			console.error("삭제 오류:", err);
			alert("삭제 중 오류가 발생했습니다.");
		}
	});

	document.querySelectorAll(".color-dot").forEach(dot => {
		dot.addEventListener("click", e => {
			e.stopPropagation();
			document.querySelectorAll(".color-dot").forEach(d => d.classList.remove("selected"));
			dot.classList.add("selected");
		});
	});

	document.querySelectorAll(".three-dots").forEach(dotBtn => {
		dotBtn.addEventListener("click", function(e) {
			e.stopPropagation();

			document.querySelectorAll(".color-popup").forEach(p => p.style.display = "none");

			const toggle = dotBtn.closest(".workspace-toggle");
			const popup = toggle.querySelector(".color-popup");
			if (!popup) return;
			popup.style.display = "flex";

			popup.querySelectorAll(".color-dot").forEach(colorDot => {
				colorDot.addEventListener("click", function(ev) {
					ev.stopPropagation();
					const newColor = colorDot.dataset.color;

					const icon = toggle.querySelector("svg.calendar-icon");
					if (icon) icon.style.color = newColor;

					toggle.dataset.color = newColor;

					popup.querySelectorAll(".color-dot")
						.forEach(d => d.classList.remove("selected"));
					colorDot.classList.add("selected");

					popup.style.display = "none";

					fetch("/function/workspace-settings", {
						method: "POST",
						credentials: "same-origin",
						headers: {
							"Content-Type": "application/json",
							[csrfHeader]: csrfToken
						},
						body: JSON.stringify({
							workspaceId: Number(toggle.dataset.workspaceId),
							visible: toggle.classList.contains("active") ? "Y" : "N",
							color: newColor
						})
					})
						.then(res => {
							if (!res.ok) {
								console.error("워크스페이스 설정 저장 실패:", res.status, res.statusText);
							}
						})
						.catch(err => console.error("워크스페이스 설정 저장 에러:", err));
				});
			});

		});
	});

	document.addEventListener("click", () => {
		document.querySelectorAll(".color-popup").forEach(p => p.style.display = "none");
	});

	const createBtn = document.querySelector(".create-btn");
	const scheduleModal = document.getElementById("scheduleModal");
	const cancelBtn = document.getElementById("cancelScheduleBtn");

	if (createBtn && scheduleModal && cancelBtn) {
		createBtn.addEventListener("click", () => {
			fetch('/function/calendar/groups', {
				credentials: 'same-origin',
				headers: { [csrfHeader]: csrfToken }
			})
				.then(res => res.json())
				.then(groups => {
					const select = document.getElementById('scheduleGroup');
					select.innerHTML = '';
					groups.forEach(g => {
						const option = document.createElement('option');
						option.value = `${g.type}:${g.id}`;
						option.textContent = g.name;
						select.appendChild(option);
					});
				})
				.catch(err => console.error('그룹 목록 로딩 실패', err));
			scheduleModal.style.display = "block";
			document.getElementById("scheduleTitle").value = "";
			document.getElementById("scheduleMemo").value = "";
			document.getElementById("scheduleStart").value = "";
			document.getElementById("scheduleEnd").value = "";
			document.getElementById("scheduleRepeat").value = "none";
			document.querySelectorAll(".color-dot")
				.forEach(d => d.classList.remove("selected"));
			document.querySelector(".color-dot")?.classList.add("selected");
			document.getElementById("addScheduleBtn").textContent = "저장하기";
			document.getElementById("addScheduleBtn").dataset.mode = "add";
			selectedEvent = null;
		});
		cancelBtn.addEventListener("click", () => {
			scheduleModal.style.display = "none";
		});
	}

	document.getElementById("sidebarToggle")?.addEventListener("click", () => {
		const layout = document.querySelector(".calendar-layout");
		layout.classList.toggle("sidebar-collapsed");
		document.getElementById("sidebarToggle").textContent = layout.classList.contains("sidebar-collapsed") ? "펼치기" : "접기";
		setTimeout(() => {
			calendar.updateSize();
		}, 300);
	});

	document.getElementById("addScheduleBtn")?.addEventListener("click", () => {
		const title = document.getElementById("scheduleTitle").value;
		const start = document.getElementById("scheduleStart").value;
		const end = document.getElementById("scheduleEnd").value;
		const repeat = document.getElementById("scheduleRepeat").value;
		const selectedDot = document.querySelector(".color-dot.selected");
		const selectedColor = selectedDot ? selectedDot.dataset.color : "#f7f7f7";

		const startDate = new Date(start);
		const endDate = new Date(end);
		const mode = document.getElementById("addScheduleBtn").dataset.mode;

		if (mode === "edit" && selectedEvent) {
			selectedEvent.setProp("title", title);
			selectedEvent.setStart(startDate);
			selectedEvent.setEnd(endDate);
			selectedEvent.setAllDay(true);
			selectedEvent.setExtendedProp("memo", document.getElementById("scheduleMemo").value);
			selectedEvent.setExtendedProp("writer", currentUserName);
			selectedEvent.setExtendedProp("repeat", repeat);
			selectedEvent.setProp("backgroundColor", selectedColor);
			document.getElementById("scheduleModal").style.display = "none";
			document.getElementById("addScheduleBtn").textContent = "저장하기";
			document.getElementById("addScheduleBtn").dataset.mode = "add";
			return;
		}

		if (!title || !start || !end) {
			alert("모든 필드를 입력해주세요.");
			return;
		}

		const newEvent = {
			title: title,
			start: startDate,
			end: endDate,
			allDay: false,
			color: selectedColor,
			extendedProps: {
				repeat: repeat,
				memo: document.getElementById("scheduleMemo").value,
				writer: currentUserName
			},
			originalStart: startDate.toISOString(),
			originalEnd: endDate.toISOString(),
			repeat: repeat,
			repeatEnd: null,
			exceptions: []
		};

		//수정됨
		//const created = calendar.addEvent(newEvent);
		//userEvents.push(newEvent);

		fetch('/function/calendar/events', {
			method: 'POST',
			credentials: 'same-origin',
			headers: {
				'Content-Type': 'application/json',
				[csrfHeader]: csrfToken
			},
			body: JSON.stringify({
				title: title,
				description: document.getElementById("scheduleMemo").value,
				color: selectedColor,
				startDate: startDate.toISOString(),
				endDate: endDate.toISOString(),
				checkRepeat: { none: 0, daily: 1, weekly: 2, monthly: 3, yearly: 4 }[repeat] || 0
			})
		})
			.then(res => res.json())
			.then(data => {
				calendar.refetchEvents();
				scheduleModal.style.display = "none";
				document.getElementById("scheduleModal").style.display = "none";
				document.getElementById("scheduleTitle").value = "";
				document.getElementById("scheduleStart").value = "";
				document.getElementById("scheduleEnd").value = "";
			})
			.catch(err => console.error('일정 저장 오류:', err));

	});

	document.querySelector(".holiday-toggle")?.addEventListener("click", () => {
		const btn = document.querySelector(".holiday-toggle");
		btn.classList.toggle("icon-off");
		document.querySelectorAll(".fc-event.group-holiday").forEach(el => {
			el.style.display = el.style.display === "none" ? "" : "none";
		});
	});

	document.querySelector(".user-schedule")?.addEventListener("click", () => {
		const btn = document.querySelector(".user-schedule");
		btn.classList.toggle("icon-off");
		document.querySelectorAll(".fc-event.group-user").forEach(el => {
			el.style.display = el.style.display === "none" ? "" : "none";
		});
	});

	/*
	document.querySelectorAll('.workspace-toggle').forEach(btn => {
		btn.addEventListener('click', () => {
			const wsId = btn.dataset.workspaceId;
			btn.classList.toggle('icon-off');
			document.querySelectorAll(`.fc-event.workspace-${wsId}`)
				.forEach(el => {
					el.style.display = el.style.display === 'none' ? '' : 'none';
				});
		});
	});
	*/
});
