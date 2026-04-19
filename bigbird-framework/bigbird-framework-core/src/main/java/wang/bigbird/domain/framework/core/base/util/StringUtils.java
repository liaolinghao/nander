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
package wang.bigbird.domain.framework.core.base.util;

import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static wang.bigbird.domain.framework.core.base.constant.CommonConstants.PLACE_HOLDER;

/**
 * 字符串操作工具类
 *
 * @author Bigbird
 */
public class StringUtils {

    private static final String[] EMPTY_STRING_ARRAY = {};

    private static final String FOLDER_SEPARATOR = "/";

    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

    private static final String TOP_PATH = "..";

    private static final String CURRENT_PATH = ".";

    private static final char EXTENSION_SEPARATOR = '.';

    /**
     * IP正则模式
     * <p>
     * ^：表示字符串的起始位置。
     * (25[0-5]|2[0-4]\\d|[01]?\\d?\\d)：用来匹配 IP 地址里每一段的数字，范围是 0 - 255。
     * 25[0-5]：匹配 250 - 255。
     * 2[0-4]\\d：匹配 200 - 249。
     * [01]?\\d?\\d：匹配 0 - 199。
     * \\.：匹配点号
     * {3}：表明前面的组合要重复 3 次，这和 IPv4 地址由 4 个数字段构成相符合。
     * $：表示字符串的结束位置。
     */
    public static final Pattern IP_PATTERN = Pattern
            .compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

    /**
     * 手机号正则模式，国家号码段分配如下：
     * <p>
     * 一、中国移动：
     * 134(0-8)、135、136、137、138、139、144(物)、147、148(物)、150、151、152、157、158、159、165(虚)、1703(虚)、1705(虚)、1706(虚)、172(物)、178、182、183、184、187、188、195、197(5G)、198
     * <p>
     * 二、中国联通：
     * 130、131、132、140(物)、145、146(物)、155、156、166、167(虚)、1704(虚)、1707(虚)、1708(虚)、1709(虚)，171(虚)、175、176、185、186、196(5G)
     * <p>
     * 三、中国电信：
     * 133、1349(卫)、141(物)、149(物)、153、162(虚)、1700(虚)、1701(虚)、1702(虚)、173、1740(0-5)(卫)、177、179(虚)、180、181、189、190(5G)、191、193、199
     * <p>
     * 四、中国广电：
     * 192(5G)
     * <p>
     * 五、其他：
     * 1、中国交通通信信息中心
     * 1749(卫)
     * <p>
     * 2、工业和信息化部应急通信保障中心
     * 1740(6-9)(卫)、1741(0-2)(卫)
     */
    public static final Pattern MOBILE_PATTERN = Pattern
            .compile("((13[0-9])|(14[^23])|(15[^4])|(16[2567])|(17[0-9])|(18[0-9])|(19[^4]))\\d{8}");

    /**
     * 座机号正则模式
     */
    public static final Pattern TELEPHONE_PATTERN = Pattern.compile("((0\\d{2,3})-?)?(\\d{7,8})(-(\\d{3,}))?");

    /**
     * 电话号正则模式
     */
    public static final Pattern MOBILE_TELEPHONE_PATTERN = Pattern
            .compile("(?<!\\d)(?:(?:((13[0-9])|(14[^23])|(15[^4])|(16[2567])|(17[0-9])|(18[0-9])|(19[^4]))\\d{8})|(?:((0\\d{2,3})-?)?(\\d{7,8})(-(\\d{3,}))?))(?!\\d)");

    /**
     * 邮箱正则模式
     */
    public static final Pattern EMAIL_PATTERN = Pattern
            .compile("(([0-9a-zA-Z]+)|([0-9a-zA-Z]+[_.0-9a-zA-Z-]*[0-9a-zA-Z-]+))@([a-zA-Z0-9-]+[.])+(net|NET|asia|ASIA|com|COM|gov|GOV|mil|MIL|org|ORG|edu|EDU|int|INT|cn|CN|cc|CC|sg|SG|([a-zA-Z]*))");

    /**
     * 时刻正则模式
     * ^ 和 $ 表示匹配整个字符串
     * ([01]?[0-9]|2[0-3]) 匹配小时：0-9、00-19 或 20-23
     * :[0-5][0-9] 匹配分钟和秒：00-59
     */
    public static final Pattern TIME_PATTERN = Pattern
            .compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$");

    /**
     * 键盘布局模式（QWERTY示例），包含键盘横向方向和键盘斜线方向
     */
    private static final String[] KEYBOARD_SEQUENCES = {
            "qwertyuiop", "asdfghjkl", "zxcvbnm",
            "1234567890", "!@#$%^&*()_+",
            "qazwsxedcrfvtgbyhnujmik,ol.p;/[",
            "1qaz2wsx3edc4rfv5tgb6yhn7ujm8ik,9ol.0p;/-['=]"
    };

    private StringUtils() {
        throw new IllegalStateException();
    }

    /**
     * Check whether the given object (possibly a {@code String}) is empty.
     * This is effectively a shortcut for {@code !hasLength(String)}.
     * <p>This method accepts any Object as an argument, comparing it to
     * {@code null} and the empty String. As a consequence, this method
     * will never return {@code true} for a non-null non-String object.
     * <p>The Object signature is useful for general attribute handling code
     * that commonly deals with Strings but generally has to iterate over
     * Objects since attributes may e.g. be primitive value objects as well.
     * <p><b>Note: If the object is typed to {@code String} upfront, prefer
     * {@link #hasLength(String)} or {@link #hasText(String)} instead.</b>
     *
     * @param str the candidate object (possibly a {@code String})
     * @see #hasLength(String)
     * @see #hasText(String)
     * @since 3.2.1
     */
    public static boolean isEmpty(Object str) {
        return (str == null || "".equals(str));
    }

    /**
     * Check whether the given object (possibly a {@code String}) is not empty.
     * This is effectively a shortcut for {@code hasLength(String)}.
     * <p>This method accepts any Object as an argument, comparing it to
     * {@code null} and the empty String. As a consequence, this method
     * will return {@code true} for a non-null non-String object.
     * <p>The Object signature is useful for general attribute handling code
     * that commonly deals with Strings but generally has to iterate over
     * Objects since attributes may e.g. be primitive value objects as well.
     * <p><b>Note: If the object is typed to {@code String} upfront, prefer
     * {@link #hasLength(String)} or {@link #hasText(String)} instead.</b>
     *
     * @param str the candidate object (possibly a {@code String})
     * @see #hasLength(String)
     * @see #hasText(String)
     * @since 3.2.1
     */
    public static boolean isNotEmpty(Object str) {
        return !isEmpty(str);
    }

    /**
     * <p>Checks if a CharSequence is empty (""), null or whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace only
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     * not empty and not null and not whitespace only
     * @since 2.0
     * @since 3.0 Changed signature from isNotBlank(String) to isNotBlank(CharSequence)
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Gets a CharSequence length or {@code 0} if the CharSequence is
     * {@code null}.
     *
     * @param cs a CharSequence or {@code null}
     * @return CharSequence length or {@code 0} if the CharSequence is
     * {@code null}.
     * @since 2.4
     * @since 3.0 Changed signature from length(String) to length(CharSequence)
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * Check that the given {@code CharSequence} is neither {@code null} nor
     * of length 0.
     * <p>Note: this method returns {@code true} for a {@code CharSequence}
     * that purely consists of whitespace.
     * <p><pre class="code">
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     *
     * @param str the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
     * @see #hasLength(String)
     * @see #hasText(CharSequence)
     */
    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

    /**
     * Check that the given {@code String} is neither {@code null} nor of length 0.
     * <p>Note: this method returns {@code true} for a {@code String} that
     * purely consists of whitespace.
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null} and has length
     * @see #hasLength(CharSequence)
     * @see #hasText(String)
     */
    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    /**
     * Check whether the given {@code CharSequence} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code CharSequence} is not {@code null}, its length is greater than
     * 0, and it contains at least one non-whitespace character.
     * <p><pre class="code">
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     *
     * @param str the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null},
     * its length is greater than 0, and it does not contain whitespace only
     * @see #hasText(String)
     * @see #hasLength(CharSequence)
     * @see Character#isWhitespace
     */
    public static boolean hasText(CharSequence str) {
        return (str != null && str.length() > 0 && containsText(str));
    }

    /**
     * Check whether the given {@code String} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code String} is not {@code null}, its length is greater than 0,
     * and it contains at least one non-whitespace character.
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null}, its
     * length is greater than 0, and it does not contain whitespace only
     * @see #hasText(CharSequence)
     * @see #hasLength(String)
     * @see Character#isWhitespace
     */
    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given {@code CharSequence} contains any whitespace characters.
     *
     * @param str the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not empty and
     * contains at least 1 whitespace character
     * @see Character#isWhitespace
     */
    public static boolean containsWhitespace(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }

        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given {@code String} contains any whitespace characters.
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not empty and
     * contains at least 1 whitespace character
     * @see #containsWhitespace(CharSequence)
     */
    public static boolean containsWhitespace(String str) {
        return containsWhitespace((CharSequence) str);
    }

    /**
     * Trim leading and trailing whitespace from the given {@code String}.
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see Character#isWhitespace
     */
    public static String trimWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        int beginIndex = 0;
        int endIndex = str.length() - 1;
        while (beginIndex <= endIndex && Character.isWhitespace(str.charAt(beginIndex))) {
            beginIndex++;
        }
        while (endIndex > beginIndex && Character.isWhitespace(str.charAt(endIndex))) {
            endIndex--;
        }
        return str.substring(beginIndex, endIndex + 1);
    }

    /**
     * Trim <i>all</i> whitespace from the given {@code String}:
     * leading, trailing, and in between characters.
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see Character#isWhitespace
     */
    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Trim leading whitespace from the given {@code String}.
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see Character#isWhitespace
     */
    public static String trimLeadingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * Trim trailing whitespace from the given {@code String}.
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see Character#isWhitespace
     */
    public static String trimTrailingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Trim all occurrences of the supplied leading character from the given {@code String}.
     *
     * @param str              the {@code String} to check
     * @param leadingCharacter the leading character to be trimmed
     * @return the trimmed {@code String}
     */
    public static String trimLeadingCharacter(String str, char leadingCharacter) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * Trim all occurrences of the supplied trailing character from the given {@code String}.
     *
     * @param str               the {@code String} to check
     * @param trailingCharacter the trailing character to be trimmed
     * @return the trimmed {@code String}
     */
    public static String trimTrailingCharacter(String str, char trailingCharacter) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Test if the given {@code String} matches the given single character.
     *
     * @param str             the {@code String} to check
     * @param singleCharacter the character to compare to
     * @since 5.2.9
     */
    public static boolean matchesCharacter(String str, char singleCharacter) {
        return (str != null && str.length() == 1 && str.charAt(0) == singleCharacter);
    }

    /**
     * Test if the given {@code String} starts with the specified prefix,
     * ignoring upper/lower case.
     *
     * @param str    the {@code String} to check
     * @param prefix the prefix to look for
     * @see String#startsWith
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return (str != null && prefix != null && str.length() >= prefix.length() &&
                str.regionMatches(true, 0, prefix, 0, prefix.length()));
    }

    /**
     * Test if the given {@code String} ends with the specified suffix,
     * ignoring upper/lower case.
     *
     * @param str    the {@code String} to check
     * @param suffix the suffix to look for
     * @see String#endsWith
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return (str != null && suffix != null && str.length() >= suffix.length() &&
                str.regionMatches(true, str.length() - suffix.length(), suffix, 0, suffix.length()));
    }

    /**
     * Test whether the given string matches the given substring
     * at the given index.
     *
     * @param str       the original string (or StringBuilder)
     * @param index     the index in the original string to start matching against
     * @param substring the substring to match at the given index
     */
    public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        if (index + substring.length() > str.length()) {
            return false;
        }
        for (int i = 0; i < substring.length(); i++) {
            if (str.charAt(index + i) != substring.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Count the occurrences of the substring {@code sub} in string {@code str}.
     *
     * @param str string to search in
     * @param sub string to search for
     */
    public static int countOccurrencesOf(String str, String sub) {
        if (!hasLength(str) || !hasLength(sub)) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    /**
     * Replace all occurrences of a substring within a string with another string.
     *
     * @param inString   {@code String} to examine
     * @param oldPattern {@code String} to replace
     * @param newPattern {@code String} to insert
     * @return a {@code String} with the replacements
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        int index = inString.indexOf(oldPattern);
        if (index == -1) {
            // no occurrence -> can return input as-is
            return inString;
        }
        int capacity = inString.length();
        if (newPattern.length() > oldPattern.length()) {
            capacity += 16;
        }
        StringBuilder sb = new StringBuilder(capacity);
        // our position in the old string
        int pos = 0;
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString, pos, index);
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        // append any characters to the right of a match
        sb.append(inString, pos, inString.length());
        return sb.toString();
    }

    /**
     * Delete all occurrences of the given substring.
     *
     * @param inString the original {@code String}
     * @param pattern  the pattern to delete all occurrences of
     * @return the resulting {@code String}
     */
    public static String delete(String inString, String pattern) {
        return replace(inString, pattern, "");
    }

    /**
     * Delete any character in a given {@code String}.
     *
     * @param inString      the original {@code String}
     * @param charsToDelete a set of characters to delete.
     *                      E.g. "az\n" will delete 'a's, 'z's and new lines.
     * @return the resulting {@code String}
     */
    public static String deleteAny(String inString, String charsToDelete) {
        if (!hasLength(inString) || !hasLength(charsToDelete)) {
            return inString;
        }
        int lastCharIndex = 0;
        char[] result = new char[inString.length()];
        for (int i = 0; i < inString.length(); i++) {
            char c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                result[lastCharIndex++] = c;
            }
        }
        if (lastCharIndex == inString.length()) {
            return inString;
        }
        return new String(result, 0, lastCharIndex);
    }

    /**
     * Quote the given {@code String} with single quotes.
     *
     * @param str the input {@code String} (e.g. "myString")
     * @return the quoted {@code String} (e.g. "'myString'"),
     * or {@code null} if the input was {@code null}
     */
    public static String quote(String str) {
        return (str != null ? "'" + str + "'" : null);
    }

    /**
     * Turn the given Object into a {@code String} with single quotes
     * if it is a {@code String}; keeping the Object as-is else.
     *
     * @param obj the input Object (e.g. "myString")
     * @return the quoted {@code String} (e.g. "'myString'"),
     * or the input object as-is if not a {@code String}
     */
    public static Object quoteIfString(Object obj) {
        return (obj instanceof String ? quote((String) obj) : obj);
    }

    /**
     * Unqualify a string qualified by a '.' dot character. For example,
     * "this.name.is.qualified", returns "qualified".
     *
     * @param qualifiedName the qualified name
     */
    public static String unqualify(String qualifiedName) {
        return unqualify(qualifiedName, '.');
    }

    /**
     * Unqualify a string qualified by a separator character. For example,
     * "this:name:is:qualified" returns "qualified" if using a ':' separator.
     *
     * @param qualifiedName the qualified name
     * @param separator     the separator
     */
    public static String unqualify(String qualifiedName, char separator) {
        return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
    }

    /**
     * Capitalize a {@code String}, changing the first letter to
     * upper case as per {@link Character#toUpperCase(char)}.
     * No other letters are changed.
     *
     * @param str the {@code String} to capitalize
     * @return the capitalized {@code String}
     */
    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    /**
     * Uncapitalize a {@code String}, changing the first letter to
     * lower case as per {@link Character#toLowerCase(char)}.
     * No other letters are changed.
     *
     * @param str the {@code String} to uncapitalize
     * @return the uncapitalized {@code String}
     */
    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (!hasLength(str)) {
            return str;
        }
        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        } else {
            updatedChar = Character.toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }
        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars, 0, chars.length);
    }

    /**
     * Extract the FileName from the given Java resource path,
     * e.g. {@code "mypath/myfile.txt" -> "myfile.txt"}.
     *
     * @param path the file path (may be {@code null})
     * @return the extracted FileName, or {@code null} if none
     */
    public static String getFileName(String path) {
        if (path == null) {
            return null;
        }
        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
    }

    /**
     * Extract the FileName extension from the given Java resource path,
     * e.g. "mypath/myfile.txt" -> "txt".
     *
     * @param path the file path (may be {@code null})
     * @return the extracted FileName extension, or {@code null} if none
     */
    public static String getFileNameExtension(String path) {
        if (path == null) {
            return null;
        }
        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return null;
        }
        int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (folderIndex > extIndex) {
            return null;
        }
        return path.substring(extIndex + 1);
    }

    /**
     * Strip the FileName extension from the given Java resource path,
     * e.g. "mypath/myfile.txt" -> "mypath/myfile".
     *
     * @param path the file path
     * @return the path with stripped FileName extension
     */
    public static String stripFileNameExtension(String path) {
        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return path;
        }
        int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (folderIndex > extIndex) {
            return path;
        }

        return path.substring(0, extIndex);
    }

    /**
     * Apply the given relative path to the given Java resource path,
     * assuming standard Java folder separation (i.e. "/" separators).
     *
     * @param path         the path to start from (usually a full file path)
     * @param relativePath the relative path to apply
     *                     (relative to the full file path above)
     * @return the full file path that results from applying the relative path
     */
    public static String applyRelativePath(String path, String relativePath) {
        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (separatorIndex != -1) {
            String newPath = path.substring(0, separatorIndex);
            if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
                newPath += FOLDER_SEPARATOR;
            }
            return newPath + relativePath;
        } else {
            return relativePath;
        }
    }

    /**
     * Normalize the path by suppressing sequences like "path/.." and
     * inner simple dots.
     * <p>The result is convenient for path comparison. For other uses,
     * notice that Windows separators ("\") are replaced by simple slashes.
     * <p><strong>NOTE</strong> that {@code cleanPath} should not be depended
     * upon in a security context. Other mechanisms should be used to prevent
     * path-traversal issues.
     *
     * @param path the original path
     * @return the normalized path
     */
    public static String cleanPath(String path) {
        if (!hasLength(path)) {
            return path;
        }
        String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);
        // Shortcut if there is no work to do
        if (pathToUse.indexOf(CommonConstants.DOT) == -1) {
            return pathToUse;
        }
        // Strip prefix from path to analyze, to not treat it as part of the
        // first path element. This is necessary to correctly parse paths like
        // "file:core/../core/io/Resource.class", where the ".." should just
        // strip the first "core" directory while keeping the "file:" prefix.
        int prefixIndex = pathToUse.indexOf(':');
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains(FOLDER_SEPARATOR)) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }
        if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
            prefix = prefix + FOLDER_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }
        String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
        Deque<String> pathElements = new ArrayDeque<>();
        int tops = 0;
        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (CURRENT_PATH.equals(element)) {
                // Points to current directory - drop it.
            } else if (TOP_PATH.equals(element)) {
                // Registering top path found.
                tops++;
            } else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.addFirst(element);
                }
            }
        }
        // All path elements stayed the same - shortcut
        if (pathArray.length == pathElements.size()) {
            return prefix + pathToUse;
        }
        // Remaining top paths need to be retained.
        for (int i = 0; i < tops; i++) {
            pathElements.addFirst(TOP_PATH);
        }
        // If nothing else left, at least explicitly point to current path.
        if (pathElements.size() == 1 && pathElements.getLast().isEmpty() && !prefix.endsWith(FOLDER_SEPARATOR)) {
            pathElements.addFirst(CURRENT_PATH);
        }
        return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
    }

    /**
     * Compare two paths after normalization of them.
     *
     * @param path1 first path for comparison
     * @param path2 second path for comparison
     * @return whether the two paths are equivalent after normalization
     */
    public static boolean pathEquals(String path1, String path2) {
        return cleanPath(path1).equals(cleanPath(path2));
    }

    /**
     * Parse the given {@code String} value into a {@link Locale}, accepting
     * the {@link Locale#toString} format as well as BCP 47 language tags.
     *
     * @param localeValue the locale value: following either {@code Locale's}
     *                    {@code toString()} format ("en", "en_UK", etc), also accepting spaces as
     *                    separators (as an alternative to underscores), or BCP 47 (e.g. "en-UK")
     *                    as specified by {@link Locale#forLanguageTag} on Java 7+
     * @return a corresponding {@code Locale} instance, or {@code null} if none
     * @throws IllegalArgumentException in case of an invalid locale specification
     * @see #parseLocaleString
     * @see Locale#forLanguageTag
     * @since 5.0.4
     */
    public static Locale parseLocale(String localeValue) {
        String[] tokens = tokenizeLocaleSource(localeValue);
        if (tokens.length == 1) {
            validateLocalePart(localeValue);
            Locale resolved = Locale.forLanguageTag(localeValue);
            if (resolved.getLanguage().length() > 0) {
                return resolved;
            }
        }
        return parseLocaleTokens(localeValue, tokens);
    }

    /**
     * Parse the given {@code String} representation into a {@link Locale}.
     * <p>For many parsing scenarios, this is an inverse operation of
     * {@link Locale#toString Locale's toString}, in a lenient sense.
     * This method does not aim for strict {@code Locale} design compliance;
     * it is rather specifically tailored for typical Spring parsing needs.
     * <p><b>Note: This delegate does not accept the BCP 47 language tag format.
     * Please use {@link #parseLocale} for lenient parsing of both formats.</b>
     *
     * @param localeString the locale {@code String}: following {@code Locale's}
     *                     {@code toString()} format ("en", "en_UK", etc), also accepting spaces as
     *                     separators (as an alternative to underscores)
     * @return a corresponding {@code Locale} instance, or {@code null} if none
     * @throws IllegalArgumentException in case of an invalid locale specification
     */
    public static Locale parseLocaleString(String localeString) {
        return parseLocaleTokens(localeString, tokenizeLocaleSource(localeString));
    }

    private static String[] tokenizeLocaleSource(String localeSource) {
        return tokenizeToStringArray(localeSource, "_ ", false, false);
    }

    private static Locale parseLocaleTokens(String localeString, String[] tokens) {
        // 明确表示至少需要2个token才会有variant部分
        int minTokensForVariant = 2;
        String language = (tokens.length > 0 ? tokens[0] : "");
        String country = (tokens.length > 1 ? tokens[1] : "");
        validateLocalePart(language);
        validateLocalePart(country);
        String variant = "";
        if (tokens.length > minTokensForVariant) {
            // There is definitely a variant, and it is everything after the country
            // code sans the separator between the country code and the variant.
            int endIndexOfCountryCode = localeString.indexOf(country, language.length()) + country.length();
            // Strip off any leading '_' and whitespace, what's left is the variant.
            variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
            if (variant.startsWith(CommonConstants.UNDERLINE)) {
                variant = trimLeadingCharacter(variant, '_');
            }
        }
        if (variant.isEmpty() && country.startsWith(CommonConstants.POUND_SIGN)) {
            variant = country;
            country = "";
        }
        return (language.length() > 0 ? new Locale(language, country, variant) : null);
    }

    private static void validateLocalePart(String localePart) {
        for (int i = 0; i < localePart.length(); i++) {
            char ch = localePart.charAt(i);
            if (ch != ' ' && ch != '_' && ch != '-' && ch != '#' && !Character.isLetterOrDigit(ch)) {
                throw new IllegalArgumentException(
                        "Locale part \"" + localePart + "\" contains invalid characters");
            }
        }
    }

    /**
     * Copy the given {@link Collection} into a {@code String} array.
     * <p>The {@code Collection} must contain {@code String} elements only.
     *
     * @param collection the {@code Collection} to copy
     *                   (potentially {@code null} or empty)
     * @return the resulting {@code String} array
     */
    public static String[] toStringArray(Collection<String> collection) {
        return (!CollectionUtils.isEmpty(collection) ? collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
    }

    /**
     * Copy the given {@link Enumeration} into a {@code String} array.
     * <p>The {@code Enumeration} must contain {@code String} elements only.
     *
     * @param enumeration the {@code Enumeration} to copy
     *                    (potentially {@code null} or empty)
     * @return the resulting {@code String} array
     */
    public static String[] toStringArray(Enumeration<String> enumeration) {
        return (enumeration != null ? toStringArray(Collections.list(enumeration)) : EMPTY_STRING_ARRAY);
    }

    /**
     * Append the given {@code String} to the given {@code String} array,
     * returning a new array consisting of the input array contents plus
     * the given {@code String}.
     *
     * @param array the array to append to (can be {@code null})
     * @param str   the {@code String} to append
     * @return the new array (never {@code null})
     */
    public static String[] addStringToArray(String[] array, String str) {
        if (ObjectUtils.isEmpty(array)) {
            return new String[]{str};
        }
        String[] newArr = new String[array.length + 1];
        System.arraycopy(array, 0, newArr, 0, array.length);
        newArr[array.length] = str;
        return newArr;
    }

    /**
     * Concatenate the given {@code String} arrays into one,
     * with overlapping array elements included twice.
     * <p>The order of elements in the original arrays is preserved.
     *
     * @param array1 the first array (can be {@code null})
     * @param array2 the second array (can be {@code null})
     * @return the new array ({@code null} if both given arrays were {@code null})
     */
    public static String[] concatenateStringArrays(String[] array1, String[] array2) {
        if (ObjectUtils.isEmpty(array1)) {
            return array2;
        }
        if (ObjectUtils.isEmpty(array2)) {
            return array1;
        }
        String[] newArr = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, newArr, 0, array1.length);
        System.arraycopy(array2, 0, newArr, array1.length, array2.length);
        return newArr;
    }

    /**
     * Sort the given {@code String} array if necessary.
     *
     * @param array the original array (potentially empty)
     * @return the array in sorted form (never {@code null})
     */
    public static String[] sortStringArray(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return array;
        }
        Arrays.sort(array);
        return array;
    }

    /**
     * Trim the elements of the given {@code String} array, calling
     * {@code String.trim()} on each non-null element.
     *
     * @param array the original {@code String} array (potentially empty)
     * @return the resulting array (of the same size) with trimmed elements
     */
    public static String[] trimArrayElements(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return array;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            String element = array[i];
            result[i] = (element != null ? element.trim() : null);
        }
        return result;
    }

    /**
     * Remove duplicate strings from the given array.
     * <p>As of 4.2, it preserves the original order, as it uses a {@link LinkedHashSet}.
     *
     * @param array the {@code String} array (potentially empty)
     * @return an array without duplicates, in natural sort order
     */
    public static String[] removeDuplicateStrings(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return array;
        }
        Set<String> set = new LinkedHashSet<>(Arrays.asList(array));
        return toStringArray(set);
    }

    /**
     * Split a {@code String} at the first occurrence of the delimiter.
     * Does not include the delimiter in the result.
     *
     * @param toSplit   the string to split (potentially {@code null} or empty)
     * @param delimiter to split the string up with (potentially {@code null} or empty)
     * @return a two element array with index 0 being before the delimiter, and
     * index 1 being after the delimiter (neither element includes the delimiter);
     * or {@code null} if the delimiter wasn't found in the given input {@code String}
     */
    public static String[] split(String toSplit, String delimiter) {
        if (!hasLength(toSplit) || !hasLength(delimiter)) {
            return new String[0];
        }
        int offset = toSplit.indexOf(delimiter);
        if (offset < 0) {
            return new String[0];
        }
        String beforeDelimiter = toSplit.substring(0, offset);
        String afterDelimiter = toSplit.substring(offset + delimiter.length());
        return new String[]{beforeDelimiter, afterDelimiter};
    }

    /**
     * Take an array of strings and split each element based on the given delimiter.
     * A {@code Properties} instance is then generated, with the left of the delimiter
     * providing the key, and the right of the delimiter providing the value.
     * <p>Will trim both the key and value before adding them to the {@code Properties}.
     *
     * @param array     the array to process
     * @param delimiter to split each element using (typically the equals symbol)
     * @return a {@code Properties} instance representing the array contents,
     * or {@code null} if the array to process was {@code null} or empty
     */
    public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter) {
        return splitArrayElementsIntoProperties(array, delimiter, null);
    }

    /**
     * Take an array of strings and split each element based on the given delimiter.
     * A {@code Properties} instance is then generated, with the left of the
     * delimiter providing the key, and the right of the delimiter providing the value.
     * <p>Will trim both the key and value before adding them to the
     * {@code Properties} instance.
     *
     * @param array         the array to process
     * @param delimiter     to split each element using (typically the equals symbol)
     * @param charsToDelete one or more characters to remove from each element
     *                      prior to attempting the split operation (typically the quotation mark
     *                      symbol), or {@code null} if no removal should occur
     * @return a {@code Properties} instance representing the array contents,
     * or {@code null} if the array to process was {@code null} or empty
     */
    public static Properties splitArrayElementsIntoProperties(
            String[] array, String delimiter, String charsToDelete) {
        if (ObjectUtils.isEmpty(array)) {
            return null;
        }
        Properties result = new Properties();
        for (String element : array) {
            if (charsToDelete != null) {
                element = deleteAny(element, charsToDelete);
            }
            String[] splittedElement = split(element, delimiter);
            result.setProperty(splittedElement[0].trim(), splittedElement[1].trim());
        }
        return result;
    }

    /**
     * Tokenize the given {@code String} into a {@code String} array via a
     * {@link StringTokenizer}.
     * <p>Trims tokens and omits empty tokens.
     * <p>The given {@code delimiters} string can consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@link #delimitedListToStringArray}.
     *
     * @param str        the {@code String} to tokenize (potentially {@code null} or empty)
     * @param delimiters the delimiter characters, assembled as a {@code String}
     *                   (each of the characters is individually considered as a delimiter)
     * @return an array of the tokens
     * @see StringTokenizer
     * @see String#trim()
     * @see #delimitedListToStringArray
     */
    public static String[] tokenizeToStringArray(String str, String delimiters) {
        return tokenizeToStringArray(str, delimiters, true, true);
    }

    /**
     * Tokenize the given {@code String} into a {@code String} array via a
     * {@link StringTokenizer}.
     * <p>The given {@code delimiters} string can consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@link #delimitedListToStringArray}.
     *
     * @param str               the {@code String} to tokenize (potentially {@code null} or empty)
     * @param delimiters        the delimiter characters, assembled as a {@code String}
     *                          (each of the characters is individually considered as a delimiter)
     * @param trimTokens        trim the tokens via {@link String#trim()}
     * @param ignoreEmptyTokens omit empty tokens from the result array
     *                          (only applies to tokens that are empty after trimming; StringTokenizer
     *                          will not consider subsequent delimiters as token in the first place).
     * @return an array of the tokens
     * @see StringTokenizer
     * @see String#trim()
     * @see #delimitedListToStringArray
     */
    public static String[] tokenizeToStringArray(
            String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {
        if (str == null) {
            return EMPTY_STRING_ARRAY;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into a
     * {@code String} array.
     * <p>A single {@code delimiter} may consist of more than one character,
     * but it will still be considered as a single delimiter string, rather
     * than as bunch of potential delimiter characters, in contrast to
     * {@link #tokenizeToStringArray}.
     *
     * @param str       the input {@code String} (potentially {@code null} or empty)
     * @param delimiter the delimiter between elements (this is a single delimiter,
     *                  rather than a bunch individual delimiter characters)
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(String str, String delimiter) {
        return delimitedListToStringArray(str, delimiter, null);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into
     * a {@code String} array.
     * <p>A single {@code delimiter} may consist of more than one character,
     * but it will still be considered as a single delimiter string, rather
     * than as bunch of potential delimiter characters, in contrast to
     * {@link #tokenizeToStringArray}.
     *
     * @param str           the input {@code String} (potentially {@code null} or empty)
     * @param delimiter     the delimiter between elements (this is a single delimiter,
     *                      rather than a bunch individual delimiter characters)
     * @param charsToDelete a set of characters to delete; useful for deleting unwanted
     *                      line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a {@code String}
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(
            String str, String delimiter, String charsToDelete) {
        if (str == null) {
            return EMPTY_STRING_ARRAY;
        }
        if (delimiter == null) {
            return new String[]{str};
        }
        List<String> result = new ArrayList<>();
        if (delimiter.isEmpty()) {
            for (int i = 0; i < str.length(); i++) {
                result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
            }
        } else {
            int pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }
            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }
        return toStringArray(result);
    }

    /**
     * Convert a comma delimited list (e.g., a row from a CSV file) into an
     * array of strings.
     *
     * @param str the input {@code String} (potentially {@code null} or empty)
     * @return an array of strings, or the empty array in case of empty input
     */
    public static String[] commaDelimitedListToStringArray(String str) {
        return delimitedListToStringArray(str, ",");
    }

    /**
     * Convert a comma delimited list (e.g., a row from a CSV file) into a set.
     * <p>Note that this will suppress duplicates, and as of 4.2, the elements in
     * the returned set will preserve the original order in a {@link LinkedHashSet}.
     *
     * @param str the input {@code String} (potentially {@code null} or empty)
     * @return a set of {@code String} entries in the list
     * @see #removeDuplicateStrings(String[])
     */
    public static Set<String> commaDelimitedListToSet(String str) {
        String[] tokens = commaDelimitedListToStringArray(str);
        return new LinkedHashSet<>(Arrays.asList(tokens));
    }

    /**
     * Convert a {@link Collection} to a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll   the {@code Collection} to convert (potentially {@code null} or empty)
     * @param delim  the delimiter to use (typically a ",")
     * @param prefix the {@code String} to start each element with
     * @param suffix the {@code String} to end each element with
     * @return the delimited {@code String}
     */
    public static String collectionToDelimitedString(
            Collection<?> coll, String delim, String prefix, String suffix) {
        if (CollectionUtils.isEmpty(coll)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(prefix).append(it.next()).append(suffix);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll  the {@code Collection} to convert (potentially {@code null} or empty)
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, "", "");
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g., CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll the {@code Collection} to convert (potentially {@code null} or empty)
     * @return the delimited {@code String}
     */
    public static String collectionToCommaDelimitedString(Collection<?> coll) {
        return collectionToDelimitedString(coll, ",");
    }

    /**
     * Convert a {@code String} array into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param arr   the array to display (potentially {@code null} or empty)
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String arrayToDelimitedString(Object[] arr, String delim) {
        if (ObjectUtils.isEmpty(arr)) {
            return "";
        }
        if (arr.length == 1) {
            return ObjectUtils.nullSafeToString(arr[0]);
        }
        StringJoiner sj = new StringJoiner(delim);
        for (Object o : arr) {
            sj.add(String.valueOf(o));
        }
        return sj.toString();
    }

    /**
     * Convert a {@code String} array into a comma delimited {@code String}
     * (i.e., CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param arr the array to display (potentially {@code null} or empty)
     * @return the delimited {@code String}
     */
    public static String arrayToCommaDelimitedString(Object[] arr) {
        return arrayToDelimitedString(arr, ",");
    }

    /**
     * 拼接字符串，该方法用于避免多个字符串采用+进行拼接
     *
     * @param strings
     * @return
     */
    public static String joinStr(Object... strings) {
        StringBuilder sb = new StringBuilder();
        for (Object string : strings) {
            sb.append(string);
        }
        return sb.toString();
    }

    /**
     * 删除字符串中的被过滤串包含的字符
     *
     * @param str         待处理字符串
     * @param filterChars 过滤串
     * @return 处理后字符串
     */
    public static String filterChars(String str, String filterChars) {
        char[] chars = str.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (char ch : chars) {
            if (!filterChars.contains(String.valueOf(ch))) {
                buffer.append(ch);
            }
        }
        return buffer.toString().trim();
    }

    /**
     * 删除字符串中的不被限制串包含的字符
     *
     * @param str        待处理字符串
     * @param limitChars 限制串
     * @return 处理后字符串
     */
    public static String limitChars(String str, String limitChars) {
        char[] chars = str.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (char ch : chars) {
            if (limitChars.contains(String.valueOf(ch))) {
                buffer.append(ch);
            }
        }
        return buffer.toString().trim();
    }

    /**
     * 处理字符串，将空串转换为""
     *
     * @param str 待处理字符串
     * @return 处理后字符串
     */
    public static String processNullStr(String str) {
        return processNullStr(str, "");
    }

    /**
     * 处理空字符串，将空字符串转换为默认字符串
     *
     * @param str          待处理字符串
     * @param defaultValue 默认值
     * @return 处理后字符串
     */
    public static String processNullStr(String str, String defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        } else {
            return str;
        }
    }

    /**
     * 判断字符串是否是IP地址
     *
     * @param ip 待判断IP字符串
     * @return 是否是IP
     */
    public static boolean isIp(String ip) {
        Matcher m = IP_PATTERN.matcher(ip);
        return m.matches();
    }

    /**
     * 是否是手机号
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobilePhone(String mobiles) {
        Matcher m = MOBILE_PATTERN.matcher(mobiles);
        return m.matches();
    }

    /**
     * 是否是座机号码
     *
     * @param telePhone
     * @return
     */
    public static boolean isTelePhone(String telePhone) {
        Matcher m = TELEPHONE_PATTERN.matcher(telePhone);
        return m.matches();
    }

    /**
     * 是否是邮箱
     *
     * @param str
     * @return
     */
    public static boolean isEmail(String str) {
        Matcher m = EMAIL_PATTERN.matcher(str);
        return m.matches();
    }

    /**
     * 是否是时刻
     *
     * @param str
     * @return
     */
    public static boolean isTime(String str) {
        Matcher m = TIME_PATTERN.matcher(str);
        return m.matches();
    }

    /**
     * 提取手机号码
     *
     * @param text
     * @return
     */
    public static List<String> pickUpMobilePhone(String text) {
        return pickUpRegex(text, MOBILE_PATTERN);
    }

    /**
     * 提取电话号码
     *
     * @param text
     * @return
     */
    public static List<String> pickUpTelePhone(String text) {
        return pickUpRegex(text, TELEPHONE_PATTERN);
    }

    /**
     * 提取电话号码或者手机号码
     *
     * @param text
     * @return
     */
    public static List<String> pickUpPhone(String text) {
        return pickUpRegex(text, MOBILE_TELEPHONE_PATTERN);
    }

    /**
     * 提取邮箱
     *
     * @param text
     * @return
     */
    public static List<String> pickUpEmail(String text) {
        return pickUpRegex(text, EMAIL_PATTERN);
    }

    /**
     * 提取符合正则匹配模式的字符串信息集合
     *
     * @param text    文本
     * @param pattern 正则匹配模式
     * @return
     */
    public static List<String> pickUpRegex(String text, Pattern pattern) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        Matcher matcher = pattern.matcher(text);
        List<String> regexes = new ArrayList<String>();
        while (matcher.find()) {
            String regex = matcher.group();
            if (!regexes.contains(regex)) {
                regexes.add(regex);
            }
        }
        return regexes;
    }

    /**
     * 从一段文本中提取出来符合指定正则的第一个子串
     *
     * @param text  待提取字符串
     * @param regex 正则表达式
     * @return 提取结果，可能为null
     */
    public static String pickUpStrByRegex(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    /**
     * 从一段文本中提取出来符合指定正则的子串集合
     *
     * @param text  待提取字符串
     * @param regex 正则表达式
     * @return 提取结果，可能为空集合
     */
    public static List<String> pickUpStrListByRegex(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        List<String> results = new ArrayList<>();
        while (m.find()) {
            results.add(m.group());
        }
        return results;
    }

    /**
     * 获取唯一性编码
     *
     * @return uuid
     */
    public static String getUuid() {
        String s = UUID.randomUUID().toString();
        return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
                + s.substring(19, 23) + s.substring(24);
    }

    /**
     * 获取指定长度的唯一性编码，该编码最长支持32位，并且不保证编码唯一的正确性，尤其在编码位数越小的时候，重复的可能性越大
     *
     * @param length 编码长度
     * @return 指定长度的随机编码
     */
    public static String getUniqueId(int length) {
        String uuid = getUuid();
        if (length > uuid.length()) {
            return uuid;
        } else {
            return uuid.substring(0, length);
        }
    }

    /**
     * 处理数字串，按照指定长度格式返回
     *
     * @param length 指定长度
     * @param number 数字串
     * @return 处理后指定长度的数字串
     */
    public static String processNumberStr(int length, String number) {
        StringBuffer sb = new StringBuffer();
        int plus = length - number.length();
        if (plus > 0) {
            for (int i = 0; i < plus; i++) {
                sb.append(0);
            }
        }
        return sb + number;
    }

    /**
     * 采用尾部填充模式将字符串采用指定字符填充为最小指定长度的字符串
     *
     * @param length 最小指定长度
     * @param c      指定字符
     * @param str    待填充字符串
     * @return 处理后字符串
     */
    public static String processStrLength(int length, char c, String str) {
        return processStrLength(length, c, false, str);
    }

    /**
     * 将字符串采用指定字符填充为最小指定长度的字符串
     *
     * @param length 最小指定长度
     * @param c      指定字符
     * @param pre    是否前缀填充模式
     * @param str    待填充字符串
     * @return 处理后字符串
     */
    public static String processStrLength(int length, char c, boolean pre, String str) {
        StringBuffer sb = new StringBuffer();
        int plus = length - str.length();
        if (plus > 0) {
            for (int i = 0; i < plus; i++) {
                sb.append(c);
            }
        }
        if (pre) {
            return sb + str;
        } else {
            return str + sb;
        }
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            // 转换为两位十六进制，不足两位补0
            // 以下转换方式比采用String.format("%02x", bytes[i])进行转换，速度快了45倍
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 十六进制字符串中每个字节对应的字符长度
     */
    private static final int HEX_DIGIT_LENGTH = 2;

    /**
     * 十六进制转字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToByte(String hex) {
        int hexlen = hex.length();
        byte[] result;
        if (hexlen % HEX_DIGIT_LENGTH == 1) {
            hexlen++;
            result = new byte[(hexlen / HEX_DIGIT_LENGTH)];
            hex = "0" + hex;
        } else {
            result = new byte[(hexlen / HEX_DIGIT_LENGTH)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += HEX_DIGIT_LENGTH) {
            result[j] = (byte) Integer.parseInt(hex.substring(i, i + HEX_DIGIT_LENGTH), 16);
            j++;
        }
        return result;
    }

    /**
     * 获取行分隔符
     *
     * @return
     */
    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }

    /**
     * 压缩字符串，去除字符串中所有空白字符
     *
     * @param str
     * @return
     */
    public static String compress(String str) {
        str = str.replace(String.valueOf((char) 12288), "");
        // ASCII码值160为网页里面&nbsp;所产生的空格
        str = str.replace(String.valueOf((char) 160), "");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 利用正则表达式判断字符串是否都是由数字构成
     *
     * @param str 字符串
     * @return 是否数字串
     */
    public static boolean isNumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]+");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断字符串是否以字母开头
     *
     * @param str 字符串
     * @return 是否以字母开头
     */
    public static boolean startsWithLetterCharacter(String str) {
        if (isBlank(str)) {
            return false;
        }
        char firstChar = str.charAt(0);
        return Character.isLetter(firstChar);
    }

    /**
     * 判断字符串是否以数字开头
     *
     * @param str 字符串
     * @return 是否以数字开头
     */
    public static boolean startsWithDigitCharacter(String str) {
        if (isBlank(str)) {
            return false;
        }
        char firstChar = str.charAt(0);
        return Character.isDigit(firstChar);
    }

    /**
     * 获取字符串的真实长度，对于一个中文字符按两个英文字符长度计算
     *
     * @param str 待计算字符串
     * @return 字符真实长度
     */
    public static int getLength(String str) {
        if (isEmpty(str)) {
            return 0;
        }
        int len = 0;
        for (int i = 0; i < str.length(); i++) {
            int ch = str.charAt(i);
            if (ch > 255) {
                len += 2;
            } else {
                len++;
            }
        }
        return len;
    }

    /**
     * 最小分割长度
     */
    private static final int MIN_SPLIT_LENGTH = 2;

    /**
     * 将字符串按照指定长度进行分割，对于一个中文字符按两个英文字符长度计算，返回分割后的字符串集合
     *
     * @param str    待分割字符串
     * @param length 分割长度
     * @return 字符串集合
     */
    public static List<String> split(String str, int length) {
        if (isEmpty(str)) {
            throw new IllegalArgumentException("The str can not be empty.");
        }
        if (length < MIN_SPLIT_LENGTH) {
            throw new IllegalArgumentException("The length must be greater than " + (MIN_SPLIT_LENGTH - 1) + ".");
        }
        List<String> strs = new ArrayList<>();
        int len = 0;
        int start = 0;
        for (int i = 0; i < str.length(); i++) {
            int ch = str.charAt(i);
            if (ch > 255) {
                len += 2;
            } else {
                len++;
            }
            if (len > length) {
                strs.add(str.substring(start, i));
                // 重新记录分段
                start = i;
                len = 0;
                // 回退一个字符重新计算
                i--;
            }
        }
        if (start < str.length()) {
            strs.add(str.substring(start));
        }
        return strs;
    }

    public static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

    /**
     * 判断字符串是否包含数字
     *
     * @param s 字符串
     * @return 是否包含数字
     */
    public static boolean containsDigit(String s) {
        return DIGIT_PATTERN.matcher(s).find();
    }

    public static final Pattern LOWER_PATTERN = Pattern.compile("[a-z]");

    /**
     * 判断字符串是否包含小写字母
     *
     * @param s 字符串
     * @return 是否包含小写字母
     */
    public static boolean containsLower(String s) {
        return LOWER_PATTERN.matcher(s).find();
    }

    public static final Pattern UPPER_PATTERN = Pattern.compile("[A-Z]");

    /**
     * 判断字符串是否包含大写字母
     *
     * @param s 字符串
     * @return 是否包含大写字母
     */
    public static boolean containsUpper(String s) {
        return UPPER_PATTERN.matcher(s).find();
    }

    public static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]");

    /**
     * 判断字符串是否包含特殊字符
     *
     * @param s 字符串
     * @return 是否包含特殊字符
     */
    public static boolean containsSpecial(String s) {
        return SPECIAL_PATTERN.matcher(s).find();
    }

    /**
     * 按顺序替换字符串中的{}占位符
     *
     * @param template 包含{}占位符的模板字符串
     * @param values   用于替换的参数数组（按顺序）
     * @return 替换后的字符串
     */
    public static String replacePlaceholders(String template, String... values) {
        if (isBlank(template) || values == null || values.length == 0) {
            return template;
        }
        StringBuilder result = new StringBuilder(template);
        int valueIndex = 0;
        int placeholderIndex;
        // 循环查找并替换所有{}占位符
        while ((placeholderIndex = result.indexOf(PLACE_HOLDER)) != -1 && valueIndex < values.length) {
            // 替换当前找到的{}占位符
            result.replace(placeholderIndex, placeholderIndex + PLACE_HOLDER.length(), values[valueIndex]);
            // 移动到下一个参数
            valueIndex++;
        }
        return result.toString();
    }

    /**
     * 解析字符串中包含的指定长度的键盘连续字符
     *
     * @param str    待解析的字符串
     * @param length 要检查的指定长度
     * @return 包含的指定长度的键盘连续字符
     */
    public static String parseKeyboardSequences(String str, int length) {
        if (isNotBlank(str)) {
            String lowerPass = str.toLowerCase();
            for (String seq : KEYBOARD_SEQUENCES) {
                int end = seq.length() - (length - 1);
                for (int i = 0; i < end; i++) {
                    String sub = seq.substring(i, i + length);
                    if (lowerPass.contains(sub)) {
                        return sub;
                    }
                }
            }
        }
        return "";
    }

    /**
     * 解析字符串中包含的指定长度的连续字符（区分大小写）
     *
     * @param str         待解析的字符串
     * @param length      要检查的指定长度
     * @param isAscending 采用升序模式或者降序模式进行判断
     * @return 包含的指定长度的连续字符
     */
    public static String parseCharSequences(String str, int length, boolean isAscending) {
        if (isNotBlank(str)) {
            // 连续字母计数器，至少为1
            int consecutiveCount = 1;
            for (int i = 1; i < str.length(); i++) {
                char current = str.charAt(i);
                char prev = str.charAt(i - 1);
                // 检查当前字符和前一个字符是否都是字母
                if (Character.isLetter(current) && Character.isLetter(prev)) {
                    // 转换为小写后检查是否连续（如 'b' - 'a' = 1）
                    int diff = current - prev;
                    if ((isAscending && diff == 1) || (!isAscending && diff == -1)) {
                        consecutiveCount++;
                        // 如果连续字母数达到要求
                        if (consecutiveCount >= length) {
                            return str.substring(i + 1 - length, i + 1);
                        }
                    } else {
                        // 不连续则重置计数器
                        consecutiveCount = 1;
                    }
                } else {
                    // 有非字母字符则重置计数器
                    consecutiveCount = 1;
                }
            }
        }
        return "";
    }

    /**
     * 解析字符串中包含的指定长度的相同字符（区分大小写）
     *
     * @param str    待解析的字符串
     * @param length 要检查的指定长度
     * @return 包含的指定长度的相同字符
     */
    public static String parseSameChar(String str, int length) {
        if (isNotBlank(str)) {
            // 相同字母计数器，至少为1
            int consecutiveCount = 1;
            for (int i = 1; i < str.length(); i++) {
                char current = str.charAt(i);
                char prev = str.charAt(i - 1);
                int diff = current - prev;
                if (diff == 0) {
                    consecutiveCount++;
                    // 如果相同字符数达到要求
                    if (consecutiveCount >= length) {
                        return str.substring(i + 1 - length, i + 1);
                    }
                } else {
                    // 不相同则重置计数器
                    consecutiveCount = 1;
                }
            }
        }
        return "";
    }

}
