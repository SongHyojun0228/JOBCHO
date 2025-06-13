package com.jobcho.function;

import com.jobcho.function.HolidayService;
import com.jobcho.function.HolidayDto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/holidays")
public class HolidayController {

	private final HolidayService holidayService;

	@GetMapping
	public List<HolidayDto> getHolidays(
		@RequestParam(name = "year") int year,
		@RequestParam(name = "month") int month) {
		return holidayService.fetchHolidays(year, month);
	}
}
