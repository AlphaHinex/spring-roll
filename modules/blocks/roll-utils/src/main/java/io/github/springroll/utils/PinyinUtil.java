package io.github.springroll.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 汉字串转换拼音串
 */
public class PinyinUtil {

    private static HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

    /**
     * 私有化工具类的构造函数，避免对工具类的实例化
     */
    private PinyinUtil() { }

    static {
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    /**
     * 转换一个字符串
     * @param str str
     * @return 字符串
     * @throws BadHanyuPinyinOutputFormatCombination if certain combination of output formats happens
     */
    public static String quanpin(String str) throws BadHanyuPinyinOutputFormatCombination {
        if (StringUtil.isBlank(str)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String tempPinyin;
        for (int i = 0; i < str.length(); ++i) {
            tempPinyin = getCharacterPinYin(str.charAt(i));
            if (tempPinyin == null) {
                // 如果str.charAt(i)非汉字，则保持原样
                sb.append(str.charAt(i));
            } else {
                sb.append(tempPinyin);
            }
        }
        return sb.toString();
    }

    /**
     * 转换单个字符
     * @param c c
     * @return 字符串
     * @throws BadHanyuPinyinOutputFormatCombination if certain combination of output formats happens
     */
    private static String getCharacterPinYin(char c) throws BadHanyuPinyinOutputFormatCombination {
        String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
        // 如果c不是汉字，toHanyuPinyinStringArray会返回null
        // 如果是多音字，取第一个发音
        return pinyin == null ? null : pinyin[0];
    }

}
