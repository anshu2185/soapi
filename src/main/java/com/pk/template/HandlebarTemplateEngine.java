package com.pk.template;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.internal.lang3.StringUtils;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

public class HandlebarTemplateEngine {

	public HandlebarTemplateEngine() {

	}

	public String getRendered(String templateFile, Map<String, Object> templateData) throws IOException {
		final com.github.jknack.handlebars.Template hTemplate = getHandlebars(templateFile);
		return hTemplate.apply(templateData);
	}

	public String getName() {
		return "mustache";
	}

	private com.github.jknack.handlebars.Template getHandlebars(String templateFile) throws IOException {
		final boolean needFileTemplateLoader = true;
		final boolean fileExist = new File(templateFile).exists();
		templateFile = templateFile.replace(".mustache", StringUtils.EMPTY).replace("\\", "/");
		final String templateDir;
		TemplateLoader templateLoader = null;
		if (needFileTemplateLoader && fileExist) {
			templateDir = "template";
			templateFile = resolveTemplateFile(templateDir, templateFile);
			templateLoader = new FileTemplateLoader(templateDir, ".mustache");
		} else {
			templateDir = "template";
			templateFile = resolveTemplateFile(templateDir, templateFile);
			templateLoader = new ClassPathTemplateLoader("/" + templateDir, ".mustache");
		}
		final Handlebars handlebars = new Handlebars(templateLoader);
		handlebars.prettyPrint(true);

		return handlebars.compile(templateFile);
	}

	private String resolveTemplateFile(String templateDir, String templateFile) {
		if (templateFile.startsWith(templateDir)) {
			templateFile = StringUtils.replaceOnce(templateFile, templateDir, StringUtils.EMPTY);
		}
		return templateFile;
	}
}
