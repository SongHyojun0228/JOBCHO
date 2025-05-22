package com.jobcho;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Value("${file.upload-dir}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String uploadPath = new File(uploadDir).getAbsolutePath();
		registry.addResourceHandler("/uploads/profileImg/**").addResourceLocations("file:" + uploadPath + "/");

		System.out.println("파일 저장 경로(절대경로): " + uploadPath);
	}
}
