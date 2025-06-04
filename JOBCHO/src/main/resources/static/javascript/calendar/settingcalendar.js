document.addEventListener('DOMContentLoaded', function() {
	var calendarEl = document.getElementById('calendar');
	
	var calendar = new FullCalendar.Calendar(calendarEl, {
		initialView: 'dayGirdMonth',
		headerToolbar: {
			left: 'prev.next today',
			center: 'title',
			right: 'dayGirdMonth, timeGirdWeek, timeGirdDay'
		},
		locale: 'ko',
		event: '/api/events'
	
	});
	
	calendar.render();
})