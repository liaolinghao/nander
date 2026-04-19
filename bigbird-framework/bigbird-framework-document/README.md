# 文档构件

本构件用于统一文档框架，该框架包括五大模块：

excel模块：专注于excel文档的制作，为excel文档制作的核心引擎，可被单独依赖使用，制作本地excel文档。

word模块：专注于word文档的制作，为word文档制作的核心引擎，可被单独依赖使用，制作本地word文档。

pdf模块：专注于pdf文档的制作，为pdf文档制作的核心引擎，可被单独依赖使用，制作本地pdf文档。

ppt模块：专注于ppt文档的制作，为ppt文档制作的核心引擎，可被单独依赖使用，制作本地ppt文档。

html模块：专注于html文档的制作，为html文档制作的核心引擎，可被单独依赖使用，制作本地html文档。

## 注意事项

本构件需要将操控office依赖jar包（lib/jacob.jar）手工安装到maven本地仓库或者私有仓库中。

```
# 进入lib目录，执行如下命令：

mvn install:install-file \
-Dfile=./jacob.jar \
-DgroupId=com.jacob \
-DartifactId=jacob \
-Dversion=1.19 \
-Dpackaging=jar \
-DgeneratePom=true
```
