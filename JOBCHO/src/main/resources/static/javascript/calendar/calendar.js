console.log("calendar.js 로드됨");

document.addEventListener("DOMContentLoaded", function() {
	const calendarEl = document.getElementById("calendar");

	const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

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
		eventSources: [{
			url: '/function/calendar/events',
			method: 'GET',
			eventDataTransform(evt) {
				return {
					id: evt.id,
					title: evt.title,
					start: evt.startDate,
					end: evt.endDate,
					color: evt.color,
					extendedProps: {
						writer: evt.writer,
						memo: evt.description,
						repeat: evt.checkRepeat
					}
				};
			},
			failure() { alert('이벤트 로드 실패'); }
		}],
		initialDate: new Date(),
		navLinks: true,
		selectable: true,
		selectMirror: true,
		editable: true,
		dayMaxEvents: true,

		eventDisplay: "block",

		dayHeaderContent: function(arg) {
			const weekdays = ["일", "월", "화", "수", "목", "금", "토"];
			return weekdays[arg.date.getDay()];
		},
		dayHeaderDidMount: function(arg) {
			const day = arg.date.getDay();
			if (day === 0) {
				arg.el.style.color = "rgb(169,69,65)";
			} else if (day === 6) {
				arg.el.style.color = "rgb(79, 130, 252)";
			}
		},
		dayCellDidMount: function(arg) {
			const day = arg.date.getDay();
			if (day === 0) {
				arg.el.style.color = "rgb(169,69,65)";
			} else if (day === 6) {
				arg.el.style.color = "rgb(79, 130, 252)";
			}
		},
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
		datesSet: function(info) {
			const centerDate = calendar.getDate();
			const year = centerDate.getFullYear();
			const month = centerDate.getMonth() + 1;

			calendar.getEvents().forEach(event => {
				if (
					event.classNames.includes("generated-repeat") ||
					event.classNames.includes("group-holiday")
				) {
					event.remove();
				}
			});

			fetch(`/api/holidays?year=${year}&month=${month}`)
				.then(res => res.json())
				.then(data => {
					data.forEach(item => {
						const dateStr = item.locdate;
						const formatted = `${dateStr.slice(0, 4)}-${dateStr.slice(4, 6)}-${dateStr.slice(6, 8)}`;
						calendar.addEvent({
							title: item.dateName,
							start: formatted,
							allDay: true,
							className: ["fc-event-holiday", "group-holiday"]
						});
					});
				})
				.catch(error => {
					console.error("공휴일 불러오기 실패:", error);
				});

			userEvents.forEach(event => {
				const repeat = event.repeat;
				if (!repeat) return;
				let s = new Date(event.originalStart);
				let e = new Date(event.originalEnd);
				while (s < info.end) {
					if (event.repeatEnd && new Date(event.repeatEnd) <= s) break;
					if (
						s.getTime() !== new Date(event.originalStart).getTime() &&
						s >= info.start &&
						!event.exceptions.includes(s.toISOString())
					) {
						calendar.addEvent({
							title: event.title,
							start: new Date(s),
							end: new Date(e),
							allDay: event.allDay,
							color: event.color,
							extendedProps: {
								repeat: event.repeat,
								memo: event.extendedProps.memo,
								writer: event.extendedProps.writer
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
					}
				}
			});

			const userToggle = document.querySelector('.user-schedule');
			if (userToggle?.classList.contains('icon-off')) {
				document.querySelectorAll('.fc-event.group-user').forEach(el => {
					el.style.display = "none";
				});
			}

			const holidayToggle = document.querySelector('.holiday-toggle');
			if (holidayToggle?.classList.contains('icon-off')) {
				document.querySelectorAll('.fc-event.group-holiday').forEach(el => {
					el.style.display = "none";
				});
			}
		}
	});

	calendar.render();

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
	const closeRepeatBtn = document.getElementById("closeRepeatDeleteModal");

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

	repeatForm?.addEventListener("submit", e => {
		e.preventDefault();
		const scope = new FormData(repeatForm).get("scope");
		const ev = window.currentPopupEvent;
		const rep = ev.extendedProps.repeat;
		const cutT = ev.start.getTime();

		if (scope === "single") {
			ev.remove();
			userEvents.forEach(ue => {
				if (ue.repeat === rep && ue.title === ev.title) {
					ue.exceptions.push(ev.start.toISOString());
				}
			});
			calendar.getEvents().forEach(ei => {
				if (ei.extendedProps.repeat === rep && ei.start.getTime() === cutT) {
					ei.remove();
				}
			});
		}
		else if (scope === "future") {
			userEvents.forEach(ue => {
				if (ue.repeat === rep && ue.title === ev.title) {
					ue.exceptions.push(
						...generateDatesBetween(
							cutT,
							calendar.view.activeEnd.getTime(),
							ue.repeat
						)
					);
				}
			});
			calendar.getEvents().forEach(ei => {
				if (
					ei.classNames.includes("generated-repeat") &&
					ei.extendedProps.repeat === rep &&
					ei.start.getTime() >= cutT
				) {
					ei.remove();
				}
			});
		}
		else if (scope === "all") {
			calendar.getEvents().forEach(ei => {
				if (ei.extendedProps.repeat === rep) ei.remove();
			});
			for (let i = userEvents.length - 1; i >= 0; i--) {
				if (userEvents[i].repeat === rep) userEvents.splice(i, 1);
			}
		}

		calendar.refetchEvents();

		repeatModal.style.display = "none";
		document.getElementById("schedulePopup").style.display = "none";
	});


	cancelRepeatBtn?.addEventListener("click", () => { repeatModal.style.display = "none"; });
	closeRepeatBtn?.addEventListener("click", () => { repeatModal.style.display = "none"; });


	cancelRepeatBtn?.addEventListener("click", () => {
		repeatModal.style.display = "none";
	});
	closeRepeatBtn?.addEventListener("click", () => {
		repeatModal.style.display = "none";
	});

	repeatForm?.addEventListener("submit", e => {
		e.preventDefault();
		const scope = new FormData(repeatForm).get("scope");
		const ev = window.currentPopupEvent;
		const rep = ev.extendedProps.repeat;
		const cutT = ev.start.getTime();

		if (scope === "single") {
			ev.remove();
			userEvents.forEach(ue => {
				if (ue.repeat === rep && ue.title === ev.title) {
					ue.exceptions.push(ev.start.toISOString());
				}
			});
			calendar.getEvents().forEach(ei => {
				if (
					ei.classNames.includes("generated-repeat") &&
					ei.extendedProps.repeat === rep &&
					ei.start.getTime() === cutT
				) {
					ei.remove();
				}
			});
		}
		else if (scope === "future") {
			// 1) 원본 반복 규칙에 종료 날짜 설정
			userEvents.forEach(ue => {
				if (ue.repeat === rep && ue.title === ev.title) {
					// 이 시점 이후로는 생성되지 않도록 repeatEnd 지정
					ue.repeatEnd = new Date(cutT).toISOString();
					// 예외 리스트에 현재 및 이후 인스턴스 추가
					ue.exceptions = ue.exceptions || [];
					ue.exceptions.push(ev.start.toISOString());
				}
			});
			// 2) 현재 렌더된 미래 인스턴스만 제거
			calendar.getEvents().forEach(ei => {
				if (ei.extendedProps.repeat === rep && ei.start.getTime() >= cutT) {
					ei.remove();
				}
			});
			// 3) 변경된 userEvents 반영
			calendar.refetchEvents();
		}
		else if (scope === "all") {
			calendar.getEvents().forEach(ei => {
				if (ei.extendedProps.repeat === rep) ei.remove();
			});
			for (let i = userEvents.length - 1; i >= 0; i--) {
				if (userEvents[i].repeat === rep) userEvents.splice(i, 1);
			}
			calendar.refetchEvents();
		}

		repeatModal.style.display = "none";
		document.getElementById("schedulePopup").style.display = "none";
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
			const popup = dotBtn.parentElement.querySelector(".color-popup");
			if (!popup) return;
			popup.style.display = "flex";
			popup.querySelectorAll(".color-dot").forEach(dot => {
				dot.addEventListener("click", e => {
					e.stopPropagation();
					const color = dot.dataset.color;
					const icon = dotBtn.closest(".user-schedule, .holiday-toggle")?.querySelector("svg.calendar-icon");
					if (icon) icon.style.color = color;
					popup.style.display = "none";
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
			scheduleModal.style.display = "block";
			document.getElementById("scheduleTitle").value = "";
			document.getElementById("scheduleMemo").value = "";
			document.getElementById("scheduleStart").value = "";
			document.getElementById("scheduleEnd").value = "";
			document.getElementById("scheduleRepeat").value = "none";
			document.querySelectorAll(".color-dot").forEach(d => d.classList.remove("selected"));
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
		const created = calendar.addEvent(newEvent);
		userEvents.push(newEvent);

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
				created.setProp('id', data.id);
			})
			.catch(err => console.error('일정 저장 오류:', err));

		if (repeat !== "none") {
			calendar.refetchEvents();
			calendar.trigger("datesSet", {
				start: calendar.view.activeStart,
				end: calendar.view.activeEnd,
				startStr: calendar.view.activeStart.toISOString(),
				endStr: calendar.view.activeEnd.toISOString(),
				view: calendar.view
			});
		}
		scheduleModal.style.display = "none";
		document.getElementById("scheduleTitle").value = "";
		document.getElementById("scheduleStart").value = "";
		document.getElementById("scheduleEnd").value = "";
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
});
