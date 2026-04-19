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
package wang.bigbird.domain.framework.core.base.tool.ini;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;
import wang.bigbird.domain.framework.core.base.tool.unicode.UnicodeReader;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * 这个类用来分析INI文件，可以对INI文件进行简单的访问，如果希望保存更为详细的信息，可以使用XML文件
 *
 * @author Bigbird
 */
@Slf4j
public class IniFileAccessor implements Serializable, IniData {

    /**
     * 序列化唯一ID
     */
    private static final long serialVersionUID = -8003249168164547092L;

    /**
     * Section起始字符
     */
    private static final char SECTION_START_CHAR = '[';
    /**
     * Section结束字符
     */
    private static final char SECTION_END_CHAR = ']';
    /**
     * 最小值表达式
     */
    private static final String MIN_VALUE = "MIN_VALUE";
    /**
     * 最大值表达式
     */
    private static final String MAX_VALUE = "MAX_VALUE";

    /**
     * 存放Section集合
     */
    private Vector<Section> sectionList;
    /**
     * 当前Section
     */
    private transient Section currentSection;
    /**
     * 存放INI格式数据的文件路径
     */
    private transient String iniFilePath;
    /**
     * 是否去掉key和value两头的空格
     */
    private boolean trim;

    /* ==========================构造方法============================== */

    /**
     * 构造方法，数据保存在内存中，并且去掉key和value两头的空格
     */
    public IniFileAccessor() {
        this(true);
    }

    /**
     * 构造方法，数据保存在内存中
     *
     * @param trim 设置是否去掉key和value两头的空格
     */
    public IniFileAccessor(boolean trim) {
        this.trim = trim;
        sectionList = new Vector<>();
        currentSection = new Section();
    }

    /**
     * 通过指定url来构造类的实例
     *
     * @param url 获取数据内容的url
     */
    public IniFileAccessor(URL url) {
        this(url, false);
    }

    /**
     * 通过指定url来构造类的实例，并根据local属性决定编码方式
     *
     * @param url   获取数据内容的url
     * @param local 是否采用本地编码
     */
    public IniFileAccessor(URL url, boolean local) {
        this();
        try {
            if (local) {
                loadLocal(url.openStream());
            } else {
                load(url.openStream());
            }
        } catch (Throwable t) {
            throw new IniException(t);
        }
    }

    /**
     * 通过指定url来构造类的实例，并采用指定的encode编码方式
     *
     * @param url    获取数据内容的url
     * @param encode 编码方式
     */
    public IniFileAccessor(URL url, String encode) {
        this();
        try {
            load(url.openStream(), encode);
        } catch (Throwable t) {
            throw new IniException(t);
        }
    }

    /**
     * 通过指定文件来构造类的实例，如果文件不存在，则创建文件
     *
     * @param file 获取数据内容的文件
     */
    public IniFileAccessor(File file) {
        this(file, true);
    }

    /**
     * 通过指定文件来构造类的实例，如果文件不存在，根据isCreate属性决定是否创建
     *
     * @param file     获取数据内容的文件
     * @param isCreate 是否创造文件
     */
    public IniFileAccessor(File file, boolean isCreate) {
        this(file.getAbsolutePath(), isCreate);
    }

    /**
     * 通过指定文件路径来构造类的实例，如果文件不存在，则创建文件
     *
     * @param filePath 获取数据内容的文件路径
     */
    public IniFileAccessor(String filePath) {
        this(filePath, true);
    }

    /**
     * 通过指定文件名来构造类的实例，如果文件不存在，根据isCreate属性决定是否创建
     *
     * @param filePath 获取数据内容的文件路径
     * @param isCreate 是否创造文件
     */
    public IniFileAccessor(String filePath, boolean isCreate) {
        this(filePath, isCreate, false);
    }

    /**
     * 通过指定文件名来构造类的实例，如果文件不存在，根据isCreate属性决定是否创建，根据local属性决定是否采用本地编码
     *
     * @param filePath 获取数据内容的文件路径
     * @param isCreate 是否创造文件
     * @param local    是否采用本地编码
     */
    public IniFileAccessor(String filePath, boolean isCreate, boolean local) {
        this();
        iniFilePath = filePath;
        FileInputStream in = null;
        File iniFile = new File(iniFilePath);
        try {
            if (iniFile.exists()) {
                in = new FileInputStream(iniFile);
                if (local) {
                    loadLocal(in);
                } else {
                    load(in, Coder.DEFAULT_ENCODING);
                }
            } else {
                if (isCreate) {
                    iniFile.createNewFile();
                }
            }
        } catch (Exception e) {
            throw new IniException(e);
        } finally {
            StreamUtils.close(in);
        }
    }

    /**
     * 通过指定INI文件Reader对象来构造类的实例
     *
     * @param strReader 获取数据内容的reader
     */
    public IniFileAccessor(Reader strReader) {
        this();
        try {
            load(strReader);
        } catch (Throwable t) {
            throw new IniException(t);
        }
    }

    /* ==========================载入数据方法============================== */

    /**
     * 通过inStream，载入数据，默认采用utf-8编码
     *
     * @param inStream 获取数据内容的输入流
     * @throws IOException IO流读取错误时，抛出的异常
     */
    private void load(InputStream inStream) throws IOException {
        load(inStream, Coder.DEFAULT_ENCODING);
    }

    /**
     * 通过inStream为INIFileAccessor对象载入数据，encode决定采用的编码方式
     *
     * @param inStream 获取数据内容的输入流
     * @param encode   编码方式
     * @throws IOException IO流读取错误时，抛出的异常
     */
    private void load(InputStream inStream, String encode) throws IOException {
        BufferedReader br = StringUtils.isBlank(encode) ? new BufferedReader(
                new InputStreamReader(inStream)) : new BufferedReader(
                new UnicodeReader(inStream, encode));
        load(br);
    }

    /**
     * 通过使用本地字符集的inStream，载入数据
     *
     * @param inStream 获取数据内容的输入流
     * @throws IOException IO流读取错误时，抛出的异常
     */
    private void loadLocal(InputStream inStream) throws IOException {
        BufferedReader br = new BufferedReader(new UnicodeReader(inStream,
                Charset.forName(System.getProperty("file.encoding"))
                        .displayName()));
        load(br);
    }

    /**
     * 通过reader，载入数据
     *
     * @param reader 获取数据内容的读取器
     * @throws IOException IO流读取错误时，抛出的异常
     */
    private void load(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        load(br);
    }

    /**
     * 通过bufferedReader，载入数据
     *
     * @param bufferedReader 获取数据内容的读取器
     * @throws IOException IO流读取错误时，抛出的异常
     */
    private synchronized void load(BufferedReader bufferedReader)
            throws IOException {
        try {
            String line;
            while (true) {
                line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                // 以#开头的为注释内容
                if (line.length() > 0 && line.charAt(0) != '#') {
                    if (isSection(line)) {
                        // 如果读入的第一句不为Section名称,将抛出NullPointException
                        String name = trim ? line.trim() : line;
                        String sectionName = getSectionName(name);
                        currentSection = new Section(sectionName);
                        sectionList.addElement(currentSection);
                    } else {
                        String key = getKey(line);
                        String value = getValue(line);
                        currentSection.put(key, value);
                    }
                }
            }
        } finally {
            StreamUtils.close(bufferedReader);
        }
    }

    /**
     * 判断是否为Section名称串
     *
     * @param line 数据行内容
     * @return 判断结果
     */
    private boolean isSection(String line) {
        boolean ret = false;
        if (line.charAt(0) == SECTION_START_CHAR
                && line.charAt(line.length() - 1) == SECTION_END_CHAR) {
            ret = true;
        }
        return ret;
    }

    /**
     * 获取Key值
     *
     * @param line 数据行内容
     * @return 键
     */
    private String getKey(String line) {
        if (line != null && line.length() > 0) {
            String key;
            int index = line.indexOf('=');
            if (index == -1) {
                key = line;
            } else {
                key = line.substring(0, index);
            }
            return trim ? key.trim() : key;
        } else {
            return null;
        }
    }

    /**
     * 获取Value值
     *
     * @param line 数据行内容
     * @return 值
     */
    private String getValue(String line) {
        if (line != null && line.length() > 0) {
            int index = line.indexOf('=');
            String value;
            if (index == -1 || (index + 1 == line.length())) {
                value = "";
            } else {
                value = trim ? line.substring(index + 1).trim() : line
                        .substring(index + 1);
            }
            return value;
        } else {
            return null;
        }
    }

    /**
     * 获取Section名称
     *
     * @param line 数据行内容
     * @return Section名称
     */
    private String getSectionName(String line) {
        String secName = line.substring(1, line.length() - 1);
        return trim ? secName.trim() : secName;
    }

    /**
     * 根据section名称找出对应的Section对象，找不到返回null
     *
     * @param section section名称
     * @return section对象
     */
    private Section getSectionByName(String section) {
        if (section == null) {
            return null;
        }
        if (currentSection != null) {
            if (section.equals(currentSection.getName())) {
                return currentSection;
            }
        }
        int count = sectionList.size();
        for (int i = 0; i < count; i++) {
            Section temp = sectionList.elementAt(i);
            if (section.equals(temp.getName())) {
                return temp;
            }
        }
        return null;
    }

    /**
     * 将一个节点写到指定文件
     *
     * @param sec 节点对象
     * @param out 输出流
     * @throws IOException 文件写入发生错误时，抛出此异常
     */
    private void writeSection(Section sec, Writer out) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append(SECTION_START_CHAR).append(sec.getName())
                .append(SECTION_END_CHAR).append('\n');
        for (String objKey : sec.keySet()) {
            String objVal = sec.get(objKey);
            buf.append(objKey);
            if (objVal != null) {
                buf.append('=').append(objVal);
            }
            buf.append('\n');
        }
        out.write(buf.toString());
    }

    /* ==========================实现方法============================== */

    @Override
    public void reload(InputStream inputStream) throws IOException {
        sectionList.removeAllElements();
        currentSection = new Section();
        load(inputStream);
    }

    @Override
    public void reload(Reader reader) throws IOException {
        sectionList.removeAllElements();
        currentSection = new Section();
        load(reader);
    }

    @Override
    public void reload(File file) {
        reload(file, false);
    }

    @Override
    public void reload(File file, boolean isCreate) {
        reload(file, isCreate, false);
    }

    @Override
    public void reload(File file, boolean isCreate, boolean local) {
        iniFilePath = file.getAbsolutePath();
        sectionList.removeAllElements();
        currentSection = new Section();
        try {
            if (file.exists()) {
                if (local) {
                    loadLocal(new FileInputStream(file));
                } else {
                    load(new FileInputStream(file),
                            Coder.DEFAULT_ENCODING);
                }
            } else {
                if (isCreate) {
                    file.createNewFile();
                }
            }
        } catch (Exception e) {
            throw new IniException(e);
        }
    }

    @Override
    public String get(String section, String key) {
        String value = null;
        Section tempSection = getSectionByName(section);
        if (tempSection != null) {
            value = tempSection.get(key);
        }
        return value;
    }

    @Override
    public String get(String section, String key, String defaultVal) {
        String value = get(section, key);
        return StringUtils.isBlank(value) ? defaultVal : value;
    }

    @Override
    public int getInt(String section, String key, int defaultVal) {
        String value = get(section, key);
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        } else if (value.equalsIgnoreCase(MIN_VALUE)) {
            return Integer.MIN_VALUE;
        } else if (value.equalsIgnoreCase(MAX_VALUE)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(value);
    }

    @Override
    public long getLong(String section, String key, long defaultVal) {
        String value = get(section, key);
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        } else if (value.equalsIgnoreCase(MIN_VALUE)) {
            return Long.MIN_VALUE;
        } else if (value.equalsIgnoreCase(MAX_VALUE)) {
            return Long.MAX_VALUE;
        }
        return Long.parseLong(value);
    }

    @Override
    public float getFloat(String section, String key, float defaultVal) {
        String value = get(section, key);
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        } else if (value.equalsIgnoreCase(MIN_VALUE)) {
            return -1 * Float.MAX_VALUE;
        } else if (value.equalsIgnoreCase(MAX_VALUE)) {
            return Float.MAX_VALUE;
        }
        return Float.parseFloat(value);
    }

    @Override
    public double getDouble(String section, String key, double defaultVal) {
        String value = get(section, key);
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        } else if (value.equalsIgnoreCase(MIN_VALUE)) {
            return -1 * Double.MAX_VALUE;
        } else if (value.equalsIgnoreCase(MAX_VALUE)) {
            return Double.MAX_VALUE;
        }
        return Double.parseDouble(value);
    }

    @Override
    public boolean getBoolean(String section, String key, boolean defaultVal) {
        String value = get(section, key);
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        } else {
            return ("1".equals(value) || "true".equalsIgnoreCase(value));
        }
    }

    @Override
    public Date getDate(String section, String key, Date defaultVal,
                        String format) {
        String value = get(section, key);
        return StringUtils.isBlank(value) ? defaultVal : DateUtils.parse(
                value, format);
    }

    @Override
    public LinkedHashMap<String, String> getSection(String section) {
        return getSectionByName(section);
    }

    @Override
    public String add(String section, String key, String value,
                      boolean overWrite) {
        Section tempSection = getSectionByName(section);
        if (tempSection == null) {
            currentSection = new Section(section);
            currentSection.put(key, value);
            sectionList.addElement(currentSection);
            return value;
        } else {
            currentSection = tempSection;
            if (currentSection.containsKey(key)
                    && StringUtils.isNotBlank(currentSection.get(key))) {
                if (overWrite) {
                    currentSection.put(key, value);
                    return value;
                } else {
                    return currentSection.get(key);
                }
            } else {
                currentSection.put(key, value);
                return value;
            }
        }
    }

    @Override
    public int add(String section, String key, int value, boolean overWrite) {
        String ret = add(section, key, String.valueOf(value), overWrite);
        return Integer.parseInt(ret);
    }

    @Override
    public long add(String section, String key, long value, boolean overWrite) {
        String ret = add(section, key, String.valueOf(value), overWrite);
        return Long.parseLong(ret);
    }

    @Override
    public float add(String section, String key, float value, boolean overWrite) {
        String ret = add(section, key, String.valueOf(value), overWrite);
        return Float.parseFloat(ret);
    }

    @Override
    public double add(String section, String key, double value,
                      boolean overWrite) {
        String ret = add(section, key, String.valueOf(value), overWrite);
        return Double.parseDouble(ret);
    }

    @Override
    public boolean add(String section, String key, boolean value,
                       boolean overWrite) {
        String ret = add(section, key, String.valueOf(value), overWrite);
        return Boolean.parseBoolean(ret);
    }

    @Override
    public Date add(String section, String key, Date value, String format,
                    boolean overWrite) {
        String ret = add(section, key, DateUtils.format(value, format),
                overWrite);
        return DateUtils.parse(ret, format);
    }

    @Override
    public LinkedHashMap<String, String> addSection(String section) {
        Section tempSection = getSectionByName(section);
        if (tempSection == null) {
            currentSection = new Section(section);
            sectionList.addElement(currentSection);
        } else {
            currentSection = tempSection;
        }
        return currentSection;
    }

    @Override
    public String remove(String section, String key) {
        Section tmp = getSectionByName(section);
        if (tmp == null) {
            return null;
        }
        currentSection = tmp;
        if (tmp.containsKey(key)) {
            String val = tmp.get(key);
            tmp.remove(key);
            return val;
        }
        return null;
    }

    @Override
    public boolean delSection(String section) {
        boolean bRet = true;
        Section tempSection = getSectionByName(section);
        if (tempSection != null) {
            bRet = sectionList.removeElement(tempSection);
        }
        return bRet;
    }

    @Override
    public void delAllSection() {
        sectionList.removeAllElements();
        currentSection = null;
    }

    @Override
    public boolean clearSection(String section) {
        boolean bRet = true;
        Section tempSection = getSectionByName(section);
        if (tempSection != null) {
            tempSection.clear();
        } else {
            bRet = false;
        }
        return bRet;
    }

    @Override
    public void save() throws IOException {
        save(false);
    }

    @Override
    public void save(boolean local) throws IOException {
        if (iniFilePath == null) {
            throw new IOException("Default Saved file is NULL");
        }
        save(iniFilePath, local);
    }

    @Override
    public void save(File file) throws IOException {
        save(file.getAbsolutePath());
    }

    @Override
    public void save(String filepath) throws IOException {
        save(filepath, false);
    }

    @Override
    public void save(String filePath, boolean local) throws IOException {
        if (local) {
            save(filePath, Charset.forName(System.getProperty("file.encoding"))
                    .displayName());
        } else {
            save(filePath, Coder.DEFAULT_ENCODING);
        }
    }

    @Override
    public void save(String filePath, String encode) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath), encode));
            int count = sectionList.size();
            for (int i = 0; i < count; i++) {
                writeSection(sectionList.elementAt(i), bw);
            }
        } finally {
            StreamUtils.close(bw);
        }
    }

    @Override
    public Enumeration<Section> sections() {
        return sectionList.elements();
    }

    @Override
    public String dumpObject() {
        StringBuilder buffer = new StringBuilder();
        int count = sectionList.size();
        for (int i = 0; i < count; i++) {
            Section map = sectionList.elementAt(i);
            buffer.append(SECTION_START_CHAR).append(map.getName())
                    .append(SECTION_END_CHAR);
            buffer.append('\n');
            for (String obj : map.keySet()) {
                buffer.append(obj).append('=').append(map.get(obj));
                buffer.append('\n');
            }
        }
        return buffer.toString();
    }

    @Override
    public boolean isEmpty() {
        return sectionList.isEmpty();
    }

    @Override
    public Vector<Section> getSectionList() {
        return sectionList;
    }

    /**
     * 返回类的基本信息
     */
    @Override
    public String toString() {
        return "INIFileAccessor from file: " + iniFilePath;
    }

}
