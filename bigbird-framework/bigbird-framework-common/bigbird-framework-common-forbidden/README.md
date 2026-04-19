# 禁用词检测组件

本组件用于检测文本中的禁用词，禁用词由禁用词词库定义。本组件只提供简单的检测功能，不支持同义词和排除特殊符号（#@%*等）干扰的功能。

## 算法

本组件使用DFA算法（Deterministic Finite Automaton，确定有穷自动机）实现禁用词检测。

## 组件结构

本组件由三个部分组成：

1. Dfa接口<br/>
此接口规定了构建DFA数据结构的方法、DFA检测禁用词的方法。

2. ForbidWordRepository抽象类<br/>
此抽象类依赖Dfa，从存储介质（文件、数据库等）中读取禁用词，构建并维护传入的Dfa实例的数据结构，供服务调用；<br/>
读取禁用词的具体方法由子类实现，根据不同的实现，可以选择是否实现增删禁用词的功能；<br/>
提供定时更新禁用词的功能。

3. IForbidWordService<br/>
提供禁用词检测功能的服务接口，具体如下：

```
/**
 * 文本是否包含禁用词
 *
 * @param text the text
 * @return the boolean
 */
boolean include(final String text);

/**
 * 文本包含的禁用词数量
 *
 * @param text the text
 * @return the int
 */
int forbidWordCount(final String text);

/**
 * 文本包含的禁用词列表
 *
 * @param text the text
 * @return the list
 */
List<String> forbidWordList(final String text);

/**
 * 将文本中包含的禁用词以指定符号代替
 *
 * @param text   the text
 * @param symbol the symbol
 * @return the string
 */
String replace(final String text, final char symbol);

/**
 * 增加禁用词，通常基于文件的禁用词库不支持持久化，基于数据库的可以支持
 *
 * @param refreshNow 成功后是否立即刷新
 * @param words      the words
 * @return 是否成功
 */
boolean addForbidWord(boolean refreshNow, String... words);

/**
 * 删除禁用词，通常基于文件的禁用词库不支持持久化，基于数据库的可以支持
 *
 * @param refreshNow 成功后是否立即刷新
 * @param words      the words
 * @return 是否成功
 */
boolean removeForbidWord(boolean refreshNow, String... words);
```

## 主要实现类

1. MemoryMapDfaImpl<br/>
Dfa接口的实现，使用Map在内存中维护DFA的数据结构，并基于此数据结构实现禁用词检测的功能。<br/>
此实现类可以满足大部分需求，但如果禁用词量极大，则会占用大量内存。在这种情况下，可以自行编写其他实现来解决，例如使用Redis维护DFA数据结构。

2. InputStreamForbidWordRepository<br/>
从输入流中读取禁用词，文件每行表示一个禁用词。

3. FileForbidWordRepository<br/>
继承InputStreamForbidWordRepository，从classpath下的文本文件中读取禁用词，文件每行表示一个禁用词。

4. ForbidWordServiceImpl<br/>
IForbidWordService的实现类，基本只需这一个实现。

## 使用示例

以下代码从classpath下的“forbid_words.txt”文件中读取禁用词，加载到内存中实现禁用词检测的功能：

```
FileForbidWordRepository repo = new FileForbidWordRepository(new MemoryMapDfaImpl(), "forbid_words.txt");
IForbidWordService service = new ForbidWordServiceImpl(repo);
```

更详细的例子可以参考代码中的测试用例。
