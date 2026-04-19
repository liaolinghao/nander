# 自定义Maven插件

提供以下增强能力的自定义Maven插件：

mybatis-plus代码生成插件：生成基于mybatis-plus的mapper、service、controller三层结构，包括entity实体类和mapper.xml文件，生成后能够直接满足多维度的增删改查操作。

## Maven插件的命名规范

一般来说，我们会将自己的插件命名为<myplugin>-maven-plugin，而不推荐使用maven-<myplugin>-plugin，因为后者是Maven团队维护官方插件的保留命名方式，使用这个命名方式会侵犯Apache Maven商标。

