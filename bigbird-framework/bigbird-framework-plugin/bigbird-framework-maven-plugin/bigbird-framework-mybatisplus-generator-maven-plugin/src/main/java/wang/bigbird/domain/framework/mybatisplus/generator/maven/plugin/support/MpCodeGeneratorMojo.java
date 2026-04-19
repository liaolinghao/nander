/*
 * Copyright (c) 2026 廖凌浩 / 鸟域
 *
 * Licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package wang.bigbird.domain.framework.mybatisplus.generator.maven.plugin.support;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import wang.bigbird.domain.framework.mybatisplus.generator.maven.plugin.support.config.MpCodeGeneratorConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Bigbird
 */
@Slf4j
@Mojo(name = "generator", defaultPhase = LifecyclePhase.COMPILE)
public class MpCodeGeneratorMojo extends AbstractMojo {

    private static final String DEFAULT_PATH = "mp-code-generator-config.yaml";

    @Parameter
    private String configurationFile;

    @Override
    public void execute() {
        InputStream inputStream = null;
        if (StringUtils.isNotBlank(configurationFile)) {
            try {
                inputStream = new FileInputStream(configurationFile);
            } catch (FileNotFoundException e) {
                log.error("Execute:", e);
            }
        } else {
            inputStream = MpCodeGeneratorMojo.class.getClassLoader().getResourceAsStream(DEFAULT_PATH);
        }
        MpCodeGeneratorConfig config = yaml2Config(inputStream);
        AutoGenerator mpg = configureAutoGenerator(config);
        mpg.execute();
    }

    private MpCodeGeneratorConfig yaml2Config(InputStream inputStream) {
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(inputStream, MpCodeGeneratorConfig.class);
    }

    private AutoGenerator configureAutoGenerator(MpCodeGeneratorConfig config) {
        AutoGenerator mpg = new AutoGenerator();
        mpg.setGlobalConfig(config.getGlobalConfig());
        mpg.setDataSource(config.getDataSourceConfig());
        mpg.setPackageInfo(config.getPackageConfig());
        mpg.setTemplate(new TemplateConfig());
        mpg.setStrategy(config.getStrategyConfig());
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        return mpg;
    }

}
